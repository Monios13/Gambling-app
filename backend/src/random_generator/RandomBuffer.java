package random_generator;

import java.util.LinkedList;
import java.util.Queue;

public class RandomBuffer {
    private final Queue<Integer> buffer = new LinkedList<>();
    private final int MAX_SIZE = 10; // Μέγεθος buffer ανά παιχνίδι

    public synchronized void put(int number) throws InterruptedException {
        while (buffer.size() == MAX_SIZE) {
            wait(); // Περιμένει αν ο buffer είναι γεμάτος
        }
        buffer.add(number);
        notifyAll(); // Ειδοποιεί τον Consumer ότι υπάρχει αριθμός
    }

    
    public synchronized int take() throws InterruptedException {
        while (buffer.isEmpty()) {
            wait(); 
        }
        int number = buffer.poll();
        notifyAll(); 
        return number;
    }
}