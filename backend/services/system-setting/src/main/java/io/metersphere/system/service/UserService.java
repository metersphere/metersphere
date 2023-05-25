package io.metersphere.system.service;

import io.metersphere.sdk.dto.UserDTO;
import io.metersphere.sdk.mapper.BaseUserMapper;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.system.domain.User;
import io.metersphere.system.domain.UserExample;
import io.metersphere.system.domain.UserExtend;
import io.metersphere.system.mapper.UserExtendMapper;
import io.metersphere.system.mapper.UserMapper;
import jakarta.annotation.Resource;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class UserService {
    @Resource
    private BaseUserMapper baseUserMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private UserExtendMapper userExtendMapper;
    @Resource
    private SqlSessionFactory sqlSessionFactory;

    public boolean save(UserDTO entity) {
        userMapper.insert(entity);

        UserExtend userExtend = new UserExtend();
        BeanUtils.copyBean(userExtend, entity);
        userExtendMapper.insert(userExtend);
        return true;
    }

    public UserDTO getById(String id) {
        return baseUserMapper.selectById(id);
    }

    public List<User> list() {
        return baseUserMapper.findAll();
    }

    public boolean batchSave2(List<User> users) {
        long start = System.currentTimeMillis();

        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            BaseUserMapper mapper = sqlSession.getMapper(BaseUserMapper.class);
            for (int i = 0, size = users.size(); i < size; i++) {
                mapper.insert(users.get(i));
                if (i % 100 == 0) {
                    sqlSession.flushStatements();
                }
            }
            sqlSession.flushStatements();
        }
        System.out.println("batch save cost: " + (System.currentTimeMillis() - start) + "ms");
        return true;
    }

    public boolean batchSave3(List<User> users) {

        long start = System.currentTimeMillis();
        int batchSize = 100;
        int size = users.size();
        int pageSize = size / batchSize;
        if (pageSize == 0) {
            baseUserMapper.batchSave(users);
            System.out.println("batch save cost: " + (System.currentTimeMillis() - start) + "ms");
            return true;
        }

        for (int i = 0; i < pageSize; i++) {
            int startIndex = i * batchSize;
            List<User> sub = users.subList(startIndex, startIndex + batchSize);
            baseUserMapper.batchSave(sub);
        }

        if (size % batchSize != 0) {
            int startIndex = pageSize * batchSize;
            List<User> sub = users.subList(startIndex, size);
            baseUserMapper.batchSave(sub);
        }
        System.out.println("batch save cost: " + (System.currentTimeMillis() - start) + "ms");
        return true;
    }

    public long count() {
        return userMapper.countByExample(new UserExample());
    }

}
