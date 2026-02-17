package com.pedrozc90.epcs.schemes.cpi.objects;

import com.pedrozc90.epcs.objects.Epc;

public record CPI(
    String tagSize,
    String filterValue,
    String partitionValue,
    String prefixLength,
    String companyPrefix,
    String componentPartReference,
    String serial,
    String epcPureIdentityURI,
    String epcTagURI,
    String epcRawURI,
    String binary,
    String rfidTag
) implements Epc {

    public static final String SCHEME = "cpi";
    public static final String IDENTIFIER = "AI 8010 + AI 8011";

    @Override
    public String epcScheme() {
        return SCHEME;
    }

    @Override
    public String applicationIdentifier() {
        return IDENTIFIER;
    }

}
