package org.epctagcoder.schemas.sgtin;

import lombok.*;
import org.epctagcoder.result.Base;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SGTIN extends Base {

    private String extensionDigit;
    private String itemReference;
    private String serial;
    private String checkDigit;

}
