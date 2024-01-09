package io.metersphere.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author wx
 */
@Data
public class BugRelateCaseDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String id;

    private String title;

    private String status;

}
