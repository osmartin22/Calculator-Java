package main;

public class Launcher {

    public static void main(String[] args) {
        String operation = "(5+5)/ (3 +2) / (4)(2)";

        System.out.println(operation);
        Calculator calculator = new Calculator(operation);
        Double result = calculator.compute();

        if (result == null) {
            System.out.println("INVALID INPUT");
        } else {
            System.out.println(result);
        }

//        System.out.println(Double.parseDouble("2.00000"));
    }

    // (5+5) - (3 +2) / (4)(2)

    // 10 / 5 / 4 * 2

    // 10 5 8
    // /  /



    // (5)/(4)(2) 2.5   WRONG
    // 5 / 4 * 2  2.5
}
