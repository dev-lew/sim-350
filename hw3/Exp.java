package hw3;

class Exp {
    // Generate a random number following an exponential distribution
    static double getExp(double lambda) {
        double y = Math.random();
        return -Math.log(1 - y) / lambda;
    }
}
