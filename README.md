# EPC Coder

A lightweight and fast Java library for encoding and decoding RFID EPC (Electronic Product Code) tags.

> Forked from [jlcout/epctagcoder](https://github.com/jlcout/epctagcoder)

## Features

- Implemented in accordance with [EPC Tag Data Standard 1.9](http://www.gs1.org/epc/tag-data-standard)
- Built with Java 21
- Simple builder pattern API
- Small footprint (~160kb)
- High performance (encode/decode 10,000 EPCs in ~200ms)

## Supported EPC Schemes

- `CPI`: Component / Part Identifier
- `GDTI`: Global Document Type Identifier
- `GIAI`: Global Individual Asset Identifier
- `GRAI`: Global Returnable Asset Identifier
- `GSRN`: Global Service Relation Number – Recipient
- `GSRNP`: Global Service Relation Number – Provider
- `SGCN`: Serialized Global Coupon Number
- `SGLN`: Global Location Number With or Without Extension
- `SGTIN`: Serialized Global Trade Item Number
- `SSCC`: Serial Shipping Container Code

## Usage

### Installation

#### Maven

```xml
<repositories>
    <repository>
        <id>github</id>
        <name>GitHub Packages</name>
        <url>https://maven.pkg.github.com/pedrozc90/epc-coder</url>
    </repository>
</repositories>
```

```xml
<dependency>
    <groupId>com.pedrozc90</groupId>
    <artifactId>epc-coder</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Encode

```java
// sgtin-96
final SGTIN sgtin = SGTINParser.builder()
    .withCompanyPrefix("0614141")
    .withExtensionDigit(SGTINExtensionDigit.EXTENSION_8)
    .withItemReference("12345")
    .withSerial("6789")
    .withTagSize(SGTINTagSize.BITS_96)
    .withFilterValue(SGTINFilterValue.RESERVED_5)
    .build();

final String rfid = sgtin.rfidTag();    // "3074257BF7194E4000001A85"
final String uri = sgtin.epcTagURI();   // "urn:epc:tag:sgtin-96:3.0614141.812345.6789"
```

### Decode RFID Tag

```java
// sgtin-96
final SGTIN sgtin = ParseSGTIN.builder()
    .withRFIDTag("3074257BF7194E4000001A85")
    .build();

final String companyPrefix = sgtin.companyPrefix();
final String itemReference = sgtin.itemReference();
final String serial = sgtin.serial();

// sscc-96
final SSCC sscc = ParseSSCC.builder()
    .withRFIDTag("31AC16465751CCD0C2000000")
    .build();

final String companyPrefix = sscc.companyPrefix();
final String itemReference = sscc.itemReference();
final String serial = sscc.serial();
```

### Decode EPC Tag URI

```java
// sscc-96
final SSCC sscc = ParseSSCC.builder()
    .withEpcTagURI("urn:epc:tag:sscc-96:5.023356789.30200002")
    .build();

final String rfid = sscc.rfidTag();
final String companyPrefix = sscc.companyPrefix();
final String itemReference = sscc.itemReference();
final String serial = sscc.serial();
```

### Decode EPC Pure Identity URI

```java
// sscc-96
final SSCC sscc = ParseSSCC.builder()
    .withEpcPureIdentityURI("urn:epc:id:sscc:023356789.30200002")
    .withTagSize(SSCCTagSize.BITS_96)
    .withFilterValue(SSCCFilterValue.RESERVED_5)
    .build();

final String rfid = sscc.rfidTag();
final String companyPrefix = sscc.companyPrefix();
final String itemReference = sscc.itemReference();
final String serial = sscc.serial();
```

## License

Please, read [LICENSE](./LICENSE) file for more information.
