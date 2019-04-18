FROM java:8

EXPOSE 8080
WORKDIR /
ADD /build/libs/back-0.0.1-SNAPSHOT.jar back-0.0.1-SNAPSHOT.jar
ADD /datasets datasets


CMD [ "java","-Xms128m","-Xmx4096m","-XX:MaxPermSize=4096m", "-jar", "back-0.0.1-SNAPSHOT.jar"]

# rodar ->sudo docker run -p 8080:8080 back
