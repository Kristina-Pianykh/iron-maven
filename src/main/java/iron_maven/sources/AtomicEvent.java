package iron_maven.sources;

import java.time.Instant;

public class AtomicEvent {
  private String type;
  private Long timestamp;

  public AtomicEvent(String type) {
    this.type = type;
    //    this.timestamp = System.currentTimeMillis() % 1000; // milliseconds
    this.timestamp = Instant.now().toEpochMilli();
  }

  public String getType() {
    return type;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public String toString() {
    String time = Instant.ofEpochSecond(this.timestamp).toString();
    return "type: " + this.type + ", timestamp: " + time;
  }
}
