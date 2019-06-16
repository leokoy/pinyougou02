package com.pinyougou.es.listener;

import com.alibaba.fastjson.JSON;
import com.pinyougou.common.pojo.MessageInfo;
import com.pinyougou.es.service.ItemService;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @author LiJiangwen
 * @version 1.0
 * @date 2019/6/13 16:26
 */
public class UpdateEsGoodsListener implements MessageListenerConcurrently {
    @Autowired
    private ItemService itemService;

    @Override
    public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> list, ConsumeConcurrentlyContext consumeConcurrentlyContext) {
                try {
                    for (MessageExt messageExt : list) {
                String topic = messageExt.getTopic();
                if("Goods_Es_Topic".equals(topic)){
                    System.out.println("消息标签是:>>>>"+messageExt.getTags());
                    if("goods_update_tag".equals(messageExt.getTags())){
                        String body = new String(messageExt.getBody());
                        MessageInfo info = JSON.parseObject(body, MessageInfo.class);
                        String idsStr = JSON.toJSONString(info.getContext());
                        //获取spu的id
                        Long[] ids = JSON.parseObject(idsStr, Long[].class);
                        //获取sku的id
                        Long[] itemIds = itemService.getItemIds(ids);
                        int method = info.getMethod();
                        if(method == 1 || method == 2){
                            itemService.updateEsGoods(itemIds);
                        }
                        if(method == 3){
                            itemService.deleteEsGoods(itemIds);
                        }
                    }
                }
            }
            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        } catch (Exception e) {
            e.printStackTrace();
            //集群模式下,消息消费失败后,可以隔一段时间后重试
            return ConsumeConcurrentlyStatus.RECONSUME_LATER;
        }
    }
}
