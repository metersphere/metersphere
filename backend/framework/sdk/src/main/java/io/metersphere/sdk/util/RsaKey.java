package io.metersphere.sdk.util;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Setter
@Getter
public class RsaKey implements Serializable {

    //公钥
    private String publicKey;

    //私钥
    private String privateKey;

}