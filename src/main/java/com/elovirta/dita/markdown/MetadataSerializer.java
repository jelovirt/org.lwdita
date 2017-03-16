package com.elovirta.dita.markdown;

import java.util.Map;

public interface MetadataSerializer {

    void write(final Map<String, Object> header);

}
