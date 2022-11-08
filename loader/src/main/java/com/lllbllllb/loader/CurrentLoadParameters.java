package com.lllbllllb.loader;

import lombok.Value;

/**
 * CurrentTestParams.
 *
 * @author Yahor Pashkouski
 * @since 08.11.2022
 */
@Value
public class CurrentLoadParameters {

    int rps;

    boolean stopWhenDisconnect;

}
