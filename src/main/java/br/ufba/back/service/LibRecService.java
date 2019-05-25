package br.ufba.back.service;

import br.ufba.back.model.ConfigurationData;
import br.ufba.back.model.Results;
import br.ufba.back.model.ScheduleObject;
import br.ufba.back.model.StatusReading;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Table;
import happy.coding.io.FileIO;
import happy.coding.io.LineConfiger;
import happy.coding.io.Logs;
import happy.coding.system.Dates;
import librec.baseline.*;
import librec.data.DataDAO;
import librec.data.DataSplitter;
import librec.data.MatrixEntry;
import librec.data.SparseMatrix;
import librec.ext.*;
import librec.intf.GraphicRecommender;
import librec.intf.IterativeRecommender;
import librec.intf.Recommender;
import librec.ranking.*;
import librec.rating.*;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static librec.intf.Recommender.Measure;
import static librec.intf.Recommender.tempDirPath;


@Service
@EnableScheduling
public class LibRecService {


    private List<ScheduleObject> scheduleList = new ArrayList<>();
    protected float binThold;
    protected int[] columns;

    private boolean isRun = false;
    // rate DAO object
    protected DataDAO rateDao;
    protected SparseMatrix rateMatrix;
    protected LineConfiger ratingOptions, outputOptions;
    protected ConfigurationData cf;

    public static boolean isMeasuresOnly = false;
    private TimeUnit timeUnit;
    private String algorithm;

    public Map<String, Double> run(ConfigurationData config) throws Exception {


        // reset general settings
        preset(config);

        // prepare data
        readData(config);

        // run a specific algorithm
        Map<String, Double> result = runAlgorithm();
//
//        // collect results
        String filename = algorithm + "@" + Dates.now() + ".txt";
        String results = tempDirPath + filename;

//        // send notification
//        notifyMe(results);
        return result;
    }

    private void preset(ConfigurationData configFile) {
        // seeding the general recommender
        cf = configFile;
        Recommender.cf = cf;

        // reset recommenders' static properties
        Recommender.resetStatics = true;
        IterativeRecommender.resetStatics = true;
        GraphicRecommender.resetStatics = true;

        // LibRec outputs
        outputOptions = cf.getParamOptions(cf.getOutputSetup());
        if (outputOptions != null) {
            isMeasuresOnly = outputOptions.contains("--measures-only");
            tempDirPath = outputOptions.getString("-dir", "./Results/");
        }
        // make output directory
        tempDirPath = FileIO.makeDirectory(tempDirPath);
    }

    private void readData(ConfigurationData config) throws Exception {


        rateDao = new DataDAO(config.getDataset());
        ratingOptions = cf.getParamOptions(config.getRatingSetup());

        List<String> cols = ratingOptions.getOptions("-columns");
        columns = new int[cols.size()];
        for (int i = 0; i < cols.size(); i++)
            columns[i] = Integer.parseInt(cols.get(i));

        binThold = ratingOptions.getFloat("-threshold");

        // time unit of ratings' timestamps
        timeUnit = TimeUnit.valueOf(ratingOptions.getString("--time-unit", "seconds").toUpperCase());
        rateDao.setTimeUnit(timeUnit);

        rateMatrix = rateDao.readData(columns, binThold);

        Recommender.rateMatrix = rateMatrix;
        Recommender.rateDao = rateDao;
        Recommender.binThold = binThold;
    }

    protected Map<String, Double> runAlgorithm() throws Exception {

        // evaluation setup
        String setup = cf.getEvaluationSetup();
        LineConfiger evalOptions = new LineConfiger(setup);

        // debug information
        if (isMeasuresOnly)
            Logs.debug("With Setup: {}", setup);
        else
            Logs.info("With Setup: {}", setup);

        Recommender algo = null;

        DataSplitter ds = new DataSplitter(rateMatrix);
        SparseMatrix[] data = null;

        int N;
        double ratio;

        switch (evalOptions.getMainParam().toLowerCase()) {
            case "cv":
                return runCrossValidation(evalOptions);
            default:
                ratio = evalOptions.getDouble("-r", 0.8);
                data = ds.getRatioByRating(ratio);
                break;
        }

        algo = getRecommender(data, -1);
        algo.execute();

        return printEvalInfo(algo, algo.measures);
    }

