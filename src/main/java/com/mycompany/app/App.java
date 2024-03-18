package com.mycompany.app;

import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.cep.CEP;
import org.apache.flink.cep.PatternStream;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.cep.pattern.conditions.SimpleCondition;
import org.apache.flink.cep.functions.PatternProcessFunction;
import org.apache.flink.util.Collector;
import java.util.List;
import java.util.Map;

public class App {
    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // Example input stream of temperature events
        DataStream<TemperatureEvent> inputEventStream = env.fromElements(
                new TemperatureEvent("sensor1", 95.0),
                new TemperatureEvent("sensor1", 105.0),
                new TemperatureEvent("sensor1", 110.0),
                new TemperatureEvent("sensor2", 99.0),
                new TemperatureEvent("sensor2", 101.0)
        );

        // Define a pattern
        Pattern<TemperatureEvent, ?> warningPattern = Pattern.<TemperatureEvent>begin("first")
                .where(new SimpleCondition<TemperatureEvent>() {
                    @Override
                    public boolean filter(TemperatureEvent value) throws Exception {
                        return value.getTemperature() > 100;
                    }
                })
                .next("second")
                .where(new SimpleCondition<TemperatureEvent>() {
                    @Override
                    public boolean filter(TemperatureEvent value) throws Exception {
                        return value.getTemperature() > 100;
                    }
                });

        // Apply the pattern to the input stream
        PatternStream<TemperatureEvent> patternStream = CEP.pattern(inputEventStream.keyBy(TemperatureEvent::getSensorId), warningPattern).inProcessingTime();

        // Select matching patterns and print them
        DataStream<String> alerts = patternStream.select(
                (Map<String, List<TemperatureEvent>> pattern) -> {
                    TemperatureEvent firstEvent = pattern.get("first").get(0);
                    TemperatureEvent secondEvent = pattern.get("second").get(0);
                    return "Warning: " + firstEvent.getSensorId() + " has consecutive high temperatures: " +
                            firstEvent.getTemperature() + ", " + secondEvent.getTemperature();
                }
        );

        alerts.print();

        env.execute("Temperature Monitoring");
    }
}
