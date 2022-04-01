package hw7;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;

class Worker implements Runnable {
    /* The list of uncracked hashes from the file */
    LinkedBlockingDeque<String> hashList;

    /*
      hardHashes holds the hashes that require a hint
      hints holds the integers that were used to crack easy hashes
      Both of these data structures are shared between threads
    */
    private LinkedBlockingDeque<String> hardHashes;
    private List<Integer> hints;

    long timeout;
    Instant startTime;

    private String assignedHash;

    /* For testing */
    static int numWorkers = 0;
    private int id;

    public Worker(LinkedBlockingDeque<String> hashList, long timeout,
                  LinkedBlockingDeque<String> hardHashes, List<Integer> hints) {
        this.hashList = hashList;
        this.timeout = timeout;
        this.hardHashes = hardHashes;
        this.hints = hints;
        this.id = numWorkers++;
    }

    private long getTimeElapsedMillis() {
        return Duration
            .between(this.startTime, Instant.now())
            .toMillis();
    }

    boolean assignHash() {
        /* If the hashList is empty, this worker is done */
        this.assignedHash = hashList.poll();
        return this.assignedHash != null;
    }

    /*
      Attempts to crack a hash and populates hints or hardHashes
      Will timeout if elapsed time is greater than specified
      timeout duration
     */
    int unhash(String toUnhash) {
        String h;
        this.startTime = Instant.now();

        for (int i = 0; ; i++) {
            h = Hash.hash(i);

            if (h.equals(toUnhash)) {
                int hint = Integer.parseInt(h);
                hints.add(hint);
                return i;
            }

            if (getTimeElapsedMillis() > timeout) {
                hardHashes.add(this.assignedHash);
                return -1;
            }
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

            /* try to assign a new one */
            if (result == -1)
                continue;
            else {
                System.out.println(result);
                // System.out.println("Cracked by " + this.id);
            }
        }
    }
}
