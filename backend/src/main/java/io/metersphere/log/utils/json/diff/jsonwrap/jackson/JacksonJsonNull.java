package io.metersphere.log.utils.json.diff.jsonwrap.jackson;

import org.codehaus.jackson.node.NullNode;

import io.metersphere.log.utils.json.diff.jsonwrap.JzonNull;


public class JacksonJsonNull extends JacksonJsonElement implements JzonNull {

    static final NullNode JNULL = NullNode.getInstance();


    public final static JacksonJsonNull INSTANCE = new JacksonJsonNull();


    public JacksonJsonNull() {
        super(JNULL);
    }

}
