package io.metersphere.metadata.vo.repository;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FileRelevanceCaseDTO {
    public String id;
    public String caseId;
    public String caseName;
    public String caseType;
    public String commitId;
}
