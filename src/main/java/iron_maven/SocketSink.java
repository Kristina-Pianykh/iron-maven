package iron_maven;

import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

  public void openSocket(int port) {
    String hostname = this.config.get(NodeConf.HOSTNAME);

    try {
      System.out.println(
          "NodeID: "
              + this.config.get(NodeConf.NODE_ID)
              + " creating socket to connect to port "
              + port);
      this.portSocketMap.put(port, new Socket(hostname, port));
      System.out.println(
          "NodeID: "
              + this.config.get(NodeConf.NODE_ID)
              + " opened socket "
              + this.portSocketMap.get(port));
    } catch (UnknownHostException e) {
      System.err.println(
          "NodeID: "
              + this.config.get(NodeConf.NODE_ID)
              + ". Hostname "
              + hostname
              + " is unknown");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void open(Configuration parameters) throws Exception {
    for (Integer port : targetPorts) {
      openSocket(port);
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
    try {
      for (Integer port : this.targetPorts) {
        Socket socket = this.portSocketMap.get(port);
        OutputStream outputStream = socket.getOutputStream();
        ObjectOutputStream socketOutputStream = new ObjectOutputStream(outputStream);
        try {
          socketOutputStream.writeObject(value);
          socketOutputStream.flush(); // ??
          System.out.printf("Sent %s to the socket \n", value);
        } catch (SocketException e) {
          System.err.println(
              "NodeID: "
                  + this.config.get(NodeConf.NODE_ID)
                  + ", socket "
                  + socket.getInetAddress()
                  + " is closed by server");
          openSocket(port);
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
      System.out.println("Failed to send event to socket from result stream");
      System.exit(1);
    }
  }
}
