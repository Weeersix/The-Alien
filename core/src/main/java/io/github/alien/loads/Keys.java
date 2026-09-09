package io.github.alien.loads;

import java.util.ArrayList;
import java.util.List;

public enum Keys {
    MODEL_SIZE_KEY,
    TEXTURE_SIZE_KEY,
    VARIATIONS_COUNT_KEY,
    MODEL_KEY,
    ADD_,

    ANOTHER_KEY;

    Keys(){}

    public String getKey(){
        return this.name()
                .replaceAll("_KEY", "")
                .replaceAll("_", "-")
                .toLowerCase();
    }

    public static Keys toKey(String value){
        return Keys.valueOf( value
                .replaceAll("-", "_")
                .toUpperCase()
                + "_KEY");
    }
}
