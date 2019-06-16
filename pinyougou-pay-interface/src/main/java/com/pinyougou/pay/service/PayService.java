package com.pinyougou.pay.service;

import java.util.Map;

/**
 * 描述
 *
 * @author 三国的包子
 * @version 1.0
 * @package com.pinyougou.pay.service *
 * @since 1.0
 */
public interface PayService {
    /**
     * 模拟浏览器发送请求  调用统一下单的API 给微信支付系统，接收 响应 获取里面的 支付二维码的连接地址
     * @param out_trade_no
     * @param total_fee
     * @return
     */
    Map<String,String> createNative(String out_trade_no, String total_fee);

    /**
     * 检测某一个订单号的支付的状态
     * @param out_trade_no
     * @return
     */
    Map<String,String> queryStatus(String out_trade_no);

    /**
     * 关闭微信的订单
     * @param out_trade_no
     * @return
     */
    Map<String,String> closePay(String out_trade_no);
}
