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

### Encode from components

```java
public class Main {
    public static void main(final String[] args) {
        final ParseSSCC parser = ParseSSCC.Builder()
            .withCompanyPrefix("023356789")
            .withExtensionDigit(SSCCExtensionDigit.EXTENSION_3)
            .withSerial("0200002")
            .withTagSize(SSCCTagSize.BITS_96)
            .withFilterValue(SSCCFilterValue.RESERVED_5)
            .build();

        final SSCC sscc = parser.getSSCC();
        System.out.println("EPC: " + sscc.getRfidTag());
    }
}
```

### Decode from RFID tag

```java
public class Main {
    public static void main(final String[] args) {
        final ParseSSCC parser = ParseSSCC.Builder()
            .withRFIDTag("31AC16465751CCD0C2000000")
            .build();
        
        final SSCC sscc = parser.getSSCC();
        System.out.println("EPC: " + sscc.getRfidTag());
    }
}
```

### Parse from EPC Tag URI

```java
public class Main {
    public static void main(final String[] args) {
        final ParseSSCC parser = ParseSSCC.Builder()
            .withEPCTagURI("urn:epc:tag:sscc-96:5.023356789.30200002")
            .build();
        
        final SSCC sscc = parser.getSSCC();
        System.out.println("EPC: " + sscc.getRfidTag());
    }
}
```

### Parse from EPC Pure Identity URI

```java
public class Main {
    public static void main(final String[] args) {
        final ParseSSCC parser = ParseSSCC.Builder()
            .withEPCPureIdentityURI("urn:epc:id:sscc:023356789.30200002")
            .withTagSize(SSCCTagSize.BITS_96)
            .withFilterValue(SSCCFilterValue.RESERVED_5)
            .build();

        final SSCC sscc = parser.getSSCC();
        System.out.println("EPC: " + sscc.getRfidTag());
    }
}
```

## License

Please, read [LICENSE](./LICENSE) file for more information.
