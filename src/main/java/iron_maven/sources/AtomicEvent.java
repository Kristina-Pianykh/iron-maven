package iron_maven.sources;

import java.text.SimpleDateFormat;
import java.io.Serializable;
import java.util.Date;

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
    SimpleDateFormat formatter = new SimpleDateFormat("MMM dd,yyyy HH:mm:ss.SS");
    Date resultdate = new Date(this.timestamp);
    //    System.out.println(sdf.format(resultdate));
    //    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss:nn");
    //    String formattedTime = this.timestamp.format(formatter);
    return "[" + formatter.format(resultdate) + "] " + this.type + " " + this.id.toString();
  }
}
