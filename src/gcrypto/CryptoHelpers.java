package gcrypto;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.jpbc.PairingParameters;
import it.unisa.dia.gas.jpbc.PairingParametersGenerator;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;
import it.unisa.dia.gas.plaf.jpbc.pairing.a.TypeACurveGenerator;
import it.unisa.dia.gas.plaf.jpbc.pairing.parameters.PropertiesParameters;

public class CryptoHelpers {

    private PairingParameters parameters;
    private Pairing pairing;
    private boolean generated;

    /**
     * Generates the parameters for the pairing.
     * @param rBits number of bits for r.
     * @param qBits number of bits for q.
     */
    public void generateParameters(int rBits, int qBits) throws Exception {
        if(generated) {
            throw new Exception("Parameters are already generated");
        }
        PairingParametersGenerator pg = new TypeACurveGenerator(rBits, qBits);

        this.parameters = pg.generate();
        this.pairing = PairingFactory.getPairing(this.parameters);
        this.generated = true;
    }

    public PublicParameters getPublicParameters() throws Exception {
        if(!generated) {
            throw new Exception("Parameters are not yet generated");
        }
        PublicParameters params = new PublicParameters();
        params.G = this.pairing.getG1();
        params.GT = this.pairing.getGT();
        params.g = params.G.newRandomElement();
        return params;
    }

}
