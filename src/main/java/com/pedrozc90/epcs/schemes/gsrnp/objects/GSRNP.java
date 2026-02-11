package com.pedrozc90.epcs.schemes.gsrnp.objects;

import com.pedrozc90.epcs.objects.Base;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class GSRNP extends Base {

    private static final String SCHEMA = "gsrnp";

    private String serviceReference;
    private String checkDigit;

    public GSRNP() {
        super(SCHEMA);
    }

}
