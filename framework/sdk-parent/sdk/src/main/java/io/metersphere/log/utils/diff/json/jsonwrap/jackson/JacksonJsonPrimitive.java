package io.metersphere.log.utils.diff.json.jsonwrap.jackson;

import com.fasterxml.jackson.databind.node.ValueNode;
import io.metersphere.log.utils.diff.json.jsonwrap.JzonPrimitive;


public class JacksonJsonPrimitive extends JacksonJsonElement implements JzonPrimitive {

    public JacksonJsonPrimitive(ValueNode wrapped) {
        super(wrapped);
    }

}
