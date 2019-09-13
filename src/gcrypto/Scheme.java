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

    /**
     * Returns a random integer that is smaller than the order of the input group G.
     * @return a random integer mod p, where p is the order of G.
     */
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

    /**
     * Calculates the following value: coeff*􏰂(Vector[i_1]*Vector[i_2]*Vector[i_3]*...*Vector[i_n]) where
     * i_1, i_2, i_3, ..., i_n are the indexes where bits[i_j]= 1
     * @param bits can be message or identity.
     * @param coeff can be either u_prime or m_prime.
     * @param vector can be either U or M.
     * @return the calculated value.
     */
    private Element calculateMultiplier(String bits, Element coeff, Element[] vector) {
        Element b = coeff.getImmutable();
        for(int i = 0; i < bits.length(); i++) {
            if(bits.charAt(i) == '1') {
                b = b.mul(vector[i]);
            }
        }
        return b.getImmutable();
    }

    // For debugging purposes.
    public BigInteger getAlpha() {
        return alpha;
    }

    // For debugging purposes.
    public Element getMasterSecret() {
        return masterSecret;
    }

    // For debugging purposes.
    public Element pair(Element a, Element b) {
        return pairing.pairing(a, b);
    }

    // *** Main functions ***

    public PublicParameters Setup(Authority authority) {
        this.alpha = chooseRandom();
        publicParameters.G = pairing.getG1();
        publicParameters.GT = pairing.getGT();
        // We make use of the fact that every element in the input field is a generator.
        publicParameters.g = publicParameters.G.newRandomElement().getImmutable();
        publicParameters.g1 = publicParameters.g.pow(alpha).getImmutable();
        publicParameters.g2 = publicParameters.G.newRandomElement().getImmutable();
        masterSecret = publicParameters.g2.pow(alpha).getImmutable();
        // Use the authority to generate u', m', U and M.
        publicParameters.uPrime = authority.uPrime(publicParameters.G);
        publicParameters.mPrime = authority.mPrime(publicParameters.G);
        publicParameters.U = authority.U(publicParameters.G, identityLength);
        publicParameters.M = authority.M(publicParameters.G, messageLength);
        return publicParameters;
    }

    public PrivateKey Extract(String identity) {
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
        return new PrivateKey(a, c);
    }

    public Signature Sign(String message, PrivateKey privateKey) {
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
        return new Signature(a, c, d);
    }

    public boolean Verify(String identity, String message, Signature signature) {
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
