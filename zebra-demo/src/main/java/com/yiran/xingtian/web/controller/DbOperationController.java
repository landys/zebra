package com.yiran.xingtian.web.controller;

import com.yiran.xingtian.common.model.User;
import com.yiran.xingtian.web.service.DbOperationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Xingtian on 2017-01-13.
 */
@Controller
public final class DbOperationController {
    @Autowired
    private DbOperationService dbOperationService;

    @ResponseBody
    @RequestMapping("say-hello")
    public String sayHello() {
        return "Hello Zebra!";
    }

    @ResponseBody
    @RequestMapping("query-user")
    public User queryUserById(@RequestParam("uid") Long uid) {
        return dbOperationService.queryUserById(uid);
    }

    @ResponseBody
    @RequestMapping("query-users-by-range")
    public List<User> queryUsersByRange(@RequestParam("beginUid") Long beginUid, @RequestParam("endUid") Long endUid) {
        return dbOperationService.queryAllUsersByIdRange(beginUid, endUid);
    }

    @ResponseBody
    @RequestMapping("update-users-test")
    public int updateUsersUids() {
        // only two sqls generated, since all of them are routed to only two db-tables.
        List<Long> uids = Arrays.asList(8L, 16L, 24L, 32L, 41L, 49L, 57L, 65L);
        return dbOperationService.updateUsersUids(uids);
    }

    @ResponseBody
    @RequestMapping("update-batch-test")
    public int updateUsersBatch() {
        // only the first item is updated. zebra doesn't support multiple sqls.
        List<User> users = Arrays.asList(new User(0L, 8L, "abcd"), new User(0L, 16L, "ef"),
                new User(0L, 1L, "gh"));
        return dbOperationService.updateUsersBatch(users);
    }

    @ResponseBody
    @RequestMapping("insert-random-user")
    public String insertRandomUser() {
        int result =  dbOperationService.insertRandomUser();

        return String.format("User inserted: %d.", result);
    }

    @ResponseBody
    @RequestMapping("test-transaction")
    public String testTransaction() {
        int result =  dbOperationService.testTransaction();

        return String.format("Test result: %d.", result);
    }
}
