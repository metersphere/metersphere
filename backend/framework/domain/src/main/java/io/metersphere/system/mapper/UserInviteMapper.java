package io.metersphere.system.mapper;

import io.metersphere.system.domain.UserInvite;
import io.metersphere.system.domain.UserInviteExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface UserInviteMapper {
    long countByExample(UserInviteExample example);

    int deleteByExample(UserInviteExample example);

    int deleteByPrimaryKey(String id);

    int insert(UserInvite record);

    int insertSelective(UserInvite record);

    List<UserInvite> selectByExampleWithBLOBs(UserInviteExample example);

    List<UserInvite> selectByExample(UserInviteExample example);

    UserInvite selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") UserInvite record, @Param("example") UserInviteExample example);

    int updateByExampleWithBLOBs(@Param("record") UserInvite record, @Param("example") UserInviteExample example);

    int updateByExample(@Param("record") UserInvite record, @Param("example") UserInviteExample example);

    int updateByPrimaryKeySelective(UserInvite record);

    int updateByPrimaryKeyWithBLOBs(UserInvite record);

    int updateByPrimaryKey(UserInvite record);

    int batchInsert(@Param("list") List<UserInvite> list);

    int batchInsertSelective(@Param("list") List<UserInvite> list, @Param("selective") UserInvite.Column ... selective);
}