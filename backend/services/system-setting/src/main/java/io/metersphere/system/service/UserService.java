package io.metersphere.system.service;

import io.metersphere.sdk.dto.UserDTO;
import io.metersphere.sdk.mapper.UserMapper;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.system.domain.User;
import io.metersphere.system.domain.UserExtend;
import io.metersphere.system.util.BatchSaveUtils;
import jakarta.annotation.Resource;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class UserService {
    @Resource
    private UserMapper userMapper;
    @Resource
    private JdbcAggregateTemplate jdbcAggregateTemplate;
    @Resource
    private SqlSessionFactory sqlSessionFactory;

    public boolean save(UserDTO entity) {
        User user = new User();
        BeanUtils.copyBean(user, entity);
        jdbcAggregateTemplate.insert(user);

        UserExtend userExtend = new UserExtend();
        BeanUtils.copyBean(userExtend, entity);
        jdbcAggregateTemplate.insert(userExtend);
        return true;
    }

    public UserDTO getById(String id) {
        return userMapper.selectById(id);
    }

    public List<User> list() {
        return userMapper.findAll();
    }

    public boolean batchSave(List<User> users) {
        long start = System.currentTimeMillis();
        BatchSaveUtils.batchSave(users);
        System.out.println("batch save cost: " + (System.currentTimeMillis() - start) + "ms");
        return true;
    }

    public boolean batchSave2(List<User> users) {
        long start = System.currentTimeMillis();

        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            UserMapper mapper = sqlSession.getMapper(UserMapper.class);
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
            userMapper.batchSave(users);
            System.out.println("batch save cost: " + (System.currentTimeMillis() - start) + "ms");
            return true;
        }

        for (int i = 0; i < pageSize; i++) {
            int startIndex = i * batchSize;
            List<User> sub = users.subList(startIndex, startIndex + batchSize);
            userMapper.batchSave(sub);
        }

        if (size % batchSize != 0) {
            int startIndex = pageSize * batchSize;
            List<User> sub = users.subList(startIndex, size);
            userMapper.batchSave(sub);
        }
        System.out.println("batch save cost: " + (System.currentTimeMillis() - start) + "ms");
        return true;
    }

    public long count() {
        return jdbcAggregateTemplate.count(User.class);
    }

}
