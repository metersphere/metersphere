package io.metersphere.base.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ErrorReportLibraryWithBLOBs extends ErrorReportLibrary implements Serializable {
    private String content;

    private String description;

    private static final long serialVersionUID = 1L;
}