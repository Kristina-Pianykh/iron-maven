package iron_maven.sources;

import java.util.UUID;
import java.lang.Object;
import java.util.Objects;
import java.io.Serializable;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class EventID {
  private final UUID eventID;
  private final String nodeID;

  @JsonCreator
  public EventID(@JsonProperty("nodeID") String nodeID, @JsonProperty("eventID") UUID eventID) {
    this.nodeID = nodeID;
    this.eventID = eventID;
  }

  public EventID(String nodeID) {
    this.nodeID = nodeID;
    this.eventID = UUID.randomUUID();
  }

  public String getNodeID() {
    return nodeID;
  }

  public UUID getEventID() {
    return eventID;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    return o.hashCode() == this.hashCode();
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.nodeID, this.eventID);
  }

  @Override
  public String toString() {
    return "NodeID: " + this.nodeID + ", EventID: " + this.eventID;
  }
}
