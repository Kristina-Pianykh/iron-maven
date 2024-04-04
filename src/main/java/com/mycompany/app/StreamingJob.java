package com.mycompany.app;

import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.cep.CEP;
import org.apache.flink.cep.PatternStream;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.cep.pattern.conditions.SimpleCondition;
// import org.apache.flink.cep.functions.PatternProcessFunction;
// import org.apache.flink.util.Collector;
import com.mycompany.app.sources.TemperatureSensor;
import com.mycompany.app.sources.Sensor;

import java.util.List;
import java.util.Map;

public class StreamingJob {
    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // Example input stream of temperature events
        //DataStream<TemperatureEvent> inputEventStream = env.fromData(
        //        new TemperatureEvent("sensor1", 95.0),
        //        new TemperatureEvent("sensor1", 105.0),
        //        new TemperatureEvent("sensor1", 110.0),
        //        new TemperatureEvent("sensor2", 99.0),
        //        new TemperatureEvent("sensor2", 101.0)
        //);
        DataStream<Sensor> inputEventStream = env.addSource(new TemperatureSensor(), "Temperature Sensor Stream");

        // Define a pattern
        Pattern<Sensor, ?> warningPattern = Pattern.<Sensor>begin("first")
                .where(new SimpleCondition<Sensor>() {
                    @Override
                    public boolean filter(Sensor value) throws Exception {
                        return value.getTemperature() > 100;
                    }
                })
                .next("second")
                .where(new SimpleCondition<Sensor>() {
                    @Override
                    public boolean filter(Sensor value) throws Exception {
                        return value.getTemperature() > 100;
                    }
                });

        // Apply the pattern to the input stream
        PatternStream<Sensor> patternStream = CEP.pattern(inputEventStream.keyBy(Sensor::getDeviceId), warningPattern).inProcessingTime();

        // Select matching patterns and print them
        DataStream<String> alerts = patternStream.select(
                (Map<String, List<Sensor>> pattern) -> {
                    Sensor firstEvent = pattern.get("first").get(0);
                    Sensor secondEvent = pattern.get("second").get(0);
                    return String.format("Temperature warning: %d: %f, %d: %f", firstEvent.getDeviceId(), firstEvent.getTemperature(),
                            secondEvent.getDeviceId(), secondEvent.getTemperature()
                    );
                    // return firstEvent.getDeviceId() + ": " +
                    //         firstEvent.getTemperature() + ", " + secondEvent.getDeviceId() + ": " + secondEvent.getTemperature();
                }
        );

        alerts.print();

        env.execute("Temperature Monitoring");
    }
}
