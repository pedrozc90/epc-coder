package com.pedrozc90.epcs.schemes.gsrn.objects;

import com.pedrozc90.epcs.objects.Base;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class GSRN extends Base {

    private static final String SCHEMA = "gsrn";

    private String serviceReference;
    private String checkDigit;

    public GSRN() {
        super(SCHEMA);
    }

}
