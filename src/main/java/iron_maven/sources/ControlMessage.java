package iron_maven.sources;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ControlMessage extends Message {
  String message;

  @JsonCreator
  public ControlMessage(
      @JsonProperty("control") boolean control, @JsonProperty("message") String message) {
    super(control);
    this.message = message;
  }

  public ControlMessage(String message) {
    super(true);
    this.message = message;
  }

  public String getMessage() {
    return message;
  }

  @Override
  public String toString() {
    return "control: " + this.control + ", message: " + this.message;
  }
}
