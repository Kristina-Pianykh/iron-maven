package iron_maven;

import iron_maven.sources.AtomicEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

public class LogTester {
  private static final Logger logger = LoggerFactory.getLogger(LogTester.class);

  public static void main(String[] args) {
    AtomicEvent event1 = new AtomicEvent("SEQ(A, K)", "0");
    AtomicEvent event2 = new AtomicEvent("AND(B, C)", "3");
    AtomicEvent[] events = {event1, event2};

    MDC.put("logFileName", "head1");
    System.out.println(event1.getID().getEventID().toString());
    //    logger.info("test", event1.getID().getEventID().toString());
    logger.atInfo().addKeyValue("EVENTID", "test").log();
    for (AtomicEvent event : events) {
      System.out.println(event.getID().getEventID().toString());
      //      logger.atInfo().addKeyValue("EVENTID", event.getID().getEventID().toString()).log();
      //      logger.info("event_id", event.getID().getEventID());
      //      logger.info(event.getID().getEventID().toString());
      //      logger.atInfo().setMessage(event.getID().getEventID().toString());
    }
    //    logger.atInfo().addKeyValue("event", event).log();
    //    logger.info("new event");
    try {
      Thread.sleep(5000);
    } catch (InterruptedException e) {
    }
  }
}
