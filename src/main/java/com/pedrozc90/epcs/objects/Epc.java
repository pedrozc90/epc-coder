package com.pedrozc90.epcs.objects;

public interface Epc {

    String epcScheme();

    String applicationIdentifier();

    String tagSize();

    String filterValue();

    String partitionValue();

    String prefixLength();

    String companyPrefix();

    String epcPureIdentityURI();

    String epcTagURI();

    String epcRawURI();

    String binary();

    String rfidTag();

}
