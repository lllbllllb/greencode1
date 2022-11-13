package com.lllbllllb.loader;

public record LoadOptions(
    int rps,

    boolean stopWhenDisconnect,

    boolean liveMode,

    int loadTimeSec
) {

}
