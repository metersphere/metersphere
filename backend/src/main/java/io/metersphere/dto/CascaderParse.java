package io.metersphere.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author song.tianyang
 * @Date 2021/3/4 2:47 下午
 * @Description
 */
public class CascaderParse {


    public static List<CascaderDTO> parseWorkspaceDataStruct(List<OrganizationMemberDTO> organizationList, Map<String, List<WorkspaceDTO>> orgIdWorkspaceMap) {
        List<CascaderDTO> returnList = new ArrayList<>();
        for (OrganizationMemberDTO orgDTO : organizationList) {
            String orgId = orgDTO.getId();
            List<WorkspaceDTO> workspaceDTOList = orgIdWorkspaceMap.get(orgId);
            if (workspaceDTOList != null) {
                List<CascaderDTO> children = new ArrayList<>();
                for (WorkspaceDTO workspace : workspaceDTOList) {
                    CascaderDTO workspaceCascader = generateCascaderDTO(workspace.getId(), workspace.getName(), null);
                    children.add(workspaceCascader);
                }
                CascaderDTO orgCascader = generateCascaderDTO(orgDTO.getId(), orgDTO.getName(), children);
                returnList.add(orgCascader);
            }
        }
        return returnList;
    }

    private static CascaderDTO generateCascaderDTO(String value, String lable, List<CascaderDTO> children) {
        CascaderDTO cascaderDTO = new CascaderDTO();
        cascaderDTO.setLabel(lable);
        cascaderDTO.setValue(value);
        if (children != null && !children.isEmpty()) {
            cascaderDTO.setChildren(children);
        }
        return cascaderDTO;
    }
}
