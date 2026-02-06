package org.epctagcoder.schemas.sscc;

import org.epctagcoder.schemas.sscc.enums.SSCCExtensionDigit;
import org.epctagcoder.schemas.sscc.enums.SSCCFilterValue;
import org.epctagcoder.schemas.sscc.enums.SSCCTagSize;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ParseSSCCTest {

    @Test
    public void encode() throws Exception {
        final SSCC result = ParseSSCC.Builder()
            .withCompanyPrefix("023356789")
            .withExtensionDigit(SSCCExtensionDigit.EXTENSION_3)
            .withSerial("0200002")
            .withTagSize(SSCCTagSize.BITS_96)
            .withFilterValue(SSCCFilterValue.RESERVED_5)
            .build()
            .getSSCC();
        assertNotNull(result);
    }

    @Test
    public void test2() throws Exception {
        final SSCC result = ParseSSCC.Builder()
            .withRFIDTag("31AC16465751CCD0C2000000")
            .build()
            .getSSCC();
        assertNotNull(result);
    }

    @Test
    public void test3() throws Exception {
        final ParseSSCC parseSSCC = ParseSSCC.Builder()
            .withEPCTagURI("urn:epc:tag:sscc-96:5.023356789.30200002")
            .build();
        final SSCC result = parseSSCC.getSSCC();
        assertNotNull(result);
    }


    @Test
    public void test4() throws Exception {
        final ParseSSCC parseSSCC = ParseSSCC.Builder()
            .withEPCPureIdentityURI("urn:epc:id:sscc:023356789.30200002")
            .withTagSize(SSCCTagSize.BITS_96)
            .withFilterValue(SSCCFilterValue.RESERVED_5)
            .build();
        final SSCC result = parseSSCC.getSSCC();
        assertNotNull(result);
    }

}
