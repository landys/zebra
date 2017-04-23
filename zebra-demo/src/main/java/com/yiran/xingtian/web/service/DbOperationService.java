package com.yiran.xingtian.web.service;

import com.yiran.xingtian.common.mapper.OrderMapper;
import com.yiran.xingtian.common.model.Order;
import com.yiran.xingtian.common.model.OrderExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Xingtian on 2017-01-13.
 */
@Service
public class DbOperationService {
    @Autowired
    private OrderMapper orderMapper;

    public Map<String, Object> queryTest(int tableSize, int dateSize) {
        Map<String, Object> results = new HashMap<String, Object>();

        List<Long> uids = generateUids(dateSize, tableSize);

        long startDate = System.currentTimeMillis();
        OrderExample orderExample = new OrderExample();
        orderExample.createCriteria().andGroupOrderIdEqualTo(34245143214L)
                .andUidIn(uids);

        List<Order> orders = orderMapper.selectByExample(orderExample);

        long endDate = System.currentTimeMillis();
        results.put("table_size", tableSize);
        results.put("date_size", dateSize);
        results.put("start_date", new Date(startDate));
        results.put("end_date", new Date(endDate));
        results.put("elapsed_time", endDate - startDate);

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
