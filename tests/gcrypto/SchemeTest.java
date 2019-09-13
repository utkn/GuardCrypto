package gcrypto;

import it.unisa.dia.gas.jpbc.Element;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

public class SchemeTest {

    Scheme scheme;
    Authority authority;

    int identityLength;
    int messageLength;

    @BeforeEach
    public void init() throws Exception {
        scheme = new Scheme(160, 512, identityLength, messageLength);
        authority = new Authority();
    }

    @Test
    public void setUpTest() {
        PublicParameters p = scheme.Setup(authority);
        Assertions.assertEquals(messageLength, p.M.length);
        Assertions.assertEquals(identityLength, p.U.length);
        Assertions.assertTrue(p.g.pow(scheme.getAlpha()).isEqual(p.g1));
    }

    @Test
    public void masterSecretTest() {
        PublicParameters p = scheme.Setup(authority);
        BigInteger alpha = scheme.getAlpha();
        Element masterSecret = scheme.getMasterSecret();
        Assertions.assertTrue(masterSecret.isEqual(p.g2.pow(alpha)));
        Assertions.assertTrue(scheme.pair(masterSecret, p.g2).isEqual(scheme.pair(p.g2, p.g2).pow(alpha)));
    }

    @Test
    public void extractTest() {
        PublicParameters p = scheme.Setup(authority);
        Pair privateKey = scheme.Extract("00101");
    }

    @Test
    public void verifyTest() {
        scheme.Setup(authority);
        String identity = "00101";
        String message = "0110011010";
        Pair privateKey = scheme.Extract(identity);
        Triplet signature = scheme.Sign(message, privateKey);
        Assertions.assertTrue(scheme.Verify(identity, message, signature));
    }
}
