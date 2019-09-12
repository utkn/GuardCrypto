package gcrypto;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;
import it.unisa.dia.gas.jpbc.Vector;
import it.unisa.dia.gas.plaf.jpbc.field.vector.VectorElement;

public class Authority {
    public Element mPrime(Field G) {
        return G.newRandomElement();
    }

    public Element uPrime(Field G) {
        return G.newRandomElement();
    }

    public Element[] M(Field G, int n_m) {
        return generateRandomVector(G, n_m);
    }

    public Element[] U(Field G, int n_u) {
        return generateRandomVector(G, n_u);
    }

    private Element[] generateRandomVector(Field field, int size) {
        Element[] vector = new Element[size];
        for(int i = 0; i < size; i++) {
            vector[i] = field.newRandomElement();
        }
        return vector;
    }
}
