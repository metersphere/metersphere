package io.metersphere.commons.constants;

public enum FileType {
    JMX(".jmx"), CSV(".csv"), JSON(".json"), PDF(".pdf"),
    JPG(".jpg"), PNG(".png"), JPEG(".jpeg"), DOC(".doc"),
    XLSX(".xlsx"), DOCX(".docx"), JAR(".jar"), JS(".js"), TXT(".txt"), CSS(".css"),
    P12(".p12"), JKS(".jks"), PFX(".pfx"), DCM(".dcm"),
    DER(".der"), CER(".cer"), PEM(".pem"), CRT(".crt"), SIDE(".side");

    // 保存后缀
    private String suffix;

    FileType(String suffix) {
        this.suffix = suffix;
    }

    public String suffix() {
        return this.suffix;
    }
}
