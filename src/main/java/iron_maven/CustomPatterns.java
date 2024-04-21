package iron_maven;

import iron_maven.sources.AtomicEvent;
import org.apache.flink.cep.nfa.aftermatch.AfterMatchSkipStrategy;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.cep.pattern.conditions.SimpleCondition;
import org.apache.flink.streaming.api.windowing.time.Time;

public class CustomPatterns {
  static AfterMatchSkipStrategy skipStrategy = AfterMatchSkipStrategy.noSkip();
  static Time timeWindowSize = Time.milliseconds(4000);

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
                })
            .within(timeWindowSize);
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
                })
            .within(timeWindowSize);
    return pattern;
  }
}
