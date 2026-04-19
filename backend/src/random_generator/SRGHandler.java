package random_generator;

import java.util.HashMap;
import java.util.Map;

public class SRGHandler {
    // Map που συνδέει το Secret Key του JSON με τον αντίστοιχο Buffer
    private static final Map<String, RandomBuffer> gameBuffers = new HashMap<>();

    public static synchronized int getNextNumber(String hashKey) throws InterruptedException {

        if (!gameBuffers.containsKey(hashKey)) {
            RandomBuffer newBuffer = new RandomBuffer();
            gameBuffers.put(hashKey, newBuffer);
            
            // Ξεκινάμε έναν νέο Producer ειδικά για αυτό το παιχνίδι
            Thread producerThread = new Thread(new Producer(newBuffer, hashKey));
            producerThread.setDaemon(true); // Κλείνει αυτόματα όταν κλείσει ο Server
            producerThread.start();
            
            System.out.println("[SRG] Started dedicated secured producer for key: " + hashKey);
        }
        
        return gameBuffers.get(hashKey).take();
    }
}