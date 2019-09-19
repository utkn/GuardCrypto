package gcrypto.threshold;


import it.unisa.dia.gas.jpbc.Element;

import java.math.BigInteger;

public class SignatureShare {

    private Element first;
    private Element second;
    private BigInteger r_k;

    public SignatureShare(BigInteger r_k, Element first, Element second) {
        this.first = first.getImmutable();
        this.second = second.getImmutable();
        this.r_k = r_k;
    }

    public Element getFirst() {
        return first;
    }

    public Element getSecond() {
        return second;
    }

    // For debugging purposes.
    public BigInteger getR_k() {
        return r_k;
    }
}