    private Map<String, Double> runCrossValidation(LineConfiger params) throws Exception {

        int kFold = params.getInt("-k", 5);
        boolean isParallelFold = params.isOn("-p", true);

        DataSplitter ds = new DataSplitter(rateMatrix, kFold);

        Thread[] ts = new Thread[kFold];
        Recommender[] algos = new Recommender[kFold];

        for (int i = 0; i < kFold; i++) {
            Recommender algo = getRecommender(ds.getKthFold(i + 1), i + 1);

            algos[i] = algo;
            ts[i] = new Thread(algo);
            ts[i].start();

            if (!isParallelFold)
                ts[i].join();
        }
        if (isParallelFold)
            for (Thread t : ts)
                t.join();

        // average performance of k-fold
        Map<Recommender.Measure, Double> avgMeasure = new HashMap<>();
        for (Recommender algo : algos) {
            for (Map.Entry<Recommender.Measure, Double> en : algo.measures.entrySet()) {
                Recommender.Measure m = en.getKey();
                double val = avgMeasure.containsKey(m) ? avgMeasure.get(m) : 0.0;
                avgMeasure.put(m, val + en.getValue() / kFold);
            }
        }
        GraphicRecommender.isInitialized = false;
        return printEvalInfo(algos[0], avgMeasure);
    }

