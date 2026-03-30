package random_generator;

import java.util.LinkedList;
import java.util.Queue;

public class RandomBuffer {
    private final Queue<Integer> queue = new LinkedList<>();
    private final int LIMIT = 20;

    public synchronized void put(int value) throws InterruptedException {
        while (queue.size() == LIMIT) {
            wait();
        }
        queue.add(value);
        notifyAll();
    }

    public synchronized int get() throws InterruptedException {
        while (queue.isEmpty()) {
            wait();
        }
        int val = queue.poll();
        notifyAll();
        return val;
    }
}