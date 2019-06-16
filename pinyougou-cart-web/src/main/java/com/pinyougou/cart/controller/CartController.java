package com.pinyougou.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.common.util.CookieUtil;
import entity.Cart;
import entity.Result;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * 描述
 *
 * @author 三国的包子
 * @version 1.0
 * @package com.pinyougou.cart.controller *
 * @since 1.0
 */
@RestController
@RequestMapping("/cart")
public class CartController {

    @Reference
    private CartService cartService;


    /**
     * 获取所有的购物车数据列表 展示给页面
     * @return
     */
    @RequestMapping("/findCartList")
    public List<Cart> findCartList(HttpServletRequest request,HttpServletResponse response){
        //
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        if("anonymousUser".equals(name)) {
            //没有登录  从cookie中获取购物车列表数据

            String cartListstr = CookieUtil.getCookieValue(request, "cartList", true);
            if(StringUtils.isEmpty(cartListstr)){
                cartListstr="[]";
            }
            List<Cart> cookieList = JSON.parseArray(cartListstr, Cart.class);

            return cookieList;

        }else{
            //已经登录  从redis中获取购物车列表
            List<Cart> cartListFromRedis = cartService.getCartListFromRedis(name);
            if(cartListFromRedis==null){
                cartListFromRedis = new ArrayList<>();
            }
            //cookie的数据合并redis中。
            //1.获取到cookie中的购物车数据
            String cartListstr = CookieUtil.getCookieValue(request, "cartList", true);
            if(StringUtils.isEmpty(cartListstr)){
                cartListstr="[]";
            }
            List<Cart> cookieList = JSON.parseArray(cartListstr, Cart.class);


            //2.获取到redis中的购物车数据
            //cartListFromRedis
            //3.合并数据 返回一个最新的购物车的列表
            List<Cart> cartListNewMost = cartService.merge(cookieList,cartListFromRedis);
            //4.将最新的购物车的数据保存回redis中
            if(cartListNewMost==null){
                cartListNewMost = new ArrayList<>();
            }
            cartService.saveToRedis(name,cartListNewMost);
            //5.删除cookie中的购物车数据
            CookieUtil.deleteCookie(request,response,"cartList");
            //6.返回给页面最新的购物车的列表数据
            return cartListNewMost;
        }
    }




    /**
     * 添加购物车：
     * 请求：/addGoodsToCartList
     * 参数：itemId,num
     * 返回值：result  成功与否
     * @return
     */
    @RequestMapping("/addGoodsToCartList")
    @CrossOrigin(origins = {"http://localhost:9105"},allowCredentials = "true")
    public Result addGoodsToCartList(Long itemId, Integer num, HttpServletRequest request, HttpServletResponse response){
        //表示 服务器 资源允许 指定的域 来访问。
        /*response.setHeader("Access-Control-Allow-Origin", "http://localhost:9105");
        //同意客户端9105携带cookie
        response.setHeader("Access-Control-Allow-Credentials", "true");//*/
        try {
            String name = SecurityContextHolder.getContext().getAuthentication().getName();
            System.out.println(name);//anonymousUser
            if("anonymousUser".equals(name)) {
                //1.判断用户是否已经登录 如果 没有登录
                System.out.println("没登录");


                //1.1 先要从cookie中获取已有的购物车列表
                String cartListstr = CookieUtil.getCookieValue(request, "cartList", true);
                if(StringUtils.isEmpty(cartListstr)){
                    cartListstr="[]";
                }
                List<Cart> cookieList = JSON.parseArray(cartListstr, Cart.class);
                //1.2 向已有的购物车中添加商品  返回一个最新的购物车列表
               List<Cart> cartListNew= cartService.addGoodsToCartList(cookieList,itemId,num);
               if(cartListNew==null){
                   cartListNew = new ArrayList<>();
               }
                //1.3 将最新的购物车列表数据重新写入到cookie中
               CookieUtil.setCookie(request,response,"cartList", JSON.toJSONString(cartListNew),7*24*3600,true);

            }else {
                //2.判断用户是否已经登录 如果 已经登录

                //2.1 先要从redis中获取已有的购物车列表

                List<Cart> cartListFromRedis = cartService.getCartListFromRedis(name);
                if(cartListFromRedis==null){
                    cartListFromRedis = new ArrayList<>();
                }

                //2.2 向已有的购物车中添加商品  返回一个最新的购物车列表
                List<Cart> cartListNew= cartService.addGoodsToCartList(cartListFromRedis,itemId,num);

                if(cartListNew==null){
                    cartListNew = new ArrayList<>();
                }

                //2.3 将最新的购物车列表数据重新写入到redis中  key value
                cartService.saveToRedis(name,cartListNew);

                System.out.println("已经登录");
            }
            return new Result(true,"添加成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"失败");
        }
    }
}
