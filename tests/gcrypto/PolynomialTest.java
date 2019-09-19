package gcrypto;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

public class PolynomialTest {

    @Test
    public void computeTest() {
        Polynomial polynomial = new Polynomial(new BigInteger[]{
                BigInteger.valueOf(7),
                BigInteger.valueOf(4),
                BigInteger.valueOf(5),
                BigInteger.valueOf(9)
        });
        // x = 1
        BigInteger result = polynomial.compute(BigInteger.valueOf(1));
        Assertions.assertEquals(BigInteger.valueOf(25), result);
        // x = 0
        result = polynomial.compute(BigInteger.valueOf(0));
        Assertions.assertEquals(BigInteger.valueOf(7), result);
        // x = 3
        result = polynomial.compute(BigInteger.valueOf(3));
        Assertions.assertEquals(BigInteger.valueOf(307), result);
    }
}
