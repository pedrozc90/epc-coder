package com.pedrozc90.epcs.schemes.gsrn.objects;

import com.pedrozc90.epcs.objects.Epc;

public record GSRN(
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

    private static final String SCHEME = "gsrn";
    private static final String IDENTIFIER = "AI 8018";

    @Override
    public String epcScheme() {
        return SCHEME;
    }

    @Override
    public String applicationIdentifier() {
        return IDENTIFIER;
    }

}
