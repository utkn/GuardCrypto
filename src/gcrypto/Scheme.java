package gcrypto;

import gcrypto.threshold.Polynomial;
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

    protected int identityLength; // n_u
    protected int messageLength; // n_m

    protected PairingParameters parameters;
    protected Pairing pairing;
    protected BigInteger alpha;

    protected Element masterSecret;
    protected PublicParameters publicParameters = new PublicParameters();

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
     * Returns a random integer that is smaller than the given order.
     * @return a random integer mod p, where p is the order.
     */
    protected BigInteger chooseRandom(BigInteger order) {
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
    protected Element calculateMultiplier(String bits, Element coeff, Element[] vector) {
        Element b = coeff.getImmutable();
        for(int i = 0; i < bits.length(); i++) {
            if(bits.charAt(i) == '1') {
                b = b.mul(vector[i]);
            }
        }
        return b.getImmutable();
    }

    // Public for debugging purposes.
    public Element calculateIdentityMultiplier(String identity) {
        return calculateMultiplier(identity,
                publicParameters.uPrime, publicParameters.U);
    }

    // Public for debugging purposes.
    public Element calculateMessageMultiplier(String message) {
        return calculateMultiplier(message,
                publicParameters.mPrime, publicParameters.M);
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
        this.alpha = chooseRandom(this.pairing.getG1().getOrder());
        publicParameters.G = pairing.getG1();
        publicParameters.GT = pairing.getGT();
        // We make use of the fact that every element in the input field is a generator.
        publicParameters.g = publicParameters.G.newRandomElement().getImmutable();
        publicParameters.g1 = publicParameters.g.pow(alpha).getImmutable();
        publicParameters.g2 = publicParameters.G.newRandomElement().getImmutable();
        masterSecret = publicParameters.g2.pow(alpha).getImmutable();
        // Use the authority to generate u', m', U and M.
        publicParameters.uPrime = authority.generateUPrime(publicParameters.G);
        publicParameters.mPrime = authority.generateMPrime(publicParameters.G);
        publicParameters.U = authority.generateUVector(publicParameters.G, identityLength);
        publicParameters.M = authority.generateMVector(publicParameters.G, messageLength);
        return publicParameters;
    }

    public PrivateKey Extract(String identity) {
        if(identity.length() != identityLength) {
            System.err.println("Identity length is not correct.");
            return null;
        }
        // Get a random integer mod p where p is the order of the input group.
        BigInteger r_u = chooseRandom(this.pairing.getG1().getOrder());
        Element a = masterSecret.mul(calculateIdentityMultiplier(identity).pow(r_u));
        Element b = publicParameters.g.pow(r_u);
        // a = (g2^alpha) * (identityMultiplier)^r_u
        // b = g^r_u
        return new PrivateKey(a, b, r_u);
    }

    public Signature Sign(String message, PrivateKey privateKey) {
        if(message.length() != messageLength) {
            System.err.print("Message length is not correct.");
            return null;
        }
        // Get a random integer mod p where p is the order of the input group.
        BigInteger r_m = chooseRandom(this.pairing.getG1().getOrder());
        Element a = privateKey.getFirst();
        Element b = calculateMessageMultiplier(message);
        b = b.pow(r_m);
        a = a.mul(b);
        Element c = privateKey.getSecond();
        Element d = publicParameters.g.pow(r_m);
        return new Signature(a, c, d);
    }

    public boolean Verify(String identity, String message, Signature signature) {
        // e(signature[1], g) = e(g2, g1)e(identityMultiplier, signature[2])e(messageMultiplier, signature[3])
        if(identity.length() != identityLength || message.length() != messageLength) {
            System.err.println((identity.length() != identityLength) ? "Identity length" : "Message length"
                    + " is not correct.");
            return false;
        }
        Element leftSide = pairing.pairing(signature.getFirst(), publicParameters.g);

        Element rightSide_1 = pairing.pairing(publicParameters.g2, publicParameters.g1);
        Element rightSide_2_1 = calculateIdentityMultiplier(identity);
        Element rightSide_2 = pairing.pairing(rightSide_2_1, signature.getSecond());
        Element rightSide_3_1 = calculateMessageMultiplier(message);
        Element rightSide_3 = pairing.pairing(rightSide_3_1, signature.getThird());
        Element rightSide = rightSide_1.mul(rightSide_2).mul(rightSide_3);

        return leftSide.isEqual(rightSide);
    }

}
