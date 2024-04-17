package io.metersphere.project.service;

import io.metersphere.project.domain.FakeError;
import io.metersphere.project.domain.FakeErrorExample;
import io.metersphere.project.domain.Project;
import io.metersphere.project.dto.FakeErrorDTO;
import io.metersphere.project.dto.filemanagement.request.FakeErrorDelRequest;
import io.metersphere.project.dto.filemanagement.request.FakeErrorRequest;
import io.metersphere.project.dto.filemanagement.request.FakeErrorStatusRequest;
import io.metersphere.project.dto.filemanagement.response.FakeErrorResponse;
import io.metersphere.project.mapper.ExtFakeErrorMapper;
import io.metersphere.project.mapper.FakeErrorMapper;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.sdk.constants.HttpMethodConstants;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.controller.handler.ResultHolder;
import io.metersphere.system.dto.table.TableBatchProcessDTO;
import io.metersphere.system.log.constants.OperationLogModule;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.log.dto.LogDTO;
import io.metersphere.system.log.service.OperationLogService;
import io.metersphere.system.service.UserLoginService;
import io.metersphere.system.uid.IDGenerator;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(rollbackFor = Exception.class)
public class FakeErrorService {

    @Resource
    private FakeErrorMapper fakeErrorMapper;
    @Resource
    private ProjectMapper projectMapper;

    @Resource
    private OperationLogService operationLogService;

    @Resource
    private ExtFakeErrorMapper extFakeErrorMapper;
    @Resource
    private UserLoginService userLoginService;

    private static final String ADD_TYPE = "ADD";

    private static final String UPDATE_TYPE = "UPDATE";


    /**
     * 保存误报规则
     *
     * @param dto
     * @return
     */
    public ResultHolder save(List<FakeErrorDTO> dto, String userId) {
        List<FakeErrorResponse> responseList = checkNameRepeat(dto, ADD_TYPE);
        if (CollectionUtils.isNotEmpty(responseList)) {
            return ResultHolder.success(responseList);
        }
        doSave(dto, userId);
        return ResultHolder.success(responseList);
    }

    private void doSave(List<FakeErrorDTO> dto, String userId) {
        dto.forEach(item -> {
            FakeError fakeError = new FakeError();
            BeanUtils.copyBean(fakeError, item);
            fakeError.setId(IDGenerator.nextStr());
            fakeError.setCreateTime(System.currentTimeMillis());
            fakeError.setUpdateTime(System.currentTimeMillis());
            fakeError.setCreateUser(userId);
            fakeError.setUpdateUser(userId);
            fakeErrorMapper.insertSelective(fakeError);
            doSaveLog(fakeError, userId);
        });
    }


    private List<FakeErrorResponse> checkNameRepeat(List<FakeErrorDTO> dto, String type) {
        List<FakeErrorResponse> responseList = new ArrayList<>();
        Map<String, FakeErrorDTO> nameMap = new HashMap<>();
        dto.forEach(item -> {
            if (nameMap.containsKey(item.getName())) {
                addRepeatResponse(responseList, item);
                return;
            }
            nameMap.put(item.getName(), item);

            FakeErrorExample example = new FakeErrorExample();
            FakeErrorExample.Criteria criteria = example.createCriteria();
            criteria.andProjectIdEqualTo(item.getProjectId()).andNameEqualTo(item.getName());
            if (StringUtils.isNotBlank(item.getId()) && StringUtils.equalsIgnoreCase(type, UPDATE_TYPE)) {
                criteria.andIdNotEqualTo(item.getId());
            }
            if (fakeErrorMapper.countByExample(example) > 0) {
                addRepeatResponse(responseList, item);
            }
        });
        return responseList;
    }

    private void addRepeatResponse(List<FakeErrorResponse> responseList, FakeErrorDTO item) {
        FakeErrorResponse fakeErrorResponse = BeanUtils.copyBean(new FakeErrorResponse(), item);
        responseList.add(fakeErrorResponse);
    }


