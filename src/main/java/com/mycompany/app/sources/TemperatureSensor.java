package com.mycompany.app.sources;

// import com.mycompany.app.sources;

import org.apache.flink.streaming.api.functions.source.RichSourceFunction;

import java.time.LocalTime;
import java.util.concurrent.ThreadLocalRandom;

public class TemperatureSensor extends RichSourceFunction<Sensor> {
    private boolean running = true;

    @Override
    public void run(SourceContext<Sensor> sourceContext) throws Exception {
        long startTime = System.currentTimeMillis();
        while (this.running) {
            final ThreadLocalRandom random = ThreadLocalRandom.current();
            long timestamp = System.currentTimeMillis();
            Sensor sensor = new Sensor(random.nextInt(0, 10), random.nextDouble(50, 110), timestamp);
            System.out.println("[" + LocalTime.now() + "] " + sensor.toString());

            // put generated sensor data to the queue
            sourceContext.collect(sensor);
            if (System.currentTimeMillis() - startTime > 10000) {
                this.running = false;
                break;
            }
            // sleep every one second after generating the fictional sensor data
            Thread.sleep(100);
        }
    }

    @Override
    public void cancel() {
        this.running = false;
    }
}