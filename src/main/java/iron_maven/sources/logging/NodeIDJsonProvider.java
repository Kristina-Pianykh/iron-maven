package iron_maven.sources.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import com.github.loki4j.logback.json.AbstractFieldJsonProvider;
import com.github.loki4j.logback.json.JsonEventWriter;

public class NodeIDJsonProvider extends AbstractFieldJsonProvider {
  public NodeIDJsonProvider() {
    setFieldName("node_id");
  }

  @Override
  protected void writeExactlyOneField(JsonEventWriter writer, ILoggingEvent event) {
    writer.writeStringField(getFieldName(), event.getFormattedMessage());
  }
}
