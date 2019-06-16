package com.pinyougou.seckill.service;
import java.util.List;
import com.pinyougou.pojo.TbSeckillOrder;

import com.github.pagehelper.PageInfo;
import com.pinyougou.core.service.CoreService;
/**
 * 服务层接口
 * @author Administrator
 *
 */
public interface SeckillOrderService extends CoreService<TbSeckillOrder> {
	
	
	
	/**
	 * 返回分页列表
	 * @return
	 */
	 PageInfo<TbSeckillOrder> findPage(Integer pageNo, Integer pageSize);
	
	

	/**
	 * 分页
	 * @param pageNo 当前页 码
	 * @param pageSize 每页记录数
	 * @return
	 */
	PageInfo<TbSeckillOrder> findPage(Integer pageNo, Integer pageSize, TbSeckillOrder SeckillOrder);

	/**
	 * 根据用户的ID 和要购买的秒杀商品的ID  创建秒杀订单
	 * @param id
	 * @param userId
	 */
    void submitOrder(Long id, String userId);

	/**
	 * 从redis中根据用户的ID 查询该用户的未支付订单
	 * @param userId
	 * @return
	 */
	TbSeckillOrder getOrderByUserId(String userId);

	/**
	 * //1.获取到redis中未支付订单
	 //2.设置redis中的订单的状态为已支付
	 //3.将redis中的订单存储到mysql中
	 //4.redis中的订单删除。
	 * @param transaction_id
	 * @param userId
	 */
    void updateOrderStatus(String transaction_id, String userId);

	/**
	 *  //2.删除redis中的该用户对应的未支付的订单
	 //3.恢复库存
	 //4.恢复队列的元素
	 * @param userId
	 */
	void deleteOrder(String userId);
}
