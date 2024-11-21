package com.example.detectoma;

public class TemperatureReading {
    private double temperature;
    private long timestamp;

    public TemperatureReading(double temperature, long timestamp) {
        this.temperature = temperature;
        this.timestamp = timestamp;
    }

    public double getTemperature() {
        return temperature;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
