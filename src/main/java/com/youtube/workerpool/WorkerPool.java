package com.youtube.workerpool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

public class WorkerPool {
    private static final Logger logger = LoggerFactory.getLogger(WorkerPool.class);

    private static final int corePoolSize = 50;
    private static final int maximumPoolSize = 100;
    private static final long keepAliveTimeInMinutes = 160;
    private static ThreadPoolExecutor executorPool;
    private static MyMonitorThread monitor;
    private volatile static WorkerPool workerPool = null;

    private WorkerPool() {

    }

    public static void getInstance() {
        if (workerPool == null) {
            synchronized (WorkerPool.class) {
                if (workerPool == null) {
                    workerPool = new WorkerPool();
                    createWorkerPool();
                }
            }
        }
    }

    private static void createWorkerPool() {
        logger.debug("creating worked pool");
        //Get the ThreadFactory implementation to use
        ThreadFactory threadFactory = Executors.defaultThreadFactory();
        //creating the ThreadPoolExecutor
         executorPool = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTimeInMinutes, TimeUnit.MINUTES, new ArrayBlockingQueue<Runnable>(20), threadFactory,new ThreadPoolExecutor.CallerRunsPolicy());
        //start the monitoring thread
        monitor = new MyMonitorThread(executorPool);
        Thread monitorThread = new Thread(monitor);
        monitorThread.start();
    }

    public static void shutdown() throws InterruptedException {
        logger.debug("Shutting down the WorkerPool");
        //shut down the pool
        executorPool.shutdown();
        executorPool.awaitTermination(1, TimeUnit.DAYS);
        //shut down the monitor thread
        monitor.shutdown();

    }

    public static void deployJob(AbstractJob job) {
        logger.debug("Deployed Job in the Pool:", job);
        executorPool.submit(job);

    }
    public static void removeJob(AbstractJob job){
        logger.debug("Removing job {}",job.toString());
        executorPool.remove(job);
    }
}