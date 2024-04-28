package iron_maven;

import java.time.Duration;
import iron_maven.sources.Message;
import org.apache.flink.api.common.eventtime.*;

import static org.apache.flink.util.Preconditions.checkArgument;
import static org.apache.flink.util.Preconditions.checkNotNull;

public class CustomWatermarkStrategy implements WatermarkStrategy<Message> {
  private static final long MAX_OUT_OF_ORDERNESS = 1000;

  @Override
  public WatermarkGenerator<Message> createWatermarkGenerator(
      WatermarkGeneratorSupplier.Context context) {
    return new CustomWatermarkGenerator();
    //    return new CustomBoundedOutOfOrderWatermark<>(Duration.ofMillis(300));
  }

  @Override
  public TimestampAssigner<Message> createTimestampAssigner(
      TimestampAssignerSupplier.Context context) {
    return new CustomTimestampAssigner(); // extract timestamp from the event
  }

  public static class CustomTimestampAssigner implements TimestampAssigner<Message> {
    @Override
    public long extractTimestamp(Message element, long recordTimestamp) {
      return System.currentTimeMillis();
      //      return element.getTimestamp();
    }
  }

  public static class CustomWatermarkGenerator implements WatermarkGenerator<Message> {
    @Override
    public void onEvent(Message event, long eventTimestamp, WatermarkOutput output) {
      //      System.out.println("Event: " + event.toString());
      //      System.out.println("Event timestamp: " + eventTimestamp);

      // emit a watermark on event to allow to match only events that arrived MAX_OUT_OF_ORDERNESS
      // before
      //      long watermark = System.currentTimeMillis() - MAX_OUT_OF_ORDERNESS;
      long watermark = System.currentTimeMillis();
      System.out.println("Emitting on event watermark: " + Niceties.timestampToString(watermark));
      output.emitWatermark(new Watermark(watermark));
      //      output.emitWatermark(new Watermark(eventTimestamp));
    }

    @Override
    public void onPeriodicEmit(WatermarkOutput output) {}
  }

  public static class CustomBoundedOutOfOrderWatermark<Message>
      implements WatermarkGenerator<Message> {
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
    public void onEvent(Message event, long eventTimestamp, WatermarkOutput output) {
      maxTimestamp = Math.max(maxTimestamp, eventTimestamp);
    }

    @Override
    public void onPeriodicEmit(WatermarkOutput output) {
      output.emitWatermark(new Watermark(maxTimestamp - outOfOrdernessMillis - 1));
    }
  }
}
