package iron_maven;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import iron_maven.sources.AtomicEvent;

import java.io.*;
import java.net.*;
import java.util.List;
import java.util.Random;
import java.util.stream.LongStream;

public class MockEventGenerator {

  public static void createEventStream(String[] args) {
    String hostname = "localhost";

    String nodeID = Niceties.extractStrArg(args, 0);
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
      PortSender sender = new PortSender(hostname, port, nodeID);
      sender.start();
    }
  }

  private static class PortSender extends Thread {
    String hostname;
    int nodeID;
    int port;
    String eventType;

    public PortSender(String hostname, int port, String nodeID) {
      this.hostname = hostname;
      this.port = port;
      this.nodeID = Integer.parseInt(nodeID);
      if (nodeID.equals("1") || nodeID.equals("2")) this.eventType = "C";
      else if (nodeID.equals("3")) this.eventType = "A";
      else this.eventType = "D";
    }

    @Override
    public void run() {
      long seed = 12345L; // Fixed seed ensures reproducibility
      Random random = new Random(seed);
      LongStream longStream = random.longs(100, 4000);

      try (Socket socket = new Socket(this.hostname, this.port);
          BufferedWriter writer =
              new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))) {
        ObjectMapper mapper = new ObjectMapper();

        longStream.forEach(
            delay -> {
              AtomicEvent event = new AtomicEvent(this.eventType, String.valueOf(nodeID));
              System.out.println(event);
              String jsonString = null;
              try {
                jsonString = mapper.writeValueAsString(event);
                writer.write(jsonString);
                writer.flush(); // Make sure to flush to ensure all data is sent
                writer.newLine();
              } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
              } catch (IOException e) {
                throw new RuntimeException(e);
              }
              System.out.println(jsonString);
              // Write JSON string to socket output stream

              try {
                Thread.sleep(delay); // simulate a delay
              } catch (InterruptedException e) {
                throw new RuntimeException(e);
              }
            });

        System.out.println("Closing writer...");
        System.out.println("Closing socket...");
      } catch (UnknownHostException e) {
        System.err.println(
            "NodeID: " + this.nodeID + ". Hostname " + this.hostname + " is unknown");
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  public static void main(String[] args) {
    createEventStream(args);
  }
}
