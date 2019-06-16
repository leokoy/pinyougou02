package com.pinyougou.cart.service;

import entity.Cart;

import java.util.List; /**
 * 描述
 *
 * @author 三国的包子
 * @version 1.0
 * @package com.pinyougou.cart.service *
 * @since 1.0
 */
public interface CartService {
    /**
     * 向已有的购物车中 添加商品  返回最新的购物车的列表
     * @param cartList  已有的购物车列表
     * @param itemId  要添加的商品 ID
     * @param num 要添加的商品的数量
     * @return
     */
    List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, Integer num);

    List<Cart> getCartListFromRedis(String name);

    void saveToRedis(String name, List<Cart> cartListNew);

    /**
     * 合并购物车 ：将cookie中的购物车合并到redis中的购物车中
     * @param cookieList  cookie中的购物车数据
     * @param cartListFromRedis redis中的购物车数据
     * @return
     */
    List<Cart> merge(List<Cart> cookieList, List<Cart> cartListFromRedis);
}
