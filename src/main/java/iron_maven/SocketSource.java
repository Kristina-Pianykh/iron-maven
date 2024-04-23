package iron_maven;

import iron_maven.sources.AtomicEvent;
import org.apache.flink.streaming.api.functions.source.RichSourceFunction;

import java.io.*;
import java.net.*;

public class SocketSource extends RichSourceFunction<AtomicEvent> {
  private String hostname = "localhost";
  private int port = 6666;

  public SocketSource(int port) {
    this.port = port;
  }

  @Override
  public void run(SourceContext<AtomicEvent> sourceContext) throws Exception {
    try (ServerSocket serverSocket = new ServerSocket(port)) {
      System.out.println(
          String.format("Server started. Listening for connections on port %d...", port));

      while (true) {
        Socket socket = serverSocket.accept();
        new ClientHandler(socket, sourceContext).start(); // Hand off to a new thread
        //        Thread.sleep(1000); // 1000 comes from Flink lib
      }
    } catch (IOException e) {
      e.printStackTrace(); // TODO: handle exception
      System.exit(1);
    }
  }

  @Override
  public void cancel() {}

  private static class ClientHandler extends Thread {
    private SourceContext<AtomicEvent> sourceContext;
    private Socket socket;

    public ClientHandler(Socket socket, SourceContext<AtomicEvent> sourceContext) {
      this.sourceContext = sourceContext;
      this.socket = socket;
    }

    @Override
    public void run() {
      try (DataInputStream inputStream = new DataInputStream(this.socket.getInputStream())) {
        System.out.println(
            "Socket for the connection: "
                + socket.getInetAddress()
                + ":"
                + socket.getPort()
                + " is open.");
        while (true) {
          try {
            ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
            AtomicEvent event = (AtomicEvent) objectInputStream.readObject();
            System.out.println("Receved event: " + event);
            // put generated sensor data to the queue
            sourceContext.collect(event);
          } catch (EOFException e) {
            System.out.println("Client has closed the connection.");
            break; // Exit the loop if EOFException is caught
          } catch (ClassNotFoundException e) {
            e.printStackTrace();
          }
        }
      } catch (IOException e) {
        e.printStackTrace();
      } finally {
        try {
          socket.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }
}
