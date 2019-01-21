import java.util.Stack;

// TODO: CHANG TO RETURN INTEGER/DOUBLE TO BE ABLE TO ]
// RETURN A NULL VALUE FOR INVALID INPUT
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

        Integer preCheckOffset = preCheckInput();
        if (preCheckOffset == null) {
            return null;
        }

        for (int offset = preCheckOffset; offset < operation.length(); offset++) {
            try {

                // Add a "*" to stack if last operand added was ")"
                if (!operatorStack.isEmpty() && !numberStack.isEmpty()) {
                    if (operatorStack.peek() == Operator.CLOSEPAREN) {
                        operatorStack.push(Operator.MULTIPLY);
                    }
                }

                int value = parseNextNum(offset);
                numberStack.push((double) value);

                offset += Integer.toString(value).length();
                if (offset >= operation.length()) {
                    break;
                }

                Operator operator = parseNextOp(offset);
                if (operator == Operator.OPENPAREN) {
                    operatorStack.push(Operator.MULTIPLY);
                    operatorStack.push(operator);

                } else if (operator == Operator.CLOSEPAREN) {
                    // COLLAPSE TILL FIRST OPENPAREN
                    // MAYBE PARSE NEXT OP FOR CASES LIKE 53 + (2/2) + 2
                    // BECOMES 53 + 1 + 2 AFTER COLLAPSE
                    // BUT NEXT TO BE READ IS "+"
                    operatorStack.push(operator);
                    //TODO: CHECK NEXT VALUE TO DECIDE IF ADDING * IS NEEDED
                    // OR COULD PEEK STACK BEFORE PARSING NUM AND PUSH * IF TOP IS ")"

                } else {
//                    collapseTop(operator);
                    operatorStack.push(operator);
                }

                // CHECK FUTURE OP IF "(" or ")"
                if (++offset < operation.length()) {
                    Integer tempOffset = checkFutureOp(offset);
                    if (tempOffset == null) {
                        return null;
                    }

                    offset = tempOffset;

                }

            } catch (NumberFormatException e) {
                System.out.println("ERROR " + e.getMessage());
                return null;
            }
        }


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
    private Integer checkFutureOp(int offset) {
        Operator futureOp = parseNextOp(offset);
        if (futureOp != Operator.BLANK) {
            // CHECKING AHEAD FOR PAREN
            if (futureOp == Operator.OPENPAREN) {
                operatorStack.push(Operator.OPENPAREN);
                offset++;


            } else if (futureOp == Operator.CLOSEPAREN) {
                if (prevIsOpenParen()) {
                    return null;
                }

                // COLLAPSE TILL FIRST OPENPAREN
                operatorStack.push(futureOp);
                offset++;
            }

            // Next parsed operator creates invalid input
            else {
                return null;
            }

            return parseNextSetOfOps(offset);
        }

        return --offset;
    }


    private Integer parseNextSetOfOps(int offset) {
        Operator operator = parseNextOp(offset);
        while (operator != Operator.BLANK) {

            if (prevIsOpenParen() && operator == Operator.CLOSEPAREN) {
                return null;
            }

            operatorStack.push(operator);
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

    private void collapseParen() {
        while (operatorStack.size() >= 1 && numberStack.size() >= 2 && operatorStack.peek() != Operator.OPENPAREN) {

        }

        operatorStack.pop();
    }


    private Integer preCheckInput() {
        int offset = 0;
        Operator firstOp = parseNextOp(offset);
        if (firstOp != Operator.BLANK) {
            if (firstOp == Operator.OPENPAREN) {
                operatorStack.push(Operator.OPENPAREN);
                parenCount++;

                Integer tempOffset = preParseOps(++offset);
                if (tempOffset == null) {
                    return null;
                }
                offset = tempOffset;

            } else {
                return null;
            }
        }

        return offset;
    }

    // If any operands besides "(" or ")" show up, input is invalid
    // If "()" shows up, mark as invalid input
    private Integer preParseOps(int offset) {
        Operator operator = parseNextOp(offset);
        while (operator != Operator.BLANK) {

            if (prevIsOpenParen() && operator == Operator.CLOSEPAREN ||
                    operator != Operator.OPENPAREN && operator != Operator.CLOSEPAREN) {
                return null;
            }

            operatorStack.push(operator);
            operator = parseNextOp(++offset);
            parenCount = (operator == Operator.OPENPAREN) ? ++parenCount : --parenCount;

        }

        // More closed parenthesis than open
        if (parenCount < 0) {
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

    private int parseNextNum(int offset) {
        StringBuilder sb = new StringBuilder();

//        while (offset < operation.length() && (Character.isDigit(c) || c == '.')) {
        while (offset < operation.length() && Character.isDigit(operation.charAt(offset))) {
            sb.append(operation.charAt(offset));
            offset++;
        }

        return Integer.parseInt(sb.toString());
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
                return 1;

            case MULTIPLY:
            case DIVIDE:
                return 2;

            case OPENPAREN:
            case CLOSEPAREN:
                return 3;

            case BLANK:
            default:
                return 0;
        }
    }
}
