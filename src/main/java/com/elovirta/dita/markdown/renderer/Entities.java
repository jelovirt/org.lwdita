package com.elovirta.dita.markdown.renderer;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Entities {

    public static final Properties ENTITIES;

    static {
        ENTITIES = new Properties();
        try (InputStream in = Entities.class.getResourceAsStream("/entities.properties")) {
            ENTITIES.load(in);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}
