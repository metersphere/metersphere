package io.metersphere.system.service;

import com.alibaba.excel.EasyExcelFactory;
import io.metersphere.sdk.constants.HttpMethodConstants;
import io.metersphere.sdk.constants.OperationLogConstants;
import io.metersphere.sdk.dto.BasePageRequest;
import io.metersphere.sdk.dto.ExcelParseDTO;
import io.metersphere.sdk.dto.LogDTO;
import io.metersphere.sdk.dto.UserDTO;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.log.constants.OperationLogModule;
import io.metersphere.sdk.log.constants.OperationLogType;
import io.metersphere.sdk.log.service.OperationLogService;
import io.metersphere.sdk.mapper.BaseUserMapper;
import io.metersphere.sdk.util.*;
import io.metersphere.system.domain.User;
import io.metersphere.system.domain.UserExample;
import io.metersphere.system.dto.UserBatchCreateDTO;
import io.metersphere.system.dto.UserCreateInfo;
import io.metersphere.system.dto.UserExtend;
import io.metersphere.system.dto.excel.UserExcel;
import io.metersphere.system.dto.excel.UserExcelRowDTO;
import io.metersphere.system.dto.request.UserBaseBatchRequest;
import io.metersphere.system.dto.request.UserChangeEnableRequest;
import io.metersphere.system.dto.request.UserEditRequest;
import io.metersphere.system.dto.response.UserBatchProcessResponse;
import io.metersphere.system.dto.response.UserImportResponse;
import io.metersphere.system.dto.response.UserTableResponse;
import io.metersphere.system.mapper.ExtUserMapper;
import io.metersphere.system.mapper.UserMapper;
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

    //批量添加用户记录日志
    public List<LogDTO> getBatchAddLogs(@Valid List<User> userList) {
        List<LogDTO> logs = new ArrayList<>();
        userList.forEach(user -> {
            LogDTO log = new LogDTO();
            log.setId(UUID.randomUUID().toString());
            log.setCreateUser(user.getCreateUser());
            log.setProjectId(OperationLogConstants.SYSTEM);
            log.setOrganizationId(OperationLogConstants.SYSTEM);
            log.setType(OperationLogType.ADD.name());
            log.setModule(OperationLogModule.SYSTEM_USER);
            log.setMethod("addUser");
            log.setCreateTime(user.getCreateTime());
            log.setSourceId(user.getId());
            log.setContent(user.getName() + "(" + user.getEmail() + ")");
            log.setOriginalValue(JSON.toJSONBytes(user));
            logs.add(log);
        });
        return logs;
    }

    public List<User> selectByIdList(@NotEmpty List<String> userIdList) {
        UserExample example = new UserExample();
        example.createCriteria().andIdIn(userIdList);
        return userMapper.selectByExample(example);
    }

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
        operationLogService.batchAdd(this.getBatchAddLogs(saveUserList));
        return userCreateDTO;
    }


    public UserDTO getUserDTOByEmail(String email) {
        UserDTO userDTO = baseUserMapper.selectByEmail(email);
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

    public UserBatchProcessResponse updateUserEnable(UserChangeEnableRequest request, String operator) {
        request.setUserIds(this.getBatchUserIds(request));
        this.checkUserInDb(request.getUserIds());
        UserBatchProcessResponse response = new UserBatchProcessResponse();
        response.setTotalCount(request.getUserIds().size());
        UserExample userExample = new UserExample();
        userExample.createCriteria().andIdIn(
                request.getUserIds()
        );
        User updateUser = new User();
        updateUser.setEnable(request.isEnable());
        updateUser.setUpdateUser(operator);
        updateUser.setUpdateTime(System.currentTimeMillis());
        response.setSuccessCount(userMapper.updateByExampleSelective(updateUser, userExample));
        response.setProcessedIds(request.getUserIds());
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


    public UserBatchProcessResponse deleteUser(@Valid UserBaseBatchRequest request, String operator) {
        List<String> userIdList = this.getBatchUserIds(request);
        this.checkUserInDb(userIdList);
        //检查是否含有Admin
        this.checkAdminAndThrowException(userIdList);
        UserExample userExample = new UserExample();
        userExample.createCriteria().andIdIn(userIdList);
        //更新删除标志位
        UserBatchProcessResponse response = new UserBatchProcessResponse();
        response.setTotalCount(userIdList.size());
        response.setProcessedIds(userIdList);
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

    public LogDTO updateLog(UserEditRequest request) {
        User user = userMapper.selectByPrimaryKey(request.getId());
        if (user != null) {
            LogDTO dto = new LogDTO(
                    OperationLogConstants.SYSTEM,
                    OperationLogConstants.SYSTEM,
                    request.getId(),
                    null,
                    OperationLogType.UPDATE.name(),
                    OperationLogModule.SYSTEM_USER,
                    JSON.toJSONString(user));
            dto.setPath("/update");
            dto.setMethod(HttpMethodConstants.POST.name());
            dto.setOriginalValue(JSON.toJSONBytes(user));
            return dto;
        }
        return null;
    }

    public List<LogDTO> batchUpdateLog(UserBaseBatchRequest request) {
        List<LogDTO> logDTOList = new ArrayList<>();
        request.setUserIds(this.getBatchUserIds(request));
        List<User> userList = this.selectByIdList(request.getUserIds());
        for (User user : userList) {
            LogDTO dto = new LogDTO(
                    OperationLogConstants.SYSTEM,
                    OperationLogConstants.SYSTEM,
                    user.getId(),
                    null,
                    OperationLogType.UPDATE.name(),
                    OperationLogModule.SYSTEM_USER,
                    JSON.toJSONString(user));
            dto.setMethod(HttpMethodConstants.POST.name());
            dto.setOriginalValue(JSON.toJSONBytes(user));
            logDTOList.add(dto);
        }
        return logDTOList;
    }

    /**
     * @param request 批量重置密码  用于记录Log使用
     */
    public List<LogDTO> resetPasswordLog(UserBaseBatchRequest request) {
        request.setUserIds(this.getBatchUserIds(request));
        List<LogDTO> returnList = new ArrayList<>();
        UserExample example = new UserExample();
        example.createCriteria().andIdIn(request.getUserIds());
        List<User> userList = userMapper.selectByExample(example);
        for (User user : userList) {
            LogDTO dto = new LogDTO(
                    OperationLogConstants.SYSTEM,
                    OperationLogConstants.SYSTEM,
                    user.getId(),
                    null,
                    OperationLogType.UPDATE.name(),
                    OperationLogModule.SYSTEM_USER,
                    user.getName());
            dto.setPath("/reset/password");
            dto.setMethod(HttpMethodConstants.POST.name());
            dto.setOriginalValue(JSON.toJSONBytes(user));
            returnList.add(dto);
        }
        return returnList;
    }

    public List<LogDTO> deleteLog(UserBaseBatchRequest request) {
        List<LogDTO> logDTOList = new ArrayList<>();
        request.getUserIds().forEach(item -> {
            User user = userMapper.selectByPrimaryKey(item);
            if (user != null) {

                LogDTO dto = new LogDTO(
                        OperationLogConstants.SYSTEM,
                        OperationLogConstants.SYSTEM,
                        user.getId(),
                        user.getCreateUser(),
                        OperationLogType.DELETE.name(),
                        OperationLogModule.SYSTEM_PROJECT,
                        user.getName());

                dto.setPath("/delete");
                dto.setMethod(HttpMethodConstants.POST.name());
                dto.setOriginalValue(JSON.toJSONBytes(user));
                logDTOList.add(dto);
            }
        });
        return logDTOList;
    }

    public List<User> getUserList() {
        UserExample example = new UserExample();
        example.setOrderByClause("update_time desc");
        return userMapper.selectByExample(example);
    }

    public List<UserExtend> getMemberOption(String sourceId) {
        return extUserMapper.getMemberOption(sourceId);
    }

    public UserBatchProcessResponse resetPassword(UserBaseBatchRequest request, String operator) {
        request.setUserIds(this.getBatchUserIds(request));
        this.checkUserInDb(request.getUserIds());

        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        UserMapper batchUpdateMapper = sqlSession.getMapper(UserMapper.class);
        int insertIndex = 0;
        long updateTime = System.currentTimeMillis();
        List<User> userList = this.selectByIdList(request.getUserIds());
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

        UserBatchProcessResponse response = new UserBatchProcessResponse();
        response.setTotalCount(request.getUserIds().size());
        response.setSuccessCount(request.getUserIds().size());
        response.setProcessedIds(request.getUserIds());
        return response;
    }

    public void checkUserLegality(List<String> userIds) {
        UserExample example = new UserExample();
        example.createCriteria().andIdIn(userIds);
        if (userMapper.countByExample(example) != userIds.size()) {
            throw new MSException(Translator.get("user.id.not.exist"));
        }
    }

    public List<String> getBatchUserIds(UserBaseBatchRequest request) {
        if (request.isSelectAll()) {
            List<User> userList = baseUserMapper.selectByKeyword(request.getCondition().getKeyword(), true);
            List<String> userIdList = userList.stream().map(User::getId).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(request.getSkipIds())) {
                userIdList.removeAll(request.getSkipIds());
            }
            return userIdList;
        } else {
            return request.getUserIds();
        }
    }
}
