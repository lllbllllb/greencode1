package com.lllbllllb.loader;

import lombok.With;

public record LoadOptions(

    @With
    int rps,

    boolean stopWhenDisconnect,

    boolean liveMode,

    int loadTimeSec
) {

}
