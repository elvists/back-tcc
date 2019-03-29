package br.ufba.back.service;

import br.ufba.back.model.ConfigurationData;
import br.ufba.back.model.Result;
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
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static librec.intf.Recommender.*;


@Service
public class LibRecService {


    protected float binThold;
    protected int[] columns;

    // rate DAO object
    protected DataDAO rateDao;
    protected SparseMatrix rateMatrix;
    protected LineConfiger ratingOptions, outputOptions;
    protected ConfigurationData cf;

    public static boolean isMeasuresOnly = false;
    private TimeUnit timeUnit;
    private String algorithm;

    public Result run(ConfigurationData config) throws Exception {


        // reset general settings
        preset(config);

        // prepare data
        readData(config);

        // run a specific algorithm
        Result result = runAlgorithm();
//
//        // collect results
       String filename =  algorithm + "@" + Dates.now() + ".txt";
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

    protected Result runAlgorithm() throws Exception {

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

    private Result runCrossValidation(LineConfiger params) throws Exception {

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

    private Result printEvalInfo(Recommender algo, Map<Recommender.Measure, Double> ms) {

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

}
