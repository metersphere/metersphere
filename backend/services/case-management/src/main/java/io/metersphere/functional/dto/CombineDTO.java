package io.metersphere.functional.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * @author wx
 */
@Data
public class CombineDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String id;
    private List<Object> value;
    private String type;
    private String operator;

}
