Install maven deps:

```bash
mvn install
```

Build with:

```bash
mvn clean package
```

Start Flink cluster from the folder with Flink files:

```bash
./bin/start-cluster.sh
```

Submit the built JAR to the cluster:

```bash
${path_to_flink_folder}/bin/flink run target/my-app-1.0-SNAPSHOT.jar
```
