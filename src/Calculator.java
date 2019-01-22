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

//                System.out.println(value.num + " PCount: " + parenCount);

                offset += value.stringNumLength;
                if (offset >= operation.length()) {
                    break;
                }

                offset = handleOperatorInput(offset);
                if (offset == null || parenCount < 0) {
                    return null;
                }

//                if(offset >= operation.length()) {
//                    break;
//                }

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

    private Integer parseNextOps(Operator prevOp, int offset) {
        Operator futureOp = parseNextOp(offset);
        while (futureOp != Operator.BLANK) {

            // Invalid input, next op after "(" can only be "("
            if (prevOp == Operator.OPENPAREN) {
                if (prevOpIsOpenParen(futureOp) == -1) {
                    return null;
                }

            } else if (prevOp == Operator.CLOSEPAREN) {
                preOpIsCloseParen(futureOp);
            }

            // Prev op was "+-*/" if next is ")" then invalid
            else {
                if (futureOp == Operator.OPENPAREN) {
                    parenCount++;
                    operatorStack.push(futureOp);
                } else {
                    return null;
                }
            }

//            else if(futureOp != Operator.OPENPAREN && futureOp != Operator.CLOSEPAREN){
//                return null;
//            }

            prevOp = futureOp;
            futureOp = parseNextOp(++offset);
        }

        if (prevOp == Operator.CLOSEPAREN) {
            operatorStack.push(Operator.MULTIPLY);
        }

        return --offset;
    }

    private int prevOpIsOpenParen(Operator futureOp) {
        if (futureOp != Operator.OPENPAREN) {
            return -1;
        }
        operatorStack.push(futureOp);
        parenCount++;
        return 0;
    }

    private void preOpIsCloseParen(Operator futureOp) {
        if (futureOp == Operator.OPENPAREN) {
            operatorStack.push(Operator.MULTIPLY);
            parenCount++;
        } else if (futureOp == Operator.CLOSEPAREN) {
            parenCount--;
        }
        operatorStack.push(futureOp);
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

        } else {
//            collapseTop(operator);
            operatorStack.push(operator);
        }

        if (offset + 1 < operation.length()) {
            offset = parseNextOps(operator, offset + 1);

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
