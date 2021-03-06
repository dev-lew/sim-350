package hw5;

class UnHash {
    /*
      Attempt to brute force the given MD5 by
      generating hashes from positive integers
      until there is a match
     */
    static int unhash(String toUnhash) {
        String h;
        for (int i = 0; ; i++) {
            h = Hash.hash(i);
            if (h.equals(toUnhash)) {
                return i;
            }
        }
    }

    public static void main(String args[]) {
        System.out.println(unhash(args[0]));
    }
}
