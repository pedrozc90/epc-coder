package org.epctagcoder.schemas.giai;

import org.epctagcoder.schemas.giai.enums.GIAIFilterValue;
import org.epctagcoder.schemas.giai.enums.GIAITagSize;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ParseGIAITest {

    @Test
    public void decodeURI() {
        final GIAI result = ParseGIAI.Builder()
            .withEPCTagURI("urn:epc:id:giai:0614141.12345400")
            .build()
            .getGIAI();
        assertNull(result);
    }

    @Test
    public void decodeInvalidRfidTag() {
        final IllegalArgumentException cause = assertThrows(
            IllegalArgumentException.class,
            () -> ParseGIAI.Builder().withRFIDTag("invalid").build()
        );
        assertNotNull(cause);
    }

    @Test
    public void encode() throws Exception {
        final GIAI result = ParseGIAI.Builder()
            .withCompanyPrefix("0614141")
            .withIndividualAssetReference("12345400")
            .withTagSize(GIAITagSize.BITS_96)
            .withFilterValue(GIAIFilterValue.RESERVED_3)
            .build()
            .getGIAI();
        assertNotNull(result);
    }

}
