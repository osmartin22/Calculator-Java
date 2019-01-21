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


    // USED TO RETURN FROM INVALID INPUT
    // i.e.     ((2+4.....more computations.......)
    // not enough closing paren, therefore previous computations were "wasted" effort
    // Could maybe combine into a single counter
    private int parenCount = 0;

    public Calculator(String operation) {
        this.operation = operation;
        numberStack = new Stack<>();
        operatorStack = new Stack<>();
    }

    public Double compute() {

        Integer preCheckOffset = preCheckInput();
        if(preCheckOffset == null) {
            return null;
        }

        for (int offset = preCheckOffset; offset < operation.length(); offset++) {
System.out.println(offset);
            try {
                int value = parseNextNum(offset);
                numberStack.push((double) value);

                offset += Integer.toString(value).length();
                if (offset >= operation.length()) {
                    break;
                }

                System.out.print(value);

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

                } else {
//                    collapseTop(operator);
                    operatorStack.push(operator);
                }

                System.out.print(" " + operator + " ");
                // CHECK FUTURE OP IF "(" or ")"
                if(++offset < operation.length()) {
                    Integer tempOffset = checkFutureOp(offset);
                    if(tempOffset == null) {
                        return null;
                    }

                    offset = tempOffset;

                }

                System.out.println("FOR LOOP OFFSET " + offset);

            } catch (NumberFormatException e) {
                System.out.println("ERROR " + e.getMessage());
                return null;
            }
        }


//        collapseTop(Operator.BLANK);
//        if (numberStack.size() == 1 && operatorStack.size() == 0) {
//            System.out.println("RESULT = " + numberStack.pop());
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
//        System.out.println(" Future " + futureOp);
        if (futureOp != Operator.BLANK) {
            // CHECKING AHEAD FOR PAREN
            if (futureOp == Operator.OPENPAREN) {
                operatorStack.push(Operator.OPENPAREN);
                offset++;


            } else if(futureOp == Operator.CLOSEPAREN){
                // COLLAPSE TILL FIRST OPENPAREN
                operatorStack.push(futureOp);
                offset++;
            }

            // Next parsed operator creates invalid input
            else {
                return null;
            }

            System.out.println(operatorStack + "\nOffset " + offset);
            return parseNextSetOfOps(offset);
        }

        return --offset;
    }


    // Parses consecutive operators
    // i.e  ((( or  +(
    // TODO: CHECK IF OP IS ")"
    // Maybe if( ')' ) {
    // peek at prev op
    // if not '(', then return invalid result

    // Currently (5+3)+2 is marked as invalid due to )+
    private Integer parseNextSetOfOps(int offset) {
        Operator operator = parseNextOp(offset);
        while (operator != Operator.BLANK) {
//            Operator temp = operatorStack.peek();
//
//            if(temp != Operator.OPENPAREN && temp != Operator.CLOSEPAREN) {
//                return null;
//            }

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
                if(tempOffset == null) {
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
    private Integer preParseOps(int offset) {
        Operator operator = parseNextOp(offset);
        while (operator != Operator.BLANK) {
            if(operator != Operator.OPENPAREN && operator != Operator.CLOSEPAREN) {
                return null;
            } else {
                operatorStack.push(operator);
                operator = parseNextOp(++offset);
                parenCount = (operator == Operator.OPENPAREN) ? ++parenCount : --parenCount;
            }
        }

        // More closed parenthesis than open
        if(parenCount < 0) {
            return null;
        }

        return offset;
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
        System.out.println("APPLYING OP " + first + " " + operator + " " + second);
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
