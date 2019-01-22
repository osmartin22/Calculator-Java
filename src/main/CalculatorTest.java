package main;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

public class CalculatorTest {

    private Calculator calculator;

    // TODO: Separate tests to actually test different types of input
    //      Currently tests are just there to test
    @Test
    public void numTest() {
        calculator = new Calculator("((5+2))) 2 + 8");
        Assert.assertNull(calculator.compute());

        calculator = new Calculator("5 + 2 ((53 + (2+5)");
        Assert.assertNull(calculator.compute());

        calculator = new Calculator("((5+2)) 2 + 8");
        Assert.assertEquals((Double) 22.0, calculator.compute());

        calculator = new Calculator("(5+2)+1");
        Assert.assertEquals((Double) 8.0, calculator.compute());

        calculator = new Calculator("(5+2) + 7 + (9*2)");
        Assert.assertEquals((Double) 32.0, calculator.compute());

        calculator = new Calculator("5 + 2 ((2 + 3 (2*2) 3) + 9)");
        Assert.assertEquals((Double) 99.0, calculator.compute());

        calculator = new Calculator("(1+2*3+4/5*6)");
        Assert.assertEquals((Double) 11.8, calculator.compute());

        calculator = new Calculator("5 + 2 (53 + (2 / 2) + 1)");
        Assert.assertEquals((Double) 115.0, calculator.compute());

        calculator = new Calculator("5 + 2 (((53) + (2+5) 2 / 2) 7)");
        Assert.assertEquals((Double) 845.0, calculator.compute());

        calculator = new Calculator("((3 + 2))");
        Assert.assertEquals((Double) 5.0, calculator.compute());

        calculator = new Calculator("(5 + (2 + 3))");
        Assert.assertEquals((Double) 10.0, calculator.compute());

        calculator = new Calculator("5 + 2 ((53 + (2+5)))");
        Assert.assertEquals((Double) 125.0, calculator.compute());

        calculator = new Calculator("(5)(4)(2)");
        Assert.assertEquals((Double) 40.0, calculator.compute());

        calculator = new Calculator("(5)/(4)(2)");
        Assert.assertEquals((Double) 2.5, calculator.compute());

        calculator = new Calculator("5 / 4 * 2");
        Assert.assertEquals((Double) 2.5, calculator.compute());
    }

    @org.junit.After
    public void tearDown() throws Exception {
    }
}