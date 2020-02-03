package io.metersphere.user;

import java.io.Serializable;

public class SessionUser implements Serializable {

    private static final long serialVersionUID = -7149638440406959033L;
    private String id;
    private String name;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
