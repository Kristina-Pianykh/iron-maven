package iron_maven.sources.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import com.github.loki4j.logback.json.AbstractFieldJsonProvider;
import com.github.loki4j.logback.json.JsonEventWriter;
import org.slf4j.event.KeyValuePair;

import java.util.List;

public class CustomJsonProvider extends AbstractFieldJsonProvider {
  public static final String FIELD_PREFIX = "custom_";

  public CustomJsonProvider() {
    setFieldName(FIELD_PREFIX);
  }

  @Override
  public boolean writeTo(JsonEventWriter writer, ILoggingEvent event, boolean startWithSeparator) {
    System.out.println("CustomJsonProvider.writeTo called");
    boolean firstFieldWritten = false;
    List<KeyValuePair> keyValuePairs = event.getKeyValuePairs();

    if (keyValuePairs == null) {
      writer.writeStringField("error", "keyValuePairs is null");
      return false;
    }

    for (KeyValuePair keyValuePair : keyValuePairs) {

      if (keyValuePair.key == null || keyValuePair.value == null) continue;
      if (startWithSeparator || firstFieldWritten) writer.writeFieldSeparator();

      writer.writeStringField(getFieldName() + keyValuePair.key, keyValuePair.value.toString());
      firstFieldWritten = true;
    }
    return firstFieldWritten;
  }

  @Override
  public boolean canWrite(ILoggingEvent event) {
    return event.getKeyValuePairs() != null && !event.getKeyValuePairs().isEmpty();
  }

  @Override
  protected void writeExactlyOneField(JsonEventWriter writer, ILoggingEvent event) {
    throw new UnsupportedOperationException(
        "CustomJsonProvider can write an arbitrary number of fields. writeExactlyOneField` should never be called for CustomJsonProvider.");
  }
}
