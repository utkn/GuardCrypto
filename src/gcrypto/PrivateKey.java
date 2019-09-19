package gcrypto;

import it.unisa.dia.gas.jpbc.Element;

public class PrivateKey {
    private Element first;
    private Element second;

    public PrivateKey(Element first, Element second) {
        this.first = first.getImmutable();
        this.second = second.getImmutable();
    }

    public Element getFirst() {
        return first.getImmutable();
    }

    public Element getSecond() {
        return second.getImmutable();
    }
}
