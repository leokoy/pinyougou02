package com.pinyougou.seckill.listener;

import com.alibaba.fastjson.JSON;
import com.pinyougou.common.pojo.MessageInfo;
import com.pinyougou.mapper.TbSeckillOrderMapper;
import com.pinyougou.pojo.TbSeckillOrder;
import com.pinyougou.seckill.service.SeckillOrderService;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * 描述
 *
 * @author 三国的包子
 * @version 1.0
 * @package com.pinyougou.seckill.listener *
 * @since 1.0
 */
public class DelayMessageListener implements MessageListenerConcurrently {
    @Autowired
    private SeckillOrderService seckillOrderService;

    @Autowired
    private TbSeckillOrderMapper orderMapper;

    @Override
    public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
        for (MessageExt msg : msgs) {
            System.out.println("=======消费消息");
            //1.获取消息体
            byte[] body = msg.getBody();
            //2.获取字符串
            String s = new String(body);
            //3.转成JSON
            MessageInfo messageInfo = JSON.parseObject(s, MessageInfo.class);
            //4.获取里面的订单的对象
            TbSeckillOrder seckillOrder = JSON.parseObject(messageInfo.getContext().toString(), TbSeckillOrder.class);
            //5.处理超时的业务逻辑
            if(messageInfo.getMethod()==MessageInfo.METHOD_UPDATE){


                //查询数据库中的数据  如果没有数据 ，没有支付
                TbSeckillOrder seckillOrder1 = orderMapper.selectByPrimaryKey(seckillOrder.getId());
                if(seckillOrder1==null) {
                    //1.关闭微信订单 引入服务  调用这个服务。
                    //2.删除 redis中的订单
                    seckillOrderService.deleteOrder(seckillOrder.getUserId());
                }
            }

        }
        return null;
    }
}
