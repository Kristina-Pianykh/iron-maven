package iron_maven;

import iron_maven.sources.AtomicEvent;
import iron_maven.sources.ControlMessage;
import iron_maven.sources.Message;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import org.apache.flink.streaming.api.functions.source.RichSourceFunction;

import java.io.*;
import java.net.*;

public class SocketSource extends RichSourceFunction<Message> {
  private volatile boolean isRunning = true;
  private String hostname = "localhost";
  private int port = 6666;

  public SocketSource(int port) {
    this.port = port;
  }

  @Override
  public void run(SourceContext<Message> sourceContext) throws Exception {
    try (ServerSocket serverSocket = new ServerSocket(port)) {
      System.out.println(
          String.format("Server started. Listening for connections on port %d...", port));

      while (this.isRunning) {
        Socket socket = serverSocket.accept();
        new ClientHandler(socket, sourceContext).start(); // Hand off to a new thread
      }
    } catch (IOException e) {
      e.printStackTrace(); // TODO: handle exception
      System.exit(1);
    }
  }

  @Override
  public void cancel() {
    this.isRunning = false; // any sockets/readers to close?
  }

  private static class ClientHandler extends Thread {
    private SourceContext<Message> sourceContext;
    private Socket socket;

    public ClientHandler(Socket socket, SourceContext<Message> sourceContext) {
      this.sourceContext = sourceContext;
      this.socket = socket;
    }

    @Override
    public void run() {
      ObjectMapper mapper = new ObjectMapper();
      Message message = null;

      try (BufferedReader reader =
          new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
        System.out.println("Socket for the connection: " + socket.getInetAddress() + " is open.");
        while (true) { // whi isRunning?
          try {

            if (reader.ready()) {
              System.out.println("reading");
              String line = reader.readLine();
              if (line != null) {
                System.out.println(line);
                try {
                  message = mapper.readValue(line, ControlMessage.class);

                } catch (UnrecognizedPropertyException e) {
                  message = mapper.readValue(line, AtomicEvent.class);
                }
                System.out.println(message);
                sourceContext.collect(message);
              }
            }
          } catch (EOFException e) {
            System.out.println("Client has closed the connection.");
            break; // Exit the loop if EOFException is caught
          }
        }
      } catch (IOException ex) {
        ex.printStackTrace();
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
