package com.yiran.xingtian.web.service;

import com.google.common.collect.Lists;
import com.yiran.xingtian.common.ShardingHelper;
import com.yiran.xingtian.common.ThreadPoolFactory;
import com.yiran.xingtian.common.mapper.OrderMapper;
import com.yiran.xingtian.common.model.Order;
import com.yiran.xingtian.common.model.OrderExample;
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

/**
 * Created by Xingtian on 2017-01-13.
 */
@Service
public class DbOperationService {
    @Autowired
    private OrderMapper orderMapper;

    private ShardingHelper shardingHelper = new ShardingHelper();

    private ExecutorService threadPool;

    @PostConstruct
    public void init() {
        threadPool = ThreadPoolFactory.newFixedThreadPoolWithQueueSize(512, 5000);
    }

    public Map<String, Object> queryTest(int tableSize, int dateSize) {
        Map<String, Object> results = new HashMap<String, Object>();

        List<Long> uids = generateUids(dateSize, tableSize);

        long startDate = System.currentTimeMillis();

        queryOrderByUids(34245143214L, uids);

        long endDate = System.currentTimeMillis();
        results.put("table_size", tableSize);
        results.put("date_size", dateSize);
        results.put("elapsed_time", endDate - startDate);

        return results;
    }

    public Map<String, Object> queryInThreadsTest(int tableSize, int dateSize) {
        Map<String, Object> results = new HashMap<String, Object>();

        List<Long> uids = generateUids(dateSize, tableSize);

        long startDate = System.currentTimeMillis();

        List<Order> orders = queryInThreads(34245143214L, uids);

        long endDate = System.currentTimeMillis();
        results.put("table_size", tableSize);
        results.put("date_size", dateSize);
        results.put("elapsed_time", endDate - startDate);

        return results;
    }

    private List<Order> queryOrderByUids(Long groupOrderId, List<Long> uids) {
        OrderExample orderExample = new OrderExample();
        orderExample.createCriteria().andGroupOrderIdEqualTo(groupOrderId)
                .andUidIn(uids);

        List<Order> orders = orderMapper.selectByExample(orderExample);
        return orders;
    }

    private List<Order> queryInThreads(Long groupOrderId, List<Long> uids) {
        // separate uids according to the sharding rules.
        Map<Integer, List<Long>> uidsMap = shardingHelper.separateUidsByDbAndTables(uids);

        // query in separate selects in thread pool.
        final CountDownLatch latch = new CountDownLatch(uidsMap.size());
        final List<Future<List<Order>>> futures = Lists.newArrayList();
        for (List<Long> sepUids : uidsMap.values()) {
            Callable<List<Order>> ordersCallable = () -> {
                try {
                    return queryOrderByUids(groupOrderId, sepUids);
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
        List<Long> uids = new ArrayList<Long>();
        int n = 0;
        long step = 0;
        while (true) {
            for (int i = 0; i < tableSize; ++i) {
                uids.add(step + i);

                if (++n == size) {
                    break;
                }
            }

            if (n == size) {
                break;
            }

            step += 512;
        }

        return uids;
    }
}
