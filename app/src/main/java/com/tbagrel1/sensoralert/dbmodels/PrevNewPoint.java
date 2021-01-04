package com.tbagrel1.sensoralert.dbmodels;

import androidx.annotation.NonNull;

/**
 * Helper class used to hold two succesive light data points and detect a state evolution.
 */
public class PrevNewPoint {
    public static final int SAME_ON = 0b11;
    public static final int SAME_OFF = 0b00;
    public static final int SWITCHED_ON = 0b01;
    public static final int SWITCHED_OFF = 0b10;

    private final double lightSwitchThreshold;
    private final LightDataPoint prevPoint;
    private LightDataPoint newPoint;

    public PrevNewPoint(double lightSwitchThreshold, LightDataPoint prevPoint) {
        this.lightSwitchThreshold = lightSwitchThreshold;
        this.prevPoint = prevPoint;
        this.newPoint = null;
    }

    public PrevNewPoint(
        double lightSwitchThreshold, LightDataPoint prevPoint, LightDataPoint newPoint
    ) {
        this.lightSwitchThreshold = lightSwitchThreshold;
        this.prevPoint = prevPoint;
        this.newPoint = newPoint;
    }

    public boolean isNewPointSet() {
        return this.newPoint != null;
    }

    /**
     * Returns the evolution of state detected between the two light data points. The result is one
     * of the following constants: SAME_ON, SAME_OFF, SWITCHED_ON or SWITCHED_OFF.
     *
     * @return one of the following constants: SAME_ON, SAME_OFF, SWITCHED_ON or SWITCHED_OFF
     */
    public int getEvolution() {
        int prevBit = this.prevPoint.value <= lightSwitchThreshold ? 0 : 1;
        int newBit = this.newPoint.value <= lightSwitchThreshold ? 0 : 1;
        return prevBit << 1 | newBit;
    }

    public LightDataPoint getPrevPoint() {
        return prevPoint;
    }

    public LightDataPoint getNewPoint() {
        return newPoint;
    }

    public void setNewPoint(LightDataPoint newPoint) {
        this.newPoint = newPoint;
    }

    @NonNull
    @Override
    public String toString() {
        return String.format(
            "PrevNewPoint { prevPoint = %s, newPoint = %s, lightSwitchThreshold = %.2f }",
            prevPoint.toString(),
            newPoint.toString(),
            lightSwitchThreshold
        );
    }

    public double getLightSwitchThreshold() {
        return lightSwitchThreshold;
    }
}
