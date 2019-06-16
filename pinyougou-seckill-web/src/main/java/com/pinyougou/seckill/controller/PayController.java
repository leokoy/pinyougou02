package com.pinyougou.seckill.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pay.service.PayService;
import com.pinyougou.pojo.TbSeckillOrder;
import com.pinyougou.seckill.service.SeckillOrderService;
import entity.Result;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
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
    private SeckillOrderService seckillOrderService;

    @RequestMapping("/createNative")
    public Map createNative(){
        //2.从 redis中获取当前的用户所对应的秒杀订单（订单号，有金额）
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();

        TbSeckillOrder seckillOrder = seckillOrderService.getOrderByUserId(userId);
        if(seckillOrder!=null) {
            double v = seckillOrder.getMoney().doubleValue();
            long totalMoney = (long) (v * 100);
            //3.调用统一下单的API(调用服务的方法)
            Map<String, String> map = payService.createNative(seckillOrder.getId() + "", totalMoney + "");
            //4.返回 订单号，有金额 有二维码连接
            return map;
        }
        return new HashMap();
    }

    @RequestMapping("/queryStatus")
    public Result queryStatus(String out_trade_no){
        Result result = new Result(false,"支付失败");
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        try {
            int count=0;
            //1.调用支付的服务 不停的查询 状态
            while(true){
               Map<String,String>  resultMap = payService.queryStatus(out_trade_no);
               count++;

               if(count>=100){
                   result=new Result(false,"支付超时");
                   //1.关闭微信的订单号
                Map<String,String> closeMap= payService.closePay(out_trade_no);
               if(closeMap==null){
                    //不需要
               }else  if("ORDERPAID".equals(closeMap.get("err_code"))){
                   seckillOrderService.updateOrderStatus(resultMap.get("transaction_id"),userId);
               }else if("SUCCESS".equals(closeMap.get("result_code")) || "ORDERCLOSED".equals(closeMap.get("err_code"))){
                   //说明关闭订单成功

                   seckillOrderService.deleteOrder(userId);
               }else{
                   //报错了之后要重新关闭订单
               }

                   break;
               }
               Thread.sleep(3000);

               //如果超时5分钟就直接退出。

               if("SUCCESS".equals(resultMap.get("trade_state"))){
                   result=new Result(true,"支付成功");

                   seckillOrderService.updateOrderStatus(resultMap.get("transaction_id"),userId);
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
