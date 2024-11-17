package iron_maven.sources;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import com.fasterxml.jackson.databind.ObjectMapper;
import iron_maven.Niceties;

public class AtomicEvent extends Message {
  private final String type;
  private final long timestamp;
  private final EventID id;

  @JsonCreator
  public AtomicEvent(
      @JsonProperty("control") boolean control,
      @JsonProperty("type") String type,
      @JsonProperty("nodeID") String nodeId,
      @JsonProperty("timestamp") long timestamp,
      @JsonProperty("id") EventID id) {
    super(control);
    this.type = type;
    this.timestamp = timestamp;
    this.id = id;
  }

  public AtomicEvent(String type, String nodeId) {
    super(false);
    this.type = type;
    this.timestamp = System.currentTimeMillis();
    this.id = new EventID(nodeId);
  }

  public String getType() {
    return this.type;
  }

  public long getTimestamp() {
    return this.timestamp;
  }

  public EventID getID() {
    return this.id;
  }

  //  public String toString() {
  //    return Niceties.timestampToString(this.timestamp) + this.type + " " + this.id.toString();
  //  }

  @Override
  public String toString() {
    ObjectMapper mapper = new ObjectMapper();
    try {
      return mapper.writeValueAsString(this);
    } catch (Exception e) {
      return super.toString();
    }
  }
}
