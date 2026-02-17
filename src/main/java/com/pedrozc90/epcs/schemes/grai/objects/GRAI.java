package com.pedrozc90.epcs.schemes.grai.objects;

import com.pedrozc90.epcs.objects.Epc;

public record GRAI(
    String tagSize,
    String filterValue,
    String partitionValue,
    String prefixLength,
    String companyPrefix,
    String assetType,
    String serial,
    String checkDigit,
    String epcPureIdentityURI,
    String epcTagURI,
    String epcRawURI,
    String binary,
    String rfidTag
) implements Epc {

    private static final String SCHEME = "grai";
    private static final String IDENTIFIER = "AI 8003";

    @Override
    public String epcScheme() {
        return SCHEME;
    }

    @Override
    public String applicationIdentifier() {
        return IDENTIFIER;
    }

}
