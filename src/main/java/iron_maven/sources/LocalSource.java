package iron_maven.sources;

import java.util.concurrent.ThreadLocalRandom;
import org.apache.flink.streaming.api.functions.source.RichSourceFunction;

public class LocalSource extends RichSourceFunction<AtomicEvent> {
  private static final String[] EVENT_TYPES = {"A", "__________", "++++++++", "C", "========="};
  private boolean running = true;

  @Override
  public void run(SourceContext<AtomicEvent> sourceContext) throws Exception {
    while (running) {
      final ThreadLocalRandom random = ThreadLocalRandom.current();
      String randomEvent = EVENT_TYPES[random.nextInt(0, 5)];
      AtomicEvent atomicEvent = new AtomicEvent(randomEvent);
      System.out.println(atomicEvent.toString());

      // put generated sensor data to the queue
      sourceContext.collect(atomicEvent);

      Thread.sleep(1000); // 1000 comes from Flink lib
    }
  }

  @Override
  public void cancel() {}
}
