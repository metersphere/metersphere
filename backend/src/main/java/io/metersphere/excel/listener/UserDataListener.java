package io.metersphere.excel.listener;

import io.metersphere.commons.constants.RoleConstants;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.controller.request.member.UserRequest;
import io.metersphere.excel.domain.UserExcelData;
import io.metersphere.i18n.Translator;
import io.metersphere.service.UserService;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

public class UserDataListener extends EasyExcelListener<UserExcelData> {

    private UserService userService;

    //key:workspace.name value:id
    Map<String, String> workspaceNameMap;
    //key:Organization.name value:id
    Map<String, String> orgNameMap;
    //已经保存的用户ID
    List<String> savedUserId;

    private List<String> ids;
    private List<String> names;

    public void setIds(List<String> ids) {
        this.ids = ids;
    }

    public void setNames(List<String> names) {
        this.names = names;
    }

    public List<String> getIds() {
        return this.ids;
    }

    public List<String> getNames() {
        return this.names;
    }

    public UserDataListener(Class clazz, Map<String, String> workspaceNameMap, Map<String, String> orgNameMap) {
        this.clazz = clazz;
        this.workspaceNameMap = workspaceNameMap;
        this.orgNameMap = orgNameMap;
        this.userService = (UserService) CommonBeanFactory.getBean("userService");
        savedUserId = userService.selectAllId();
    }

    @Override
    public String validate(UserExcelData data, String errMsg) {
        StringBuilder stringBuilder = new StringBuilder(errMsg);

        //判断组织管理员组织
        String orgManagerOrgCheck = this.checkOrganization(data.getUserIsOrgAdmin(), data.getOrgAdminOrganization());
        if (orgManagerOrgCheck != null) {
            stringBuilder.append(orgManagerOrgCheck);
        }
        //判断组织成员组织
        String orgMemberOrgCheck = this.checkOrganization(data.getUserIsOrgMember(), data.getOrgMemberOrganization());
        if (orgMemberOrgCheck != null) {
            stringBuilder.append(orgMemberOrgCheck);
        }
        //判断测试经理工作空间
        String testManagerWorkspaceCheck = this.checkWorkSpace(data.getUserIsTestManager(), data.getTestManagerWorkspace());
        if (testManagerWorkspaceCheck != null) {
            stringBuilder.append(testManagerWorkspaceCheck);
        }
        //判断测试人员工作空间
        String testerWorkspaceCheck = this.checkWorkSpace(data.getUserIsTester(), data.getTesterWorkspace());
        if (testerWorkspaceCheck != null) {
            stringBuilder.append(testerWorkspaceCheck);
        }
        //判断只读用户工作空间
        String viewerWorkspaceCheck = this.checkWorkSpace(data.getUserIsViewer(), data.getViewerWorkspace());
        if (viewerWorkspaceCheck != null) {
            stringBuilder.append(viewerWorkspaceCheck);
        }
        return stringBuilder.toString();
    }

    @Override
    public void saveData() {
        //检查有无重复数据
        String checkRepeatDataResult = this.checkRepeatIdAndEmail(list);
        if (!StringUtils.isEmpty(checkRepeatDataResult)) {
            MSException.throwException(checkRepeatDataResult);
        }

        //无错误数据才插入数据
        if (!errList.isEmpty()) {
            return;
        }
        Collections.reverse(list);
        List<UserRequest> result = list.stream().map(item -> this.convert2UserRequest(item)).collect(Collectors.toList());
        List<String> ids = new LinkedList<>();
        List<String> names = new LinkedList<>();
        for (UserRequest userRequest : result) {
            String id = userRequest.getId();
            ids.add(id);
            names.add(userRequest.getName());
            if (savedUserId.contains(id)) {
                //已经在数据库内的，走更新逻辑
                userService.updateUserRole(userRequest);
            } else {
                //不再数据库中的走新建逻辑
                userService.insert(userRequest);
            }
        }
        this.setIds(ids);
        this.setNames(names);
    }

