package com.automapart.autobuilder;

public class AutoBuilder {
    private static AutoBuilder instance = new AutoBuilder();

    private static void setInstance() {
        instance = new AutoBuilder();
    }

    public static AutoBuilder getInstance() {
        if (instance == null) {
            setInstance();

        }
        return instance;
    }

    private AutoBuilder() {

    }

    private boolean enabled = false;
    private boolean paused = false;

    public void onTick() {
        if (!enabled || !paused) {
            return;
        }

        // more stuff here
    }

    public void pause() {
        paused = true;
    }

    public void unpause() {
        paused = false;
    }

    public void enable() {
        enabled = true;
        paused = false;
    }

    public void disable() {
        enabled = false;
        paused = false;
    }
}
