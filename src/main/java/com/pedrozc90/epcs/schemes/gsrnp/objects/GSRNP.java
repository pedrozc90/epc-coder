package com.pedrozc90.epcs.schemes.gsrnp.objects;

import com.pedrozc90.epcs.objects.Epc;

public record GSRNP(
    String tagSize,
    String filterValue,
    String partitionValue,
    String prefixLength,
    String companyPrefix,
    String serviceReference,
    String checkDigit,
    String epcPureIdentityURI,
    String epcTagURI,
    String epcRawURI,
    String binary,
    String rfidTag
) implements Epc {

    private static final String SCHEME = "gsrnp";
    private static final String IDENTIFIER = "AI 8017";

    @Override
    public String epcScheme() {
        return SCHEME;
    }

    @Override
    public String applicationIdentifier() {
        return IDENTIFIER;
    }

}
