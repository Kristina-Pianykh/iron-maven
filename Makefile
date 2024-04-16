# Variables to define the path of the Flink libraries
FLINK_HOME := flink-1.19.0
FLINK_LIBS := $(shell find $(FLINK_HOME)/lib -name '*.jar' | tr '\n' ':' | sed 's/.$$//')
CLASSPATH := target/classes:$(FLINK_LIBS)
VM_OPTIONS := -ea --add-opens java.base/java.util=ALL-UNNAMED
JAVA_TOOL_OPTIONS := -Dfile.encoding=UTF-8 \
                     -Dsun.stdout.encoding=UTF-8 \
                     -Dsun.stderr.encoding=UTF-8


# Default target executed when no arguments are given to make.
default: build

debug:
	@echo FLINK_LIBS: $(FLINK_LIBS)
	@echo VM_OPTIONS: $(VM_OPTIONS)
	@echo JAVA_TOOL_OPTIONS: $(JAVA_TOOL_OPTIONS)

# Target for building the project
build:
	mvn clean package

node1: build
		java $(VM_OPTIONS) \
    $(JAVA_TOOL_OPTIONS) \
    -classpath $(CLASSPATH) \
    iron_maven.StreamingJob \
    1 6666 6667

node2: build
		java $(VM_OPTIONS) \
    $(JAVA_TOOL_OPTIONS) \
    -classpath $(CLASSPATH) \
    iron_maven.StreamingJob \
		2 6667

events1: build
		java $(VM_OPTIONS) \
    $(JAVA_TOOL_OPTIONS) \
    -classpath $(CLASSPATH) \
		iron_maven.MockEventGenerator \
		6666 1

events2: build
		java $(VM_OPTIONS) \
    $(JAVA_TOOL_OPTIONS) \
    -classpath $(CLASSPATH) \
		iron_maven.MockEventGenerator \
		6667 2

all: build node1 node2 events1 events2

# Declare that these targets are not files.
.PHONY: default build node1 events1 events2 all
