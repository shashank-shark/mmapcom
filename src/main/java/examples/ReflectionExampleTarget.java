package examples;

public class ReflectionExampleTarget {
    public String mapX400(long startingAddress, long endingAddress) {
        return String.valueOf(startingAddress + endingAddress);
    }
    public String mapX900(long startingAddress, long endingAddress) {
        return String.valueOf(startingAddress + endingAddress);
    }
}
