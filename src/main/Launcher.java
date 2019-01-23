package main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Launcher {

    public static void main(String[] args) {
        System.out.print("Please enter your input. To quit the program type Exit or Quit: ");
        Scanner scanner = new Scanner(System.in);
        String operation = scanner.nextLine();

        List<String> exitWords = new ArrayList<>(Arrays.asList("exit", "quit"));

        Calculator calculator = new Calculator(operation);

        while (!exitWords.contains(operation.toLowerCase())) {
            Double result = calculator.compute();

            if (result == null) {
                System.out.println("Invalid input, try again");
            } else {
                System.out.println("The result is: " + result);
            }

            System.out.print("Input you operation: ");
            operation = scanner.nextLine();
            calculator.setOperation(operation);
        }
    }
}
