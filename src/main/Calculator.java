package main;

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

                offset += value.stringNumLength;
                if (offset >= operation.length()) {
                    break;
                }

                offset = handleOperatorInput(offset);
                if (offset == null || parenCount < 0) {
                    return null;
                }

            } catch (NumberFormatException e) {
                System.out.println("ERROR " + e.getMessage());
                return null;
            }
        }

        if (parenCount != 0) {
            return null;
        }

        if (!operatorStack.isEmpty() && operatorStack.peek() == Operator.CLOSEPAREN) {
            collapseParen();
        }

        collapseTop(Operator.BLANK);

        System.out.println("\n" + numberStack);
        System.out.println(operatorStack);

        return numberStack.pop();
    }

    // Parse at the start for any ops that can make the input invalid
    private Integer preOpParse() {
        int offset = 0;
        Operator operator = parseNextOp(offset);
        while (operator == Operator.OPENPAREN) {
            operatorStack.push(Operator.OPENPAREN);
            parenCount++;
            offset++;
            operator = parseNextOp(offset);
        }

        return (operator != Operator.BLANK) ? null : offset;
    }

    private Integer handleOperatorInput(Integer offset) {
        Operator operator = parseNextOp(offset);
        if (operator == Operator.OPENPAREN || operator == Operator.CLOSEPAREN) {
            if (operator == Operator.OPENPAREN) {
                operatorStack.push(Operator.MULTIPLY);
                operatorStack.push(Operator.OPENPAREN);
                parenCount++;

            } else {
                operatorStack.push(Operator.CLOSEPAREN);
                parenCount--;
                collapseParen();
            }

        } else {
            collapseTop(operator);
            operatorStack.push(operator);
        }

        if (offset + 1 < operation.length()) {
            offset = parseNextOps(operator, offset + 1);

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

    private Integer parseNextOps(Operator prevOp, int offset) {
        Operator futureOp = parseNextOp(offset);
        while (futureOp != Operator.BLANK) {

            // Invalid input, next op after "(" can only be "("
            if (prevOp == Operator.OPENPAREN) {
                if (prevOpIsOpenParen(futureOp) == -1) {
                    return null;
                }

            } else if (prevOp == Operator.CLOSEPAREN) {
                if (prevOpIsCloseParen(futureOp) == -1) {
                    return null;
                }

                // Prev op was "+-*/" if next is ")" then invalid
            } else {
                if (futureOp == Operator.OPENPAREN) {
                    parenCount++;
                    operatorStack.push(futureOp);
                } else {
                    return null;
                }
            }

            prevOp = futureOp;
            futureOp = parseNextOp(++offset);
        }

        if (prevOp == Operator.CLOSEPAREN && offset < operation.length()) {
            operatorStack.push(Operator.MULTIPLY);
        }

        return --offset;
    }

    private void collapseTop(Operator futureTop) {
        while (operatorStack.size() >= 1 && numberStack.size() >= 2) {
            if (operatorPriority(futureTop) <= operatorPriority(operatorStack.peek())) {
                collapseHelper();
            } else {
                break;
            }
        }
    }

    private void collapseParen() {
        operatorStack.pop();    // Pop ")"
        while (operatorStack.size() >= 1 && numberStack.size() >= 2 && operatorStack.peek() != Operator.OPENPAREN) {
            collapseHelper();
        }
        operatorStack.pop();    // Pop "("
    }

    private void collapseHelper() {
        double second = numberStack.pop();
        double first = numberStack.pop();
        Operator operator = operatorStack.pop();
        double collapsed = applyOp(first, operator, second);
        numberStack.push(collapsed);

        System.out.println(first + " " + operator + " " + second + " = " + collapsed);
    }

    private int prevOpIsOpenParen(Operator futureOp) {
        if (futureOp != Operator.OPENPAREN) {
            return -1;
        }
        operatorStack.push(futureOp);
        parenCount++;
        return 0;
    }

    // TODO: MAYBE FIX PROBLEM HERE
    private int prevOpIsCloseParen(Operator futureOp) {
//        collapseTop(futureOp);
        if (futureOp == Operator.OPENPAREN) {
            operatorStack.push(Operator.MULTIPLY);
            operatorStack.push(Operator.OPENPAREN);
            parenCount++;

        } else if (futureOp == Operator.CLOSEPAREN) {
            if (--parenCount < 0) {
                return -1;
            }
            operatorStack.push(Operator.CLOSEPAREN);
            collapseParen();
        } else {
            operatorStack.push(futureOp);
        }

        return 0;
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