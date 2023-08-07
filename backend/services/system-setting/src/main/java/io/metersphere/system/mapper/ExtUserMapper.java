package io.metersphere.system.mapper;

import io.metersphere.system.dto.UserExtend;

import java.util.List;

public interface ExtUserMapper {

    List<UserExtend> getMemberOption(String sourceId);
}
