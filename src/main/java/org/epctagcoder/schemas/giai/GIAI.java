package org.epctagcoder.schemas.giai;

import lombok.*;
import org.epctagcoder.result.Base;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class GIAI extends Base {

    private String individualAssetReference;

}
