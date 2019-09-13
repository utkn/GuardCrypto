package gcrypto;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.jpbc.PairingParameters;
import it.unisa.dia.gas.jpbc.PairingParametersGenerator;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;
import it.unisa.dia.gas.plaf.jpbc.pairing.a.TypeACurveGenerator;

import java.math.BigInteger;
import java.security.SecureRandom;

public class Scheme {
    SecureRandom rand = new SecureRandom();

    private int identityLength; // n_u
    private int messageLength; // n_m

    private PairingParameters parameters;
    private Pairing pairing;
    private BigInteger alpha;

    private Element masterSecret;
    private PublicParameters publicParameters = new PublicParameters();

    /**
     * Generates the parameters for the pairing.
     * @param rBits number of bits for r.
     * @param qBits number of bits for q.
     */
    public Scheme(int rBits, int qBits, int identityLength, int messageLength) {
        this.identityLength = identityLength;
        this.messageLength = messageLength;

        PairingParametersGenerator pg = new TypeACurveGenerator(rBits, qBits);
        this.parameters = pg.generate();
        this.pairing = PairingFactory.getPairing(this.parameters);
    }

    private BigInteger chooseRandom() {
        BigInteger order = this.pairing.getG1().getOrder();
        BigInteger alpha;
        while(true) {
            alpha = new BigInteger(order.bitLength(), rand);
            if(alpha.compareTo(order) < 0) {
                break;
            }
        }
        return alpha;
    }

    private Element calculateMultiplier(String bits, Element coeff, Element[] vector) {
        Element b = coeff;
        for(int i = 0; i < bits.length(); i++) {
            if(bits.charAt(i) == '1') {
                b = b.mul(vector[i]);
            }
        }
        return b;
    }

    public BigInteger getAlpha() {
        return alpha;
    }

    public Element getMasterSecret() {
        return masterSecret;
    }

    public Element pair(Element a, Element b) {
        return pairing.pairing(a, b);
    }

    public PublicParameters Setup(Authority authority) {
        this.alpha = chooseRandom();
        publicParameters.G = pairing.getG1();
        publicParameters.GT = pairing.getGT();
        // We make use of the fact that every element in the input field is a generator.
        publicParameters.g = publicParameters.G.newRandomElement();
        publicParameters.g1 = publicParameters.g.pow(alpha);
        publicParameters.g2 = publicParameters.G.newRandomElement();
        masterSecret = publicParameters.g2.pow(alpha);
        // Use the authority to generate u', m', U and M.
        publicParameters.uPrime = authority.uPrime(publicParameters.G);
        publicParameters.mPrime = authority.mPrime(publicParameters.G);
        publicParameters.U = authority.U(publicParameters.G, identityLength);
        publicParameters.M = authority.M(publicParameters.G, messageLength);
        return publicParameters;
    }

    public Pair Extract(String identity) {
        if(identity.length() != identityLength) {
            System.err.println("Identity length is not correct.");
            return null;
        }
        BigInteger r_u = chooseRandom();
        Element a = masterSecret;
        Element b = calculateMultiplier(identity,
                publicParameters.uPrime, publicParameters.U);
        b = b.pow(r_u);
        a = a.mul(b);
        Element c = publicParameters.g.pow(r_u);
        return new Pair(a, c);
    }

    public Triplet Sign(String message, Pair privateKey) {
        if(message.length() != messageLength) {
            System.err.print("Message length is not correct.");
            return null;
        }
        BigInteger r_m = chooseRandom();
        Element a = privateKey.getFirst();
        Element b = calculateMultiplier(message,
                publicParameters.mPrime, publicParameters.M);
        b = b.pow(r_m);
        a = a.mul(b);
        Element c = privateKey.getSecond();
        Element d = publicParameters.g.pow(r_m);
        return new Triplet(a, c, d);
    }

    public boolean Verify(String identity, String message, Triplet signature) {
        if(identity.length() != identityLength || message.length() != messageLength) {
            System.err.println((identity.length() != identityLength) ? "Identity length" : "Message length"
                    + " is not correct.");
            return false;
        }
        Element leftSide = pairing.pairing(signature.getFirst(), publicParameters.g);

        Element rightSide_1 = pairing.pairing(publicParameters.g2, publicParameters.g1);
        Element rightSide_2_1 = calculateMultiplier(identity, publicParameters.uPrime, publicParameters.U);
        Element rightSide_2 = pairing.pairing(rightSide_2_1, signature.getSecond());
        Element rightSide_3_1 = calculateMultiplier(message, publicParameters.mPrime, publicParameters.M);
        Element rightSide_3 = pairing.pairing(rightSide_3_1, signature.getThird());
        Element rightSide = rightSide_1.mul(rightSide_2).mul(rightSide_3);

        return leftSide.isEqual(rightSide);
    }

}
