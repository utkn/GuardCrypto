package gcrypto;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;
import it.unisa.dia.gas.plaf.jpbc.pairing.a.TypeACurveGenerator;
import it.unisa.dia.gas.plaf.jpbc.pairing.parameters.PropertiesParameters;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static gcrypto.Helper.power;

public class GeneralTests {

    TypeACurveGenerator generator;
    PropertiesParameters params;
    Pairing pairing;

    @BeforeEach
    public void init() {
        generator = new TypeACurveGenerator(160, 512);
        params = (PropertiesParameters)generator.generate();
        pairing = PairingFactory.getPairing(params);
    }

    // Assert that r is a prime factor of q+1.
    @Test
    public void qTest() {
        BigInteger rInt = params.getBigInteger("r");
        BigInteger qInt = params.getBigInteger("q");
        BigInteger qPlusOne = qInt.add(BigInteger.ONE);
        Assertions.assertEquals(BigInteger.ZERO, qPlusOne.remainder(rInt));
        Assertions.assertTrue(rInt.isProbablePrime(10));
    }

    // Assert that the elements of G1 have two inverses: one for addition, and one for multiplication.
    @Test
    public void inverseTest() {
        Field g1 = pairing.getG1();
        for(int i = 0; i < 100; i++) {
            Element g = g1.newRandomElement().getImmutable();
            Element g_neg = g.negate().getImmutable();
            Element g_inv = g.invert().getImmutable();
            Assertions.assertTrue(g.add(g_neg).isZero());
            Assertions.assertTrue(g.mul(g_inv).isOne());
        }
    }

    // Assert that the elements of the G1 are generators.
    @Test
    public void generatorTest() {
        Field g1 = pairing.getG1();
        BigInteger order = g1.getOrder();
        for(int i = 0; i < 100; i++) {
            Element g = g1.newRandomElement();
            Assertions.assertTrue(power(g, order).isOne());
        }
    }

    @Test
    public void pairingTest() {
        BigInteger a = BigInteger.valueOf(3);
        BigInteger b = BigInteger.valueOf(7);
        Element g = pairing.getG1().newRandomElement().getImmutable();

        // 1
        Element leftSide = pairing.pairing(power(g, a), power(g, b));
        Element rightSide = pairing.pairing(power(g, b), power(g, a));

        Assertions.assertTrue(leftSide.isEqual(rightSide));

        // 2
        leftSide = pairing.pairing(power(g, a), power(g, b));
        rightSide = pairing.pairing(g, power(power(g, a), b));

        Assertions.assertTrue(leftSide.isEqual(rightSide));

        // 3
        leftSide = pairing.pairing(power(g, a), power(g, b));
        rightSide = power(power(pairing.pairing(g, g), a), b);

        Assertions.assertTrue(leftSide.isEqual(rightSide));
    }
}