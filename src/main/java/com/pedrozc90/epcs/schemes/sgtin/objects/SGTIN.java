package com.pedrozc90.epcs.schemes.sgtin.objects;

import com.pedrozc90.epcs.objects.Base;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SGTIN extends Base {

    private static final String SCHEMA = "sgtin";
    private static final String APPLICATION_IDENTIFIER = "AI 414 + AI 254";

    private String extensionDigit;
    private String itemReference;
    private String serial;
    private String checkDigit;

    public SGTIN() {
        super(SCHEMA);
    }

}
