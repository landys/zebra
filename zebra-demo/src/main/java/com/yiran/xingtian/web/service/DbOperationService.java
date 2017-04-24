package com.yiran.xingtian.web.service;

import com.google.common.collect.Lists;
import com.yiran.xingtian.common.ShardingHelper;
import com.yiran.xingtian.common.ThreadPoolFactory;
import com.yiran.xingtian.common.mapper.OrderMapper;
import com.yiran.xingtian.common.model.Order;
import com.yiran.xingtian.common.model.OrderExample;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * Created by Xingtian on 2017-01-13.
 */
@Service
public class DbOperationService {
    @Autowired
    private OrderMapper orderMapper;

    private ShardingHelper shardingHelper = new ShardingHelper();

    private ExecutorService threadPool;

    private Logger logger = LoggerFactory.getLogger(getClass());

    @PostConstruct
    public void init() {
        threadPool = ThreadPoolFactory.newFixedThreadPoolWithQueueSize(512, 5000);
    }

    public Map<String, Object> queryTest(int tableSize, int dateSize, int repeatTime) {
        return doQuery(tableSize, dateSize, repeatTime, this::queryOrderByUids);
    }

    public Map<String, Object> queryInThreadsTest(int tableSize, int dateSize, int repeatTime) {
        return doQuery(tableSize, dateSize, repeatTime, this::queryInThreads);
    }

    public Map<String, Object> queryTestHackedWay(int tableSize, int dateSize, int repeatTime) {
        return doQuery(tableSize, dateSize, repeatTime, this::queryOrderByUidsHackedWay);
    }

    public Map<String, Object> queryInThreadsTestHackedWay(int tableSize, int dateSize, int repeatTime) {
        return doQuery(tableSize, dateSize, repeatTime, this::queryInThreadsHackedWay);
    }

    private Map<String, Object> doQuery(int tableSize, int dateSize, int repeatTime, Function<QueryParam, List<Order>> funQuery) {
        Map<String, Object> results = new HashMap<>();

        long startDate = System.currentTimeMillis();

        for (int i=0; i<repeatTime; ++i) {
            long groupOrderId = RandomUtils.nextLong();
            List<Long> uids = generateUids(dateSize, tableSize);

            long startEach = System.currentTimeMillis();
            funQuery.apply(new QueryParam(groupOrderId, uids));
            long endEach = System.currentTimeMillis();
            System.out.println("each_run " + (endEach - startEach));
        }

        long endDate = System.currentTimeMillis();
        results.put("table_size", tableSize);
        results.put("date_size", dateSize);
        results.put("elapsed_time", endDate - startDate);

        System.out.println("total_run " + (endDate - startDate));

        return results;
    }

    private List<Order> queryOrderByUids(QueryParam queryParam) {
        OrderExample orderExample = new OrderExample();
        orderExample.createCriteria().andGroupOrderIdEqualTo(queryParam.getGroupOrderId())
                .andUidIn(queryParam.getUids());

        List<Order> orders = orderMapper.selectByExample(orderExample);
        return orders;
    }

    private List<Order> queryOrderByUidsHackedWay(QueryParam queryParam) {
        OrderExample orderExample = new OrderExample();
        OrderExample.Criteria criteria = orderExample.createCriteria().andGroupOrderIdEqualTo(queryParam.getGroupOrderId());
        shardingHelper.addCriterion(criteria, generateCriterion("uid", queryParam.getUids()));

        List<Order> orders = orderMapper.selectByExample(orderExample);
        return orders;
    }

    private String generateCriterion(String key, List<Long> uids) {
        StringBuilder sb = new StringBuilder(key);
        sb.append(" in (").append(uids.get(0));
        for (int i = 1; i <  uids.size(); ++i) {
            sb.append(',').append(uids.get(i));
        }
        sb.append(')');

        return sb.toString();
    }

    private List<Order> queryInThreads(QueryParam queryParam) {
        return doQueryInThreads(queryParam, this::queryOrderByUids);
    }

    private List<Order> queryInThreadsHackedWay(QueryParam queryParam) {
        return doQueryInThreads(queryParam, this::queryOrderByUidsHackedWay);
    }

    private List<Order> doQueryInThreads(QueryParam queryParam, Function<QueryParam, List<Order>> funQuery) {
        // separate uids according to the sharding rules.
        Map<Integer, List<Long>> uidsMap = shardingHelper.separateUidsByDbAndTables(queryParam.getUids());

        // query in separate selects in thread pool.
        final CountDownLatch latch = new CountDownLatch(uidsMap.size());
        final List<Future<List<Order>>> futures = Lists.newArrayList();
        for (List<Long> sepUids : uidsMap.values()) {
            Callable<List<Order>> ordersCallable = () -> {
                try {
                    return funQuery.apply(new QueryParam(queryParam.getGroupOrderId(), sepUids));
                } finally {
                    latch.countDown();
                }
            };

            Future<List<Order>> future = threadPool.submit(ordersCallable);
            futures.add(future);
        }

        // combine results.
        List<Order> results = Lists.newArrayList();
        try {
            latch.await(5L, TimeUnit.SECONDS);
            for (Future<List<Order>> future : futures) {
                results.addAll(future.get());
            }
        } catch (Exception e) {

        }

        return results;
    }

    private List<Long> generateUids(int size, int tableSize) {
        List<Long> uids = new ArrayList<>();
        int n = 0;
        long start = RandomUtils.nextLong() / 2;
        while (true) {
            for (long i = 0; i < tableSize; ++i) {
                uids.add(start + i);

                if (++n == size) {
                    break;
                }
            }

            if (n == size) {
                break;
            }

            start += 512;
        }

        return uids;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private static class QueryParam {
        private Long groupOrderId;

        private List<Long> uids;
    }
}
