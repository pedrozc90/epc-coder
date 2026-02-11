package com.pedrozc90.epcs.schemes.cpi.objects;

import com.pedrozc90.epcs.objects.Base;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CPI extends Base {

    private static final String SCHEMA = "cpi";

    private String componentPartReference;
    private String serial;

    public CPI() {
        super(SCHEMA);
    }
}
