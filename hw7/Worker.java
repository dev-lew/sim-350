package hw7;

import java.util.ArrayDeque;
import java.time.Duration;
import java.time.Instant;

class Worker implements Runnable {
    /*
      The list of uncracked hashes from the file
      Each Worker carries a reference to it, so it is shared
    */
    ArrayDeque<String> hashList;

    long timeout;
    Instant startTime;

    private String assignedHash;

    /* For testing */
    static int numWorkers = 0;
    private int id;

    public Worker(ArrayDeque<String> hashList, long timeout) {
        this.hashList = hashList;
        this.timeout = timeout;
        this.id = numWorkers++;
    }

    private long getTimeElapsedMillis() {
        return Duration
            .between(this.startTime, Instant.now())
            .toMillis();
    }

    boolean assignHash() {
        /* If the hashList is empty, this worker is done */
        synchronized (hashList) {
            this.assignedHash = hashList.poll();
            return this.assignedHash != null;
        }
    }

    /*
      Attempts to crack a hash
      Will timeout if elapsed time is greater than specified
      timeout duration
     */
    int unhash(String toUnhash) {
        String h;
        this.startTime = Instant.now();

        for (int i = 0; ; i++) {
            h = Hash.hash(i);

            if (h.equals(toUnhash))
                return i;

            if (getTimeElapsedMillis() > timeout)
                return -1;
        }
    }

    /*
      This method is executed concurrently with all other Workers
    */
    public void run() {
        while (true) {
            if (!assignHash())
                break;

            /* Attempt to crack */
            int result = unhash(this.assignedHash);

            /* Print uncrackable hash and try to assign a new one */
            if (result == -1) {
                System.out.println(this.assignedHash);
                continue;
            } else {
                System.out.println(result);
                System.out.println("Cracked by " + this.id);
            }
        }
    }
}
