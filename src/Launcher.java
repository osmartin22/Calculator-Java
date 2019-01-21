
public class Launcher {

    public static void main(String[] args) {
        String operation = "5 + 2 (((53) + (2+5) 2 / 2) 7)";

        System.out.println(operation);
        Calculator calculator = new Calculator(operation);
        Double result = calculator.compute();

        if(result == null) {
            System.out.println("INVALID INPUT");
        } else {
            System.out.println(result);
        }


//        System.out.println(Double.parseDouble("2.00000"));
    }

    // ((5+2))) 2 + 8


    // 5 + 2 ((2 + 3 (2*2) 3) + 9)

    // Stack    5 +
    // prevOp   BLANK
    // Count



    // (1+2*3+4/5*6)



    // 5 + 2 (53 + (2 / 2) + 1)

    // 5 2 53
    // + * ( + check next op



    // 5 + 2 (((53) + (2+5) 2 / 2) 7)



    // 5 + (2 + 3)

    // 5 5
    // + ( )

    // ((3 + 2))


    // 5 + 2 ((53 + (2+5) 2 / 2) 7)

    // 5 + 2 ((53 + (2+5)

}
