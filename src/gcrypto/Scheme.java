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

    private int identityLength; // n_u
    private int messageLength; // n_m

    private PairingParameters parameters;
    private Pairing pairing;
    private boolean generated;
    private BigInteger alpha;

    /**
     * Generates the parameters for the pairing.
     * @param rBits number of bits for r.
     * @param qBits number of bits for q.
     */
    public void generate(int rBits, int qBits) throws Exception {
        if(generated) {
            throw new Exception("Parameters are already generated");
        }
        PairingParametersGenerator pg = new TypeACurveGenerator(rBits, qBits);

        this.parameters = pg.generate();
        this.pairing = PairingFactory.getPairing(this.parameters);
        this.generated = true;
        this.alpha = chooseAlpha();
    }

    private BigInteger chooseAlpha() {
        SecureRandom rand = new SecureRandom();
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

    public PublicParameters computePublicParameters(Authority authority) {
        if(!generated) {
            System.err.println("Parameters are not yet generated!");
            return null;
        }
        PublicParameters publicParameters = new PublicParameters();
        publicParameters.G = this.pairing.getG1();
        publicParameters.GT = this.pairing.getGT();
        // We make use of the fact that every element in the input field is a generator.
        publicParameters.g = publicParameters.G.newRandomElement();
        publicParameters.g1 = publicParameters.g.pow(this.alpha);
        publicParameters.g2 = publicParameters.G.newRandomElement();
        // Use the authority to generate u', m', U and M.
        publicParameters.uPrime = authority.uPrime(publicParameters.G);
        publicParameters.mPrime = authority.mPrime(publicParameters.G);
        publicParameters.U = authority.U(publicParameters.G, this.identityLength);
        publicParameters.M = authority.M(publicParameters.G, this.identityLength);
        return publicParameters;
    }
}
