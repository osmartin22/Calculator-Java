package main;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Stack;

public class Calculator {

    private enum Operator {
        ADD, SUBTRACT, MULTIPLY, DIVIDE, OPENPAREN, CLOSEPAREN, BLANK
    }

    private final int ROUND_VALUE = 5;

    private Stack<Double> numberStack;
    private Stack<Operator> operatorStack;
    private String operation;
    private int parenCount = 0;

    public Calculator(String operation) {
        this.operation = operation.replaceAll("\\s+", "");
        numberStack = new Stack<>();
        operatorStack = new Stack<>();
    }

    public void setOperation(String operation) {
        this.operation = operation.replaceAll("\\s+", "");
        numberStack.clear();
        operatorStack.clear();
        parenCount = 0;
    }

    public Double compute() {

        if (operation.isEmpty()) {
            return null;
        }

        Integer preCheckOffset = preOpParse();
        if (preCheckOffset == null) {
            return null;
        }

        for (Integer offset = preCheckOffset; offset < operation.length(); offset++) {
            try {
                NumHelper value = parseNextNum(offset);
                numberStack.push(value.num);

                offset += value.stringNumLength;
                if (offset >= operation.length()) {
                    break;
                }

                offset = handleOperatorInput(offset);
                if (offset == null || parenCount < 0) {
                    return null;
                }

            } catch (NumberFormatException e) {
                return null;
            }
        }

        if (parenCount != 0) {
            return null;
        }

        if (!operatorStack.isEmpty() && operatorStack.peek() == Operator.CLOSEPAREN) {
            if (!collapseParen()) {
                return null;
            }
        }

        if (!collapseTop(Operator.BLANK)) {
            return null;
        }

        return (numberStack.isEmpty() ? null : roundResult(numberStack.pop()));
    }

    private Integer preOpParse() {
        int offset = 0;
        Operator operator = parseNextOp(offset);
        while (operator == Operator.OPENPAREN) {
            operatorStack.push(Operator.OPENPAREN);
            parenCount++;
            offset++;
            operator = parseNextOp(offset);
        }

        // Only "(" is a valid op before any numbers appear
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
                if (--parenCount < 0) {
                    return null;
                }
                if (!collapseParen()) {
                    return null;
                }
            }

        } else {
            if (!collapseTop(operator)) {
                return null;
            }
            operatorStack.push(operator);
        }

        // Check if more ops appear after the one we just read, and handle what to do with it
        if (offset + 1 < operation.length()) {
            offset = parseNextOps(operator, offset + 1);

        }

        return offset;
    }

    private NumHelper parseNextNum(int offset) {
        StringBuilder sb = new StringBuilder();

        while (offset < operation.length() &&
                (Character.isDigit(operation.charAt(offset)) || operation.charAt(offset) == '.')) {
            sb.append(operation.charAt(offset));
            offset++;
        }

        return new NumHelper(Double.parseDouble(sb.toString()), sb.length());
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

            // Next op after "(" can only be "("
            if (prevOp == Operator.OPENPAREN) {
                if (prevOpIsOpenParen(futureOp) == -1) {
                    return null;
                }

            } else if (prevOp == Operator.CLOSEPAREN) {
                if (prevOpIsCloseParen(futureOp) == -1) {
                    return null;
                }


            } else {
                if (futureOp == Operator.OPENPAREN) {
                    parenCount++;
                    operatorStack.push(futureOp);

                    // Prev op was "+-*/" if next is ")" then it is invalid input
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

    private boolean collapseTop(Operator futureTop) {
        while (operatorStack.size() >= 1 && numberStack.size() >= 2) {
            if (operatorPriority(futureTop) <= operatorPriority(operatorStack.peek())) {
                if (!collapseHelper()) {
                    return false;
                }
            } else {
                break;
            }
        }

        return true;
    }

    private boolean collapseParen() {
        operatorStack.pop();    // Pop ")"
        while (operatorStack.size() >= 1 && numberStack.size() >= 2 && operatorStack.peek() != Operator.OPENPAREN) {
            if (!collapseHelper()) {
                return false;
            }
        }
        operatorStack.pop();    // Pop "("

        // Collapse even further if operator at the top is "*" or "/", else wrong result
        // is returned due to incorrect collapsing for some inputs
        if (!operatorStack.isEmpty() && operatorPriority(operatorStack.peek()) == 3) {
            return collapseHelper();
        }

        return true;
    }

    private boolean collapseHelper() {
        double second = numberStack.pop();
        double first = numberStack.pop();
        Operator operator = operatorStack.pop();
        Double collapsed = applyOp(first, operator, second);

        if (collapsed == null) {
            return false;
        }

        numberStack.push(collapsed);
        return true;
    }

    private int prevOpIsOpenParen(Operator futureOp) {
        if (futureOp != Operator.OPENPAREN) {
            return -1;
        }
        operatorStack.push(futureOp);
        parenCount++;
        return 0;
    }

    private int prevOpIsCloseParen(Operator futureOp) {
        if (futureOp == Operator.OPENPAREN) {
            operatorStack.push(Operator.MULTIPLY);
            operatorStack.push(Operator.OPENPAREN);
            parenCount++;

        } else if (futureOp == Operator.CLOSEPAREN) {
            if (--parenCount < 0) {
                return -1;
            }

            operatorStack.push(Operator.CLOSEPAREN);
            if (!collapseParen()) {
                return -1;
            }

        } else {
            operatorStack.push(futureOp);
        }

        return 0;
    }

    private Double applyOp(double first, Operator operator, double second) {
        if (operator == Operator.ADD) {
            return first + second;
        } else if (operator == Operator.SUBTRACT) {
            return first - second;
        } else if (operator == Operator.MULTIPLY) {
            return first * second;
        } else if (operator == Operator.DIVIDE) {
            return (second == 0) ? null : first / second;
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

    // Rounding since Double calculations are not exact
    // 2.2*5.5 != 12.1 using double calculations
    private Double roundResult(Double value) {
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(ROUND_VALUE, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    private class NumHelper {
        private int stringNumLength;
        private double num;

        private NumHelper(double num, int stringNumLength) {
            this.num = num;
            this.stringNumLength = stringNumLength;
        }
    }
}
