package com.pinyougou.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbOrderItem;
import entity.Cart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 描述
 *
 * @author 三国的包子
 * @version 1.0
 * @package com.pinyougou.cart.service.impl *
 * @since 1.0
 */
@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private TbItemMapper tbItemMapper;

    @Override
    public List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, Integer num) {
        //1.先根据商品的ID 获取商品的数据
        TbItem tbItem = tbItemMapper.selectByPrimaryKey(itemId);
        //2.获取商品中的商家的ID
        String sellerId = tbItem.getSellerId();
        //3.判断 要添加的商品 是否在已有的购物车中的 商家ID 存在  如果 不存在  直接添加商品。
        Cart cart = searchBySellerId(cartList,sellerId);
        if(cart==null){
            cart = new Cart();
            cart.setSellerId(sellerId);
            cart.setSellerName(tbItem.getSeller());//商家的名称

            List<TbOrderItem> orderitemList = new ArrayList<>();

            TbOrderItem orderItem = new TbOrderItem();//要添加的商品的数据所封装的POJO

            orderItem.setSellerId(sellerId);
            orderItem.setPicPath(tbItem.getImage());
            orderItem.setNum(num);
            orderItem.setPrice(tbItem.getPrice());
            double v = tbItem.getPrice().doubleValue() * num;
            orderItem.setTotalFee(new BigDecimal(v));//小计
            orderItem.setTitle(tbItem.getTitle());
            orderItem.setGoodsId(tbItem.getGoodsId());
            orderItem.setItemId(itemId);

            orderitemList.add(orderItem);

            cart.setOrderItemList(orderitemList);

            cartList.add(cart);

        }else {
            //4 判断 要添加的商品 是否在已有的购物车中的 商家ID 存在  如果 存在
            List<TbOrderItem> orderItemList = cart.getOrderItemList();//明细列表
            TbOrderItem tbOrderItemfind = searchByItemId(orderItemList,itemId);

            if(tbOrderItemfind==null) {
                //4.1 判断 要添加的商品 是否在 商家下的明细列表中是否存在  如果 不存在  直接添加商品
                tbOrderItemfind = new TbOrderItem();//要添加的商品的数据所封装的POJO

                tbOrderItemfind.setSellerId(sellerId);
                tbOrderItemfind.setPicPath(tbItem.getImage());
                tbOrderItemfind.setNum(num);
                tbOrderItemfind.setPrice(tbItem.getPrice());
                double v = tbItem.getPrice().doubleValue() * num;
                tbOrderItemfind.setTotalFee(new BigDecimal(v));//小计
                tbOrderItemfind.setTitle(tbItem.getTitle());
                tbOrderItemfind.setGoodsId(tbItem.getGoodsId());
                tbOrderItemfind.setItemId(itemId);

                orderItemList.add(tbOrderItemfind);

            }else {
                //4.2 判断 要添加的商品 是否在 商家下的明细列表中是否存在  如果 存在    数量相加

                tbOrderItemfind.setNum(tbOrderItemfind.getNum()+num);
                double v = tbOrderItemfind.getPrice().doubleValue() * tbOrderItemfind.getNum();
                tbOrderItemfind.setTotalFee(new BigDecimal(v));

                if(tbOrderItemfind.getNum()<=0){
                    //移除商品
                    orderItemList.remove(tbOrderItemfind);//[]
                }
                //判断如果没有买商品 移除掉商家的信息
                if(orderItemList.size()<=0){
                    cartList.remove(cart);
                }

            }
        }

        return cartList;
    }

    @Autowired
    private RedisTemplate redisTemplate;
    @Override
    public List<Cart> getCartListFromRedis(String name) {
        List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("Redis_CartList").get(name);
        return cartList;
    }

    @Override
    public void saveToRedis(String name, List<Cart> cartListNew) {
        redisTemplate.boundHashOps("Redis_CartList").put(name,cartListNew);
    }

    @Override
    public List<Cart> merge(List<Cart> cookieList, List<Cart> cartListFromRedis) {
        for (Cart cart : cookieList) {
            List<TbOrderItem> orderItemList = cart.getOrderItemList();
            for (TbOrderItem orderItem : orderItemList) {
                //orderItem 就是要添加的商品对象

                cartListFromRedis = addGoodsToCartList(cartListFromRedis, orderItem.getItemId(), orderItem.getNum());
            }
        }

        return cartListFromRedis;
    }

    /**
     * 在明细列表中查询 要添加的商品是否存在
     * @param orderItemList
     * @param itemId
     * @return
     */
    private TbOrderItem searchByItemId(List<TbOrderItem> orderItemList, Long itemId) {
        for (TbOrderItem orderItem : orderItemList) {
            if (orderItem.getItemId()==itemId.longValue()) {//找到了
                return orderItem;
            }
        }
        return null;
    }

    /**
     * 从已有的购物车中查询 商家的ID 是否存在
     * @param cartList
     * @param sellerId
     * @return
     */
    private Cart searchBySellerId(List<Cart> cartList, String sellerId) {

        for (Cart cart : cartList) {
            if (cart.getSellerId().equals(sellerId)) {
                return cart;
            }
        }
        return null;
    }
}
