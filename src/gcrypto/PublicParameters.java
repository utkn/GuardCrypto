package gcrypto;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;
import it.unisa.dia.gas.jpbc.Vector;

public class PublicParameters {
    // Input field.
    Field G;
    // Output field.
    Field GT;
    // A random element from G.
    Element g;
    // g to the power alpha.
    Element g1;
    // A random element from G.
    Element g2;
    // A random element from G.
    Element uPrime;
    // A random element from G.
    Element mPrime;
    // A vector of random elements from G.
    Element[] U;
    // A vector of random elements from G.
    Element[] M;
}
