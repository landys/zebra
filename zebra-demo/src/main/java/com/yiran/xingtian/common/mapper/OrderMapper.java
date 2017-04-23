package com.yiran.xingtian.common.mapper;

import com.yiran.xingtian.common.model.Order;
import com.yiran.xingtian.common.model.OrderExample;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

import java.util.List;

public interface OrderMapper {
    int countByExample(OrderExample example);

    //int deleteByExample(OrderExample example);

    //int deleteByPrimaryKey(Long id);

    int insert(Order record);

    int insertSelective(Order record);

    List<Order> selectByExampleWithRowbounds(OrderExample example, RowBounds rowBounds);

    List<Order> selectByExample(OrderExample example);

    //Order selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") Order record, @Param("example") OrderExample example);

    int updateByExample(@Param("record") Order record, @Param("example") OrderExample example);

    //int updateByPrimaryKeySelective(Order record);

    //int updateByPrimaryKey(Order record);
}