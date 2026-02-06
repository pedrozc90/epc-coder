package org.epctagcoder.schemas.sgtin;

import org.epctagcoder.exception.EPCParseException;
import org.junit.jupiter.api.Test;

import static org.epctagcoder.schemas.sgtin.ParseSGTIN.Builder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ParseSGTINTest {

    @Test
    public void whenRfidTagIsNotGS1_thenParseExceptionIsRaised() {
        assertThrows(EPCParseException.class, () -> Builder().withRFIDTag("F45349FB11DF49FA935AB6FF").build());
    }

    @Test
    public void whenPartitionIsWrong_thenParseExceptionIsRaised() {
        assertThrows(EPCParseException.class, () -> Builder().withRFIDTag("303C83F1B7DD441678901234").build());
    }

    @Test
    public void parseEpcSerialTest() throws EPCParseException {
        final SGTIN sgtin = Builder().withRFIDTag("3024698E2CB1005678901234").build().getSGTIN();
        assertEquals("96511988276", sgtin.getSerial());
    }

    @Test
    public void parseEpcEanTest() throws EPCParseException {
        final SGTIN sgtin = Builder().withRFIDTag("3024698E2CB1005678901234").build().getSGTIN();
        assertEquals("141674018641", sgtin.getCompanyPrefix() + sgtin.getItemReference());
    }

    @Test
    public void parseEpcFilterTest() throws EPCParseException {
        final SGTIN sgtin = Builder().withRFIDTag("3024698E2CB1005678901234").build().getSGTIN();
        assertEquals("1", sgtin.getFilterValue());
    }

    @Test
    public void parseEpcPartitionTest() throws EPCParseException {
        final SGTIN sgtin = Builder().withRFIDTag("30285471BD5A0A5678901234").build().getSGTIN();
        assertEquals("2", sgtin.getPartitionValue());
    }

    @Test
    public void parseNonHexEpc() {
        assertThrows(IllegalArgumentException.class, () -> Builder().withRFIDTag("30285471BD5A0A56789G1234").build());
    }

}
