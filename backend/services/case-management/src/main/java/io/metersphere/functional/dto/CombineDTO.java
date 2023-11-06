package io.metersphere.functional.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

/**
 * @author wx
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class CombineDTO implements Serializable {

    private String id;
    private List<Object> value;
    private String type;
    private String operator;

}
