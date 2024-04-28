package iron_maven.sources;

import iron_maven.Niceties;

public class AtomicEvent extends Message {
  private String type;
  private long timestamp;
  private EventID id;

  public AtomicEvent(String type, String nodeId) {
    super(false);
    this.type = type;
    this.timestamp = System.currentTimeMillis();
    this.id = new EventID(nodeId);
  }

  public String getType() {
    return type;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public EventID getID() {
    return this.id;
  }

  public String toString() {
    return Niceties.timestampToString(this.timestamp) + this.type + " " + this.id.toString();
  }
}
