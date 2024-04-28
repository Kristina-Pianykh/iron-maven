package iron_maven;

import com.fasterxml.jackson.databind.ObjectMapper;
import iron_maven.sources.ControlMessage;
import java.io.*;
import java.net.*;

public class CoordinatorStub {
  public static void main(String[] args) {
    int port = 6668;

    try {
      Socket socket = new Socket("localhost", port);
      // DataOutputStream out = new DataOutputStream(socket.getOutputStream());
      BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
      for (int i = 0; i < 10; i++) {
        ControlMessage message = new ControlMessage(true, "Hello World");
        System.out.println(message);
        ObjectMapper mapper = new ObjectMapper();
        String jsonString = mapper.writeValueAsString(message);
        System.out.println(jsonString);
        // Write JSON string to socket output stream
        writer.write(jsonString);
        writer.flush(); // Make sure to flush to ensure all data is sent
        writer.newLine();
        System.out.println("Sent message from loop iter " + i);
      }
      writer.close();
      socket.close(); // Close the socket once all messages are sent
    } catch (ConnectException e) {
      System.out.println("Server is not running. Please start the server and try again.");
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
