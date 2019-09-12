package gcrypto;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;
import it.unisa.dia.gas.plaf.jpbc.pairing.a.TypeACurveGenerator;
import it.unisa.dia.gas.plaf.jpbc.pairing.parameters.PropertiesParameters;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

public class GeneralTests {

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

    // Assert that the elements of the G1 are generators.
    @Test
    public void generatorTest() {
        TypeACurveGenerator generator = new TypeACurveGenerator(160, 512);
        PropertiesParameters params = (PropertiesParameters)generator.generate();
        Pairing pairing = PairingFactory.getPairing(params);
        Field g1 = pairing.getG1();
        BigInteger order = g1.getOrder();
        for(int i = 0; i < 100; i++) {
            Element g = g1.newRandomElement();
            Assertions.assertTrue(g.pow(order).isOne());
        }
    }
}