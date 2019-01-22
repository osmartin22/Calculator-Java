package main;

public class Launcher {

    public static void main(String[] args) {
        String operation = "(5+2) + 3)";

        System.out.println(operation);
        Calculator calculator = new Calculator(operation);
        Double result = calculator.compute();

        if (result == null) {
            System.out.println("INVALID INPUT");
        } else {
            System.out.println(result);
        }
    }
}
