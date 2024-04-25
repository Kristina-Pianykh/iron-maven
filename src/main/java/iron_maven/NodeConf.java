package iron_maven;

import org.apache.flink.configuration.ConfigOption;
import org.apache.flink.configuration.ConfigOptions;
import org.apache.flink.configuration.Configuration;

import java.util.List;

public class NodeConf {

  public NodeConf() {}

  public static final ConfigOption<Integer> HOST_PORT =
      ConfigOptions.key("hostPort").intType().noDefaultValue();

  public static final ConfigOption<List<Integer>> TARGET_PORTS =
      ConfigOptions.key("targetPorts").intType().asList().noDefaultValue();

  public static final ConfigOption<String> HOSTNAME =
      ConfigOptions.key("hostname").stringType().defaultValue("localhost");

  public static final ConfigOption<String> NODE_ID =
      ConfigOptions.key("nodeID").stringType().noDefaultValue();

  public static final ConfigOption<String> PATTERN_ID =
      ConfigOptions.key("patternID").stringType().noDefaultValue();

  public static boolean shouldForward(Configuration config) {
    return config.contains(TARGET_PORTS);
  }
}
