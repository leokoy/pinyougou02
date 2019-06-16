package com.pinyougou.order.service;

import com.pinyougou.pojo.TbOrder;
import com.pinyougou.pojo.TbPayLog;

/**
 * 描述
 *
 * @author 三国的包子
 * @version 1.0
 * @package com.pinyougou.order.service *
 * @since 1.0
 */
public interface OrderService {
    void add(TbOrder order);


    TbPayLog getPayLogFromRedis(String userId);

    /**
     * 更新支付日志记录
     *
     *  //1.更新 支付日志的记录（交易流水  交易的状态  交易的时间）
     //2.更新 支付日志记录  关联到的订单的 状态 和支付时间
     //3.删除 该用户的redis中的支付日志

     * @param transaction_id
     * @param out_trade_no
     */
    void updateStatus(String transaction_id, String out_trade_no);
}
