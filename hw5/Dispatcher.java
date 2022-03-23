package hw5;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

class Dispatcher {
    /*
      Reads a list of MD5 hashes in hex from a file
      The file should have each hash separated by newlines
    */
    private static List<String> readHashes(String filename) {
        try {
            List<String> hashes = Files.readAllLines(Paths.get(filename));
            return hashes;
        } catch (IOException e) {
            System.out.println(e.getMessage());
            System.exit(-1);
        }
        // Make compiler happy
        return null;
    }

    void crack(String filename) {
        List<String> hashes = readHashes(filename);

        for (String hash : hashes) {
            System.out.println(UnHash.unhash(hash));
        }
    }

    public static void main(String[] args) {
        Dispatcher d = new Dispatcher();
        d.crack(args[0]);
    }
}
