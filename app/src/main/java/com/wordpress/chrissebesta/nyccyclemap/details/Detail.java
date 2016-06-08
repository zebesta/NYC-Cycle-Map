package com.wordpress.chrissebesta.nyccyclemap.details;

/**
 * Created by chrissebesta on 5/26/16.
 * Custom class for an array adapter to display the details pulls from the JSON related to a unique key
 */
public class Detail {
    public String key;
    public String value;

    /**
     * Key and value pulled from the JSON returned by the NYC Open Data database
     * @param key
     * @param value
     */
    public Detail(String key, String value){
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
