package gcrypto;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.jpbc.PairingParametersGenerator;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;
import it.unisa.dia.gas.plaf.jpbc.pairing.a.TypeACurveGenerator;
import it.unisa.dia.gas.plaf.jpbc.pairing.parameters.PropertiesParameters;

import java.math.BigInteger;

public class Main {

    public static void main(String[] args) {
        TypeACurveGenerator generator = new TypeACurveGenerator(160, 512);
        PropertiesParameters params = (PropertiesParameters)generator.generate();
        Pairing pairing = PairingFactory.getPairing(params);
        Field g1 = pairing.getG1();
        BigInteger order = g1.getOrder();
        BigInteger orderPlusOne = order.add(BigInteger.ONE);
        System.out.println(order);
        g1.newElement(1);
    }
}
