package com.pinyougou.page.listener;

import com.alibaba.fastjson.JSON;
import com.pinyougou.common.pojo.MessageInfo;
import com.pinyougou.page.service.ItemPageService;
import com.pinyougou.pojo.TbItem;
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
 * @package com.pinyougou.page.listener *
 * @since 1.0
 */
public class GoodsPageGenHtmlMessageListener implements MessageListenerConcurrently {

    @Autowired
    private ItemPageService itemPageService;
    @Override
    public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
        try {
            if (msgs != null) {
                //1.循环遍历消息对象
                for (MessageExt msg : msgs) {
                    //2.获取到消息体
                    byte[] body = msg.getBody();
                    String jsonstring = new String(body);
                    //3.获取到消息体对应的自定义的消息对象
                    MessageInfo messageInfo = JSON.parseObject(jsonstring, MessageInfo.class);
                    //4.获取方法类型（用于判断）
                    int method = messageInfo.getMethod();// 1  2  3
                    //5.判断 方法 做 页面的生成 或者页面的修改 或者页面的删除

                    switch (method) {
                        case 1: {//新增 生成静态页面
                            String context1 = messageInfo.getContext().toString();//List<TbItem>  对应的字符串

                            List<TbItem> itemList = JSON.parseArray(context1, TbItem.class);
                            for (TbItem tbItem : itemList) {
                                itemPageService.genItemHtml(tbItem.getGoodsId());//SPU的ID
                            }
                            break;
                        }
                        case 2: { //修改  生成静态页面
                            String context1 = messageInfo.getContext().toString();//List<TbItem>  对应的字符串

                            List<TbItem> itemList = JSON.parseArray(context1, TbItem.class);
                            for (TbItem tbItem : itemList) {
                                itemPageService.genItemHtml(tbItem.getGoodsId());//SPU的ID
                            }
                            break;
                        }
                        case 3: {//删除原来的静态页面
                            String context1 = messageInfo.getContext().toString();//SPU ID的数组对应的字符串
                            Long[] longs = JSON.parseObject(context1, Long[].class);
                            //删除静态页面的方法
                            itemPageService.deleteByIds(longs);
                            break;
                        }
                        default: {
                            break;
                        }
                    }


                }
            }
            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ConsumeConcurrentlyStatus.RECONSUME_LATER;
    }
}
