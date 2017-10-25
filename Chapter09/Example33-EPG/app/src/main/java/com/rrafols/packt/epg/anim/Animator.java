package com.rrafols.packt.epg.anim;

import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

public class Animator {
    private HashMap<String, AnimPair> values;

    public Animator() {
        values = new HashMap<>();
    }

    public boolean hasPendingAnimations() {
        Collection<AnimPair> animValues = values.values();
        for (AnimPair ap : animValues) {
            if (!ap.isFinished()) return false;
        }

        return true;
    }

    public void update(String key, float dt) {

        if (values.containsKey(key)) {
            AnimPair ap = values.get(key);
            ap.setTarget(ap.getTarget() + dt);
        } else {
            // add a new animation key, assuming current value is 0
            put(key, 0, dt);
        }
    }

    public void put(String key, float current, float target) {
        AnimPair ap;

        if (values.containsKey(key)) {
            ap = values.get(key);
        } else {
            ap = new AnimPair(current, target);
        }

        values.put(key, ap);
    }

    public float get(String key) {
        if (values.containsKey(key)) {
            return values.get(key).getCurrent();
        } else {
            return 0.f; // should throw an exception
        }
    }

    public HashMap<String, AnimPair> getFullState() {
        return values;
    }

    public void logicTick() {
        Collection<AnimPair> animValues = values.values();
        for (AnimPair ap : animValues) ap.logicTick();
    }
}