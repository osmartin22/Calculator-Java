package main;

import javafx.util.Pair;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class CalculatorTest {

    @Test
    public void moreCloseThanOpenParenTest() {
        List<String> operationList = new ArrayList<>();
        operationList.add("(((5+2))))");
        operationList.add("(5+2) + 3)");
        operationList.add("5+2(3)+3)-4");
        operationList.add("((3)))");
        operationList.add("3 + (2))");

        assertNullHelper(operationList);
    }

    @Test
    public void pemdasTestNoParen() {
        List<Pair<Double, String>> pairList = new ArrayList<>();
        pairList.add(new Pair<>(7.0, "1 + 2 + 3 - 4 + 5"));
        pairList.add(new Pair<>(7.5, "1 * 2 * 3 / 4 * 5"));
        pairList.add(new Pair<>(13.0, "1 + 2 * 3 * 5 - 9 * 2"));
        pairList.add(new Pair<>(16.0, "1 + 2 / 2 * 4 *3 + 3"));
        pairList.add(new Pair<>(2.5, "5 / 4 * 2"));

        assertEqualsHelper(pairList);
    }

    @Test
    public void parenMultiplicationTest() {
        List<Pair<Double, String>> pairList = new ArrayList<>();
        pairList.add(new Pair<>(12.0, "(3)(4)"));
        pairList.add(new Pair<>(12.0, "3(4)"));
        pairList.add(new Pair<>(12.0, "(3)4"));

        pairList.add(new Pair<>(24.0, "(3)(4)(2)"));
        pairList.add(new Pair<>(24.0, "(3)4(2)"));
        pairList.add(new Pair<>(24.0, "3(4)(2)"));
        pairList.add(new Pair<>(24.0, "(3)(4)2"));

        pairList.add(new Pair<>(720.0, "(3)(4)2(5)(6)"));

        assertEqualsHelper(pairList);
    }

    @Test
    public void collapseParenTest() {
        List<Pair<Double, String>> pairList = new ArrayList<>();
        pairList.add(new Pair<>(1.0, "(1)"));
        pairList.add(new Pair<>(3.0, "(1+2)"));
        pairList.add(new Pair<>(3.0, "(((1+2)))"));

        pairList.add(new Pair<>(10.0, "(1+2) + (3+4)"));
        pairList.add(new Pair<>(21.0, "(1+2) * (3+4)"));
        pairList.add(new Pair<>(0.5, "(1+2) / (3+3)"));

        pairList.add(new Pair<>(17.0, "(1+2) + (3+4) * (2)"));
        pairList.add(new Pair<>(30.0, "((1+2) * 3 * 2 + (4(2+1)))"));
        pairList.add(new Pair<>(92.0, "2 + (((4 + 2) 5) 3)"));
        pairList.add(new Pair<>(2.5, "(5)/(4)(2)"));
        pairList.add(new Pair<>(10.0, "(5 + (2 + 3))"));

        pairList.add(new Pair<>(845.0, "5 + 2 (((53) + (2+5) 2 / 2) 7)"));
        pairList.add(new Pair<>(11.8, "(1+2*3+4/5*6)"));
        pairList.add(new Pair<>(99.0, "5 + 2 ((2 + 3 (2*2) 3) + 9)"));

        assertEqualsHelper(pairList);
    }

    @Test
    public void divideByZeroTest() {
        List<String> operationList = new ArrayList<>();
        operationList.add("1/0");
        operationList.add("(5+4) * (3/0");
        operationList.add("(8+9)/0");

        assertNullHelper(operationList);
    }

    @Test
    public void decimalInputTest() {
        List<Pair<Double, String>> pairList = new ArrayList<>();
        pairList.add(new Pair<>(3.5, "1.1 + 2.4"));
        pairList.add(new Pair<>(-1.5, "2.0 - 3.5"));
        pairList.add(new Pair<>(9.0, "99.9 / 11.1"));
        pairList.add(new Pair<>(12.1, "2.2 * 5.5"));
        pairList.add(new Pair<>(5.94, "1.1(3.4 + 2)"));

        assertEqualsHelper(pairList);
    }

    @Test
    public void irregularInputTest() {
        List<Pair<Double, String>> pairList = new ArrayList<>();

        pairList.add(new Pair<>(3.0, "0000000000000000000000003"));
        pairList.add(new Pair<>(19.0, "0000005 + 07 * 0000002"));
        pairList.add(new Pair<>(19.0, "(005 + ((07)0002))"));

        pairList.add(new Pair<>(3.5, "00000000003.50000000000000"));
        pairList.add(new Pair<>(0.3, "0000000.30000000"));
        pairList.add(new Pair<>(5.1, "(01.1000 + 4.000000) "));

        assertEqualsHelper(pairList);
    }

    private void assertNullHelper(List<String> operationList) {
        Calculator calculator = new Calculator("");
        for (String operation : operationList) {
            calculator.setOperation(operation);
            Assert.assertNull(calculator.compute());
        }
    }

    private void assertEqualsHelper(List<Pair<Double, String>> pairList) {
        Calculator calculator = new Calculator("");
        for (Pair<Double, String> pair : pairList) {
            calculator.setOperation(pair.getValue());
            Assert.assertEquals(pair.getKey(), calculator.compute());
        }
    }
}