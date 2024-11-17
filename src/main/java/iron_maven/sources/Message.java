package iron_maven.sources;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Message {
  public boolean control;

  @JsonCreator
  public Message(@JsonProperty("control") boolean control) {
    this.control = control;
  }
}
