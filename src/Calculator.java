import java.util.Stack;

public class Calculator {

    private enum Operator {
        ADD, SUBTRACT, MULTIPLY, DIVIDE, OPENPAREN, CLOSEPAREN, BLANK
    }

    // 5 + 2 (53 + (2 / 2) + 1)

    // 5 2 53 1
    // + * ( +

    // 5 2
    // +


    // 5 + 2 ((53 + (2+5) 2 / 2) 7)

    // 5 2 60*7
    // + *


    // 5 + (2 + 3)

    // 5 2 3
    // + ( + )

    // ((3 + 2))


    private Stack<Double> numberStack;
    private Stack<Operator> operatorStack;
    private String operation;
    private int parenCount = 0;

    public Calculator(String operation) {
        this.operation = operation.replaceAll("\\s+", "");
        numberStack = new Stack<>();
        operatorStack = new Stack<>();
    }

    public Double compute() {

        Integer preCheckOffset = preOpParse();
        if (preCheckOffset == null) {
            return null;
        }

        for (Integer offset = preCheckOffset; offset < operation.length(); offset++) {
            try {

                NumHelper value = parseNextNum(offset);
                numberStack.push((double) value.num);

                System.out.println(value.num + " PCount: " + parenCount);

                offset += value.stringNumLength;
                if (offset >= operation.length()) {
                    break;
                }

                offset = handleOperatorInput(offset);
                if (offset == null || parenCount < 0) {
                    return null;
                }

//                checkForCloseParen();

            } catch (NumberFormatException e) {
                System.out.println("ERROR " + e.getMessage());
                return null;
            }
        }

//        collapseParen();
//        collapseTop(Operator.BLANK);
//        if (numberStack.size() == 1 && operatorStack.size() == 0) {
//        }

        System.out.println("\n" + numberStack);
        System.out.println(operatorStack);

        return numberStack.pop();
    }


    // Only to be called after parsing in a num and op
    // Returns null if next parse is an operation that is
    // not "(" or ")" meaning it is invalid input
    private Integer checkFutureOp(Operator currentOp, int offset) {
        Operator futureOp = parseNextOp(offset);
        if (futureOp != Operator.BLANK) {

            if (futureOp == Operator.OPENPAREN) {
                operatorStack.push(futureOp);
                parenCount++;

            } else if (futureOp == Operator.CLOSEPAREN) {
                if (prevIsOpenParen() || --parenCount < 0) {
                    return null;
                }

                // MAYBE COLLAPSE INPUT HERE AFTER DECIDING IF * IS NECESSARY

                operatorStack.push(futureOp);   // COLLAPSE TILL FIRST OPENPAREN
            }

            // Next parsed operator creates invalid input
            else if (currentOp != Operator.CLOSEPAREN) {
                return null;
            }

            return parseNextSetOfOps(++offset);
        }

        // MAYBE
        // if offset < length
        //      check if current is CLOSEPAREN
        //      collapse and push MULTIPLY

        // OR
        // CHECK IF CLOSEPAREN AT START OF METHOD
        // HEN PUSH MULTIPLY IF NEEDED AFTER CHECKING NEXT OPS


        return --offset;
    }

    private Integer parseNextSetOfOps(int offset) {
        Operator operator = parseNextOp(offset);
        while (operator != Operator.BLANK) {

            if (prevIsOpenParen() && operator != Operator.OPENPAREN) {
                return null;
            }

            if (operator == Operator.OPENPAREN) {
                parenCount++;
                operatorStack.push(operator);
            }


            operator = parseNextOp(++offset);
        }
        return --offset;
    }


    // TODO: MAKE IT TO RECOGNIZE "(" and ")"
    // MAYBE COULD LOWER "(" and ")" priority
    // need to check how the rest is affected
    private void collapseTop(Operator futureTop) {
        while (operatorStack.size() >= 1 && numberStack.size() >= 2) {
            if (operatorPriority(futureTop) <= operatorPriority(operatorStack.peek())) {
                double second = numberStack.pop();
                double first = numberStack.pop();
                Operator operator = operatorStack.pop();
                double collapsed = applyOp(first, operator, second);
                numberStack.push(collapsed);

            } else {
                break;
            }
        }
    }


