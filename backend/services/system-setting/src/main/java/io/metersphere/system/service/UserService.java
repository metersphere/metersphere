package io.metersphere.system.service;

import com.alibaba.excel.EasyExcelFactory;
import io.metersphere.sdk.dto.*;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.log.service.OperationLogService;
import io.metersphere.sdk.mapper.BaseUserMapper;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.CodingUtil;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.domain.User;
import io.metersphere.system.domain.UserExample;
import io.metersphere.system.dto.UserBatchCreateDTO;
import io.metersphere.system.dto.UserCreateInfo;
import io.metersphere.system.dto.UserExtend;
import io.metersphere.system.dto.excel.UserExcel;
import io.metersphere.system.dto.excel.UserExcelRowDTO;
import io.metersphere.system.mapper.ExtUserMapper;
import io.metersphere.system.mapper.UserMapper;
import io.metersphere.system.request.user.UserChangeEnableRequest;
import io.metersphere.system.request.user.UserEditRequest;
import io.metersphere.system.response.user.UserImportResponse;
import io.metersphere.system.response.user.UserTableResponse;
import io.metersphere.system.utils.UserImportEventListener;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class UserService {
    @Resource
    private BaseUserMapper baseUserMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private ExtUserMapper extUserMapper;
    @Resource
    private UserRoleRelationService userRoleRelationService;
    @Resource
    private OperationLogService operationLogService;
    @Resource
    private GlobalUserRoleService globalUserRoleService;
    @Resource
    private UserRoleService userRoleService;
    @Resource
    private SqlSessionFactory sqlSessionFactory;

    @Resource
    private UserLogService userLogService;
    @Resource
    private UserToolService userToolService;

    private void validateUserInfo(List<UserCreateInfo> userList) {
        //判断参数内是否含有重复邮箱
        List<String> emailList = new ArrayList<>();
        List<String> repeatEmailList = new ArrayList<>();
        var userInDbMap = baseUserMapper.selectUserIdByEmailList(
                        userList.stream().map(UserCreateInfo::getEmail).collect(Collectors.toList()))
                .stream().collect(Collectors.toMap(User::getEmail, User::getId));
        for (UserCreateInfo user : userList) {
            if (emailList.contains(user.getEmail())) {
                repeatEmailList.add(user.getEmail());
            } else {
                //判断邮箱是否已存在数据库中
                if (userInDbMap.containsKey(user.getEmail())) {
                    repeatEmailList.add(user.getEmail());
                } else {
                    emailList.add(user.getEmail());
                }
            }
        }
        if (CollectionUtils.isNotEmpty(repeatEmailList)) {
            throw new MSException(Translator.get("user.email.repeat", repeatEmailList.toString()));
        }
    }

    public UserBatchCreateDTO addUser(UserBatchCreateDTO userCreateDTO, String source, String operator) {
        this.validateUserInfo(userCreateDTO.getUserInfoList());
        globalUserRoleService.checkRoleIsGlobalAndHaveMember(userCreateDTO.getUserRoleIdList(), true);
        return this.saveUserAndRole(userCreateDTO, source, operator);
    }

    private UserBatchCreateDTO saveUserAndRole(UserBatchCreateDTO userCreateDTO, String source, String operator) {
        long createTime = System.currentTimeMillis();
        List<User> saveUserList = new ArrayList<>();
        //添加用户
        for (UserCreateInfo userInfo : userCreateDTO.getUserInfoList()) {
            userInfo.setId(UUID.randomUUID().toString());
            User user = new User();
            BeanUtils.copyBean(user, userInfo);
            user.setCreateUser(operator);
            user.setCreateTime(createTime);
            user.setUpdateUser(operator);
            user.setUpdateTime(createTime);
            user.setPassword(CodingUtil.md5(user.getEmail()));
            user.setSource(source);
            user.setDeleted(false);
            userMapper.insertSelective(user);
            saveUserList.add(user);
        }
        userRoleRelationService.batchSave(userCreateDTO.getUserRoleIdList(), saveUserList);
        //写入操作日志
        operationLogService.batchAdd(userLogService.getBatchAddLogs(saveUserList));
        return userCreateDTO;
    }


    public UserDTO getUserDTOByKeyword(String email) {
        UserDTO userDTO = baseUserMapper.selectDTOByKeyword(email);
        if (userDTO != null) {
            userDTO.setUserRoleRelations(
                    userRoleRelationService.selectByUserId(userDTO.getId())
            );
            userDTO.setUserRoles(
                    userRoleService.selectByUserRoleRelations(userDTO.getUserRoleRelations())
            );
        }
        return userDTO;
    }

    public List<UserTableResponse> list(BasePageRequest request) {
        List<UserTableResponse> returnList = new ArrayList<>();
        List<User> userList = baseUserMapper.selectByKeyword(request.getKeyword(), false);
        if (CollectionUtils.isNotEmpty(userList)) {
            List<String> userIdList = userList.stream().map(User::getId).collect(Collectors.toList());
            Map<String, UserTableResponse> roleAndOrganizationMap = userRoleRelationService.selectGlobalUserRoleAndOrganization(userIdList);
            for (User user : userList) {
                UserTableResponse userInfo = new UserTableResponse();
                BeanUtils.copyBean(userInfo, user);
                UserTableResponse roleOrgModel = roleAndOrganizationMap.get(user.getId());
                if (roleOrgModel != null) {
                    userInfo.setUserRoleList(roleOrgModel.getUserRoleList());
                    userInfo.setOrganizationList(roleOrgModel.getOrganizationList());
                }
                returnList.add(userInfo);
            }
        }
        return returnList;
    }

    public UserEditRequest updateUser(UserEditRequest userEditRequest, String operator) {
        //检查用户组合法性
        globalUserRoleService.checkRoleIsGlobalAndHaveMember(userEditRequest.getUserRoleIdList(), true);
        User user = new User();
        BeanUtils.copyBean(user, userEditRequest);
        user.setUpdateUser(operator);
        user.setUpdateTime(System.currentTimeMillis());
        user.setCreateUser(null);
        user.setCreateTime(null);
        userMapper.updateByPrimaryKeySelective(user);
        userRoleRelationService.updateUserSystemGlobalRole(user, user.getUpdateUser(), userEditRequest.getUserRoleIdList());
        return userEditRequest;
    }

    public TableBatchProcessResponse updateUserEnable(UserChangeEnableRequest request, String operator) {
        request.setSelectIds(userToolService.getBatchUserIds(request));
        this.checkUserInDb(request.getSelectIds());
        TableBatchProcessResponse response = new TableBatchProcessResponse();
        response.setTotalCount(request.getSelectIds().size());
        UserExample userExample = new UserExample();
        userExample.createCriteria().andIdIn(
                request.getSelectIds()
        );
        User updateUser = new User();
        updateUser.setEnable(request.isEnable());
        updateUser.setUpdateUser(operator);
        updateUser.setUpdateTime(System.currentTimeMillis());
        response.setSuccessCount(userMapper.updateByExampleSelective(updateUser, userExample));
        return response;
    }

    private void checkUserInDb(List<String> userIdList) {
        if (CollectionUtils.isEmpty(userIdList)) {
            throw new MSException(Translator.get("user.not.exist"));
        }
        List<String> userInDb = baseUserMapper.selectUnDeletedUserIdByIdList(userIdList);
        if (userIdList.size() != userInDb.size()) {
            throw new MSException(Translator.get("user.not.exist"));
        }
    }

    public UserImportResponse importByExcel(MultipartFile excelFile, String source, String sessionId) {
        UserImportResponse importResponse = new UserImportResponse();
        try {
            ExcelParseDTO<UserExcelRowDTO> excelParseDTO = this.getUserExcelParseDTO(excelFile);
            if (CollectionUtils.isNotEmpty(excelParseDTO.getDataList())) {
                this.saveUserByExcelData(excelParseDTO.getDataList(), source, sessionId);
            }
            importResponse.generateResponse(excelParseDTO);
        } catch (Exception e) {
            LogUtils.info("import user  error", e);
        }
        return importResponse;
    }

    public ExcelParseDTO<UserExcelRowDTO> getUserExcelParseDTO(MultipartFile excelFile) throws Exception {
        UserImportEventListener userImportEventListener = new UserImportEventListener();
        EasyExcelFactory.read(excelFile.getInputStream(), UserExcel.class, userImportEventListener).sheet().doRead();
        return this.validateExcelUserInfo(userImportEventListener.getExcelParseDTO());
    }

    /**
     * 校验excel导入的数据是否与数据库中的数据冲突
     *
     */
    private ExcelParseDTO<UserExcelRowDTO> validateExcelUserInfo(@Valid @NotNull ExcelParseDTO<UserExcelRowDTO> excelParseDTO) {
        List<UserExcelRowDTO> prepareSaveList = excelParseDTO.getDataList();
        if (CollectionUtils.isNotEmpty(prepareSaveList)) {
            var userInDbMap = baseUserMapper.selectUserIdByEmailList(
                            prepareSaveList.stream().map(UserExcelRowDTO::getEmail).collect(Collectors.toList()))
                    .stream().collect(Collectors.toMap(User::getEmail, User::getId));
            for (UserExcelRowDTO userExcelRow : prepareSaveList) {
                //判断邮箱是否已存在数据库中
                if (userInDbMap.containsKey(userExcelRow.getEmail())) {
                    userExcelRow.setErrorMessage(Translator.get("user.email.repeat") + ": " + userExcelRow.getEmail());
                    excelParseDTO.addErrorRowData(userExcelRow.getDataIndex(), userExcelRow);
                }
            }
            excelParseDTO.getDataList().removeAll(excelParseDTO.getErrRowData().values());
        }
        return excelParseDTO;
    }

    private void saveUserByExcelData(@Valid @NotEmpty List<UserExcelRowDTO> dataList, @Valid @NotEmpty String source, @Valid @NotBlank String sessionId) {
        UserBatchCreateDTO userBatchCreateDTO = new UserBatchCreateDTO();
        userBatchCreateDTO.setUserRoleIdList(new ArrayList<>() {{
            add("member");
        }});
        List<UserCreateInfo> userCreateInfoList = new ArrayList<>();
        dataList.forEach(userExcelRowDTO -> {
            UserCreateInfo userCreateInfo = new UserCreateInfo();
            BeanUtils.copyBean(userCreateInfo, userExcelRowDTO);
            userCreateInfoList.add(userCreateInfo);
        });
        userBatchCreateDTO.setUserInfoList(userCreateInfoList);
        this.saveUserAndRole(userBatchCreateDTO, source, sessionId);
    }


    public TableBatchProcessResponse deleteUser(@Valid TableBatchProcessDTO request, String operator) {
        List<String> userIdList = userToolService.getBatchUserIds(request);
        this.checkUserInDb(userIdList);
        //检查是否含有Admin
        this.checkAdminAndThrowException(userIdList);
        UserExample userExample = new UserExample();
        userExample.createCriteria().andIdIn(userIdList);
        //更新删除标志位
        TableBatchProcessResponse response = new TableBatchProcessResponse();
        response.setTotalCount(userIdList.size());
        response.setSuccessCount(this.deleteUserByList(userIdList, operator));
        //删除用户角色关系
        userRoleRelationService.deleteByUserIdList(userIdList);
        return response;
    }

    private void checkAdminAndThrowException(List<String> userIdList) {
        for (String userId : userIdList) {
            if (userId.equals("admin")) {
                throw new MSException(Translator.get("user.not.delete"));
            }
        }
    }

    private int deleteUserByList(List<String> updateUserList, String operator) {
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        BaseUserMapper batchDeleteMapper = sqlSession.getMapper(BaseUserMapper.class);
        int insertIndex = 0;
        long deleteTime = System.currentTimeMillis();
        for (String userId : updateUserList) {
            batchDeleteMapper.deleteUser(userId, operator, deleteTime);
            insertIndex++;
            if (insertIndex % 50 == 0) {
                sqlSession.flushStatements();
            }
        }
        sqlSession.flushStatements();
        SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
        return insertIndex;
    }

    public List<User> getUserList(BasePageRequest request) {
        return extUserMapper.selectUserList(request);
    }

    /**
     * 获取组织, 项目系统成员选项
     *
     * @param sourceId 组织ID, 项目ID
     * @return 系统用户选项
     */
    public List<UserExtend> getMemberOption(String sourceId) {
        return extUserMapper.getMemberOption(sourceId);
    }

    public TableBatchProcessResponse resetPassword(TableBatchProcessDTO request, String operator) {
        request.setSelectIds(userToolService.getBatchUserIds(request));
        this.checkUserInDb(request.getSelectIds());

        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        UserMapper batchUpdateMapper = sqlSession.getMapper(UserMapper.class);
        int insertIndex = 0;
        long updateTime = System.currentTimeMillis();
        List<User> userList = userToolService.selectByIdList(request.getSelectIds());
        for (User user : userList) {
            User updateModel = new User();
            updateModel.setId(user.getId());
            if (StringUtils.equalsIgnoreCase("admin", user.getId())) {
                updateModel.setPassword(CodingUtil.md5("metersphere"));
            } else {
                updateModel.setPassword(CodingUtil.md5(user.getEmail()));
            }
            updateModel.setUpdateTime(updateTime);
            updateModel.setUpdateUser(operator);
            batchUpdateMapper.updateByPrimaryKeySelective(updateModel);
            insertIndex++;
            if (insertIndex % 50 == 0) {
                sqlSession.flushStatements();
            }
        }
        sqlSession.flushStatements();
        SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);

        TableBatchProcessResponse response = new TableBatchProcessResponse();
        response.setTotalCount(request.getSelectIds().size());
        response.setSuccessCount(request.getSelectIds().size());
        return response;
    }

    public void checkUserLegality(List<String> userIds) {
        if (CollectionUtils.isEmpty(userIds)) {
            throw new MSException(Translator.get("user.not.exist"));
        }
        UserExample example = new UserExample();
        example.createCriteria().andIdIn(userIds);
        if (userMapper.countByExample(example) != userIds.size()) {
            throw new MSException(Translator.get("user.id.not.exist"));
        }
    }


    public List<User> getUserListByOrgId(String organizationId) {
        return extUserMapper.getUserListByOrgId(organizationId);
    }
}
