package iron_maven.sources;

import java.util.UUID;
import java.lang.Object;
import java.util.Objects;
import java.util.List;
import java.util.ArrayList;
import java.io.Serializable;

public class EventID implements Serializable {
  private final UUID eventID = UUID.randomUUID();
  private final String nodeID;

  public EventID(String nodeID) {
    this.nodeID = nodeID;
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