    /**
     * 检查工作空间
     *
     * @param userRoleInExcel      excel表里的用户权限填写信息
     * @param workspaceInfoInExcel excel表中用户的工作空间填写信息
     * @return 报错信息
     */
    private String checkWorkSpace(String userRoleInExcel, String workspaceInfoInExcel) {
        String result = null;
        if (StringUtils.equalsIgnoreCase(Translator.get("options_yes"), userRoleInExcel)) {
            String[] workspaceArr = workspaceInfoInExcel.split("\n");
            for (String workspace :
                    workspaceArr) {
                if (!workspaceNameMap.containsKey(workspace)) {
                    if (result == null) {
                        result = new String(Translator.get("user_import_workspace_not_fond") + "：" + workspace + "; ");
                    } else {
                        result += Translator.get("user_import_workspace_not_fond") + "：" + workspace + "; ";
                    }
                }
            }
        }
        return result;
    }

    /**
     * 检查组织
     *
     * @param userRoleInExcel         excel表里的用户权限填写信息
     * @param organizationInfoInExcel excel表中用户组织填写信息
     * @return 报错信息
     */
    private String checkOrganization(String userRoleInExcel, String organizationInfoInExcel) {
        String result = null;
        if (StringUtils.equalsIgnoreCase(Translator.get("options_yes"), userRoleInExcel)) {
            String[] organizationArr = organizationInfoInExcel.split("\n");
            for (String organization :
                    organizationArr) {
                if (!orgNameMap.containsKey(organization)) {
                    if (result == null) {
                        result = new String(Translator.get("user_import_organization_not_fond") + "：" + organization + "; ");
                    } else {
                        result += Translator.get("user_import_organization_not_fond") + "：" + organization + "; ";
                    }
                }
            }
        }
        return result;
    }

    /**
     * 通过excel的信息，以及id字典对象，获取相对应的ID
     *
     * @param userRoleInExcel  excel中的信息，是否进行工作空间或者组织的id转化
     * @param nameStringInExce excel中的信息，组织或者工作空间的名称
     * @param idDic            id字典对象，传入组织或者工作空间的<name:id>类
     * @return 转化后的id集合
     */
    private List<String> getIdByExcelInfoAndIdDic(String userRoleInExcel, String nameStringInExce, Map<String, String> idDic) {
        List<String> resultList = new ArrayList<>();
        if (StringUtils.equalsIgnoreCase(Translator.get("options_yes"), userRoleInExcel)) {
            String[] nameArr = nameStringInExce.split("\n");
            for (String name : nameArr) {
                if (idDic.containsKey(name)) {
                    resultList.add(idDic.get(name));
                }
            }
        }
        return resultList;
    }

    private UserRequest convert2UserRequest(UserExcelData data) {
        UserRequest request = new UserRequest();
        request.setId(data.getId());
        request.setStatus("1");
        request.setSource("LOCAL");
        request.setName(data.getName());
        request.setEmail(data.getEmail());
        request.setPhone(data.getPhone());
        //这里的password要加密
        request.setPassword(data.getPassword());

        List<Map<String, Object>> roleMapList = new ArrayList<>();
        //判断是否是Admin
        if (StringUtils.equalsIgnoreCase(Translator.get("options_yes"), data.getUserIsAdmin())) {
            List<String> adminIdList = new ArrayList<>();
            adminIdList.add("adminSourceId");
            Map<String, Object> adminRoleMap = this.genRoleMap(RoleConstants.ADMIN, adminIdList);
            roleMapList.add(adminRoleMap);
        }
        //判断组织管理员
        List<String> orgManagerOrdIdList = this.getIdByExcelInfoAndIdDic(data.getUserIsOrgAdmin(), data.getOrgAdminOrganization(), orgNameMap);
        if (!orgManagerOrdIdList.isEmpty()) {
            Map<String, Object> orgAdminRoleMap = this.genRoleMap(RoleConstants.ORG_ADMIN, orgManagerOrdIdList);
            roleMapList.add(orgAdminRoleMap);
        }
        //判断组织成员
        List<String> orgMemberOrdIdList = this.getIdByExcelInfoAndIdDic(data.getUserIsOrgMember(), data.getOrgMemberOrganization(), orgNameMap);
        if (!orgMemberOrdIdList.isEmpty()) {
            Map<String, Object> orgMemberRoleMap = this.genRoleMap(RoleConstants.ORG_MEMBER, orgMemberOrdIdList);
            roleMapList.add(orgMemberRoleMap);
        }
        //判断测试经理
        List<String> testManagerWorkspaceIdList = this.getIdByExcelInfoAndIdDic(data.getUserIsTestManager(), data.getTestManagerWorkspace(), workspaceNameMap);
        if (!testManagerWorkspaceIdList.isEmpty()) {
            Map<String, Object> testManagerRoleMap = this.genRoleMap(RoleConstants.TEST_MANAGER, testManagerWorkspaceIdList);
            roleMapList.add(testManagerRoleMap);
        }
        //判断测试人员
        List<String> testgerWorkspaceIdList = this.getIdByExcelInfoAndIdDic(data.getUserIsTester(), data.getTesterWorkspace(), workspaceNameMap);
        if (!testgerWorkspaceIdList.isEmpty()) {
            Map<String, Object> testerRoleMap = this.genRoleMap(RoleConstants.TEST_USER, testgerWorkspaceIdList);
            roleMapList.add(testerRoleMap);
        }
        //判断只读用户
        List<String> viewerWorkspaceIdList = this.getIdByExcelInfoAndIdDic(data.getUserIsViewer(), data.getViewerWorkspace(), workspaceNameMap);
        if (!viewerWorkspaceIdList.isEmpty()) {
            Map<String, Object> testViewerRoleMap = this.genRoleMap(RoleConstants.TEST_VIEWER, viewerWorkspaceIdList);
            roleMapList.add(testViewerRoleMap);
        }
        request.setRoles(roleMapList);
        return request;
    }