    // TODO: MAKE SURE TO HANDLE (53) CORRECTLY
    private void collapseParen() {
        operatorStack.pop();    // Pop ")"
        while (operatorStack.size() >= 1 && numberStack.size() >= 2 && operatorStack.peek() != Operator.OPENPAREN) {
            double second = numberStack.pop();
            double first = numberStack.pop();
            Operator operator = operatorStack.pop();
            double collapsed = applyOp(first, operator, second);
            numberStack.push(collapsed);

            System.out.println(first + " " + operator + " " + second + " = " + collapsed);
        }

        operatorStack.pop();    // Pop "("
    }

    private Integer handleOperatorInput(Integer offset) {
        Operator operator = parseNextOp(offset);
        if (operator == Operator.OPENPAREN || operator == Operator.CLOSEPAREN) {
            if (operator == Operator.OPENPAREN) {
                operatorStack.push(Operator.MULTIPLY);
                operatorStack.push(operator);
                parenCount++;

            } else {
                operatorStack.push(operator);
                parenCount--;
            }

            // CHECK NEXT OPS
            if (++offset < operation.length()) {
                offset = checkFutureOp(operator, offset);

            }

        } else {
//            collapseTop(operator);
            operatorStack.push(operator);
        }

        return offset;
    }

    // Parse at the start for any ops that can make the input invalid
    private Integer preOpParse() {
        int offset = 0;
        Operator operator = parseNextOp(offset);
        while (operator == Operator.OPENPAREN) {
            operatorStack.push(operator);
            parenCount++;
            offset++;

            operator = parseNextOp(offset);
        }

        // Invalid op appeared
        if (operator != Operator.BLANK) {
            return null;
        }

        return offset;
    }

    private boolean prevIsOpenParen() {
        if (!operatorStack.isEmpty()) {
            return operatorStack.peek() == Operator.OPENPAREN;
        }
        return false;
    }

    private boolean prevIsCloseParen() {
        if (!operatorStack.isEmpty()) {
            return operatorStack.peek() == Operator.CLOSEPAREN;
        }
        return false;
    }

    private NumHelper parseNextNum(int offset) {
        StringBuilder sb = new StringBuilder();

//        while (offset < operation.length() && (Character.isDigit(c) || c == '.')) {
        while (offset < operation.length() && Character.isDigit(operation.charAt(offset))) {
            sb.append(operation.charAt(offset));
            offset++;
        }

        return new NumHelper(Integer.parseInt(sb.toString()), sb.length());
    }

    private Operator parseNextOp(int offset) {
        if (offset < operation.length()) {
            switch (operation.charAt(offset)) {
                case '+':
                    return Operator.ADD;
                case '-':
                    return Operator.SUBTRACT;
                case '*':
                    return Operator.MULTIPLY;
                case '/':
                    return Operator.DIVIDE;
                case '(':
                    return Operator.OPENPAREN;
                case ')':
                    return Operator.CLOSEPAREN;
            }
        }

        return Operator.BLANK;
    }

    private double applyOp(double first, Operator operator, double second) {
        if (operator == Operator.ADD) {
            return first + second;
        } else if (operator == Operator.SUBTRACT) {
            return first - second;
        } else if (operator == Operator.MULTIPLY) {
            return first * second;
        } else if (operator == Operator.DIVIDE) {
            return first / second;
        } else return first;
    }

    private int operatorPriority(Operator operator) {
        switch (operator) {
            case ADD:
            case SUBTRACT:
                return 2;

            case MULTIPLY:
            case DIVIDE:
                return 3;

            case OPENPAREN:
            case CLOSEPAREN:
                return 1;

            case BLANK:
            default:
                return 0;
        }
    }

    private class NumHelper {
        private int stringNumLength;
        private int num;

        private NumHelper(int num, int stringNumLength) {
            this.num = num;
            this.stringNumLength = stringNumLength;
        }
    }
}
