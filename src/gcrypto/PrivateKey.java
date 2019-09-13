package gcrypto;

import it.unisa.dia.gas.jpbc.Element;

public class PrivateKey {
    private Element first;
    private Element second;

    public PrivateKey(Element first, Element second) {
        this.first = first;
        this.second = second;
    }

    public Element getFirst() {
        return first;
    }

    public Element getSecond() {
        return second;
    }
}
