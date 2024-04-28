package iron_maven;

import iron_maven.sources.AtomicEvent;
import org.apache.flink.cep.nfa.aftermatch.AfterMatchSkipStrategy;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.cep.pattern.conditions.IterativeCondition;
import org.apache.flink.cep.pattern.conditions.SimpleCondition;
import org.apache.flink.streaming.api.windowing.time.Time;

public class CustomPatterns {
  static AfterMatchSkipStrategy skipStrategy = AfterMatchSkipStrategy.noSkip();
  static long timestampDiff = 3000;
  static Time statePersistWindow =
      Time.milliseconds(10000); // TODO: experiment with frequency of clearing state

  public static Pattern<AtomicEvent, ?> getPattern(String patternID) {
    switch (patternID) {
      case "1":
        return CustomPatterns.getPattern1();
      case "2":
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
                new IterativeCondition<AtomicEvent>() {
                  @Override
                  public boolean filter(AtomicEvent value, Context<AtomicEvent> ctx)
                      throws Exception {

                    AtomicEvent startEvent = ctx.getEventsForPattern("first").iterator().next();
                    boolean correctTimeDiff =
                        (value.getTimestamp() - startEvent.getTimestamp()) <= timestampDiff;
                    return value.getType().equals("C") && correctTimeDiff;
                  }
                })
            .within(statePersistWindow);
    return pattern;
  }

  public static Pattern<AtomicEvent, ?> getPattern2() {
    Pattern<AtomicEvent, ?> pattern =
        Pattern.<AtomicEvent>begin("first", skipStrategy)
            .where(
                new SimpleCondition<AtomicEvent>() {
                  @Override
                  public boolean filter(AtomicEvent atomicEvent) throws Exception {
                    boolean correctType = atomicEvent.getType().equals("A");
                    if (!correctType) {
                      return false;
                    }
                    //                    System.out.println();
                    //                    System.out.println("first. Evaluating: " +
                    // atomicEvent.getType());
                    //                    System.out.println(
                    //                        "atomicEvent.getType().equals(\"A\") = "
                    //                            + atomicEvent.getType().equals("A"));
                    return true;
                  }
                })
            .followedByAny("second")
            .where(
                new IterativeCondition<AtomicEvent>() {
                  @Override
                  public boolean filter(AtomicEvent value, Context<AtomicEvent> ctx)
                      throws Exception {

                    if (!value.getType().equals("D")) return false;
                    AtomicEvent startEvent = ctx.getEventsForPattern("first").iterator().next();
                    boolean correctOrder = value.getTimestamp() > startEvent.getTimestamp();
                    boolean correctTimeDiff =
                        (value.getTimestamp() - startEvent.getTimestamp()) <= timestampDiff;
                    //                    System.out.println();
                    //                    System.out.println("second. Evaluating: " +
                    // value.getType());
                    //                    System.out.println("value.getTimestamp() = " +
                    // value.getTimestamp());
                    //                    System.out.println("startEvent.getTimestamp() = " +
                    // startEvent.getTimestamp());
                    //                    System.out.println(
                    //                        "timeDiff value-startEvent:" + (value.timestamp -
                    // startEvent.timestamp));
                    //                    System.out.println("correctTimeDiff = " +
                    // correctTimeDiff);
                    //                    System.out.println("correctOrder = " + correctOrder);
                    return correctOrder && correctTimeDiff;
                  }
                })
            .followedByAny("third")
            .where(
                new IterativeCondition<AtomicEvent>() {
                  @Override
                  public boolean filter(AtomicEvent value, Context<AtomicEvent> ctx)
                      throws Exception {

                    if (!value.getType().equals("C")) return false;
                    AtomicEvent startEvent = ctx.getEventsForPattern("first").iterator().next();
                    AtomicEvent middleEvent = ctx.getEventsForPattern("second").iterator().next();
                    //                    System.out.println("Event type: " + value.getType());
                    boolean correctOrder = value.getTimestamp() > middleEvent.getTimestamp();
                    boolean correctTimeDiff =
                        (value.getTimestamp() - startEvent.getTimestamp()) <= timestampDiff;
                    //                    System.out.println("third. Evaluating: " +
                    // value.getType());
                    //                    System.out.println("value.getTimestamp() = " +
                    // value.getTimestamp());
                    //                    System.out.println(
                    //                        "timeDiff value-startEvent:" + (value.timestamp -
                    // startEvent.timestamp));
                    //                    System.out.println("correctTimeDiff = " +
                    // correctTimeDiff);
                    //                    System.out.println("correctOrder = " + correctOrder);
                    //                    System.out.println("evaluating: " + value.getType());
                    //                    System.out.println("correctOrder: " + correctOrder);
                    //                    System.out.println("correctTimeDiff: " + correctTimeDiff);
                    return correctOrder && correctTimeDiff;
                  }
                })
            .within(statePersistWindow);
    return pattern;
  }
}
