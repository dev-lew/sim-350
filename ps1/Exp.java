class Exp {
    // Generate a random number following an exponential distribution
    static double getExp(double lambda) {
        double y = Math.random();
        return -Math.log(1 - y) / lambda;
    }

    // Parameters: lambda and N (number of samples)
    public static void main(String args[]) {
        double lambda = Double.parseDouble(args[0]);
        int n = Integer.parseInt(args[1]);

        for (int i = 0; i < n; i++) {
            System.out.println(getExp(lambda));
        }
    }
}
