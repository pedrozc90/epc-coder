package org.epctagcoder.schemas.cpi;

import lombok.*;
import org.epctagcoder.result.Base;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CPI extends Base {

    private String componentPartReference;
    private String serial;

}
