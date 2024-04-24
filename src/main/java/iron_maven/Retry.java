package iron_maven;

import java.io.*;
import java.net.*;

public class Retry {
  public static final int MAX_RETRIES = 3; // can it lead to congestions and inconsistencies?

  public static Socket openSocket(String hostname, int port) {
    Socket socket = null;
    while (socket == null) {
      try {
        socket = new Socket(hostname, port);
        System.out.println("(Re)connected to " + hostname + " on " + socket.getInetAddress());
        break;
      } catch (UnknownHostException e) {
        e.printStackTrace();
        System.exit(-1);
      } catch (IOException e) {
        System.err.println(
            "Failed to connect to "
                + hostname
                + " on port "
                + port
                + ". Attempting a new socket...");
        //        e.printStackTrace();
      }
    }
    return socket;
  }
}
