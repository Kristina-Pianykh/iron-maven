package iron_maven;

import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import iron_maven.sources.AtomicEvent;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;

public class SocketSink<AtomicEvent> extends RichSinkFunction<AtomicEvent> {
  Configuration config;
  Map<Integer, Socket> portSocketMap = new HashMap<>();
  List<Integer> targetPorts;

  public SocketSink(Configuration config) {
    this.config = config;
    this.targetPorts =
        this.config.get(NodeConf.TARGET_PORTS); // TODO: identify nodes by map<NodeID,port>
  }

  @Override
  public void open(Configuration parameters) throws Exception {
    String hostname = this.config.get(NodeConf.HOSTNAME);

    for (Integer port : targetPorts) {
      Socket socket = Retry.openSocket(hostname, port);
      this.portSocketMap.put(port, socket);
    }
  }

  @Override
  public void close() throws Exception {
    for (Socket socket : this.portSocketMap.values()) {
      System.out.println(
          "NodeID: "
              + this.config.get(NodeConf.NODE_ID)
              + ". Closing socket "
              + socket.getInetAddress());
      socket.close();
    }
  }

  @Override
  public void invoke(AtomicEvent value, Context ctx) throws Exception {
    final int MAX_RETRIES = 3; // can it lead to congestions and inconsistencies?
    int numRetries = 0;

    for (Integer port : this.targetPorts) {

      while (numRetries < MAX_RETRIES) {
        Socket socket = this.portSocketMap.get(port);
        try {
          OutputStream outputStream = socket.getOutputStream();
          ObjectOutputStream socketOutputStream = new ObjectOutputStream(outputStream);
          socketOutputStream.writeObject(value);
          socketOutputStream.flush(); // ??
          System.out.printf("Sent %s to the socket \n", value);
          break;
        } catch (SocketException e) {
          System.err.println("Failed to send " + value.toString());
          System.err.println(
              "Failed to connect to socket cause connection was closed by the server");
          //        try {
          //          sleepTime = (++numRetries * 1000) + threadRandom.nextLong(1, 1000);
          //          Thread.sleep(sleepTime);
          //        } catch (InterruptedException ex) {
          //          System.err.println("Sleep interrupted");
          //          throw new RuntimeException(ex); // replace? how to handle this one?
          //        }
          Socket newSocket = Retry.openSocket(this.config.get(NodeConf.HOSTNAME), port);
          this.portSocketMap.put(port, newSocket);
          numRetries++;
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }
}
