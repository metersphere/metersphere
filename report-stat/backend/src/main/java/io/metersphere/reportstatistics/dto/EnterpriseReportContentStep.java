package io.metersphere.reportstatistics.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

@Data
public class EnterpriseReportContentStep implements Serializable {
    private String name;
    private String type;
    //type为report
    private String reportRecordId;
    private Map<String, Object> reportRecordData;
    private String recordImageContent;
    private Map<String, String> tableData;
    //type为txt
    private String previewContext;
}
