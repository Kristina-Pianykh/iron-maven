package iron_maven;

import org.apache.flink.cep.nfa.aftermatch.AfterMatchSkipStrategy;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.cep.pattern.conditions.SimpleCondition;
import iron_maven.sources.AtomicEvent;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Niceties {

  public static int extractPatternNum(String[] args, int idx) {
    assert args.length > 0 : "Specify at least one argument";
    int patternNum = -1;

    try {
      patternNum = Integer.parseInt(args[idx]);
    } catch (NumberFormatException e) {
      e.printStackTrace();
    }
    return patternNum;
  }

  public static int extractPort(String[] args, int idx) {
    assert args.length > 0 : "Specify at least one argument";
    int port = -1;

    try {
      port = Integer.parseInt(args[idx]);
      assert (port >= 1024 && port <= 49151) : "Port should be between 1024 and 49151";
    } catch (NumberFormatException e) {
      e.printStackTrace();
    }
    return port;
  }

  public static List<Integer> extractPorts(String[] args, int idx) {
    assert args.length > 0 : "Specify at least one argument";
    List<Integer> ports = new ArrayList<>();
    String portsString = null;

    try {
      portsString = args[idx];
    } catch (ArrayIndexOutOfBoundsException e) {
      System.out.println("Specify at least one port");
      System.exit(1);
    }

    try {
      for (String port : portsString.split(",")) {
        int portInt = Integer.parseInt(port);
        assert (portInt >= 1024 && portInt <= 49151) : "Port should be between 1024 and 49151";
        ports.add(portInt);
      }
    } catch (NumberFormatException e) {
      System.out.println("Port must be a numher");
      e.printStackTrace();
    }
    return ports;
  }

  public static int extractNodeNum(String[] args, int idx) {
    assert args.length > 0 : "Specify at least one argument";
    int nodeNum = -1;

    try {
      nodeNum = Integer.parseInt(args[idx]);
    } catch (NumberFormatException e) {
      e.printStackTrace();
      System.exit(-1);
    }
    return nodeNum;
  }

  public static String timestampToString(long timestamp) {
    //    SimpleDateFormat formatter = new SimpleDateFormat("MMM dd,yyyy HH:mm:ss.SS");
    SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss.SS");
    Date resultdate = new Date(timestamp);
    return "[" + formatter.format(resultdate) + "] ";
  }

  public static class CustomPatterns {
    static AfterMatchSkipStrategy skipStrategy = AfterMatchSkipStrategy.noSkip();

    public static Pattern<AtomicEvent, ?> getPattern(int patternNum) {
      switch (patternNum) {
        case 1:
          return CustomPatterns.getPattern1();
        case 2:
          return CustomPatterns.getPattern2();
      }
      return null;
    }

    public static Pattern<AtomicEvent, ?> getPattern1() {
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
      return pattern;
    }

    public static Pattern<AtomicEvent, ?> getPattern2() {
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
                      return atomicEvent.getType().equals("D");
                    }
                  })
              .followedByAny("third")
              .where(
                  new SimpleCondition<AtomicEvent>() {
                    @Override
                    public boolean filter(AtomicEvent atomicEvent) throws Exception {
                      return atomicEvent.getType().equals("C");
                    }
                  });
      return pattern;
    }
  }
}
