package iron_maven;

public class Niceties {

  public static int extractPatternNum(String[] args, int idx) {
    assert args.length > 0 : "Specify at least one argument";
    int patternNum = -1;

    try {
      patternNum = Integer.parseInt(args[idx]);
    } catch (NumberFormatException e) {
      e.printStackTrace();
    }
    return patternNum;
  }

  public static int extractPort(String[] args, int idx) {
    assert args.length > 0 : "Specify at least one argument";
    int port = -1;

    try {
      port = Integer.parseInt(args[idx]);
      assert (port >= 1024 && port <= 49151) : "Port should be between 1024 and 49151";
    } catch (NumberFormatException e) {
      e.printStackTrace();
    }
    return port;
  }
}
