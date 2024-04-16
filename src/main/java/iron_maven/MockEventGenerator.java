package iron_maven;

import iron_maven.sources.AtomicEvent;
import org.apache.flink.cep.PatternFlatSelectFunction;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.io.*;
import java.net.*;
import java.util.concurrent.ThreadLocalRandom;

public class MockEventGenerator {
  private static final String[] EVENT_TYPES_NODE1 = {
    "A", "__________", ">>>>>>>>>", "++++++++", "C"
  };

  private static final String[] EVENT_TYPES_NODE2 = {
    "A", "__________", "D", ">>>>>>>>>", "++++++++"
  };

  public static AtomicEvent generateEvent(int nodeNum) {
    String[] eventTypes = {};
    if (nodeNum == 1) {
      eventTypes = EVENT_TYPES_NODE1;
    } else if (nodeNum == 2) {
      eventTypes = EVENT_TYPES_NODE2;
    } else {
      System.err.println("Invalid node number: " + nodeNum);
      System.exit(1);
    }
    int randomIndex = (int) (Math.random() * eventTypes.length);
    return new AtomicEvent(eventTypes[randomIndex]);
  }

  public static void main(String[] args) {
    int poolSize = 40;
    int port = Niceties.extractPort(args, 0);
    int nodeNum = Niceties.extractNodeNum(args, 1);
    System.out.printf("Sending events to node %d on port %d\n", nodeNum, port);
    // wait 3 sec for the cep engine to start
    try {
      Thread.sleep(3000);
    } catch (InterruptedException e) {
      System.out.println("Thread sleep interrupted");
      e.printStackTrace();
      System.exit(1);
    }

    for (int i = 0; i < poolSize; i++) {
      try (Socket socket = new Socket("localhost", port)) {
        System.out.println("Connected to server from Client " + i);

        ObjectOutputStream socketOutputStream = new ObjectOutputStream(socket.getOutputStream());
        AtomicEvent event = generateEvent(nodeNum);
        socketOutputStream.writeObject(event); // Send message to server

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
