# EPCs

## Identification Schemes

- Trade Items
    - `SGTIN`: Serialized Global Trade Item Number
    - `GTIN`: Global Trade Item Number (class-level, no serial)
    - `GDTI`: Global Document Type Identifier
    - `CPI`: Component / Part Identifier
    - `GSIN`: Global Shipment Identification Number
    - `GINC`: Global Identification Number for Consignment
- Logistics Units
    - `SSCC`: Serial Shipping Container Code
- Assets
    - `GRAI`: Global Returnable Asset Identifier
    - `GIAI`: Global Individual Asset Identifier
- Locations
    - `SGLN`: Serialized Global Location Number
    - `GLN`: Global Location Number (class-level)
- Coupons & services
    - `GCN`: Global Coupon Number
- Identification keyset (less used, but valid EPC schemes)
    - `ITIP`: Individual Trade Item Piece
    - `UPUI`: Unit Pack Unique Identifier 
- Internal / reserved
    - `GID`: General Identifier (legacy / EPC-global internal)

## String encoding

│ EPC Scheme      │ Bit Length   │ Serial Encoding   │
|:----------------|:------------:|:-----------------:|
│ SGTIN-96        │ 96 bits      │ 6-bit alphabet    │
│ SGTIN-198       │ 198 bits     │ 7-bit alphabet    │
│ SSCC-96         │ 96 bits      │ Pure numeric      │
│ SGLN-96         │ 96 bits      │ Pure numeric      │
│ SGLN-198        │ 198 bits     │ 7-bit alphabet    │
│ GRAI-96         │ 96 bits      │ Pure numeric      │
│ GRAI-198        │ 198 bits     │ 7-bit alphabet    │
│ GIAI-96         │ 96 bits      │ Pure numeric      │
│ GIAI-202        │ 202 bits     │ 7-bit alphabet    │
│ GDTI-96         │ 96 bits      │ Pure numeric      │
│ GDTI-174        │ 174 bits     │ 7-bit alphabet    │
│ GID-96          │ 96 bits      │ Pure numeric      │
│ GSRN-96         │ 96 bits      │ Pure numeric      │
│ DOD-96          │ 96 bits      │ Pure numeric      │
│ SGCN-96         │ 96 bits      │ Pure numeric      │
│ CPI-96          │ 96 bits      │ 6-bit alphabet    │
│ CPI-var         │ Variable     │ 7-bit alphabet    │
