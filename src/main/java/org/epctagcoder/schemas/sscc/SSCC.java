package org.epctagcoder.schemas.sscc;

import lombok.*;
import org.epctagcoder.result.Base;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SSCC extends Base {

    private String extensionDigit;
    private String serial;
    private String checkDigit;

}
