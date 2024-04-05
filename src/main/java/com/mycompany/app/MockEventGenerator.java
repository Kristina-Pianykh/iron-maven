package com.mycompany.app;

import java.io.*;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;

public class MockEventGenerator {
  private static final String[] EVENT_TYPES = {"A", "__________", "++++++++", "C", "========="};

  public static String generateEvent() {
    int randomIndex = (int) (Math.random() * EVENT_TYPES.length);
    return EVENT_TYPES[randomIndex];
  }

  public static void main(String[] args) {
    int port = 6666;
    int poolSize = 40;
    // wait 3 sec for the cep engine to start
    try {
      Thread.sleep(3000);
    } catch (InterruptedException e) {
      System.out.println("Thread sleep interrupted");
      e.printStackTrace();
    }
    ExecutorService executor = Executors.newFixedThreadPool(poolSize); // Pool of 10 threads

    for (int i = 0; i < poolSize; i++) {
      int clientId = i; // Effectively final for use in lambda
      executor.submit(
          () -> {
            try (Socket socket = new Socket("localhost", port)) {
              System.out.println("Connected to server from Client " + clientId);

              DataOutputStream socketOutputStream = new DataOutputStream(socket.getOutputStream());
              String message = generateEvent();
              socketOutputStream.writeUTF(message); // Send message to server

              final ThreadLocalRandom random = ThreadLocalRandom.current();
              int delay = random.nextInt(800, 3000);
              Thread.sleep(delay); // simulate a delay

            } catch (IOException e) {
              e.printStackTrace();
            } catch (InterruptedException e) {
              System.out.println("Thread sleep interrupted");
              e.printStackTrace();
            }
          });
    }
    executor.shutdown(); // Initiates an orderly shutdown
  }
}