    /**
     * 获取误报列表
     *
     * @param request
     * @return
     */
    public List<FakeError> list(FakeErrorRequest request) {
        FakeErrorExample example = new FakeErrorExample();
        FakeErrorExample.Criteria criteria = example.createCriteria();
        criteria.andProjectIdEqualTo(request.getProjectId());
        if (StringUtils.isNotBlank(request.getKeyword())) {
            criteria.andNameLike("%" + request.getKeyword() + "%");
        }
        List<FakeError> fakeErrors = fakeErrorMapper.selectByExample(example);
        //把createUser和updateUser提取出来生成新的set
        List<String> userIds = new ArrayList<>();
        userIds.addAll(fakeErrors.stream().map(FakeError::getCreateUser).toList());
        userIds.addAll(fakeErrors.stream().map(FakeError::getUpdateUser).toList());
        Map<String, String> userMap = userLoginService.getUserNameMap(userIds.stream().filter(StringUtils::isNotBlank).distinct().toList());
        fakeErrors.forEach(fakeError -> {
            fakeError.setCreateUser(userMap.get(fakeError.getCreateUser()));
            fakeError.setUpdateUser(userMap.get(fakeError.getUpdateUser()));
        });

        return fakeErrors;
    }


    /**
     * 更新误报规则
     *
     * @param dto
     * @param userId
     * @return
     */
    public ResultHolder update(List<FakeErrorDTO> dto, String userId) {
        List<FakeErrorResponse> responseList = checkNameRepeat(dto, UPDATE_TYPE);
        if (CollectionUtils.isNotEmpty(responseList)) {
            return ResultHolder.success(responseList);
        }
        doUpdate(dto, userId);
        return ResultHolder.success(responseList);
    }

    private void doUpdate(List<FakeErrorDTO> dto, String userId) {
        dto.forEach(item -> {
            FakeError fakeError = new FakeError();
            BeanUtils.copyBean(fakeError, item);
            fakeError.setUpdateUser(userId);
            fakeError.setUpdateTime(System.currentTimeMillis());
            fakeErrorMapper.updateByPrimaryKeySelective(fakeError);
            updateLog(fakeError, userId);
        });
    }


