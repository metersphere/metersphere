package io.metersphere.system.constants;

import io.metersphere.system.dto.UserViewDTO;
import io.metersphere.system.dto.sdk.CombineCondition;
import io.metersphere.system.dto.sdk.CombineSearch;

import java.util.List;
import java.util.function.Supplier;

/**
 * @Author: jianxing
 * @CreateTime: 2024-09-02  10:47
 */
public enum InternalUserView {
    ALL_DATA(() -> {
        UserViewDTO userViewDTO = getUserViewDTO("all_data");
        userViewDTO.setConditions(List.of());
        return userViewDTO;
    }),
    MY_FOLLOW(() -> {
        UserViewDTO userViewDTO = getUserViewDTO("my_follow");
        CombineCondition condition = new CombineCondition();
        condition.setName("follower");
        condition.setValue(getCurrentUserValue());
        condition.setOperator(CombineCondition.CombineConditionOperator.IN.name());
        userViewDTO.setConditions(List.of(condition));
        return userViewDTO;
    }),
    MY_CREATE(() -> {
        UserViewDTO userViewDTO = getUserViewDTO("my_create");
        CombineCondition condition = new CombineCondition();
        condition.setName("createUser");
        condition.setValue(getCurrentUserValue());
        condition.setOperator(CombineCondition.CombineConditionOperator.IN.name());
        userViewDTO.setConditions(List.of(condition));
        return userViewDTO;
    }),
    MY_REVIEW(() -> {
        UserViewDTO userViewDTO = getUserViewDTO("my_review");
        CombineCondition condition = new CombineCondition();
        condition.setName("reviewUser");
        condition.setValue(getCurrentUserValue());
        condition.setOperator(CombineCondition.CombineConditionOperator.IN.name());
        userViewDTO.setConditions(List.of(condition));
        return userViewDTO;
    });

    private static UserViewDTO getUserViewDTO(String name) {
        UserViewDTO userViewDTO = new UserViewDTO();
        userViewDTO.setSearchMode(CombineSearch.SearchMode.AND.name());
        userViewDTO.setName(name);
        userViewDTO.setId(name);
        return userViewDTO;
    }

    private UserViewDTO userView;
    public static final String CURRENT_USER = "CURRENT_USER";

    InternalUserView(Supplier<UserViewDTO> initCombineSearchFunc) {
        userView = initCombineSearchFunc.get();
    }

    public static String getCurrentUserValue() {
        return CURRENT_USER;
    }

    public UserViewDTO getUserView() {
        return userView;
    }
}
