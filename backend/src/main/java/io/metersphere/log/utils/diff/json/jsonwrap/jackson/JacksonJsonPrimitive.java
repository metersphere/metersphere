package io.metersphere.log.utils.diff.json.jsonwrap.jackson;

import org.codehaus.jackson.node.ValueNode;

import io.metersphere.log.utils.diff.json.jsonwrap.JzonPrimitive;


public class JacksonJsonPrimitive extends JacksonJsonElement implements JzonPrimitive {

    public JacksonJsonPrimitive(ValueNode wrapped) {
        super(wrapped);
    }

}
