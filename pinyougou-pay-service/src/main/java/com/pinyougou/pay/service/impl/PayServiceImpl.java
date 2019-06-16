package com.pinyougou.pay.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.wxpay.sdk.WXPayUtil;
import com.pinyougou.common.util.HttpClient;
import com.pinyougou.pay.service.PayService;

import java.util.HashMap;
import java.util.Map;

/**
 * 描述
 *
 * @author 三国的包子
 * @version 1.0
 * @package com.pinyougou.pay.service.impl *
 * @since 1.0
 */
@Service
public class PayServiceImpl implements PayService {


    @Override
    public Map<String, String> createNative(String out_trade_no, String total_fee) {
        try {
            //1.组合参数 到map中
            Map<String,String> map = new HashMap<>();//参数map
            map.put("appid","wx8397f8696b538317");
            map.put("mch_id","1473426802");
            map.put("nonce_str", WXPayUtil.generateNonceStr());
            map.put("body", "品优购");
            map.put("out_trade_no",out_trade_no);
            map.put("total_fee",total_fee);//分
            map.put("spbill_create_ip","127.0.0.1");
            map.put("notify_url","http://a31ef7db.ngrok.io/WeChatPay/WeChatPayNotify");
            map.put("trade_type","NATIVE");

            //自动添加了签名的xml字符串
            String xmlParam = WXPayUtil.generateSignedXml(map, "T6m9iK73b0kn9g5v426MKfHQH7X8rKwb");//商户的签名

            //2.调用httpclient 模拟浏览器发送请求
            HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");
            httpClient.setHttps(true);
            httpClient.setXmlParam(xmlParam);//请求体
            httpClient.post();
            //3.调用httpclient获取响应内容，解析出里面的code_url
            String content = httpClient.getContent();//响应的结果
            System.out.println(content);
            Map<String, String> wxmap = WXPayUtil.xmlToMap(content);
            //4.返回map(订单号，金额  code_url);
            HashMap<String, String> resultMap = new HashMap<>();
            resultMap.put("out_trade_no",out_trade_no);
            resultMap.put("total_fee",total_fee);
            resultMap.put("code_url",wxmap.get("code_url"));
            return resultMap;
        } catch (Exception e) {
            e.printStackTrace();
            return  new HashMap<>();
        }
    }

    @Override
    public Map<String, String> queryStatus(String out_trade_no) {
        try {
            //1.组合参数 到map中
            Map<String,String> map = new HashMap<>();//参数map
            map.put("appid","wx8397f8696b538317");
            map.put("mch_id","1473426802");
            map.put("out_trade_no",out_trade_no);
            map.put("nonce_str", WXPayUtil.generateNonceStr());
            String xmlParam = WXPayUtil.generateSignedXml(map, "T6m9iK73b0kn9g5v426MKfHQH7X8rKwb");//商户的签名
            //2.调用httpclient 模拟浏览器发送请求
            HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/orderquery");
            httpClient.setHttps(true);
            httpClient.setXmlParam(xmlParam);
            httpClient.post();
            //3.调用httpclient获取响应内容，解析出里面的支付的状态
            String content = httpClient.getContent();//xml
            System.out.println("支付的状态查询字符串："+content);
            //4.返回
            Map<String, String> resultMap = WXPayUtil.xmlToMap(content);
            return resultMap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Map<String, String> closePay(String out_trade_no) {
        try {
            //1.组合参数 到map中
            Map<String,String> map = new HashMap<>();//参数map
            map.put("appid","wx8397f8696b538317");
            map.put("mch_id","1473426802");
            map.put("out_trade_no",out_trade_no);
            map.put("nonce_str", WXPayUtil.generateNonceStr());
            String xmlParam = WXPayUtil.generateSignedXml(map, "T6m9iK73b0kn9g5v426MKfHQH7X8rKwb");//商户的签名
            //2.调用httpclient 模拟浏览器发送请求
            HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/closeorder");
            httpClient.setHttps(true);
            httpClient.setXmlParam(xmlParam);
            httpClient.post();
            //3.调用httpclient获取响应内容，解析出里面的支付的状态
            String content = httpClient.getContent();//xml
            System.out.println("关闭订单的结果："+content);
            //4.返回
            Map<String, String> resultMap = WXPayUtil.xmlToMap(content);
            return resultMap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


}
