package io.metersphere.system.constants;

import io.metersphere.sdk.constants.ValueEnum;
import io.metersphere.sdk.exception.MSException;

import java.util.List;

/**
 * @Author: jianxing
 * @CreateTime: 2024-09-02  10:47
 *
 * 视图的类型
 * 例如：功能用例视图
 *
 */
public enum UserViewType implements ValueEnum {

    FUNCTIONAL_CASE("functional-case",
            List.of(InternalUserView.ALL_DATA, InternalUserView.MY_FOLLOW, InternalUserView.MY_CREATE)),
    REVIEW_FUNCTIONAL_CASE("review-functional-case",
            List.of(InternalUserView.ALL_DATA, InternalUserView.MY_REVIEW, InternalUserView.MY_CREATE)),
    API_DEFINITION("api-definition",
            List.of(InternalUserView.ALL_DATA, InternalUserView.MY_FOLLOW, InternalUserView.MY_CREATE)),
    ;

    private String value;
    private List<InternalUserView> internalViews;

    UserViewType(String value, List<InternalUserView> internalViews) {
        this.value = value;
        this.internalViews = internalViews;
    }

    @Override
    public Object getValue() {
        return this.value;
    }

    public List<InternalUserView> getInternalViews() {
        return internalViews;
    }

    public static UserViewType getByValue(String value) {
        for (UserViewType userViewType : UserViewType.values()) {
            if (userViewType.value.equals(value)) {
                return userViewType;
            }
        }
        throw new MSException("No such view type");
    }
}
