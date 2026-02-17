package com.pedrozc90.epcs.schemes.sscc.objects;

import com.pedrozc90.epcs.objects.Epc;

public record SSCC(
    String tagSize,
    String filterValue,
    String partitionValue,
    String prefixLength,
    String companyPrefix,
    String extensionDigit,
    String serial,
    String checkDigit,
    String epcPureIdentityURI,
    String epcTagURI,
    String epcRawURI,
    String binary,
    String rfidTag
) implements Epc {

    private static final String SCHEME = "sscc";
    private static final String IDENTIFIER = "AI 00";

    @Override
    public String epcScheme() {
        return SCHEME;
    }

    @Override
    public String applicationIdentifier() {
        return IDENTIFIER;
    }

}
