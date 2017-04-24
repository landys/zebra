package com.yiran.xingtian.web.controller;

import com.yiran.xingtian.web.service.DbOperationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

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
    @RequestMapping("query-test")
    public Map<String, Object> queryTest(@RequestParam("t") int tableSize, @RequestParam("n") int dataSize, @RequestParam("r") int repeatTime) {
        return dbOperationService.queryTest(tableSize, dataSize, repeatTime);
    }

    @ResponseBody
    @RequestMapping("query-test-thread")
    public Map<String, Object> queryInThreadsTest(@RequestParam("t") int tableSize, @RequestParam("n") int dataSize, @RequestParam("r") int repeatTime) {
        return dbOperationService.queryInThreadsTest(tableSize, dataSize, repeatTime);
    }

    @ResponseBody
    @RequestMapping("query-test-hacked")
    public Map<String, Object> queryTestHacked(@RequestParam("t") int tableSize, @RequestParam("n") int dataSize, @RequestParam("r") int repeatTime) {
        return dbOperationService.queryTestHackedWay(tableSize, dataSize, repeatTime);
    }

    @ResponseBody
    @RequestMapping("query-test-thread-hacked")
    public Map<String, Object> queryInThreadsTestHeacked(@RequestParam("t") int tableSize, @RequestParam("n") int dataSize, @RequestParam("r") int repeatTime) {
        return dbOperationService.queryInThreadsTestHackedWay(tableSize, dataSize, repeatTime);
    }
}
