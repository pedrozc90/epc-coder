package com.pedrozc90.epcs.schemes.gdti.objects;

import com.pedrozc90.epcs.objects.Base;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class GDTI extends Base {

    private static final String SCHEMA = "gdti";

    private String docType;
    private String serial;
    private String checkDigit;

    public GDTI() {
        super(SCHEMA);
    }

}
