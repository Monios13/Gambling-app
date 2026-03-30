package random_generator;

import java.util.Random;

public class Producer extends Thread {
    private RandomBuffer buffer;

    public Producer(RandomBuffer buffer) {
        this.buffer = buffer;
    }

    @Override
    public void run() {
        Random r = new Random();
        try {
            while (true) {
                buffer.put(r.nextInt(1000000));
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}