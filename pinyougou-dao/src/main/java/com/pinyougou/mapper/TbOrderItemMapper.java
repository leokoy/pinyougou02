package com.pinyougou.mapper;

import com.pinyougou.pojo.TbOrderItem;
import entity.Order;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface TbOrderItemMapper extends Mapper<TbOrderItem> {
    List<Order> findCount();

}