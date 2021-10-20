package io.metersphere.log.utils.diff.json.jsonwrap.gson;

import com.google.gson.JsonNull;

import io.metersphere.log.utils.diff.json.jsonwrap.JzonNull;


public class GsonJsonNull extends GsonJsonElement implements JzonNull {

    static final JsonNull JNULL = new JsonNull();


    public final static GsonJsonNull INSTANCE = new GsonJsonNull();


    public GsonJsonNull() {
        super(JNULL);
    }

}
