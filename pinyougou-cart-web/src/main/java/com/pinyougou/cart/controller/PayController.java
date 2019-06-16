package com.pinyougou.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.common.util.IdWorker;
import com.pinyougou.order.service.OrderService;
import com.pinyougou.pay.service.PayService;
import com.pinyougou.pojo.TbPayLog;
import entity.Result;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 描述
 *
 * @author 三国的包子
 * @version 1.0
 * @package com.pinyougou.cart.controller *
 * @since 1.0
 */
@RestController
@RequestMapping("/pay")
public class PayController {
    @Reference
    private PayService payService;

    @Reference
    private OrderService orderService;

    @RequestMapping("/createNative")
    public Map createNative(){
        //1.生成支付订单号
        //String out_trade_no = new IdWorker(0, 1).nextId()+"";
        //2.获取支付的金额
       // String total_fee="1";

        //获取redis中根据用户的ID 获取到该用户的支付日志记录  获取到订单号和金额
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        TbPayLog payLog =  orderService.getPayLogFromRedis(userId);

        //3.调用统一下单的API(调用服务的方法)
        Map<String,String> map = payService.createNative(payLog.getOutTradeNo(),payLog.getTotalFee()+"");
        //4.返回 订单号，有金额 有二维码连接
        return map;
    }

    @RequestMapping("/queryStatus")
    public Result queryStatus(String out_trade_no){
        Result result = new Result(false,"支付失败");
        try {
            int count=0;
            //1.调用支付的服务 不停的查询 状态
            while(true){
               Map<String,String>  resultMap = payService.queryStatus(out_trade_no);
               count++;

               if(count>=100){
                   result=new Result(false,"支付超时");
                   break;
               }
               Thread.sleep(3000);

               //如果超时5分钟就直接退出。

               if("SUCCESS".equals(resultMap.get("trade_state"))){


                   result=new Result(true,"支付成功");

                   //1.更新 支付日志的记录（交易流水  交易的状态  交易的时间）
                   //2.更新 支付日志记录  关联到的订单的 状态 和支付时间
                   //3.删除 该用户的redis中的支付日志

                   orderService.updateStatus(resultMap.get("transaction_id"),out_trade_no);


                   break;
               }
            }
            //2.返回结果
            return result;

        } catch (InterruptedException e) {
            e.printStackTrace();
            return new Result(false,"支付失败");
        }
    }
}
