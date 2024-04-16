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
    private Socket socket;
    private SourceContext<AtomicEvent> sourceContext;

    public ClientHandler(Socket socket, SourceContext<AtomicEvent> sourceContext) {
      this.socket = socket;
      this.sourceContext = sourceContext;
    }

    @Override
    public void run() {
      try {
        InputStream inputStream = socket.getInputStream();
        ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
        AtomicEvent event = (AtomicEvent) objectInputStream.readObject();
        System.out.println("Receved event: " + event);

        //        LocalTime now = LocalTime.now();
        //        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        //        String formattedTime = now.format(formatter);
        //        System.out.println("[" + formattedTime + "] " + event.toString());

        // put generated sensor data to the queue
        sourceContext.collect(event);

        socket.close(); // Close connection
      } catch (IOException e) {
        e.printStackTrace(); // TODO: handle exception
        System.exit(1);
      } catch (ClassNotFoundException e) {
        throw new RuntimeException(e);
      }
    }
  }
}
