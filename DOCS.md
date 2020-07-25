# GuardCrypto Documentation
We use [this paper](https://eprint.iacr.org/2012/073.pdf) in our implementation. The scheme builds up a threshold signature
scheme from a non-threshold version that makes use of bilinear pairings.
## Contents
* [Non Threshold Scheme](#non-threshold-scheme)
  * [Construction](#construction)
  * [Methods](#methods)
* [Threshold Scheme](#threshold-scheme)
  * [Construction](#construction-1)
  * [Methods](#methods-1)
## Non Threshold Scheme
### Construction
```java
// We create instance of the scheme.
Scheme scheme = new Scheme(rBits, qBits, identityLength, messageLength);
Authority auth = new Authority();
```
### Methods

#### PublicParameters SetUp(Authority)
This method constructs the public parameters and returns them. An `Authority` object is required, since it will be used 
in deciding some parameters. The master secret is also generated at this phase.

#### PrivateKey Extract(String identity)
This method calculates and returns the *private key* for an identity. The identity must be a bit array, i.e. the 
input string must only be consisted of `0`s and `1`s. Also, the length of the string must be equal to the `identityLength`
parameter that was provided to the constructor of the `Scheme` object.
```java
// Identity length = 5
PrivateKey key = scheme.Extract("01001");
```

#### Signature Sign(String message, PrivateKey)
Once the private key is extracted, a signature for a message can be constructed using this method. Similar to an identity,
the message must be a bit array and the length must be equal to the `messageLength` parameter that was provided in the constructor.
```java
PrivateKey key = scheme.Extract("01001");
// Message length = 10
Signature signature = scheme.Sign("100110010", key);
```

#### bool Verify(String identity, String message, Signature)
This method returns true if the given signature is indeed signed by the given identity on the given message. The usage is
very simple.
```java
PrivateKey key = scheme.Extract("01001");
// Message length = 10
Signature signature = scheme.Sign("100110010", key);
// Verify the signature.
boolean signed = scheme.Verify("01001", "100110010", signature);
if(!signed) {
    throw new Exception("Invalid signature!!");
}
```

## Threshold Scheme
### Construction
We use `ThresholdScheme` (that extends from `Scheme`) objects to instantiate a threshold-signature scheme.
```java
// We create instance of the scheme.
ThresholdScheme scheme = new ThresholdScheme(rBits, qBits, identityLength, messageLength);
Authority auth = new Authority();
```

### Methods
#### DistributedKeys KeyDis(PrivateKey, int servers, int threshold, String identity)
This method constructs the public parameters and private keys for the threshold-scheme by "splicing" a private-key into `servers` many
different private-keys, each of them belonging to a server with the index in [1, `servers`].
```java
// First, construct the expected signature.
PrivateKey privateKey = scheme.Extract(identity);
// Generate the distributed keys.
DistributedKeys distKeys = scheme.KeyDis(privateKey, servers, threshold, identity);
```
#### SignatureShare ThrSig(int server, String message, String identity, DistributedKeys)
Once the distributed keys are calculated and stored in a `DistributedKeys` object, we can compute the partial-signatures for each server index in [1, `servers`].
```java
// Collecting only a single partial signature for the 3rd server.
SignatureShare signatureShare = scheme.ThrSig(3, message, identity, distKeys);
```
```java
// Collecting every partial signature.
SignatureShare[] signatureShares = new SignatureShare[servers];
// Collect the signature shares for each server.
for(int server = 1; server <= servers; server++) {
    signatureShares[server-1] = scheme.ThrSig(server, message, identity, distKeys);
}
```
#### Signature Reconstruct(int[] serverIndexes, SignatureShare[] signatureShares, DistributedKeys)
From the signature shares, it is possible to reconstruct a valid signature. Please note that the generated signature won't be the same as a signature generated from the non-threshold scheme, however, they will both be able to verify the messages.

The first parameter is a list of server indexes in [1, `servers`]. The second parameter is their corresponding signature shares. The order of the signature shares need to correspond to the order of the indexes given in the first argument.
```java
// Reconstruct from every server.
int[] allIndexes = new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
Signature reconstructedSignature = scheme.Reconstruct(allIndexes, signatureShares, distKeys);
if(!scheme.Verify(identity, message, reconstructedSignature)) {
    throw new Exception("Not authentic!!");
}
```
```java
// Reconstruct from just enough servers (if t=3).
int[] justEnoughIndexes = new int[] { 3, 1, 7 };
SignatureShare[] justEnoughSignatureShares = new SignatureShare[] {
        // The signature shares that correspond to the server indexes.
        signatureShares[2], signatureShares[0], signatureShares[6]
};
reconstructedSignature = scheme.Reconstruct(justEnoughIndexes, justEnoughSignatureShares, distKeys);
if(!scheme.Verify(identity, message, reconstructedSignature)) {
    throw new Exception("Not authentic!!");
}
```
