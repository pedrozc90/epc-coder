package com.pedrozc90.epcs.schemes.sscc.objects;

import com.pedrozc90.epcs.objects.Base;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SSCC extends Base {

    private static final String SCHEMA = "sscc";

    private String extensionDigit;
    private String serial;
    private String checkDigit;

    public SSCC() {
        super(SCHEMA);
    }

}
