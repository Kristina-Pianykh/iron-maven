package iron_maven;

import java.io.*;
import java.net.*;
import java.util.concurrent.ThreadLocalRandom;

public class MockEventGenerator {
  private static final String[] EVENT_TYPES = {
    "A", "__________", "D", ">>>>>>>>>", "++++++++", "C"
  };

  public static String generateEvent() {
    int randomIndex = (int) (Math.random() * EVENT_TYPES.length);
    return EVENT_TYPES[randomIndex];
  }

  public static void main(String[] args) {
    int poolSize = 40;
    int port = Niceties.extractPort(args, 0);
    System.out.println("Sending events to port " + port);
    // wait 3 sec for the cep engine to start
    try {
      Thread.sleep(3000);
    } catch (InterruptedException e) {
      System.out.println("Thread sleep interrupted");
      e.printStackTrace();
    }

    for (int i = 0; i < poolSize; i++) {
      try (Socket socket = new Socket("localhost", port)) {
        System.out.println("Connected to server from Client " + i);

        DataOutputStream socketOutputStream = new DataOutputStream(socket.getOutputStream());
        String message = generateEvent();
        socketOutputStream.writeUTF(message); // Send message to server

        final ThreadLocalRandom random = ThreadLocalRandom.current();
        int delay = random.nextInt(300, 2500);
        Thread.sleep(delay); // simulate a delay

      } catch (IOException e) {
        e.printStackTrace();
      } catch (InterruptedException e) {
        System.out.println("Thread sleep interrupted");
        e.printStackTrace();
      }
    }
    ;
  }
}
