package gcrypto;

import it.unisa.dia.gas.jpbc.Element;

public class Triplet {
    private Element first;
    private Element second;
    private Element third;

    public Triplet(Element first, Element second, Element third) {
        this.first = first;
        this.second = second;
        this.third = third;
    }

    public Element getFirst() {
        return first;
    }

    public Element getSecond() {
        return second;
    }

    public Element getThird() {
        return third;
    }
}
