package org.apache.jmeter.config;

import lombok.Data;

@Data
public class KeystoreDTO {
    private int startIndex;
    private int endIndex;
    private String preload;
    private String clientCertAliasVarName;
    private String pwd;
    private String path;
}
