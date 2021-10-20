package io.metersphere.log.utils.diff.json.jsonwrap.gson;

import com.google.gson.JsonElement;

import io.metersphere.log.utils.diff.json.jsonwrap.JzonElement;


public class GsonJsonElement implements JzonElement {

    final JsonElement wrapped;


    protected GsonJsonElement(JsonElement wrapped) {
        this.wrapped = wrapped;
    }


    @Override
    public boolean isJsonObject() {
        return wrapped.isJsonObject();
    }


    @Override
    public boolean isJsonArray() {
        return wrapped.isJsonArray();
    }


    @Override
    public boolean isJsonPrimitive() {
        return wrapped.isJsonPrimitive();
    }


    @Override
    public boolean isJsonNull() {
        return wrapped.isJsonNull();
    }


    @Override
    public Object unwrap() {
        return wrapped;
    }


    @Override
    public String toString() {
        return wrapped.toString();
    }


    @Override
    public boolean equals(Object obj) {
        return wrapped.equals(obj);
    }


    @Override
    public int hashCode() {
        return wrapped.hashCode();
    }

}
