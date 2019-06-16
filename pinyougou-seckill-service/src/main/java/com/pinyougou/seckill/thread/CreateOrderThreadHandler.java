package com.pinyougou.seckill.thread;

import com.alibaba.fastjson.JSON;
import com.pinyougou.common.pojo.MessageInfo;
import com.pinyougou.common.pojo.SysConstants;
import com.pinyougou.common.util.IdWorker;
import com.pinyougou.mapper.TbSeckillGoodsMapper;
import com.pinyougou.pojo.TbSeckillGoods;
import com.pinyougou.pojo.TbSeckillOrder;
import com.pinyougou.seckill.pojo.SeckillStatus;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;

/**
 * 多线程操作的类
 *
 * @author 三国的包子
 * @version 1.0
 * @package com.pinyougou.seckill.thread *
 * @since 1.0
 */
public class CreateOrderThreadHandler {


    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private TbSeckillGoodsMapper tbSeckillGoodsMapper;

    @Autowired
    private DefaultMQProducer producer;

    //多线程的注解:表示异步
    @Async
    public void handlerCreateOrder(){

        System.out.println("模拟创建订单的耗时操作=====开始"+Thread.currentThread().getName());
        try {
            //1.算积分
            //2.算优惠
            //3.存储用户的操作日志
            //4.大数据分析
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("模拟创建订单的耗时操作=====结束"+Thread.currentThread().getName());


        //从队列中获取排队的元素（抢购的用户的ID  抢购的商品的ID）

        SeckillStatus seckillStatus = (SeckillStatus) redisTemplate.boundListOps(SysConstants.SEC_KILL_USER_ORDER_LIST).rightPop();


        if(seckillStatus!=null) {

            TbSeckillGoods seckillGoods = (TbSeckillGoods) redisTemplate.boundHashOps(SysConstants.SEC_KILL_GOODS).get(seckillStatus.getGoodsId());
            //3.减库存
            seckillGoods.setStockCount(seckillGoods.getStockCount() - 1);
            redisTemplate.boundHashOps(SysConstants.SEC_KILL_GOODS).put(seckillStatus.getGoodsId(), seckillGoods);

            //算积分  3秒
            //用户的操作日志。 1秒
            //计算优惠卷 5秒
            //,......
            if (seckillGoods.getStockCount() <= 0) {
                //4.判断 如果库存为0 更新到数据库中  删除 redis中的商品
                tbSeckillGoodsMapper.updateByPrimaryKeySelective(seckillGoods);
                redisTemplate.boundHashOps(SysConstants.SEC_KILL_GOODS).delete(seckillStatus.getGoodsId());
            }

            //5.创建秒杀的预订单到redis中
            TbSeckillOrder order = new TbSeckillOrder();

            order.setId(new IdWorker(0, 2).nextId());
            order.setSeckillId(seckillStatus.getGoodsId());
            order.setMoney(seckillGoods.getCostPrice());
            order.setUserId(seckillStatus.getUserId());
            order.setSellerId(seckillGoods.getSellerId());//商家
            order.setCreateTime(new Date());//创建时间
            order.setStatus("0");//未支付的状态
            redisTemplate.boundHashOps(SysConstants.SEC_KILL_ORDER).put(seckillStatus.getUserId(), order);

            //移除掉排队的标识
            //将用户存储一个正在排队的标识
            redisTemplate.boundHashOps(SysConstants.SEC_USER_QUEUE_FLAG_KEY).delete(seckillStatus.getUserId());


            //发送消息 （延时消息）
            MessageInfo messageInfo = new MessageInfo("TOPIC_SECKILL_DELAY","TAG_SECKILL_DELAY","handleOrder_DELAY",order,MessageInfo.METHOD_UPDATE);
            Message message = new Message(messageInfo.getTopic(),messageInfo.getTags(),messageInfo.getKeys(), JSON.toJSONString(messageInfo).getBytes());
            try {
                //设置延时的等级
                message.setDelayTimeLevel(16);//使用等级就是第5级  代表的延时  1分钟
                SendResult send = producer.send(message);
                System.out.println(send);
                System.out.println("发送了消息");

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
