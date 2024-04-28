package iron_maven.sources;

import java.io.Serializable;

public class Message implements Serializable {
  private static final long serialVersionUID = 7L;
  public boolean control;

  public Message(boolean control) {
    this.control = control;
  }
}
