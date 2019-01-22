package main;

public class Launcher {

    public static void main(String[] args) {
        String operation = "5 / 4 * 2";

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

    // ((5+2))) 2 + 8   Invalid input
    // ((5+2)) 2 + 8    22

    // (5+2)+1  8
    // (5+2) + 7 + (9*2)    32

    // 5 + 2 ((2 + 3 (2*2) 3) + 9)  99

    // (1+2*3+4/5*6)    11.8

    // 5 + 2 (53 + (2 / 2) + 1)     115

    // 5 + 2 (((53) + (2+5) 2 / 2) 7)   845

    // ((3 + 2))    5

    // (5 + (2 + 3))    10

    // 5 + 2 ((53 + (2+5)       Invalid Input
    // 5 + 2 ((53 + (2+5)))     125

    // (5)(4)(2)    40

    // (5)/(4)(2) 2.5   WRONG
    // 5 / 4 * 2  2.5
}
