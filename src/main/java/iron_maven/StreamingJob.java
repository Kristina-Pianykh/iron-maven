package iron_maven;

import iron_maven.sources.AtomicEvent;
import iron_maven.sources.SocketSource;
import org.apache.flink.cep.CEP;
import org.apache.flink.cep.PatternSelectFunction;
import org.apache.flink.cep.PatternStream;
import org.apache.flink.cep.functions.PatternProcessFunction;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class StreamingJob {

  public static void main(String[] args) throws Exception {
    int patternNum = Niceties.extractPatternNum(args, 0);
    assert patternNum >= 1 : "Pattern number must be a positive integer";
    System.out.println("Selected pattern: " + patternNum);

    int port = Niceties.extractPort(args, 1);
    assert port > 0 : "Port is not set";

    final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

    DataStream<AtomicEvent> inputEventStream =
        env.addSource(new SocketSource(port), "Socket Source")
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
    DataStream<List<AtomicEvent>> matches =
        patternStream.select(
            new PatternSelectFunction<AtomicEvent, List<AtomicEvent>>() {
              @Override
              public List<AtomicEvent> select(Map<String, List<AtomicEvent>> pattern) {
                AtomicEvent first = pattern.get("first").get(0);
                AtomicEvent second = pattern.get("second").get(0);
                List<AtomicEvent> partialMatches = new ArrayList<>();
                partialMatches.add(first);
                partialMatches.add(second);
                return partialMatches;
              }
            });
    matches.writeToSocket(); // TODO: implement

    env.execute("CEP Pattern Matching Job");
  }
}
