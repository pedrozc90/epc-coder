package com.pedrozc90.epcs.schemes.grai.objects;

import com.pedrozc90.epcs.objects.Base;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class GRAI extends Base {

    private static final String SCHEMA = "grai";

    private String assetType;
    private String serial;
    private String checkDigit;

    public GRAI() {
        super(SCHEMA);
    }

}
