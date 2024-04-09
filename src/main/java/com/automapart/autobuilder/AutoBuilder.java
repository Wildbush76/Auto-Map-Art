package com.automapart.autobuilder;

public class AutoBuilder {
    private static final AutoBuilder INSTANCE = new AutoBuilder();

    public AutoBuilder getInstance() {
        return INSTANCE;
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
