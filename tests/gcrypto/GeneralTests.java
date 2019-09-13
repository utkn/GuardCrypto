package gcrypto;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;
import it.unisa.dia.gas.plaf.jpbc.pairing.a.TypeACurveGenerator;
import it.unisa.dia.gas.plaf.jpbc.pairing.parameters.PropertiesParameters;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

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

    // Assert that the elements of the G1 are generators.
    @Test
    public void generatorTest() {
        Field g1 = pairing.getG1();
        BigInteger order = g1.getOrder();
        for(int i = 0; i < 100; i++) {
            Element g = g1.newRandomElement();
            Assertions.assertTrue(g.pow(order).isOne());
        }
    }

    @Test
    public void pairingTest() {
        BigInteger a = BigInteger.valueOf(3);
        BigInteger b = BigInteger.valueOf(7);
        Element g = pairing.getG1().newRandomElement().getImmutable();

        // 1
        Element leftSide = pairing.pairing(g.pow(a), g.pow(b));
        Element rightSide = pairing.pairing(g.pow(b), g.pow(a));

        Assertions.assertTrue(leftSide.isEqual(rightSide));

        // 2
        leftSide = pairing.pairing(g.pow(a), g.pow(b));
        rightSide = pairing.pairing(g, g.pow(a).pow(b));

        Assertions.assertTrue(leftSide.isEqual(rightSide));

        // 3
        leftSide = pairing.pairing(g.pow(a), g.pow(b));
        rightSide = pairing.pairing(g, g).pow(a).pow(b);

        Assertions.assertTrue(leftSide.isEqual(rightSide));
    }
}