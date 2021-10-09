package io.metersphere.log.utils.json.diff.jsonwrap.jackson2;

import com.fasterxml.jackson.databind.node.ValueNode;
import io.metersphere.log.utils.json.diff.jsonwrap.JzonPrimitive;


public class Jackson2JsonPrimitive extends Jackson2JsonElement implements JzonPrimitive {

    public Jackson2JsonPrimitive(ValueNode wrapped) {
        super(wrapped);
    }

}
