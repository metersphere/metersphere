package io.metersphere.system.dto.user;

import io.metersphere.project.domain.Project;
import io.metersphere.system.domain.Organization;
import io.metersphere.system.domain.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = false)
public class PersonalDTO extends User {

    @Schema(description = "头像")
    private String avatar;

    @Schema(description = "用户所属组织和项目")
    private List<OrgInfo> orgProjectList = new ArrayList<>();

    public void setOrgProjectList(Map<Organization, List<Project>> organizationProjectMap) {
        for (Map.Entry<Organization, List<Project>> entry : organizationProjectMap.entrySet()) {
            Organization org = entry.getKey();
            List<Project> projectList = entry.getValue();
            OrgInfo orgInfo = new OrgInfo(org, projectList);
            this.orgProjectList.add(orgInfo);
        }
    }
}

@Data
@NoArgsConstructor
class OrgInfo {
    private String orgId;
    private String orgName;
    List<ProjectInfo> projectList = new ArrayList<>();

    public OrgInfo(Organization org, List<Project> projectList) {
        this.orgId = org.getId();
        this.orgName = org.getName();
        for (Project project : projectList) {
            ProjectInfo projectInfo = new ProjectInfo();
            projectInfo.setProjectId(project.getId());
            projectInfo.setProjectName(project.getName());
            this.projectList.add(projectInfo);
        }

    }
}

@Data
class ProjectInfo {
    private String projectId;
    private String projectName;
}
