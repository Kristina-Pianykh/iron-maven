package com.mycompany.app.sources;

import org.apache.flink.streaming.api.functions.source.RichSourceFunction;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class SocketSource extends RichSourceFunction<AtomicEvent> {
    private String hostname = "localhost";
    private int port = 6666;

    @Override
    public void run(SourceContext<AtomicEvent> sourceContext) throws Exception {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println(String.format("Server started. Listening for connections on port %d...", port));

            while (true) {
//            final ThreadLocalRandom random = ThreadLocalRandom.current();
//            long timestamp = System.currentTimeMillis();
//            String[] eventTypes = new String[]{"A", "C", "E", "G", "I"};
//            AtomicEvent atomicEvent = new AtomicEvent(eventTypes[random.nextInt(0, 5)]);

                Socket socket = serverSocket.accept();
                new ClientHandler(socket, sourceContext).start(); // Hand off to a new thread

                Thread.sleep(1000); // 1000 comes from Flink lib
            }
        } catch (IOException e) {
            e.printStackTrace(); // TODO: handle exception
        }
    }

    @Override
    public void cancel() {
    }

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
                DataInputStream socketInputStream = new DataInputStream(socket.getInputStream());
                String message = socketInputStream.readUTF(); // TODO: how to read??
                AtomicEvent event = new AtomicEvent(message);

                LocalTime now = LocalTime.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
                String formattedTime = now.format(formatter);
                System.out.println("[" + formattedTime + "] " + event.toString());

                // put generated sensor data to the queue
                sourceContext.collect(event);

                socket.close(); // Close connection
            } catch (IOException e) {
                e.printStackTrace(); // TODO: handle exception
            }
        }
    }
}
