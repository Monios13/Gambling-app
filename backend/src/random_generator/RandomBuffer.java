package random_generator;

import java.util.LinkedList;
import java.util.Queue;

public class RandomBuffer {
    private final Queue<Integer> buffer = new LinkedList<>();
    private final int MAX_SIZE = 10; // Μέγεθος buffer ανά παιχνίδι

    // Ο Producer καλεί αυτή τη μέθοδο για να προσθέσει τυχαίο αριθμό
    public synchronized void put(int number) throws InterruptedException {
        while (buffer.size() == MAX_SIZE) {
            wait(); // Περιμένει αν ο buffer είναι γεμάτος
        }
        buffer.add(number);
        notifyAll(); // Ειδοποιεί τον Consumer ότι υπάρχει αριθμός
    }

    // Ο Handler καλεί αυτή τη μέθοδο για να πάρει αριθμό και να τον στείλει στον Worker
    public synchronized int take() throws InterruptedException {
        while (buffer.isEmpty()) {
            wait(); // Περιμένει αν ο buffer είναι άδειος
        }
        int number = buffer.poll();
        notifyAll(); // Ειδοποιεί τον Producer να παράγει κι άλλους
        return number;
    }
}