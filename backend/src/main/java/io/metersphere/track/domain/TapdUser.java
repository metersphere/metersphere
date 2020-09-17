package io.metersphere.track.domain;

import lombok.Data;
import java.io.Serializable;
import java.util.List;

@Data
public class TapdUser implements Serializable {
    private List<String> roleId;
    private String name;
    private String user;
}