    /**
     * 封装用户权限数据格式
     *
     * @param roleName   权限名称
     * @param roleIdList 对应的权限ID
     * @return 保存用户时，对应的数据权限的数据格式
     */
    private Map<String, Object> genRoleMap(String roleName, List<String> roleIdList) {
        Map<String, Object> roleMap = new HashMap<>();
        if (roleName == null || roleIdList == null) {
            return roleMap;
        }
        roleMap.put("id", roleName);
        roleMap.put("ids", roleIdList);
        return roleMap;
    }

    /**
     * 检查是否有重复的ID和Email
     *
     * @param list
     * @return
     */
    private String checkRepeatIdAndEmail(List<UserExcelData> list) {
        String checkRepeatIdResult = new String();

        List<String> allIdList = new ArrayList<>();
        List<String> allEmailList = new ArrayList<>();

        for (UserExcelData data : list) {
            allIdList.add(data.getId());
            allEmailList.add(data.getEmail());
        }
        List<String> repeatIdList = allIdList.stream()
                .collect(Collectors.toMap(e -> e, e -> 1, (a, b) -> a + b)) // 获得元素出现频率的 Map，键为元素，值为元素出现的次数
                .entrySet().stream() // Set<Entry>转换为Stream<Entry>
                .filter(entry -> entry.getValue() > 1) // 过滤出元素出现次数大于 1 的 entry
                .map(entry -> entry.getKey()) // 获得 entry 的键（重复元素）对应的 Stream
                .collect(Collectors.toList());
        if (!repeatIdList.isEmpty()) {
            checkRepeatIdResult += Translator.get("user_import_id_is_repeat") + "：";
            for (String repeatID : repeatIdList) {
                checkRepeatIdResult += repeatID + ";";
            }
        }

        List<String> repeatEmailList = allEmailList.stream()
                .collect(Collectors.toMap(e -> e, e -> 1, (a, b) -> a + b)) // 获得元素出现频率的 Map，键为元素，值为元素出现的次数
                .entrySet().stream() // Set<Entry>转换为Stream<Entry>
                .filter(entry -> entry.getValue() > 1) // 过滤出元素出现次数大于 1 的 entry
                .map(entry -> entry.getKey()) // 获得 entry 的键（重复元素）对应的 Stream
                .collect(Collectors.toList());
        if (!repeatEmailList.isEmpty()) {
            checkRepeatIdResult += Translator.get("user_import_email_is_repeat") + "：";
            for (String repeatEmail : repeatEmailList) {
                checkRepeatIdResult += repeatEmail + ";";
            }
        }

        return checkRepeatIdResult;
    }


}
