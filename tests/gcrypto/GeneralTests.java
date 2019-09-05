package gcrypto;

import it.unisa.dia.gas.jpbc.PairingParameters;
import it.unisa.dia.gas.plaf.jpbc.pairing.a.TypeACurveGenerator;
import it.unisa.dia.gas.plaf.jpbc.pairing.parameters.PropertiesParameters;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import sun.jvm.hotspot.utilities.Assert;

import java.math.BigInteger;

public class GeneralTests {
    @Test
    public void bitsTest() {
        for(int i = 4; i <= 20; i++) {
            System.out.println("Testing r bits=" + i);
            bitsTestHelper(i);
        }
    }

    // Assert that r is a prime factor of q+1.
    @Test
    public void qTest() {
        TypeACurveGenerator generator = new TypeACurveGenerator(160, 512);
        PropertiesParameters params = (PropertiesParameters)generator.generate();
        BigInteger rInt = params.getBigInteger("r");
        BigInteger qInt = params.getBigInteger("q");
        BigInteger qPlusOne = qInt.add(BigInteger.ONE);
        Assertions.assertEquals(BigInteger.ZERO, qPlusOne.remainder(rInt));
        Assertions.assertTrue(rInt.isProbablePrime(10));
    }

    public void bitsTestHelper(int rBits) {
        TypeACurveGenerator generator = new TypeACurveGenerator(160, 512);
        PropertiesParameters params = (PropertiesParameters)generator.generate();
        BigInteger rInt = params.getBigInteger("r");
        BigInteger qInt = params.getBigInteger("q");
        BigInteger maxR = maxValue(160);
        BigInteger maxQ = maxValue(512);
        System.out.println("r=" + rInt + " max=" + maxR);
        System.out.println("q=" + qInt + " max=" + maxQ);
        Assertions.assertTrue(rInt.compareTo(maxR) <= 0);
        Assertions.assertTrue(qInt.compareTo(maxQ) <= 0);
//        q bits do not conform!
    }

    // Returns the maximum decimal value an unsigned bit-array with given length can take.
    private BigInteger maxValue(int bits) {
        // 2^n-1
        return BigInteger.valueOf(2).pow(bits).subtract(BigInteger.ONE);
    }
}