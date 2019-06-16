package com.pinyougou.search.listener;

import com.alibaba.fastjson.JSON;
import com.pinyougou.common.pojo.MessageInfo;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;
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
 * @package com.pinyougou.search.listener *
 * @since 1.0
 */
public class GoodsMessageListener implements MessageListenerConcurrently {

    @Autowired
    private ItemSearchService itemSearchService;
    @Override
    public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
        try {
            if (msgs != null) {
                System.out.println("============start========");
                //1.循环遍历消息对象
                for (MessageExt msg : msgs) {
                //2.获取消息体（我们自己封装的消息对象）
                    byte[] body = msg.getBody();
                    String info = new String(body);// 消息对象的字符串
                    MessageInfo messageInfo = JSON.parseObject(info, MessageInfo.class);
                     //3.获取方法（add/delete/update）
                    int method = messageInfo.getMethod();//1 :新增  2 :修改   3 ：删除
                    System.out.println("=========1111===="+method);
                    //4.判断 方法 是 add /deelte /update  分别进行 Cud操作（例如：更新索引 删除索引）
                    switch (method){
                        case 1:{//说明要新增
                            String context1 = messageInfo.getContext().toString();//商品列表的字符串
                            List<TbItem> itemList = JSON.parseArray(context1, TbItem.class);
                            itemSearchService.updateIndex(itemList);
                            break;
                        }
                        case 2:{//说明要更新
                            String context1 = messageInfo.getContext().toString();//商品列表的字符串
                            List<TbItem> itemList = JSON.parseArray(context1, TbItem.class);
                            System.out.println("=========22222====");
                            itemSearchService.updateIndex(itemList);
                            break;
                        }
                        case 3:{//说明要删除
                            String context1 = messageInfo.getContext().toString();//SPU id 数组 对应的字符串
                            Long[] longs = JSON.parseObject(context1, Long[].class);
                            itemSearchService.deleteByIds(longs);
                            break;
                        }
                        default:{
                            throw new RuntimeException("你发送的东西不对");
                        }
                    }
                }
            }


            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ConsumeConcurrentlyStatus.RECONSUME_LATER;//
    }
}