    /**
     * 删除误报规则
     *
     * @param request
     * @return
     */
    public void delete(FakeErrorDelRequest request) {
        List<String> ids = doSelectIds(request);
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }
        FakeErrorExample example = new FakeErrorExample();
        example.createCriteria().andIdIn(ids);
        fakeErrorMapper.deleteByExample(example);
    }


    /**
     * 保存日志
     *
     * @param fakeError
     * @return
     */
    private void doSaveLog(FakeError fakeError, String userId) {
        Project project = projectMapper.selectByPrimaryKey(fakeError.getProjectId());
        LogDTO dto = new LogDTO(
                fakeError.getProjectId(),
                project.getOrganizationId(),
                fakeError.getId(),
                userId,
                OperationLogType.ADD.name(),
                OperationLogModule.PROJECT_MANAGEMENT_PERMISSION_MENU_MANAGEMENT_FAKE_ERROR,
                fakeError.getName());
        dto.setMethod(HttpMethodConstants.POST.name());
        dto.setPath("/fake/error/add");
        dto.setModifiedValue(JSON.toJSONBytes(fakeError));
        operationLogService.add(dto);
    }


    /**
     * 更新日志
     *
     * @param fakeError
     * @return
     */
    public void updateLog(FakeError fakeError, String userId) {
        Project project = projectMapper.selectByPrimaryKey(fakeError.getProjectId());
        FakeError originalValue = fakeErrorMapper.selectByPrimaryKey(fakeError.getId());
        LogDTO dto = new LogDTO(
                fakeError.getProjectId(),
                project.getOrganizationId(),
                fakeError.getId(),
                userId,
                OperationLogType.UPDATE.name(),
                OperationLogModule.PROJECT_MANAGEMENT_PERMISSION_MENU_MANAGEMENT_FAKE_ERROR,
                fakeError.getName());
        dto.setMethod(HttpMethodConstants.POST.name());
        dto.setPath("/fake/error/update");
        dto.setOriginalValue(JSON.toJSONBytes(fakeError));
        dto.setOriginalValue(JSON.toJSONBytes(originalValue));
        operationLogService.add(dto);
    }


    /**
     * 删除日志
     *
     * @param request
     * @return
     */
    public List<LogDTO> deleteLog(FakeErrorDelRequest request) {
        List<String> ids = doSelectIds(request);
        if (CollectionUtils.isEmpty(ids)) {
            return new ArrayList<>();
        }
        Project project = projectMapper.selectByPrimaryKey(request.getProjectId());
        FakeErrorExample example = new FakeErrorExample();
        example.createCriteria().andIdIn(ids).andProjectIdEqualTo(request.getProjectId());
        List<FakeError> fakeErrors = fakeErrorMapper.selectByExample(example);
        List<LogDTO> logs = new ArrayList<>();
        fakeErrors.forEach(fakeError -> {
            LogDTO dto = new LogDTO(
                    fakeError.getProjectId(),
                    project.getOrganizationId(),
                    fakeError.getId(),
                    null,
                    OperationLogType.UPDATE.name(),
                    OperationLogModule.PROJECT_MANAGEMENT_PERMISSION_MENU_MANAGEMENT_FAKE_ERROR,
                    fakeError.getName());
            dto.setOriginalValue(JSON.toJSONBytes(fakeError));
            logs.add(dto);
        });
        return logs;
    }


    /**
     * 更新状态
     *
     * @param request
     * @param userId
     * @return
     */
    public void updateEnable(FakeErrorStatusRequest request, String userId) {
        List<String> ids = doSelectIds(request);
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }
        FakeErrorExample example = new FakeErrorExample();
        example.createCriteria().andIdIn(ids).andProjectIdEqualTo(request.getProjectId());
        FakeError fakeError = new FakeError();
        fakeError.setEnable(request.getEnable());
        fakeError.setUpdateUser(userId);
        fakeError.setUpdateTime(System.currentTimeMillis());
        fakeErrorMapper.updateByExampleSelective(fakeError, example);
    }

    private <T> List<String> doSelectIds(T dto) {
        TableBatchProcessDTO request = (TableBatchProcessDTO) dto;
        if (request.isSelectAll()) {
            List<String> ids = extFakeErrorMapper.selectByKeyword(request.getCondition().getKeyword());
            if (CollectionUtils.isNotEmpty(request.getExcludeIds())) {
                ids.removeAll(request.getExcludeIds());
            }
            return ids;
        } else {
            return request.getSelectIds();
        }
    }


    public List<LogDTO> batchUpdateEnableLog(FakeErrorStatusRequest request) {
        List<String> ids = doSelectIds(request);
        if (CollectionUtils.isEmpty(ids)) {
            return new ArrayList<>();
        }
        Project project = projectMapper.selectByPrimaryKey(request.getProjectId());
        FakeErrorExample example = new FakeErrorExample();
        example.createCriteria().andIdIn(ids).andProjectIdEqualTo(request.getProjectId());
        List<FakeError> fakeErrors = fakeErrorMapper.selectByExample(example);
        List<LogDTO> logs = new ArrayList<>();
        fakeErrors.forEach(fakeError -> {
            LogDTO dto = new LogDTO(
                    fakeError.getProjectId(),
                    project.getOrganizationId(),
                    fakeError.getId(),
                    null,
                    OperationLogType.UPDATE.name(),
                    OperationLogModule.PROJECT_MANAGEMENT_PERMISSION_MENU_MANAGEMENT_FAKE_ERROR,
                    fakeError.getName());
            dto.setOriginalValue(JSON.toJSONBytes(fakeError));
            logs.add(dto);
        });
        return logs;
    }
}
