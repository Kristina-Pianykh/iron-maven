package iron_maven;

import iron_maven.sources.AtomicEvent;
import org.apache.flink.cep.PatternFlatSelectFunction;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    return new AtomicEvent(eventTypes[randomIndex], String.valueOf(nodeNum));
  }

  public static void createRandomEventStream(String[] args) {
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
  }

  public static void createDeterministicEventStream(String[] args) {
    String hostname = "localhost";

    int nodeID = Niceties.extractNodeNum(args, 0);
    List<Integer> ports = Niceties.extractPorts(args, 1);
    System.out.println(ports);

    try {
      Thread.sleep(3000);
    } catch (InterruptedException e) {
      System.out.println("Thread sleep interrupted");
      e.printStackTrace();
      System.exit(1);
    }

    for (int port : ports) {
      try (Socket socket = new Socket(hostname, port)) {
        MultiPortSender sender = new MultiPortSender(hostname, port, socket, nodeID);
        sender.start();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  private static class MultiPortSender extends Thread {
    String hostname;
    int nodeID;
    int port;
    Socket socket;

    public MultiPortSender(String hostname, int port, Socket socket, int nodeID) {
      this.hostname = hostname;
      this.port = port;
      this.nodeID = nodeID;
      this.socket = socket;
    }

    @Override
    public void run() {
      int[] delays;
      String[] eventTypes;

      int[] delays1 = {3000, 100, 4000};
      String[] eventTypes1 = new String[delays1.length];
      Arrays.fill(eventTypes1, "C");

      int[] delays2 = {300, 100, 2400, 800, 900};
      String[] eventTypes2 = new String[delays2.length];
      Arrays.fill(eventTypes2, "C");

      int[] delays3 = {450, 1250, 700, 500};
      String[] eventTypes3 = new String[delays3.length];
      Arrays.fill(eventTypes3, "A");

      if (this.nodeID == 1) {
        delays = delays1;
        eventTypes = eventTypes1;
      } else if (this.nodeID == 2) {
        delays = delays2;
        eventTypes = eventTypes2;
      } else {
        delays = delays3;
        eventTypes = eventTypes3;
      }

      int i = 0;

      while (i < delays.length) {
        try {
          ObjectOutputStream socketOutputStream = new ObjectOutputStream(socket.getOutputStream());
          AtomicEvent event = new AtomicEvent(eventTypes[i], String.valueOf(nodeID));
          socketOutputStream.writeObject(event); // Send message to server
        } catch (IOException e) {
          throw new RuntimeException(e);
        }

        try {
          Thread.sleep(delays[i]); // simulate a delay
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
        i++;
      }
    }
  }

  public static void main(String[] args) {
    createDeterministicEventStream(args);
  }
}
