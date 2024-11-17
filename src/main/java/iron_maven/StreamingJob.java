package iron_maven;

import iron_maven.sources.AtomicEvent;
import iron_maven.sources.ControlMessage;
import iron_maven.sources.Message;

import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.cep.CEP;
import org.apache.flink.cep.PatternFlatSelectFunction;
import org.apache.flink.cep.PatternStream;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;
import org.apache.flink.configuration.Configuration;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import net.logstash.logback.decorate.CompositeJsonGeneratorDecorator;
// import com.github.loki4j.logback.Loki4jAppender;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashSet;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public class StreamingJob {
  //  private static final Logger logger = LoggerFactory.getLogger(StreamingJob.class);

  public static HashSet<Integer> processedMatches = new HashSet<>();

  public static void main(String[] args) throws Exception {
    Configuration nodeConfig = new Configuration();

    String patternID = Niceties.extractStrArg(args, 0);
    nodeConfig.setString(NodeConf.PATTERN_ID, patternID);
    //    logger.info("Selected patter: {}", patternID);
    //    System.out.println("Selected pattern: " + patternID);

    String nodeID = Niceties.extractStrArg(args, 1);
    nodeConfig.setString(NodeConf.NODE_ID, nodeID);

    int hostPort = Niceties.extractPort(args, 2);
    assert hostPort > 0 : "Host Port is not set";
    nodeConfig.setInteger(NodeConf.HOST_PORT, hostPort);

    if (args.length == 4) {
      //      String targetPorts = Niceties.extractStrArg(args, 3);
      List<Integer> targetPorts = Niceties.extractPorts(args, 3);
      nodeConfig.set(NodeConf.TARGET_PORTS, targetPorts);
      //      System.out.println("Target ports are set to " + targetPorts);
      //      logger.info("Target ports are set to {}", targetPorts);
    }

    //    logger.info("Initialized Node Config: {}", nodeConfig);
    //    System.out.println("Initialized Node Config: " + nodeConfig.toString());

    final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

    DataStream<Message> inputStream =
        env.addSource(new SocketSource(hostPort), "Socket Source")
            .assignTimestampsAndWatermarks(new CustomWatermarkStrategy());

    DataStream<ControlMessage> controlStream =
        inputStream
            .filter(
                new FilterFunction<Message>() {
                  @Override
                  public boolean filter(Message value) throws Exception {
                    return value.control;
                  }
                })
            .map(item -> (ControlMessage) item);
    controlStream.print();

    DataStream<AtomicEvent> eventStream =
        inputStream
            .filter(
                new FilterFunction<Message>() {
                  @Override
                  public boolean filter(Message value) throws Exception {
                    return !value.control;
                  }
                })
            .map(item -> (AtomicEvent) item);

    ArrayList<Pattern<AtomicEvent, ?>> patterns = new ArrayList<>();

    Pattern<AtomicEvent, ?> pattern1 = CustomPatterns.getPattern("1");
    Pattern<AtomicEvent, ?> pattern2 = CustomPatterns.getPattern("2");
    patterns.add(pattern1);
    patterns.add(pattern2);

    // Apply the pattern to the input stream
    //    assert pattern != null : "Pattern is not set";
    for (Pattern<AtomicEvent, ?> pattern : patterns) {
      PatternStream<AtomicEvent> patternStream = CEP.pattern(eventStream, pattern).inEventTime();

      // this is stupid. Just to convert PatternStream into DataStream
      DataStream<AtomicEvent> matches =
          patternStream.flatSelect(
              new PatternFlatSelectFunction<AtomicEvent, AtomicEvent>() {
                @Override
                public void flatSelect(
                    Map<String, List<AtomicEvent>> map, Collector<AtomicEvent> collector)
                    throws Exception {

                  System.out.println("\n======================================");
                  System.out.println(
                      "====================================== MATCH ======================================");
                  for (String key : map.keySet()) {
                    List<AtomicEvent> values = map.get(key);
                    //                    logger.info(String.format("%s: %s", key,
                    // values.toString()));
                    //                  System.out.println(key + ": " + values);
                    System.out.println(key + ": " + values.toString());
                    for (AtomicEvent event : values) {
                      Integer hashedEvent = event.hashCode();
                      if (!processedMatches.contains(hashedEvent)) {
                        //                        System.out.println("Sending event: " +
                        // event.toString());
                        processedMatches.add(hashedEvent);
                        //                        logger.info("Sending event: {}",
                        // event.toString());
                        collector.collect(event);
                      }
                    }
                  }
                  System.out.println("======================================\n");
                }
              });

      //      logger.info("The Node should forward: {}", NodeConf.shouldForward((nodeConfig)));
      //    System.out.println(NodeConf.shouldForward((nodeConfig)));
      if (NodeConf.shouldForward(nodeConfig)) {
        matches.addSink(new SocketSink(nodeConfig)).setParallelism(1);
      }
    }

    //    System.out.println(env.getExecutionPlan());
    env.getExecutionPlan();
    env.execute("CEP Pattern Matching Job");

    final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    Runnable task =
        () -> {
          patterns.remove(1);
          System.out.println("\n+++++++++++++++++++++++++++++++++++++\n");
          System.out.println("\n+++++++++++++++++++++++++++++++++++++\n");
          System.out.println("\n+++++++++++++++++++++++++++++++++++++\n");
          System.out.println("    REMOVED PATTERN 2     ");
          System.out.println("\n+++++++++++++++++++++++++++++++++++++\n");
          System.out.println("\n+++++++++++++++++++++++++++++++++++++\n");
          System.out.println("\n+++++++++++++++++++++++++++++++++++++\n");
        };
    scheduler.schedule(task, 12, TimeUnit.SECONDS);

    scheduler.shutdown();
  }
}
