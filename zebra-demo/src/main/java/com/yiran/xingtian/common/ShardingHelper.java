package com.yiran.xingtian.common;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Xingtian on 08/02/2017.
 */
@Component
public class ShardingHelper {
    private static final int DbNum = 4;

    private static final int TableNum = 128;

    private static final int DbMask = DbNum - 1;

    private static final int TableMask = TableNum - 1;

    private static final int TableBitsNum = 7;

    private static final String UidShardKey = "uid";

    private static final String GroupOrderIdShardKey = "group_order_id";

    private static final String ShardKeySelf = "shard_key";

    /**
     * The ids smaller than the IdLimit are considered as old ids. For order id and group order id.
     */
    private static final long IdLimit = 181684562366721289L;

    /**
     * The length of the order sn in new db.
     */
    private static final int OrderSnLenLimit = 22;

    private Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Get the sharding db number.
     *
     * @return
     */
    public int getDbNum() {
        return DbNum;
    }

    /**
     * Get the sharding table number for each sharding db.
     *
     * @return
     */
    public int getTableNum() {
        return TableNum;
    }

    /**
     * Check if the order id or group order id is from old db.
     *
     * @param id
     * @return
     */
    public boolean isOldId(Long id) {
        return id == null || id < IdLimit;
    }

    /**
     * Check if the order sn is from the old db.
     *
     * @param orderSn
     * @return
     */
    public boolean isOldOrderSn(String orderSn) {
        return (orderSn == null || orderSn.length() < OrderSnLenLimit);
    }

    public ShardIndexVO getShardResultByOrderSn(String orderSn) {
        if (orderSn == null || orderSn.length() < 4) {
            return null;
        }

        int key = Integer.parseInt(orderSn.substring(orderSn.length() - 4));
        return new ShardIndexVO((key >> TableBitsNum) & DbMask, key & TableMask);
    }

    public ShardIndexVO getShardResultById(Long id) {
        if (id == null) {
            return null;
        }

        int last4d = (int) (id % 10000);
        return new ShardIndexVO((last4d >> TableBitsNum) & DbMask, last4d & TableMask);
    }

    /**
     * Shard db index and table index.
     * @param uid
     * @return
     */
    public ShardIndexVO getShardResultByUid(Long uid) {
        if (uid == null) {
            return null;
        }

        return new ShardIndexVO((int) ((uid >> TableBitsNum) & DbMask), (int) (uid & TableMask));
    }

    public Integer getShardIndexByUid(Long uid) {
        return calcShardIndex(getShardResultByUid(uid));
    }

    private Integer calcShardIndex(ShardIndexVO shardIndexVO) {
        if (shardIndexVO == null) {
            return null;
        }

        return shardIndexVO.getDbIndex() * TableNum + shardIndexVO.getTableIndex();
    }

    /**
     * 把uids按分库分表后结果进行分组, 其中key为库表的唯一index, 计算公式为: db_index * 128 + table_index
     * @param uids
     * @return
     */
    public Map<Integer, List<Long>> separateUidsByDbAndTables(List<Long> uids) {
        if (uids == null || uids.isEmpty()) {
            return Maps.newHashMap();
        }

        return uids.stream().collect(Collectors.groupingBy(this::getShardIndexByUid));
    }

    /**
     * 把分组的uids map根据uids threshold进行重装组装。
     * @param uidsMap
     * @param threshold
     * @return
     */
    public List<List<Long>> reOrgUidsList(Map<Integer, List<Long>> uidsMap, int threshold) {
        if (uidsMap == null || uidsMap.size() == 0) {
            return Lists.newArrayList();
        }

        List<List<Long>> resepUidsList = Lists.newArrayList();
        List<Long> curUids = Lists.newArrayList();
        for (List<Long> sepUids : uidsMap.values()) {
            if (sepUids.size() >= threshold) {
                resepUidsList.add(sepUids);
            } else {
                curUids.addAll(sepUids);
                if (curUids.size() >= threshold) {
                    resepUidsList.add(curUids);
                    curUids = Lists.newArrayList();
                }
            }
        }

        if (curUids.size() > 0) {
            resepUidsList.add(curUids);
        }

        return resepUidsList;
    }

    /**
     * It uses uid as sharding key.
     *
     * @param criteria
     * @param dbIndex
     * @param tableIndex
     * @param <T>
     */
    public <T> void addUidShardKeyCriterion(T criteria, int dbIndex, int tableIndex) {
        addUidShardKeyCriterion(criteria, ((dbIndex << TableBitsNum) | tableIndex));
    }

    public <T> void addUidShardKeyCriterion(T criteria, long shardValue) {
        doAddShardKeyCriterion(criteria, UidShardKey, shardValue);
    }

    public <T> void addShardKeyCriterion(T criteria, int dbIndex) {
        doAddShardKeyCriterion(criteria, ShardKeySelf, (dbIndex << TableBitsNum));
    }

    /**
     * It uses group_order_id as sharding key.
     *
     * @param criteria
     * @param dbIndex
     * @param tableIndex
     * @param <T>
     */
    public <T> void addGroupOrderIdShardKeyCriterion(T criteria, int dbIndex, int tableIndex) {
        addGroupOrderIdShardKeyCriterion(criteria, ((dbIndex << TableBitsNum) | tableIndex));
    }

    /**
     * 添加shard key, 让查询或更新语句通过shardValue来分库分表, 并且该值仅仅用来分库分表, 而不会对查询语句本身产生影响。
     * @param criteria
     * @param shardValue
     * @param <T>
     */
    public <T> void addGroupOrderIdShardKeyCriterion(T criteria, long shardValue) {
        doAddShardKeyCriterion(criteria, GroupOrderIdShardKey, shardValue);
    }

    private <T> void doAddShardKeyCriterion(T criteria, String shardKey, long shardValue) {
        if (criteria == null || StringUtils.isBlank(shardKey)) {
            return;
        }

        // use reflect to add criterion.
        try {
            Method method = criteria.getClass().getSuperclass().getDeclaredMethod("addCriterion", String.class);
            method.setAccessible(true);
            method.invoke(criteria, String.format("(1 or %s=%d)", shardKey, shardValue));
        } catch (Exception e) {
            // should not be here.
            logger.error("Error to add sharding key criterion: {}", shardValue, e);
            throw new RuntimeException("Error to add sharding key criterion");
        }
    }

    public boolean checkCommandOrQueryV2(final String className) {
        boolean isV2 = false;
        String name = className;
        if (StringUtils.isNotBlank(name)) {
            // support the command even if it's enhanced by spring or other aop.
            int index = name.indexOf('$');
            if (index >= 0) {
                name = name.substring(0, index);
            }

            isV2 = name.endsWith("V2");
        }
        return isV2;
    }
}
