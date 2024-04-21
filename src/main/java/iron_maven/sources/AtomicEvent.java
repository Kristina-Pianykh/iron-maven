package iron_maven.sources;

import java.io.Serializable;
import iron_maven.Niceties;

public class AtomicEvent implements Serializable {
  private static final long serialVersionUID = 7L;
  private String type;
  private long timestamp;
  private EventID id;

  public AtomicEvent(String type, String nodeId) {
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
