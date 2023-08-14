package org.mericoztiryaki.domain.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class ExecutorManager {

    public static List<ExecutorService> executors = new ArrayList<>();

    public static void assign(ExecutorService executorService) {
        executors.add(executorService);
    }

    public static void shutdown() {
        executors.forEach(pool -> {
            // Disable new tasks from being submitted
            pool.shutdown();
            try {
                // Wait a while for existing tasks to terminate
                if (!pool.awaitTermination(60, TimeUnit.SECONDS)) {
                    // Cancel currently executing tasks forcefully
                    pool.shutdownNow();
                    // Wait a while for tasks to respond to being cancelled
                    if (!pool.awaitTermination(60, TimeUnit.SECONDS))
                        System.err.println("Pool did not terminate");
                }
            } catch (InterruptedException ex) {
                // (Re-)Cancel if current thread also interrupted
                pool.shutdownNow();
                // Preserve interrupt status
                Thread.currentThread().interrupt();
            }
        });
    }
}
