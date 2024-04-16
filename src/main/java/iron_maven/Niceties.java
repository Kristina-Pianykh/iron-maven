package iron_maven;

import org.apache.flink.cep.nfa.aftermatch.AfterMatchSkipStrategy;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.cep.pattern.conditions.SimpleCondition;
import iron_maven.sources.AtomicEvent;

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
