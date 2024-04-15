package iron_maven;

import iron_maven.sources.AtomicEvent;
import iron_maven.sources.SocketSource;
import java.io.*;
import java.net.*;

import org.apache.flink.api.common.serialization.SerializationSchema;
import org.apache.flink.cep.CEP;
import org.apache.flink.cep.PatternSelectFunction;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;
import org.apache.flink.cep.PatternFlatSelectFunction;
import org.apache.flink.cep.PatternStream;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.util.Collector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class StreamingJob {
  public static final String HOSTNAME = "localhost";

  public static void main(String[] args) throws Exception {
    int targetPort = -1;

    int patternNum = Niceties.extractPatternNum(args, 0);
    assert patternNum >= 1 : "Pattern number must be a positive integer";
    System.out.println("Selected pattern: " + patternNum);

    int hostPort = Niceties.extractPort(args, 1);
    assert hostPort > 0 : "Host Port is not set";

    if (args.length >= 3) {
      targetPort = Niceties.extractPort(args, 2);
      System.out.println("Target port is set to " + targetPort);
    }
    //    assert targetPort > 0 : "Target Port is not set";

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
    DataStream<String> matches =
        patternStream.flatSelect(
            new PatternFlatSelectFunction<AtomicEvent, String>() {
              @Override
              public void flatSelect(
                  Map<String, List<AtomicEvent>> map, Collector<String> collector)
                  throws Exception {
                for (List<AtomicEvent> list : map.values()) {
                  for (AtomicEvent event : list) {
                    collector.collect(event.getType());
                    System.out.println("sent to the collector: " + event.getType());
                  }
                }
              }
            });
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
    System.out.println("target port is set to " + targetPort);
    if (targetPort > 0) {
      System.out.printf("flushing matches of SEQ(A,C) to port %d\n", targetPort);
      int finalTargetPort = targetPort;
      matches.addSink(
          new RichSinkFunction<String>() {
            @Override
            public void invoke(String value, Context context) throws Exception {
              Socket socket = new Socket(HOSTNAME, finalTargetPort);
              DataOutputStream socketOutputStream = new DataOutputStream(socket.getOutputStream());
              socketOutputStream.writeUTF(value); // Send message to server
              socketOutputStream.flush();
              System.out.println("sent to the socket: " + value);
            }
          });
    }
    env.execute("CEP Pattern Matching Job");
  }
}
