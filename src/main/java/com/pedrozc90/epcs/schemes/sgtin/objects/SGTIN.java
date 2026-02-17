package com.pedrozc90.epcs.schemes.sgtin.objects;

import com.pedrozc90.epcs.objects.Epc;

public record SGTIN(
    String tagSize,
    String filterValue,
    String partitionValue,
    String prefixLength,
    String companyPrefix,
    String itemReference,
    String extensionDigit,
    String serial,
    String checkDigit,
    String epcPureIdentityURI,
    String epcTagURI,
    String epcRawURI,
    String binary,
    String rfidTag
) implements Epc {

    private static final String SCHEME = "sgtin";
    private static final String IDENTIFIER = "AI 414 + AI 254";

    @Override
    public String epcScheme() {
        return SCHEME;
    }

    @Override
    public String applicationIdentifier() {
        return IDENTIFIER;
    }

}
