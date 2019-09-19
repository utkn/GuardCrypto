package gcrypto.threshold;

import gcrypto.PrivateKey;
import gcrypto.Scheme;
import gcrypto.Signature;
import it.unisa.dia.gas.jpbc.Element;

import java.math.BigInteger;

public class ThresholdScheme extends Scheme {

    private BigInteger r_up;


    /**
     * Generates the parameters for the pairing.
     *
     * @param rBits          number of bits for r.
     * @param qBits          number of bits for q.
     * @param identityLength
     * @param messageLength
     */
    public ThresholdScheme(int rBits, int qBits, int identityLength, int messageLength) {
        super(rBits, qBits, identityLength, messageLength);
    }

    /**
     *
     * @param threshold
     * @param order
     * @return
     */
    private Polynomial constructKeyDisPolynomial(int threshold, BigInteger order) {
        BigInteger[] coefficients = new BigInteger[threshold];
        for(int i = 0; i < threshold; i++) {
            coefficients[i] = chooseRandom(order);
        }
        return new Polynomial(coefficients, order);
    }

    // For debugging purposes.
    public BigInteger getR_up() {
        return r_up;
    }

    public DistributedKeys KeyDis(PrivateKey privateKey, int servers, int threshold, String identity) {
        // Construct a0+a1x+a2x^2+...+a(t-1)x^(t-1) where a0,a1,a2,...,a(t-1) are chosen from Zp.
        Polynomial polynomial = constructKeyDisPolynomial(threshold, pairing.getG1().getOrder());
        // r_u' = a0
        r_up = polynomial.getCoefficient(0);
        // Y is the public parameter for all servers.
        Element[] Y = new Element[2];
        Element identityMultiplier = calculateIdentityMultiplier(identity);
        // Y[0] = privateKey[0]/(identityMultiplier^r_up)
        //      = (g2^alpha)*(identityMultiplier)^(r_u - r_up)
        Element divisor = identityMultiplier.pow(r_up);
        Y[0] = privateKey.getFirst().div(divisor);
        Y[1] = privateKey.getSecond();
        // Construct the private/verification keys for each server.
        BigInteger[] distributedPrivateKeys = new BigInteger[servers];
        Element[] distributedVerificationKeys = new Element[servers];
        for(int server = 1; server <= servers; server++) {
            BigInteger f_k = polynomial.compute(BigInteger.valueOf(server));
            distributedPrivateKeys[server-1] = f_k;
            distributedVerificationKeys[server-1] = pair(identityMultiplier, publicParameters.g).pow(f_k);
        }
        return new DistributedKeys(Y, distributedPrivateKeys, distributedVerificationKeys);
    }

    public SignatureShare ThrSig(int server, String message, String identity, DistributedKeys distributedKeys) {
        BigInteger r_k = chooseRandom(publicParameters.G.getOrder());
        Element first_1 = calculateIdentityMultiplier(identity).pow(distributedKeys.getPrivateKey(server));
        Element first_2 = calculateMessageMultiplier(message).pow(r_k);
        Element first = first_1.getImmutable().mul(first_2);
        Element second = publicParameters.g.pow(r_k);
        return new SignatureShare(r_k, first, second);
    }

    private int lagrangeCoefficient(int[] omega, int k) {
        double result = 1.0;
        for(int j : omega) {
            if(j == k) continue;
            result *= (double)(-j) / (double)(k - j);
        }
        return (int)result;
    }

    public Signature Reconstruct(int[] servers, SignatureShare[] signatureShares, DistributedKeys distKeys) {
        Element first = distKeys.getPublicParameters()[0];
        Element second = distKeys.getPublicParameters()[1];
        Element third = publicParameters.G.newOneElement().getImmutable();
        for(int i = 0; i < servers.length; i++) {
            int server = servers[i];
            SignatureShare signatureShare = signatureShares[i];
            BigInteger lagrangeCoeff = BigInteger.valueOf(lagrangeCoefficient(servers, server));
            first = first.mul(signatureShare.getFirst().pow(lagrangeCoeff));
            third = third.mul(signatureShare.getSecond().pow(lagrangeCoeff));
        }
        return new Signature(first, second, third);
    }
}
