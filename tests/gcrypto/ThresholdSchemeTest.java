package gcrypto;

import com.sun.tools.internal.xjc.reader.dtd.bindinfo.BIAttribute;
import gcrypto.threshold.DistributedKeys;
import gcrypto.threshold.SignatureShare;
import gcrypto.threshold.ThresholdScheme;
import it.unisa.dia.gas.jpbc.Element;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static gcrypto.Helper.power;

public class ThresholdSchemeTest {
    private ThresholdScheme scheme;
    private Authority authority;

    private int identityLength = 5;
    private int messageLength = 10;
    private int threshold = 3;
    private int servers = 10;

    @BeforeEach
    public void init() throws Exception {
        scheme = new ThresholdScheme(160, 512, identityLength, messageLength);
        authority = new Authority();
        scheme.Setup(authority);
    }

    @Test
    public void keyDisTest() {
        String identity = "00101";

        PrivateKey privateKey = scheme.Extract(identity);
        DistributedKeys distKeys = scheme.KeyDis(privateKey, servers, threshold, identity);

        // Check whether the constructed public threshold parameters are correct.
        BigInteger exponent = privateKey.getR_u().subtract(scheme.getR_up());
        // Y[1] = masterSecret * identityMult^(r_u - r_u')
        Element pp1Expected = scheme.getMasterSecret().mul(power(scheme.calculateIdentityMultiplier(identity), exponent));
        Element pp1Actual = distKeys.getPublicParameters()[0];
        Assertions.assertEquals(pp1Expected, pp1Actual);
        // Y[2] = g^r_u
        Element pp2Expected = power(scheme.publicParameters.g, privateKey.getR_u());
        Element pp2Actual = distKeys.getPublicParameters()[1];
        Assertions.assertEquals(pp2Expected, pp2Actual);

        // Check whether we can construct the private key from the public threshold parameters.

        // Assuming we have found r_u'
        BigInteger r_up = scheme.getR_up();

        // We should be able to reconstruct the private key.
        // Y[1] = masterSecret * identityMultiplier^(r_u - r_u')
        // PrivateKey[1] = masterSecret * identityMultiplier^(r_u) = Y[1] * identityMultiplier^(r_u')
        Element reconstructedFirstPart_1 = power(scheme.calculateIdentityMultiplier(identity), r_up);
        Element reconstructedFirstPart = distKeys.getPublicParameters()[0].mul(reconstructedFirstPart_1);
        Element reconstructedSecondPart = distKeys.getPublicParameters()[1];

        // Make sure that this is the case.
        Assertions.assertEquals(privateKey.getFirst(), reconstructedFirstPart);
        Assertions.assertEquals(privateKey.getSecond(), reconstructedSecondPart);
    }

    @Test
    public void signatureShareValidityTest() {
        String identity = "00101";
        String message = "1010011001";

        PrivateKey privateKey = scheme.Extract(identity);
        DistributedKeys distKeys = scheme.KeyDis(privateKey, servers, threshold, identity);

        // Check whether the signature-shares are valid.
        for(int server = 1; server <= servers; server++) {
            // For each server, construct a signature share.
            SignatureShare signatureShare = scheme.ThrSig(server, message, identity, distKeys);
            // And make sure that it is valid.
            Element leftSide = scheme.pair(signatureShare.getFirst(), scheme.publicParameters.g);
            Element rightPairing = scheme.pair(scheme.calculateMessageMultiplier(message), signatureShare.getSecond());
            Element rightSide = distKeys.getVerificationKey(server).mul(rightPairing);
            Assertions.assertEquals(leftSide, rightSide);
        }
    }

    @Test
    public void reconstructTest() {
        String identity = "00101";
        String message = "1010011001";

        // We want to make sure that the "reconstructed" signatures from the signature-shares are valid.
        // First, construct the expected signature.
        PrivateKey privateKey = scheme.Extract(identity);
        // Generate the distributed keys.
        DistributedKeys distKeys = scheme.KeyDis(privateKey, servers, threshold, identity);
        SignatureShare[] signatureShares = new SignatureShare[servers];
        // Collect the signature shares for each server.
        for(int server = 1; server <= servers; server++) {
            signatureShares[server-1] = scheme.ThrSig(server, message, identity, distKeys);
        }

        int[] allIndexes = new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
        // Reconstruct from every server.
        Signature reconstructedSignature = scheme.Reconstruct(allIndexes, signatureShares, distKeys);
        // Perform simple assertions.
        Assertions.assertEquals(scheme.publicParameters.g.mul(privateKey.getR_u()), reconstructedSignature.getSecond());
        Assertions.assertTrue(scheme.Verify(identity, message, reconstructedSignature));

        // Reconstruct from too few servers.
        int[] tooFewServerIndexes = new int[] { 1, 2 };
        SignatureShare[] tooFewSignatureShares = new SignatureShare[] {
                signatureShares[0], signatureShares[1]
        };
        reconstructedSignature = scheme.Reconstruct(tooFewServerIndexes, tooFewSignatureShares, distKeys);
        Assertions.assertFalse(scheme.Verify(identity, message, reconstructedSignature));

        // Reconstruct from just enough servers.
        int[] justEnoughIndexes = new int[] { 1, 2, 3 };
        SignatureShare[] justEnoughSignatureShares = new SignatureShare[] {
                signatureShares[0], signatureShares[1], signatureShares[2]
        };
        reconstructedSignature = scheme.Reconstruct(justEnoughIndexes, justEnoughSignatureShares, distKeys);
        Assertions.assertTrue(scheme.Verify(identity, message, reconstructedSignature));
    }
}
