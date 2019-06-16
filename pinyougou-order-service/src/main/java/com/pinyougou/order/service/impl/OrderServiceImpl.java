package com.pinyougou.order.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.common.util.IdWorker;
import com.pinyougou.mapper.TbOrderItemMapper;
import com.pinyougou.mapper.TbOrderMapper;
import com.pinyougou.mapper.TbPayLogMapper;
import com.pinyougou.order.service.OrderService;
import com.pinyougou.pojo.TbOrder;
import com.pinyougou.pojo.TbOrderItem;
import com.pinyougou.pojo.TbPayLog;
import entity.Cart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *  //1.订单号不能重复
    //2.订单要拆单
 *
 * @author 三国的包子
 * @version 1.0
 * @package com.pinyougou.order.service.impl *
 * @since 1.0
 */
@Service
public class OrderServiceImpl implements OrderService  {

    @Autowired
    private TbOrderMapper orderMapper;

    @Autowired
    private TbOrderItemMapper orderItemMapper;
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private TbPayLogMapper payLogMapper;

    @Override
    public void add(TbOrder order) {
        //1.获取订单的数据 插入到订单表

        //1.0 先获取 从redis中获取当前登录的用户的购物车列表：List<cart>  循环遍历拆单
        List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("Redis_CartList").get(order.getUserId());

        double total_fee = 0;
        List<String> orderList =new ArrayList<>();
        for (Cart cart : cartList) {
            //1.1 生成订单号
            long orderId = new IdWorker(0, 1).nextId();
            orderList.add(orderId+"");
            //1.2 补全属性
            TbOrder tbOrder = new TbOrder();
            tbOrder.setOrderId(orderId);
            double totalMoney=0;
            List<TbOrderItem> orderItemList = cart.getOrderItemList();//购物车明细列表
            for (TbOrderItem orderItem : orderItemList) {//订单选项

                //2.获取订单选项的数据 订单选项表
                long orderItemId = new IdWorker(0, 1).nextId();
                orderItem.setId(orderItemId);
                orderItem.setOrderId(orderId);

                orderItemMapper.insert(orderItem);
                totalMoney+=orderItem.getTotalFee().doubleValue();
            }

            //计算总金额
            total_fee+=totalMoney;//元



            tbOrder.setPayment(new BigDecimal(totalMoney));//设置应付金额  商家对应的金额

            tbOrder.setPaymentType(order.getPaymentType());
            tbOrder.setPostFee("0");//
            tbOrder.setStatus("1");//未付款的状态 1、未付款，2、已付款，3、未发货，4、已发货，5、交易成功，6、交易关闭,7、待评价',

            tbOrder.setCreateTime(new Date());
            tbOrder.setUpdateTime(tbOrder.getCreateTime());
            tbOrder.setUserId(order.getUserId());

            tbOrder.setReceiverAreaName(order.getReceiverAreaName());//收货详细地址
            tbOrder.setReceiverMobile(order.getReceiverMobile());//电话
            tbOrder.setReceiver(order.getReceiver());//收货人
            tbOrder.setSourceType("2");//订单来源 PC端
            tbOrder.setSellerId(cart.getSellerId());//商家的ID
            orderMapper.insert(tbOrder);

        }

        //需要创建支付日志记录
        TbPayLog payLog = new TbPayLog();

        payLog.setOutTradeNo(new IdWorker(0,1).nextId()+"");
        payLog.setCreateTime(new Date());//
        double v = total_fee * 100;
        payLog.setTotalFee((long)v);//总金额
        payLog.setTradeState("0");//未支付的状态
        payLog.setPayType("1");//0 支付宝  1 微信支付 2.银行
        payLog.setUserId(order.getUserId());
        // [1,2]
        payLog.setOrderList(orderList.toString().replace("[","").replace("]",""));//设置关联的订单号  存储以：订单号,订单号,订单号

        payLogMapper.insert(payLog);

        redisTemplate.boundHashOps(TbPayLog.class.getSimpleName()).put(order.getUserId(),payLog);


        //清空某一个用户的购物车
        redisTemplate.boundHashOps("Redis_CartList").delete(order.getUserId());

    }

    @Override
    public TbPayLog getPayLogFromRedis(String userId) {
        TbPayLog tbPayLog = (TbPayLog) redisTemplate.boundHashOps(TbPayLog.class.getSimpleName()).get(userId);
        return tbPayLog;
    }

    @Override
    public void updateStatus(String transaction_id, String out_trade_no) {
        //1.更新 支付日志的记录（交易流水  交易的状态  交易的时间）
        TbPayLog payLog = payLogMapper.selectByPrimaryKey(out_trade_no);

        payLog.setPayTime(new Date());
        payLog.setTransactionId(transaction_id);
        payLog.setTradeState("1");
        payLogMapper.updateByPrimaryKey(payLog);

        //2.更新 支付日志记录  关联到的订单的 状态 和支付时间
        String orderList = payLog.getOrderList();//  1,2

        //切割  出订单号
        String[] split = orderList.split(",");
        //获取订单号对应的对象
        for (String orderId : split) {
            TbOrder tbOrder = orderMapper.selectByPrimaryKey(Long.valueOf(orderId));//订单对象
            //更新 到数据库中
            tbOrder.setStatus("2");//已付款状态
            tbOrder.setUpdateTime(new Date());
            tbOrder.setPaymentTime(new Date());
            orderMapper.updateByPrimaryKey(tbOrder);
        }
        //3.删除 该用户的redis中的支付日志
        redisTemplate.boundHashOps(TbPayLog.class.getSimpleName()).delete(payLog.getUserId());

    }

    public static void main(String[] args) {
      List<String> o = new ArrayList<>();
      o.add("1");
      o.add("2");
        System.out.println(o.toString());
    }


}
