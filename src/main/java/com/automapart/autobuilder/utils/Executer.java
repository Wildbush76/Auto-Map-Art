package com.automapart.autobuilder.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class Executer {
    public static final AtomicInteger count = new AtomicInteger(1);

    public static final ExecutorService executor = Executors.newCachedThreadPool(task -> {
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.setName("AutoMapArt " + count.getAndIncrement());
        return thread;
    });

    public static void execute(Runnable task) {
        executor.execute(task);
    }

    private Executer() {
    }

}
