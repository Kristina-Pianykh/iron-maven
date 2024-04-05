package com.mycompany.app.sources;

// import com.mycompany.app.sources;

import org.apache.flink.streaming.api.functions.source.RichSourceFunction;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

public class EventBroker extends RichSourceFunction<AtomicEvent> {
    private boolean running = true;

    @Override
    public void run(SourceContext<AtomicEvent> sourceContext) throws Exception {
        long startTime = System.currentTimeMillis();
        while (this.running) {
            final ThreadLocalRandom random = ThreadLocalRandom.current();
//            long timestamp = System.currentTimeMillis();
            String[] eventTypes = new String[]{"A", "C", "E", "G", "I"};
            AtomicEvent atomicEvent = new AtomicEvent(eventTypes[random.nextInt(0, 5)]);

            LocalTime now = LocalTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
            String formattedTime = now.format(formatter);
            System.out.println("[" + formattedTime + "] " + atomicEvent.toString());

            // put generated sensor data to the queue
            sourceContext.collect(atomicEvent);
            if (System.currentTimeMillis() - startTime > 30000) {
                this.running = false;
                break;
            }
            // sleep every one second after generating the fictional sensor data
            Thread.sleep(200);
        }
    }

    @Override
    public void cancel() {
        this.running = false;
    }
}
