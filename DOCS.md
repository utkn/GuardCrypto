### GuardCrypto Documentation
We use the [TODO cite] paper in our implementation. The scheme builds up a threshold signature
scheme from a non-threshold version that makes use of bilinear pairings.
#### Non Threshold Version
##### Construction
```java
// We create instance of the scheme.
Scheme scheme = new Scheme(rBits, qBits, identityLength, messageLength);
Authority auth = new Authority();
```
##### Methods

###### PublicParameters SetUp(Authority)
This method constructs the public parameters and returns them. An `Authority` object is required, since it will be used 
in deciding some parameters. The master secret is also generated at this phase.

###### PrivateKey Extract(String identity)
This method calculates and returns the *private key* for an identity. The identity must be a bit array, i.e. the 
input string must only be consisted of `0`s and `1`s. Also, the length of the string must be equal to the `identityLength`
parameter that was provided to the constructor of the `Scheme` object.
```java
// Identity length = 5
PrivateKey key = scheme.Extract("01001");
```

###### Signature Sign(String message, PrivateKey)
Once the private key is extracted, a signature for a message can be constructed using this method. Similar to an identity,
the message must be a bit array and the length must be equal to the `messageLength` parameter that was provided in the constructor.
```java
PrivateKey key = scheme.Extract("01001");
// Message length = 10
Signature signature = scheme.Sign("100110010", key);
```

###### bool Verify(String identity, String message, Signature)
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