package gcrypto;

import gcrypto.threshold.DistributedKeys;
import gcrypto.threshold.SignatureShare;
import gcrypto.threshold.ThresholdScheme;
import it.unisa.dia.gas.jpbc.Element;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

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

        // Check whether we can construct the private key from the public threshold parameters.
        PrivateKey privateKey = scheme.Extract(identity);
        DistributedKeys distKeys = scheme.KeyDis(privateKey, servers, threshold, identity);

        // Assuming we have found r_u'
        BigInteger r_up = scheme.getR_up();

        // We should be able to reconstruct the private key.
        Element reconstructedFirstPart_1 = scheme.calculateIdentityMultiplier(identity).pow(r_up);
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
            // Make sure that the constructed signature share is valid.
            Element rightSide_1 = scheme.pair(scheme.calculateMessageMultiplier(message), signatureShare.getSecond());
            Element rightSide = distKeys.getVerificationKey(server).mul(rightSide_1);
            Element leftSide = scheme.pair(signatureShare.getFirst(), scheme.publicParameters.g);
            Assertions.assertEquals(rightSide, leftSide);
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
        Assertions.assertTrue(scheme.Verify(identity, message, reconstructedSignature));

        // Reconstruct from too few servers.
        int[] tooFewServerIndexes = new int[] { 5, 1 };
        SignatureShare[] tooFewSignatureShares = new SignatureShare[] {
                signatureShares[4], signatureShares[0]
        };
        reconstructedSignature = scheme.Reconstruct(tooFewServerIndexes, tooFewSignatureShares, distKeys);
        Assertions.assertFalse(scheme.Verify(identity, message, reconstructedSignature));

        // Reconstruct from just enough servers.
        int[] justEnoughIndexes = new int[] { 1, 5, 3 };
        SignatureShare[] justEnoughSignatureShares = new SignatureShare[] {
                signatureShares[0], signatureShares[4], signatureShares[2]
        };
        reconstructedSignature = scheme.Reconstruct(justEnoughIndexes, justEnoughSignatureShares, distKeys);
        Assertions.assertTrue(scheme.Verify(identity, message, reconstructedSignature));
    }
}
