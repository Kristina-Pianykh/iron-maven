package iron_maven;

import iron_maven.sources.AtomicEvent;

import org.apache.flink.cep.CEP;
import org.apache.flink.cep.PatternFlatSelectFunction;
import org.apache.flink.cep.PatternStream;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;
import org.apache.flink.configuration.Configuration;

import java.util.List;
import java.util.Map;
import java.util.HashSet;

public class StreamingJob {
  public static HashSet<Integer> processedMatches = new HashSet<>();

  public static void main(String[] args) throws Exception {
    Configuration nodeConfig = new Configuration();

    String patternID = Niceties.extractStrArg(args, 0);
    nodeConfig.setString(NodeConf.PATTERN_ID, patternID);
    System.out.println("Selected pattern: " + patternID);

    String nodeID = Niceties.extractStrArg(args, 1);
    nodeConfig.setString(NodeConf.NODE_ID, nodeID);

    int hostPort = Niceties.extractPort(args, 2);
    assert hostPort > 0 : "Host Port is not set";
    nodeConfig.setInteger(NodeConf.HOST_PORT, hostPort);

    if (args.length == 4) {
      //      String targetPorts = Niceties.extractStrArg(args, 3);
      List<Integer> targetPorts = Niceties.extractPorts(args, 3);
      nodeConfig.set(NodeConf.TARGET_PORTS, targetPorts);
      System.out.println("Target ports are set to " + targetPorts);
    }

    System.out.println("Initialized Node Config: " + nodeConfig.toString());

    final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

    DataStream<AtomicEvent> inputEventStream =
        env.addSource(new SocketSource(hostPort), "Socket Source")
            .assignTimestampsAndWatermarks(new CustomWatermarkStrategy());

    Pattern<AtomicEvent, ?> pattern =
        CustomPatterns.getPattern(nodeConfig.get(NodeConf.PATTERN_ID));

    // Apply the pattern to the input stream
    PatternStream<AtomicEvent> patternStream = CEP.pattern(inputEventStream, pattern).inEventTime();

    // this is stupid. Just to convert PatternStream into DataStream
    DataStream<AtomicEvent> matches =
        patternStream.flatSelect(
            new PatternFlatSelectFunction<AtomicEvent, AtomicEvent>() {
              @Override
              public void flatSelect(
                  Map<String, List<AtomicEvent>> map, Collector<AtomicEvent> collector)
                  throws Exception {
                System.out.println("\n======================================");
                for (String key : map.keySet()) {
                  List<AtomicEvent> values = map.get(key);
                  System.out.println(key + ": " + values);
                  for (AtomicEvent event : values) {
                    Integer hashedEvent = event.hashCode();
                    if (!processedMatches.contains(hashedEvent)) {
                      System.out.println("Sending event: " + event.toString());
                      processedMatches.add(hashedEvent);
                      collector.collect(event);
                    }
                  }
                }
                System.out.println("======================================\n");
              }
            });
    System.out.println(NodeConf.shouldForward((nodeConfig)));
    if (NodeConf.shouldForward(nodeConfig)) {
      matches.addSink(new SocketSink(nodeConfig)).setParallelism(1);
    }
    System.out.println(env.getExecutionPlan());
    env.getExecutionPlan();
    env.execute("CEP Pattern Matching Job");
  }
}