    protected void writeData(SparseMatrix trainMatrix, SparseMatrix testMatrix, int fold) {
        if (outputOptions != null && outputOptions.contains("--fold-data")) {

            String prefix = rateDao.getDataDirectory() + rateDao.getDataName();
            String suffix = ((fold >= 0) ? "-" + fold : "") + ".txt";

            try {
                writeMatrix(trainMatrix, prefix + "-train" + suffix);
                writeMatrix(testMatrix, prefix + "-test" + suffix);
            } catch (Exception e) {
                Logs.error(e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void writeMatrix(SparseMatrix data, String filePath) throws Exception {
        // delete old file first
        FileIO.deleteFile(filePath);

        Table<Integer, Integer, Long> timestamps = rateDao.getTimestamps();

        List<String> lines = new ArrayList<>(1500);
        for (MatrixEntry me : data) {
            int u = me.row();
            int j = me.column();
            double ruj = me.get();

            if (ruj <= 0)
                continue;

            String user = rateDao.getUserId(u);
            String item = rateDao.getItemId(j);
            String timestamp = timestamps != null ? " " + timestamps.get(u, j) : "";

            lines.add(user + " " + item + " " + (float) ruj + timestamp);

            if (lines.size() >= 1000) {
                FileIO.writeList(filePath, lines, true);
                lines.clear();
            }
        }

        if (lines.size() > 0)
            FileIO.writeList(filePath, lines, true);

        Logs.debug("Matrix data is written to: {}", filePath);
    }

    protected Recommender getRecommender(SparseMatrix[] data, int fold) throws Exception {

        algorithm = cf.getRecommender();

        SparseMatrix trainMatrix = data[0], testMatrix = data[1];

        // output data
        writeData(trainMatrix, testMatrix, fold);

        switch (algorithm.toLowerCase()) {

            /* baselines */
            case "globalavg":
                return new GlobalAverage(trainMatrix, testMatrix, fold);
            case "useravg":
                return new UserAverage(trainMatrix, testMatrix, fold);
            case "itemavg":
                return new ItemAverage(trainMatrix, testMatrix, fold);
            case "usercluster":
                return new UserCluster(trainMatrix, testMatrix, fold);
            case "itemcluster":
                return new ItemCluster(trainMatrix, testMatrix, fold);
            case "random":
                return new RandomGuess(trainMatrix, testMatrix, fold);
            case "constant":
                return new ConstantGuess(trainMatrix, testMatrix, fold);
            case "mostpop":
                return new MostPopular(trainMatrix, testMatrix, fold);

            /* rating prediction */
            case "userknn":
                return new UserKNN(trainMatrix, testMatrix, fold);
            case "itemknn":
                return new ItemKNN(trainMatrix, testMatrix, fold);
            case "itembigram":
                return new ItemBigram(trainMatrix, testMatrix, fold);
            case "regsvd":
                return new PMF(trainMatrix, testMatrix, fold);
            case "biasedmf":
                return new BiasedMF(trainMatrix, testMatrix, fold);
            case "gplsa":
                return new GPLSA(trainMatrix, testMatrix, fold);
            case "svd++":
                return new SVDPlusPlus(trainMatrix, testMatrix, fold);
            case "timesvd++":
                return new TimeSVD(trainMatrix, testMatrix, fold);
            case "pmf":
                return new PMF(trainMatrix, testMatrix, fold);
            case "bpmf":
                return new BPMF(trainMatrix, testMatrix, fold);
            case "socialmf":
                return new SocialMF(trainMatrix, testMatrix, fold);
            case "trustmf":
                return new TrustMF(trainMatrix, testMatrix, fold);
            case "sorec":
                return new SoRec(trainMatrix, testMatrix, fold);
            case "soreg":
                return new SoReg(trainMatrix, testMatrix, fold);
            case "rste":
                return new RSTE(trainMatrix, testMatrix, fold);
            case "trustsvd":
                return new TrustSVD(trainMatrix, testMatrix, fold);
            case "urp":
                return new URP(trainMatrix, testMatrix, fold);
            case "ldcc":
                return new LDCC(trainMatrix, testMatrix, fold);

            /* item ranking */
            case "climf":
                return new CLiMF(trainMatrix, testMatrix, fold);
            case "fismrmse":
                return new FISMrmse(trainMatrix, testMatrix, fold);
            case "fism":
            case "fismauc":
                return new FISMauc(trainMatrix, testMatrix, fold);
            case "lrmf":
                return new LRMF(trainMatrix, testMatrix, fold);
            case "rankals":
                return new RankALS(trainMatrix, testMatrix, fold);
            case "ranksgd":
                return new RankSGD(trainMatrix, testMatrix, fold);
            case "wrmf":
                return new WRMF(trainMatrix, testMatrix, fold);
            case "bpr":
                return new BPR(trainMatrix, testMatrix, fold);
            case "wbpr":
                return new WBPR(trainMatrix, testMatrix, fold);
            case "gbpr":
                return new GBPR(trainMatrix, testMatrix, fold);
            case "sbpr":
                return new SBPR(trainMatrix, testMatrix, fold);
            case "slim":
                return new SLIM(trainMatrix, testMatrix, fold);
            case "lda":
                return new LDA(trainMatrix, testMatrix, fold);

            /* extension */
            case "nmf":
                return new NMF(trainMatrix, testMatrix, fold);
            case "hybrid":
                return new Hybrid(trainMatrix, testMatrix, fold);
            case "slopeone":
                return new SlopeOne(trainMatrix, testMatrix, fold);
            case "pd":
                return new PD(trainMatrix, testMatrix, fold);
            case "ar":
                return new AR(trainMatrix, testMatrix, fold);
            case "prankd":
                return new PRankD(trainMatrix, testMatrix, fold);
            case "external":
                return new External(trainMatrix, testMatrix, fold);

            /* both tasks */
            case "bucm":
                return new BUCM(trainMatrix, testMatrix, fold);
            case "bhfree":
                return new BHfree(trainMatrix, testMatrix, fold);

            default:
                throw new Exception("No recommender is specified!");
        }
    }

    private Map<String, Double> printEvalInfo(Recommender algo, Map<Measure, Double> ms) {

        final ObjectMapper mapper = new ObjectMapper(); // jackson's objectmapper
        String result = Recommender.getEvalInfo(ms);

        // we add quota symbol to indicate the textual format of time
        String time = String.format("'%s',  '%s'", Dates.parse(ms.get(Recommender.Measure.TrainTime).longValue()),
                Dates.parse(ms.get(Recommender.Measure.TestTime).longValue()));
        // double commas as the separation of results and configuration
        String evalInfo = String.format("%s,%s,,%s,%s%s", algo.algoName, result, algo.toString(), time,
                (outputOptions.contains("--measures-only") ? "" : "\n"));

        Logs.info(evalInfo);
        return Recommender.getResult(ms);
    }

    public List<Map<String, Object>> tratar(List<Map<String, Double>> resultados, List<String> algoritmos) {
        List<Map<String, Object>> todos = new ArrayList();
        Map<String, Object> a1 = new HashMap<>();
        Map<String, Object> a2 = new HashMap<>();
        Map<String, Object> a3 = new HashMap<>();
        Map<String, Object> a4 = new HashMap<>();
        Map<String, Object> a5 = new HashMap<>();
        Map<String, Object> a6 = new HashMap<>();
        Map<String, Object> a7 = new HashMap<>();
        Map<String, Object> a8 = new HashMap<>();
        int i = 0;
        for (Map<String, Double> a : resultados) {

            if (a.get("isRankingPred") == 1) {
                if (i == 0) {
                    a1.put("name", String.valueOf(Recommender.Measure.Pre5));
                    a2.put("name", String.valueOf(Recommender.Measure.Pre10));
                    a3.put("name", String.valueOf(Recommender.Measure.Rec5));
                    a4.put("name", String.valueOf(Recommender.Measure.Rec10));
                    a5.put("name", String.valueOf(Recommender.Measure.AUC));
                    a6.put("name", String.valueOf(Recommender.Measure.MAP));
                    a7.put("name", String.valueOf(Recommender.Measure.NDCG));
                    a8.put("name", String.valueOf(Recommender.Measure.MRR));
                }
                a1.put(algoritmos.get(i), a.get(String.valueOf(Recommender.Measure.Pre5)));
                a2.put(algoritmos.get(i), a.get(String.valueOf(Recommender.Measure.Pre10)));
                a3.put(algoritmos.get(i), a.get(String.valueOf(Recommender.Measure.Rec5)));
                a4.put(algoritmos.get(i), a.get(String.valueOf(Recommender.Measure.Rec10)));
                a5.put(algoritmos.get(i), a.get(String.valueOf(Recommender.Measure.AUC)));
                a6.put(algoritmos.get(i), a.get(String.valueOf(Recommender.Measure.MAP)));
                a7.put(algoritmos.get(i), a.get(String.valueOf(Recommender.Measure.NDCG)));
                a8.put(algoritmos.get(i), a.get(String.valueOf(Recommender.Measure.MRR)));
            } else {
                if (i == 0) {
                    a1.put("name", String.valueOf(Recommender.Measure.MAE));
                    a2.put("name", String.valueOf(Recommender.Measure.RMSE));
                    a3.put("name", String.valueOf(Recommender.Measure.NMAE));
                    a4.put("name", String.valueOf(Recommender.Measure.rMAE));
                    a5.put("name", String.valueOf(Recommender.Measure.rRMSE));
                    a6.put("name", String.valueOf(Recommender.Measure.MPE));
                }
                a1.put(algoritmos.get(i), a.get(String.valueOf(Recommender.Measure.MAE)));
                a2.put(algoritmos.get(i), a.get(String.valueOf(Recommender.Measure.RMSE)));
                a3.put(algoritmos.get(i), a.get(String.valueOf(Recommender.Measure.NMAE)));
                a4.put(algoritmos.get(i), a.get(String.valueOf(Recommender.Measure.rMAE)));
                a5.put(algoritmos.get(i), a.get(String.valueOf(Recommender.Measure.rRMSE)));
                a6.put(algoritmos.get(i), a.get(String.valueOf(Recommender.Measure.MPE)));
            }
            i++;
        }

        todos.add(a1);
        todos.add(a2);
        todos.add(a3);
        todos.add(a4);
        todos.add(a5);
        todos.add(a6);
        if (!a8.isEmpty()) {
            todos.add(a7);
            todos.add(a8);
        }
        return todos;
    }

    @Scheduled(fixedDelay = 5000)
    void iasdjoad() {
        if (!isRun && existsConfigToRun()) {
            ConfigurationData config = nextConfigToRun();
            try {
                config.setStatus(StatusReading.RUN);
                config.setResultados(run(config));
                config.setStatus(StatusReading.FINISHED);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private boolean existsConfigToRun() {
        if (scheduleList.isEmpty()) {
            return false;
        }
        for (ScheduleObject scheduleObject : scheduleList) {
            Boolean run = false;

            if (scheduleObject.getStatus() == StatusReading.WAITING) {
                return true;
            } else if (scheduleObject.getStatus() == StatusReading.RUN) {
                for (ConfigurationData config : scheduleObject.getListConfiguration()) {
                    if (config.getStatus() == StatusReading.WAITING) {
                        return true;
                    }
                    if (config.getStatus() == StatusReading.RUN) {
                        run = true;
                    }
                }
                if (!run) {
                    scheduleObject.setStatus(StatusReading.FINISHED);
                }

            }
        }

        return false;
    }

    private ConfigurationData nextConfigToRun() {
        for (ScheduleObject scheduleObject : scheduleList) {
            if (scheduleObject.getStatus() == StatusReading.RUN) {
                for (ConfigurationData config : scheduleObject.getListConfiguration()) {
                    if (config.getStatus() == StatusReading.WAITING) {
                        return config;
                    }
                }
            } else if (scheduleObject.getStatus() == StatusReading.WAITING) {
                for (ConfigurationData config : scheduleObject.getListConfiguration()) {
                    if (config.getStatus() == StatusReading.WAITING) {
                        scheduleObject.setStatus(StatusReading.RUN);
                        return config;
                    }
                }
            }
        }
        return null;
    }


    public String addConfig(List<ConfigurationData> configs) {
        ScheduleObject a = new ScheduleObject();
        for (ConfigurationData config : configs) {
            config.setStatus(StatusReading.WAITING);
        }
        a.setListConfiguration(configs);
        String id = UUID.randomUUID().toString();
        a.setId(id);
        a.setStatus(StatusReading.WAITING);
        scheduleList.add(a);
        return id;
    }

    public Object getResult(String id) {
        List<Map<String, Double>> resultados = new ArrayList<>();
        List<String> algoritmos = new ArrayList<>();

        for (ScheduleObject so : scheduleList) {
            if (so.getId().equals(id)) {
                if (so.getStatus() == StatusReading.WAITING) {
                    return "Suas configurações estão na lista para serem processadas.";
                }

                if (so.getStatus() == StatusReading.RUN) {
                    return "Suas configurações estão sendo executadas.";
                }
                for (ConfigurationData config : so.getListConfiguration()) {
                    resultados.add(config.getResultados());
                    algoritmos.add(config.getRecommender());
                }
            }

        }
        if(resultados.isEmpty()){
            return "ID não encontrado!";
        }
        return new Results(tratar(resultados, algoritmos), algoritmos);
    }

    public List<ScheduleObject> getList() {
        return scheduleList;
    }
}
