package worker;

import java.util.LinkedList;
import java.util.Queue;

public class RandomBuffer {

    private final Queue<Integer> queue = new LinkedList<>();
    private final int limit;

    public RandomBuffer(int limit) {
        this.limit = limit;
    }

    public synchronized void put(int value) throws InterruptedException {
        while (queue.size() == limit){
            wait();
        }
        queue.add(value);
        notifyAll();
    }

    public synchronized int take() throws InterruptedException {
        while (queue.isEmpty()) {
            wait();
        }
        int value = queue.poll();
        notifyAll();
        return value;
    }
}
