# Installation

## Prerequisites

- maven
- JDK 21
- Flink 1.19.0 (download from [here](https://flink.apache.org/downloads/))

1. Install maven deps:

2. Extract Flink archive into the project root:


# Build

```bash
make build
```

# Launch Nodes

Run in separate terminal seshions/processes:

```bash
make node1
```

```bash
make node2
```

# Launch Mock Event Generators

```bash
make events1 events2

```

# Centralized Flink Cluster

Not relevant for the project, keeping the docs for reference.

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
