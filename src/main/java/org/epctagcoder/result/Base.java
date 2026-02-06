package org.epctagcoder.result;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public abstract class Base {

    private String epcScheme;
    private String applicationIdentifier;
    private String tagSize;
    private String filterValue;
    private String partitionValue;
    private String prefixLength;
    private String companyPrefix;
    private String epcPureIdentityURI;
    private String epcTagURI;
    private String epcRawURI;
    private String binary;
    private String rfidTag;
    private String exception;

}
