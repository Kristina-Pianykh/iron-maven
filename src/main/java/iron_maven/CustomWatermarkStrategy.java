package iron_maven;

import java.time.Duration;
import iron_maven.sources.AtomicEvent;
import org.apache.flink.api.common.eventtime.*;

import static org.apache.flink.util.Preconditions.checkArgument;
import static org.apache.flink.util.Preconditions.checkNotNull;

public class CustomWatermarkStrategy implements WatermarkStrategy<AtomicEvent> {
  private static final long MAX_OUT_OF_ORDERNESS = 800;

  @Override
  public WatermarkGenerator<AtomicEvent> createWatermarkGenerator(
      WatermarkGeneratorSupplier.Context context) {
    return new CustomWatermarkGenerator();
    //    return new CustomBoundedOutOfOrderWatermark<>(Duration.ofMillis(300));
  }

  @Override
  public TimestampAssigner<AtomicEvent> createTimestampAssigner(
      TimestampAssignerSupplier.Context context) {
    return new CustomTimestampAssigner(); // extract timestamp from the event
  }

  public static class CustomTimestampAssigner implements TimestampAssigner<AtomicEvent> {
    @Override
    public long extractTimestamp(AtomicEvent element, long recordTimestamp) {
      return element.getTimestamp();
    }
  }

  public static class CustomWatermarkGenerator implements WatermarkGenerator<AtomicEvent> {
    @Override
    public void onEvent(AtomicEvent event, long eventTimestamp, WatermarkOutput output) {
      //      System.out.println("Event: " + event.toString());
      //      System.out.println("Event timestamp: " + eventTimestamp);

      // emit a watermark on event to allow to match only events that arrived MAX_OUT_OF_ORDERNESS
      // before
      output.emitWatermark(new Watermark(System.currentTimeMillis() - MAX_OUT_OF_ORDERNESS));
      //      output.emitWatermark(new Watermark(eventTimestamp));
    }

    @Override
    public void onPeriodicEmit(WatermarkOutput output) {}
  }

  public static class CustomBoundedOutOfOrderWatermark<Event> implements WatermarkGenerator<Event> {
    /** The maximum timestamp encountered so far. */
    private long maxTimestamp;

    /** The maximum out-of-orderness that this watermark generator assumes. */
    private final long outOfOrdernessMillis;

    public CustomBoundedOutOfOrderWatermark(Duration maxOutOfOrderness) {
      checkNotNull(maxOutOfOrderness, "maxOutOfOrderness");
      checkArgument(!maxOutOfOrderness.isNegative(), "maxOutOfOrderness cannot be negative");

      this.outOfOrdernessMillis = maxOutOfOrderness.toMillis();

      // start so that our lowest watermark would be Long.MIN_VALUE.
      this.maxTimestamp = Long.MIN_VALUE + outOfOrdernessMillis + 1;
    }

    @Override
    public void onEvent(Event event, long eventTimestamp, WatermarkOutput output) {
      maxTimestamp = Math.max(maxTimestamp, eventTimestamp);
      //      System.out.println("Max timestamp: " + maxTimestamp);
      //      output.emitWatermark(new Watermark(eventTimestamp));
    }

    @Override
    public void onPeriodicEmit(WatermarkOutput output) {
      //      System.out.println(
      //          "Emitting periodic watermark: " + (maxTimestamp - outOfOrdernessMillis - 1));
      output.emitWatermark(new Watermark(maxTimestamp - outOfOrdernessMillis - 1));
    }
  }
}
