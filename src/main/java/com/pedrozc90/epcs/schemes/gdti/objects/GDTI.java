package com.pedrozc90.epcs.schemes.gdti.objects;

import com.pedrozc90.epcs.objects.Epc;

public record GDTI(
    String tagSize,
    String filterValue,
    String partitionValue,
    String prefixLength,
    String companyPrefix,
    String docType,
    String serial,
    String checkDigit,
    String epcPureIdentityURI,
    String epcTagURI,
    String epcRawURI,
    String binary,
    String rfidTag
) implements Epc {

    public static final String SCHEME = "gdti";
    public static final String IDENTIFIER = "AI 253";

    @Override
    public String epcScheme() {
        return SCHEME;
    }

    @Override
    public String applicationIdentifier() {
        return IDENTIFIER;
    }

}
