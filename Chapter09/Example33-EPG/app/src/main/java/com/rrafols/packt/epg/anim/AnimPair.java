package com.rrafols.packt.epg.anim;

public class AnimPair {
    private static final float THRESHOLD = 0.01f;
    private static final float DEFAULT_FACTOR = 0.25f;
    private float current;
    private float target;
    private float factor;

    public AnimPair(float current, float target) {
        this(current, target, DEFAULT_FACTOR);
    }

    public AnimPair(float current, float target, float factor) {
        this.current = current;
        this.target = target;
        this.factor = factor;
    }

    public float getCurrent() {
        return current;
    }

    public float getTarget() {
        return target;
    }

    protected void setTarget(float target) {
        this.target = target;
    }

    protected void setCurrent(float current) {
        this.current = current;
    }

    protected void logicTick() {
        current += (target - current) / factor;
    }

    boolean isFinished() {
        return (Math.abs(target - current) < THRESHOLD);
    }
}
