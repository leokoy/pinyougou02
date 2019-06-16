package com.pinyougou.seckill.listener;

import com.alibaba.fastjson.JSON;
import com.pinyougou.common.pojo.MessageInfo;
import com.pinyougou.mapper.TbSeckillGoodsMapper;
import com.pinyougou.pojo.TbSeckillGoods;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 描述
 *
 * @author 三国的包子
 * @version 1.0
 * @package com.pinyougou.seckill.listener *
 * @since 1.0
 */
public class PageMessageListener implements MessageListenerConcurrently {

    @Autowired
    private FreeMarkerConfigurer freeMarkerConfigurer;

    @Autowired
    private TbSeckillGoodsMapper seckillGoodsMapper;

    @Value("${PageDir}")
    private String PageDir;

    @Override
    public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
        try {
            if (msgs != null) {
                for (MessageExt msg : msgs) {
                    //1.获取消息体
                    byte[] body = msg.getBody();
                    //2.将消息体 转成字符串
                    String s = new String(body);
                    //3.字符串转成自定义的消息对象 有要的生成的商品的ID的数组
                    MessageInfo messageInfo = JSON.parseObject(s, MessageInfo.class);
                    //4.判断 消费类型（ADD/DELELTE/UPDATE） 实现逻辑
                    if (messageInfo.getMethod() == MessageInfo.METHOD_ADD) {
                        String s1 = messageInfo.getContext().toString();
                        Long[] longs = JSON.parseObject(s1, Long[].class);//秒杀商品的ID的数组
                        //生成静态页码 定义一个方法  用于使用freeamrker 根据秒杀商品的ID 生成静态页面
                        for (Long aLong : longs) {
                            genHTML("item.ftl",aLong);
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


    //生成静态页面
    private void genHTML(String templateName, Long id) {
        FileWriter writer = null;

        try {
            //freemarker生成静态页面的核心： 模板+数据集=html

            //1.创建一个configuration对象

            Configuration configuration = freeMarkerConfigurer.getConfiguration();
            //2.设置字符编码 和模板加载的路径
            //3.创建模板文件
            //4.加载模板文件
            Template template = configuration.getTemplate(templateName);
            //5.准备数据集（数据库中的秒杀商品的数据）
            Map model = new HashMap();

            TbSeckillGoods seckillGoods = seckillGoodsMapper.selectByPrimaryKey(id);

            model.put("seckillGoods", seckillGoods);

            //6.创建写流对象
            writer = new FileWriter(new File(PageDir + id + ".html"));

            //7.生成静态页面
            template.process(model, writer);


        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //8.关闭流
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


    }
}
