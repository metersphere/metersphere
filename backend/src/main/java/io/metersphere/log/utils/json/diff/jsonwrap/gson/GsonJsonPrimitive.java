package io.metersphere.log.utils.json.diff.jsonwrap.gson;

import com.google.gson.JsonPrimitive;

import io.metersphere.log.utils.json.diff.jsonwrap.JzonPrimitive;


public class GsonJsonPrimitive extends GsonJsonElement implements JzonPrimitive {

    public GsonJsonPrimitive(JsonPrimitive wrapped) {
        super(wrapped);
    }

}
