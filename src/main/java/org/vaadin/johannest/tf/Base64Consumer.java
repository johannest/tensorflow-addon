package org.vaadin.johannest.tf;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class Base64Consumer {

    private byte[] bytes;

    public void extractBytes(String base64Image) {
        bytes = Base64.getDecoder().decode(base64Image.startsWith("data") ? base64Image.split(",")[1].getBytes(StandardCharsets.UTF_8) : base64Image.getBytes(StandardCharsets.UTF_8));
    }

    public byte[] getBytes() {
        return bytes;
    }
}
