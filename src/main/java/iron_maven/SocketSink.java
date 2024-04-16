package iron_maven;

import iron_maven.sources.AtomicEvent;

import java.io.*;
import java.net.*;

import org.apache.flink.cep.CEP;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;
import org.apache.flink.cep.PatternFlatSelectFunction;
import org.apache.flink.cep.PatternStream;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;

import java.util.List;
import java.util.Map;

public class SocketSink<AtomicEvent> extends RichSinkFunction<AtomicEvent> {
  String hostname;
  int port;

  public SocketSink(String hostname, int port) {
    this.hostname = hostname;
    this.port = port;
  }

  @Override
  public void open(Configuration parameters) throws Exception {}

  @Override
  public void close() throws Exception {}

  @Override
  public void invoke(AtomicEvent value, Context ctx) throws Exception {
    try {
      Socket socket = new Socket(this.hostname, this.port);
      OutputStream outputStream = socket.getOutputStream();
      ObjectOutputStream socketOutputStream = new ObjectOutputStream(outputStream);
      socketOutputStream.writeObject(value);
      //      socketOutputStream.writeUTF(value); // Send message to server
      socketOutputStream.flush();
      System.out.printf("Sent %s to the socket \n", value);
    } catch (IOException e) {
      e.printStackTrace();
      System.out.println("Failed to send event to socket from result stream");
      System.exit(1);
    }
  }
}
