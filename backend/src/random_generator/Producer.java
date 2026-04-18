package random_generator;

import java.security.SecureRandom;

public class Producer implements Runnable {
    private final RandomBuffer buffer;
    private final String hashKey;
    private final SecureRandom secureRandom;

    public Producer(RandomBuffer buffer, String hashKey) {
        this.buffer = buffer;
        this.hashKey = hashKey;
        // Δημιουργία Secured Random με Seed το hashKey του παιχνιδιού
        this.secureRandom = new SecureRandom();
        this.secureRandom.setSeed(hashKey.getBytes());
    }

    @Override
    public void run() {
        try {
            while (true) {
                int randomNumber = secureRandom.nextInt(100); 
                buffer.put(randomNumber);
                // Μικρή καθυστέρηση 
                Thread.sleep(100); 
            }
        } catch (InterruptedException e) {
            System.out.println("Producer for " + hashKey + " interrupted.");
        }
    }
}