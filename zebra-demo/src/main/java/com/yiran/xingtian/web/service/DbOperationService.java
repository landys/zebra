package com.yiran.xingtian.web.service;

import com.yiran.xingtian.common.mapper.OrderMapper;
import com.yiran.xingtian.common.model.Order;
import com.yiran.xingtian.common.model.OrderExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Xingtian on 2017-01-13.
 */
@Service
public class DbOperationService {
    @Autowired
    private OrderMapper orderMapper;

    public int queryTest() {
        List<Long> uids = generateUids(1000);
        OrderExample orderExample = new OrderExample();
        orderExample.createCriteria().andGroupOrderIdEqualTo(34245143214L)
                .andUidIn(uids);

        List<Order> orders = orderMapper.selectByExample(orderExample);

        return orders.size();
    }

    private List<Long> generateUids(int size) {
        List<Long> uids = new ArrayList<Long>();
        for (int i = 0; i < size; ++i) {
            uids.add((long)i);
        }

        return uids;
    }
}
