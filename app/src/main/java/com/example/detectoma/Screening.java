package com.example.detectoma;

public class Screening {
    private String timestamp;
    private boolean asymmetry;
    private boolean border;
    private boolean color;
    private boolean diameter;
    private boolean evolving;
    private String temperatureDiff;
    private String distanceSurface;
    private String distanceArm;

    public Screening() {
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isAsymmetry() {
        return asymmetry;
    }

    public void setAsymmetry(boolean asymmetry) {
        this.asymmetry = asymmetry;
    }

    public boolean isBorder() {
        return border;
    }

    public void setBorder(boolean border) {
        this.border = border;
    }

    public boolean isColor() {
        return color;
    }

    public void setColor(boolean color) {
        this.color = color;
    }

    public boolean isDiameter() {
        return diameter;
    }

    public void setDiameter(boolean diameter) {
        this.diameter = diameter;
    }

    public boolean isEvolving() {
        return evolving;
    }

    public void setEvolving(boolean evolving) {
        this.evolving = evolving;
    }

    public String getTemperatureDiff() {
        return temperatureDiff;
    }

    public void setTemperatureDiff(String temperatureDiff) {
        this.temperatureDiff = temperatureDiff;
    }

    public String getDistanceSurface() {
        return distanceSurface;
    }

    public void setDistanceSurface(String distanceSurface) {
        this.distanceSurface = distanceSurface;
    }

    public String getDistanceArm() {
        return distanceArm;
    }

    public void setDistanceArm(String distanceArm) {
        this.distanceArm = distanceArm;
    }
}

