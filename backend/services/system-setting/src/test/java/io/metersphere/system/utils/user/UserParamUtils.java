package io.metersphere.system.utils.user;

import io.metersphere.sdk.constants.UserRoleScope;
import io.metersphere.sdk.constants.UserRoleType;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.system.domain.UserRole;
import io.metersphere.system.dto.request.UserInviteRequest;
import io.metersphere.system.dto.request.user.UserEditRequest;
import io.metersphere.system.dto.sdk.BasePageRequest;
import io.metersphere.system.dto.user.UserCreateInfo;
import io.metersphere.system.dto.user.UserDTO;
import io.metersphere.system.dto.user.request.UserBatchCreateRequest;
import io.metersphere.system.dto.user.response.UserImportResponse;
import io.metersphere.system.dto.user.response.UserSelectOption;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.stream.Collectors;

public class UserParamUtils {

    public static UserBatchCreateRequest getUserCreateDTO(
            List<UserSelectOption> userRoleList,
            List<UserCreateInfo> userInfoList) {
        UserBatchCreateRequest userMaintainRequest = new UserBatchCreateRequest();
        if (CollectionUtils.isNotEmpty(userRoleList)) {
            userMaintainRequest.setUserRoleIdList(
                    userRoleList.stream().map(UserSelectOption::getId).collect(Collectors.toList()));
        }
        userMaintainRequest.setUserInfoList(userInfoList);
        return userMaintainRequest;
    }

    public static UserInviteRequest getUserInviteRequest(
            List<UserSelectOption> userRoleList,
            List<String> inviteEmailList) {
        UserInviteRequest request = new UserInviteRequest();
        if (CollectionUtils.isNotEmpty(userRoleList)) {
            request.setUserRoleIds(
                    userRoleList.stream().map(UserSelectOption::getId).collect(Collectors.toList()));
        }
        request.setInviteEmails(inviteEmailList);
        return request;
    }

    public static UserEditRequest getUserUpdateDTO(UserCreateInfo user, List<UserSelectOption> userRoleList) {
        UserEditRequest returnDTO = new UserEditRequest();
        if (user.getPhone() == null) {
            user.setPhone("");
        }
        BeanUtils.copyBean(returnDTO, user);
        if (CollectionUtils.isNotEmpty(userRoleList)) {
            returnDTO.setUserRoleIdList(
                    userRoleList.stream().map(UserSelectOption::getId).collect(Collectors.toList()));
        }
        return returnDTO;
    }

    public static BasePageRequest getDefaultPageRequest() {
        return new BasePageRequest() {{
            this.setCurrent(1);
            this.setPageSize(10);
        }};
    }

    public static byte[] getFileBytes(String filePath) {
        File file = new File(filePath);
        byte[] buffer = new byte[0];
        try (FileInputStream fi = new FileInputStream(file)) {
            buffer = new byte[(int) file.length()];
            int offset = 0;
            int numRead;
            while (offset < buffer.length
                    && (numRead = fi.read(buffer, offset, buffer.length - offset)) >= 0) {
                offset += numRead;
            }
        } catch (Exception ignore) {
        }
        return buffer;
    }


    //用户信息对比
    public static void compareUserDTO(UserEditRequest editRequest, UserDTO selectUserDTO) {
        Assertions.assertNotNull(editRequest);
        Assertions.assertNotNull(selectUserDTO);
        //判断ID是否一样
        Assertions.assertTrue(StringUtils.equals(editRequest.getId(), selectUserDTO.getId()));
        //判断名称是否一样
        Assertions.assertTrue(StringUtils.equals(editRequest.getName(), selectUserDTO.getName()));
        //判断邮箱是否一样
        Assertions.assertTrue(StringUtils.equals(editRequest.getEmail(), selectUserDTO.getEmail()));
        //判断电话号码是否一样
        Assertions.assertTrue(StringUtils.equals(editRequest.getPhone(), selectUserDTO.getPhone()));
        //判断系统权限是否一样
        List<String> selectUserSystemRoleId
                = selectUserDTO.getUserRoles().stream()
                .filter(item -> (StringUtils.equalsIgnoreCase(item.getType(), UserRoleType.SYSTEM.name())
                        && StringUtils.equalsIgnoreCase(item.getScopeId(), UserRoleScope.GLOBAL)))
                .map(UserRole::getId).toList();
        Assertions.assertTrue(
                editRequest.getUserRoleIdList().containsAll(selectUserSystemRoleId)
                        && selectUserSystemRoleId.containsAll(editRequest.getUserRoleIdList()));
    }

    //导入结果判断
    public static void checkImportResponse(UserImportResponse responsePost, int successCount, int[] errorDataIndex) {
        //导入总数据是否一致
        Assertions.assertEquals(responsePost.getImportCount(), successCount + errorDataIndex.length);
        //导入成功数据是否一致
        Assertions.assertEquals(responsePost.getSuccessCount(), successCount);
        //报错数据数量是否一致
        Assertions.assertEquals(responsePost.getErrorMessages().size(), errorDataIndex.length);
        //报错数据行编码是否一致
        for (int index : errorDataIndex) {
            Assertions.assertTrue(responsePost.getErrorMessages().containsKey(index));
        }
    }
}
