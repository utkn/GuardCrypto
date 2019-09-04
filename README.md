### JPBC Documentation
#### Overview
In order to generate the pairing, first we need to generate the parameters like `r` and `q`.
To generate the parameters, we use a `PairingParametersGenerator` as following:
```java
PairingParametersGenerator pg = new TypeACurveGenerator(rBits, qBits);
PairingParameters parameters = pg.generate();
```
Here, `rBits` and `qBits` denote the number of bits those parameters will have.
In the second line, we generate the parameters using the parameter generator.
Then, we can simply create a `Pairing` from the parameters using the `PairingFactory` factory class, as following:
```java
Pairing pairing = PairingFactory.getPairing(parameters);
```
In order to generate pairings, we simply call `pairing` method on the `pairing` that
we have just created, as following:
```java
Element out = pairing.pairing(elem1, elem2)
```
Here, both elem1 and elem2 are of type `Element`.

#### Pairing object
The `Pairing` object is used to create pairings. In particular, it maps an element
from `G1 x G2` to `GT` where the properties of a bilinear pairing are satisfied.

To get the `G1` field:
```java
Field G1 = pairing.getG1();
```
To get the `G2` field:
```java
Field G2 = pairing.getG2();
```
In Type-A curves that we use, the pairing is **symmetrical** which means that `G1 = G2`.

To get the `GT` field:
```java
Field GT = pairing.getGT();
```
We can get the degree of a pairing as following: **(???)**
```java
int order = pairing.getDegree();
```
#### Field object
In Type-A curves, all fields have the same order.
#### Element object
`Element`s denote an element from the groups `G1`, `G2` or `GT`.