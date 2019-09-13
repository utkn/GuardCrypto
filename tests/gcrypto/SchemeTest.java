package gcrypto;

import it.unisa.dia.gas.jpbc.Element;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

public class SchemeTest {

    private Scheme scheme;
    private Authority authority;

    private int identityLength = 5;
    private int messageLength = 10;

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
    public void verifyTest() {
        scheme.Setup(authority);

        String identity = "00101";
        String message = "0110011010";

        String falseIdentity = "01101";
        String falseMsg = "0110011011";

        PrivateKey privateKey = scheme.Extract(identity);
        Signature signature = scheme.Sign(message, privateKey);

        Assertions.assertTrue(scheme.Verify(identity, message, signature));
        Assertions.assertFalse(scheme.Verify(identity, falseMsg, signature));
        Assertions.assertFalse(scheme.Verify(falseIdentity, message, signature));
    }
}
