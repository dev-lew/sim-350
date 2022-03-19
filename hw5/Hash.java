package hw5;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

class Hash {
    static String hash(int intToHash) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytesToHash = Integer.toString(intToHash).getBytes();
            byte[] md5digest = md.digest(bytesToHash);

            // The resulting integer should be interpreted as positive
            BigInteger intDigest = new BigInteger(1, md5digest);
            return intDigest.toString(16);
        } catch(NoSuchAlgorithmException e) {
            // Most platforms implement MD5, should not get here
            System.err.println(e.getMessage());
            System.exit(-1);
        }

        // Make compiler happy
        return "";
    }
}
