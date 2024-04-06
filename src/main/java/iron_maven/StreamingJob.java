package iron_maven;

import iron_maven.sources.AtomicEvent;
import iron_maven.sources.SocketSource;
import org.apache.flink.cep.CEP;
import org.apache.flink.cep.PatternStream;
import org.apache.flink.cep.functions.PatternProcessFunction;
import org.apache.flink.cep.nfa.aftermatch.AfterMatchSkipStrategy;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.cep.pattern.conditions.SimpleCondition;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;

import java.util.List;
import java.util.Map;

public class StreamingJob {
  public static void main(String[] args) throws Exception {
    final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

    DataStream<AtomicEvent> inputEventStream = env.addSource(new SocketSource(), "Socket Source");

    // Define a pattern
    AfterMatchSkipStrategy skipStrategy = AfterMatchSkipStrategy.noSkip();
    Pattern<AtomicEvent, ?> pattern =
        Pattern.<AtomicEvent>begin("first", skipStrategy)
            .where(
                new SimpleCondition<AtomicEvent>() {
                  @Override
                  public boolean filter(AtomicEvent atomicEvent) throws Exception {
                    return atomicEvent.getType().equals("A");
                  }
                })
            .followedByAny("second")
            .where(
                new SimpleCondition<AtomicEvent>() {
                  @Override
                  public boolean filter(AtomicEvent atomicEvent) throws Exception {
                    return atomicEvent.getType().equals("C");
                  }
                });

    // Apply the pattern to the input stream
    PatternStream<AtomicEvent> patternStream =
        CEP.pattern(inputEventStream, pattern).inProcessingTime();

    // Select matching patterns and print them
    DataStream<String> matches =
        patternStream.process(
            new PatternProcessFunction<AtomicEvent, String>() {
              @Override
              public void processMatch(
                  Map<String, List<AtomicEvent>> matches, Context ctx, Collector<String> out) {
                out.collect(matches.toString());
                //                AtomicEvent first = matches.get("first").get(0);
                //                AtomicEvent second = matches.get("second").get(0);
                //                out.collect("SEQ(A, C) detected: " + first.getType() + ", " +
                // second.getType());
              }
            });

    matches.print();

    env.execute("CEP Pattern Matching Job");
  }
}
