package org.epctagcoder.schemas.cpi;

import org.epctagcoder.schemas.cpi.enums.CPIFilterValue;
import org.epctagcoder.schemas.cpi.enums.CPITagSize;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ParseCPITest {

    @Test
    public void decode_RfidTag() throws Exception {
        final CPI result = ParseCPI.Builder()
            .withRFIDTag("3C34257BF400181C80000190").build()
            .getCPI();
        assertNull(result);
    }

    @Test
    public void decode_PureIdentityURI() throws Exception {
        final CPI result = ParseCPI.Builder()
            .withEPCTagURI("urn:epc:tag:cpi-96:1.0614141.12345.400").build()
            .getCPI();
        assertNull(result);
    }

    @Test
    public void decodeURI() throws Exception {
        final CPI result = ParseCPI.Builder()
            .withEPCTagURI("urn:epc:id:cpi:0614141.123ABC.123456789").build()
            .getCPI();
        assertNull(result);
    }

    @Test
    public void decodeURI_2() throws Exception {
        final CPI result = ParseCPI.Builder()
            .withEPCTagURI("urn:epc:id:cpi:0614141.123456.123456789").build()
            .getCPI();
        assertNull(result);
    }

    @Test
    public void decodeInvalidRfidTag() throws Exception {
        final IllegalArgumentException cause = assertThrows(
            IllegalArgumentException.class,
            () -> ParseCPI.Builder().withRFIDTag("invalid").build()
        );
        assertNotNull(cause);
    }

    @Test
    public void encode() throws Exception {
        final CPI result = ParseCPI.Builder()
            .withCompanyPrefix("0614141")
            .withComponentPartReference("123456")
            .withSerial("123456789")
            .withTagSize(CPITagSize.BITS_96)
            .withFilterValue(CPIFilterValue.RESERVED_3)
            .build()
            .getCPI();
        assertNotNull(result);
    }

}
