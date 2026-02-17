package com.pedrozc90.epcs.schemes;

import com.pedrozc90.epcs.objects.Epc;

public interface EpcParser<T extends Epc> {

    default int remainder(int length) {
        return (int) (Math.ceil((length / 16.0)) * 16) - length;
    }

}
