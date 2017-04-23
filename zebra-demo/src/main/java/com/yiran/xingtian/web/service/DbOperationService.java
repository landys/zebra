package com.yiran.xingtian.web.service;

import com.yiran.xingtian.common.model.User;
import com.yiran.xingtian.common.util.IdGenUtil;
import com.yiran.xingtian.web.mapper.UserMapper;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by Xingtian on 2017-01-13.
 */
@Service
public class DbOperationService {
    @Autowired
    private UserMapper userMapper;

    public User queryUserById(Long uid) {
        if (uid == null || uid <= 0) {
            return null;
        }

        return userMapper.findByUid(uid);
    }

    public int insertRandomUser() {
        long uid = IdGenUtil.generateUid();
        String name = RandomStringUtils.randomAlphanumeric(10);

        User user = new User(null, uid, name);

        return userMapper.insert(user);
    }

    public List<User> queryAllUsersByIdRange(Long beginUid, Long endUid) {
        if (beginUid == null || endUid == null) {
            return null;
        }

        return userMapper.findAllByUidRange(beginUid, endUid);
    }

    public int updateUsersUids(List<Long> uids) {
        return userMapper.updateUsersUids(uids);
    }

    public int updateUsersBatch(List<User> users) {
        return userMapper.updateUsersBatch(users);
    }

    @Transactional
    public int testTransaction() {
        User user0 = new User(null, 8L, RandomStringUtils.randomAlphanumeric(10));
        User user1 = new User(null, 12L, RandomStringUtils.randomAlphanumeric(10));

        int result0 = userMapper.insert(user0);
        int result1 = userMapper.insert(user1);

        if (result0 == 1 && result1 == 1) {
            throw new RuntimeException("hello world");
        }

        return result0 + result1;
    }
}
