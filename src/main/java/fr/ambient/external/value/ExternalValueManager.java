package fr.ambient.external.value;

import java.util.HashMap;

public class ExternalValueManager extends HashMap<String, ExternalValue> {

    @Override
    public ExternalValue get(Object key) {
        ExternalValue val = super.get(key);
        if (val == null) {
            val = new ExternalValue();
            if (key instanceof String) {
                this.put((String) key, val);
            }
        }
        return val;
    }
}
