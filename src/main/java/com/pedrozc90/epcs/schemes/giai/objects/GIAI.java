package com.pedrozc90.epcs.schemes.giai.objects;

import com.pedrozc90.epcs.objects.Epc;

public record GIAI(
    String tagSize,
    String filterValue,
    String partitionValue,
    String prefixLength,
    String companyPrefix,
    String individualAssetReference,
    String epcPureIdentityURI,
    String epcTagURI,
    String epcRawURI,
    String binary,
    String rfidTag
) implements Epc {

    private static final String SCHEME = "giai";
    private static final String IDENTIFIER = "AI 8004";

    @Override
    public String epcScheme() {
        return SCHEME;
    }

    @Override
    public String applicationIdentifier() {
        return IDENTIFIER;
    }

}
