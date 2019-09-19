package gcrypto;

import java.math.BigInteger;

public class Polynomial {

    private BigInteger[] parameters;


    public Polynomial(BigInteger[] parameters) {
        if(parameters.length < 1) {
            System.err.println("At least one parameter for the polynomial required!");
        }
        this.parameters = parameters;
    }

    public BigInteger compute(BigInteger x) {
        BigInteger result = parameters[0];
        BigInteger x_i = x;
        for(int i = 1; i < parameters.length; i++) {
            // result += parameter * x^i
            result = result.add(parameters[i].multiply(x_i));
            x_i = x_i.multiply(x);
        }
        return result;
    }
}
