package io.metersphere.dto;

import bsh.StringUtil;
import io.metersphere.commons.constants.RoleConstants;
import io.metersphere.i18n.Translator;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author song.tianyang
 * @Date 2021/3/4 2:47 下午
 * @Description
 */
public class CascaderParse {
    public static List<CascaderDTO> parseUserRoleDataStruct(List<OrganizationMemberDTO> organizationList, Map<String, List<WorkspaceDTO>> orgIdWorkspaceMap,boolean hideOrgRole) {
        List<CascaderDTO> returnList = new ArrayList<>();
        for (OrganizationMemberDTO orgDTO : organizationList) {
            String orgId = orgDTO.getId();
            List<WorkspaceDTO> workspaceDTOList = orgIdWorkspaceMap.get(orgId);
            CascaderDTO orgCascader = generateCascaderDTO(orgDTO.getId(), orgDTO.getName(), null);
            if (workspaceDTOList != null) {
                List<CascaderDTO> children = new ArrayList<>();
                for (WorkspaceDTO workspace : workspaceDTOList) {
                    String parentCascaderType = "workspace";
                    if(hideOrgRole){
                        parentCascaderType = "hideOrg";
                    }
                    List<CascaderDTO> cascaderDTOList = getUserRoleCascaderDTO(parentCascaderType, workspace.getId());
                    CascaderDTO workspaceCascader = generateCascaderDTO(workspace.getId(), workspace.getName(), cascaderDTOList);
                    children.add(workspaceCascader);
                }
                orgCascader.setChildren(children);
            } else {
                List<CascaderDTO> cascaderDTOList = getUserRoleCascaderDTO("org", orgDTO.getId());
                orgCascader.setChildren(cascaderDTOList);
            }
            returnList.add(orgCascader);
        }
        return returnList;
    }

    private static List<CascaderDTO> getUserRoleCascaderDTO(String parentCascaderType, String parentID) {
        String idPrefix = "";
        String idSuffix = "<->" + parentCascaderType;
        if (!StringUtils.isEmpty(parentID)) {
            idPrefix = parentID + "<->";
        }
        CascaderDTO orgAdminCascasder = generateCascaderDTO(idPrefix + RoleConstants.ORG_ADMIN + idSuffix, Translator.get("org_admin"), null);
        CascaderDTO orgMemberCascasder = generateCascaderDTO(idPrefix + RoleConstants.ORG_MEMBER + idSuffix, Translator.get("org_member"), null);
        CascaderDTO testManagerCascasder = generateCascaderDTO(idPrefix + RoleConstants.TEST_MANAGER + idSuffix, Translator.get("test_manager"), null);
        CascaderDTO testerCascasder = generateCascaderDTO(idPrefix + RoleConstants.TEST_USER + idSuffix, Translator.get("tester"), null);
        CascaderDTO readOnlyUserCascasder = generateCascaderDTO(idPrefix + RoleConstants.TEST_VIEWER + idSuffix, Translator.get("read_only_user"), null);

        //默认都要添加这两种类型
        List<CascaderDTO> returnList = new ArrayList<>();

        switch (parentCascaderType) {
            case "workspace":
                returnList.add(orgAdminCascasder);
                returnList.add(orgMemberCascasder);
                returnList.add(testManagerCascasder);
                returnList.add(testerCascasder);
                returnList.add(readOnlyUserCascasder);
                break;
            case "org":
                returnList.add(orgAdminCascasder);
                returnList.add(orgMemberCascasder);
                break;
            case "hideOrg":
                returnList.add(testManagerCascasder);
                returnList.add(testerCascasder);
                returnList.add(readOnlyUserCascasder);
                break;
        }
        return returnList;
    }

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
