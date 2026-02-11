package com.pedrozc90.epcs.schemes.sgln.objects;

import com.pedrozc90.epcs.objects.Base;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SGLN extends Base {

    private static final String SCHEMA = "sgln";

    private String locationReference;
    private String extension;
    private String checkDigit;

    public SGLN() {
        super(SCHEMA);
    }

}
