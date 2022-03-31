package hw6;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.List;

class Dispatcher {
    /* Entertain the idea of multiple dispatchers */
    private String filename;
    private int numCpus;
    private long timeout;

    /* Allow a dispatch that has no timeout */
    public Dispatcher(String filename, int numCpus) {
        this.filename = filename;
        this.numCpus = numCpus;
        this.timeout = Long.MAX_VALUE;
    }
    public Dispatcher(String filename, int numCpus, long timeout) {
        this.filename = filename;
        this.numCpus = numCpus;
        this.timeout = timeout;
    }

    /*
      Reads a list of MD5 hashes in hex from a file
      The file should have each hash separated by newlines
    */
    private List<String> readHashes() {
        try {
            List<String> hashes = Files.readAllLines(Paths.get(this.filename));
            return hashes;
        } catch (IOException e) {
            System.out.println(e.getMessage());
            System.exit(-1);
        }
        // Make compiler happy
        return null;
    }



    /*
      Spawn workers and TODO: handle timeouts, workers may need
      to be stored in a data structure
     */
    void dispatch() {
        ArrayDeque<String> hashes = toArrayDeque(readHashes());

        for (int i = 0; i < this.numCpus; i++) {
            Worker w = new Worker(hashes, this.timeout);
            w.start();
        }
    }

    /*
      Workers use ArrayDeques, so we must convert from
      a List
      The ArrayDeque constructor takes a collection and constructs the
      Deque
    */
    private ArrayDeque<String> toArrayDeque(List<String> hashes) {
        return new ArrayDeque<String>(hashes);
    }


    public static void main(String[] args) {
        Dispatcher d;
        String filename = args[0];
        int numCpus = Integer.parseInt(args[1]);

        if (args.length == 3) {
            long timeout = Long.parseLong(args[2]);
            d = new Dispatcher(filename, numCpus, timeout);
        } else
            d = new Dispatcher(filename, numCpus);

        d.dispatch();
    }
}
