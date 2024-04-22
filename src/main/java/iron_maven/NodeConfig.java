package iron_maven;

import java.lang.reflect.Field;

public class NodeConfig {
  String patternID;
  int nodeID;
  int hostPort;
  Integer destPort = null;

  public NodeConfig(String patternID, int nodeID, int hostPort, int destPort) {
    this.patternID = patternID;
    this.nodeID = nodeID;
    this.hostPort = hostPort;
    this.destPort = destPort;
  }

  public NodeConfig(String patternID, int nodeID, int hostPort) {
    this.patternID = patternID;
    this.nodeID = nodeID;
    this.hostPort = hostPort;
  }

  public boolean shouldForward() {
    if (this.destPort != null) {
      return true;
    }
    return false;
  }
}
