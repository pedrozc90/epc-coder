package com.pedrozc90.epcs.schemes.sgln.objects;

import com.pedrozc90.epcs.objects.Epc;

public record SGLN(
    String tagSize,
    String filterValue,
    String partitionValue,
    String prefixLength,
    String companyPrefix,
    String locationReference,
    String extension,
    String checkDigit,
    String epcPureIdentityURI,
    String epcTagURI,
    String epcRawURI,
    String binary,
    String rfidTag
) implements Epc {

    private static final String SCHEME = "sgln";
    private static final String IDENTIFIER = "AI 254";

    @Override
    public String epcScheme() {
        return SCHEME;
    }

    @Override
    public String applicationIdentifier() {
        return IDENTIFIER;
    }

}
