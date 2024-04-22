package iron_maven;

import iron_maven.sources.AtomicEvent;

import org.apache.flink.cep.CEP;
import org.apache.flink.cep.PatternFlatSelectFunction;
import org.apache.flink.cep.PatternStream;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;
import org.w3c.dom.Node;

import java.util.List;
import java.util.Map;
import java.util.HashSet;

public class StreamingJob {
  public static final String HOSTNAME = "localhost";
  public static HashSet<Integer> processedMatches = new HashSet<>();

  public static void main(String[] args) throws Exception {
    int targetPort = -1;

    int patternNum = Niceties.extractPatternNum(args, 0);
    assert patternNum >= 1 : "Pattern number must be a positive integer";
    System.out.println("Selected pattern: " + patternNum);

    int nodeID = Niceties.extractNodeNum(args, 1);
    assert nodeID > 0 : "Node ID is not set";

    int hostPort = Niceties.extractPort(args, 2);
    assert hostPort > 0 : "Host Port is not set";

    if (args.length >= 4) {
      targetPort = Niceties.extractPort(args, 3);
      System.out.println("Target port is set to " + targetPort);
      NodeConfig nodeConfig =
          new NodeConfig(String.valueOf(patternNum), nodeID, hostPort, targetPort);
    } else {
      NodeConfig nodeConfig = new NodeConfig(String.valueOf(patternNum), nodeID, hostPort);
    }

    final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

    DataStream<AtomicEvent> inputEventStream =
        env.addSource(new SocketSource(hostPort), "Socket Source")
            .assignTimestampsAndWatermarks(new CustomWatermarkStrategy());

    Pattern<AtomicEvent, ?> pattern = CustomPatterns.getPattern(patternNum);

    // Apply the pattern to the input stream
    PatternStream<AtomicEvent> patternStream = CEP.pattern(inputEventStream, pattern).inEventTime();

    //    // Select matching patterns and print them
    //    DataStream<String> matches =
    //        patternStream.process(
    //            new PatternProcessFunction<AtomicEvent, String>() {
    //              @Override
    //              public void processMatch(
    //                  Map<String, List<AtomicEvent>> matches, Context ctx, Collector<String> out)
    // {
    //                out.collect(matches.toString());
    //                //                AtomicEvent first = matches.get("first").get(0);
    //                //                AtomicEvent second = matches.get("second").get(0);
    //                //                out.collect("SEQ(A, C) detected: " + first.getType() + ", "
    // +
    //                // second.getType());
    //              }
    //            });
    //    matches.print();

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
    //    matches.print();
    //    DataStream<List<AtomicEvent>> matches =
    //        patternStream.select(
    //            new PatternSelectFunction<AtomicEvent, List<AtomicEvent>>() {
    //              @Override
    //              public List<AtomicEvent> select(Map<String, List<AtomicEvent>> pattern)
    // {
    //                AtomicEvent first = pattern.get("first").get(0);
    //                AtomicEvent second = pattern.get("second").get(0);
    //                List<AtomicEvent> partialMatches = new ArrayList<>();
    //                partialMatches.add(first);
    //                partialMatches.add(second);
    //                return partialMatches;
    //              }
    //            });
    if (targetPort > 0) {
      matches.addSink(new SocketSink(HOSTNAME, targetPort));
    }
    env.execute("CEP Pattern Matching Job");
  }
}
