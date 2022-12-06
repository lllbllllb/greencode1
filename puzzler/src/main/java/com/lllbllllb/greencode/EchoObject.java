package com.lllbllllb.greencode;

public record EchoObject(String param) {

    public EchoObject {
        System.out.println(param);
    }
}
