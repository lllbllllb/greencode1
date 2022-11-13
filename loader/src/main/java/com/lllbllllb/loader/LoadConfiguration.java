package com.lllbllllb.loader;

public record LoadConfiguration(
    int rps,

    boolean stopWhenDisconnect,

    boolean observerMode
) {

}
