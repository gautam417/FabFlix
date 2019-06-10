package edu.uci.ics.gmehta1.service.api_gateway.threadpool;

import edu.uci.ics.gmehta1.service.api_gateway.logger.ServiceLogger;

public class ThreadPool {
    private int numWorkers;
    private Worker[] workers;
    private ClientRequestQueue queue;

    public ThreadPool(int numWorkers) {
        this.numWorkers=numWorkers;
        this.queue = new ClientRequestQueue();
        this.workers = new Worker[this.numWorkers];

        for (int i = 0; i < numWorkers; i++){
            ServiceLogger.LOGGER.config("Creating worker: "+ i);
            workers[i] = Worker.CreateWorker(i, this);
            ServiceLogger.LOGGER.config("Starting worker: "+ i);
            workers[i].start();
        }
    }

    public void add(ClientRequest clientRequest) {
        queue.enqueue(clientRequest);
    }

    public ClientRequest remove() throws InterruptedException { return queue.dequeue(); }

    public ClientRequestQueue getQueue() {
        return queue;
    }

}
