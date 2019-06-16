package com.pinyougou.seckill.service.impl;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.pinyougou.common.pojo.SysConstants;
import com.pinyougou.common.util.IdWorker;
import com.pinyougou.mapper.TbSeckillGoodsMapper;
import com.pinyougou.pojo.TbSeckillGoods;
import com.pinyougou.seckill.pojo.SeckillStatus;
import com.pinyougou.seckill.service.SeckillOrderService;
import com.pinyougou.seckill.thread.CreateOrderThreadHandler;
import org.apache.zookeeper.Op;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo; 									  
import org.apache.commons.lang3.StringUtils;
import com.pinyougou.core.service.CoreServiceImpl;

import org.springframework.data.redis.core.RedisTemplate;
import tk.mybatis.mapper.entity.Example;

import com.pinyougou.mapper.TbSeckillOrderMapper;
import com.pinyougou.pojo.TbSeckillOrder;  





/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class SeckillOrderServiceImpl extends CoreServiceImpl<TbSeckillOrder>  implements SeckillOrderService {

	
	private TbSeckillOrderMapper seckillOrderMapper;

	@Autowired
	public SeckillOrderServiceImpl(TbSeckillOrderMapper seckillOrderMapper) {
		super(seckillOrderMapper, TbSeckillOrder.class);
		this.seckillOrderMapper=seckillOrderMapper;
	}

	
	

	
	@Override
    public PageInfo<TbSeckillOrder> findPage(Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo,pageSize);
        List<TbSeckillOrder> all = seckillOrderMapper.selectAll();
        PageInfo<TbSeckillOrder> info = new PageInfo<TbSeckillOrder>(all);

        //序列化再反序列化
        String s = JSON.toJSONString(info);
        PageInfo<TbSeckillOrder> pageInfo = JSON.parseObject(s, PageInfo.class);
        return pageInfo;
    }

	
	

	 @Override
    public PageInfo<TbSeckillOrder> findPage(Integer pageNo, Integer pageSize, TbSeckillOrder seckillOrder) {
        PageHelper.startPage(pageNo,pageSize);

        Example example = new Example(TbSeckillOrder.class);
        Example.Criteria criteria = example.createCriteria();

        if(seckillOrder!=null){			
						if(StringUtils.isNotBlank(seckillOrder.getUserId())){
				criteria.andLike("userId","%"+seckillOrder.getUserId()+"%");
				//criteria.andUserIdLike("%"+seckillOrder.getUserId()+"%");
			}
			if(StringUtils.isNotBlank(seckillOrder.getSellerId())){
				criteria.andLike("sellerId","%"+seckillOrder.getSellerId()+"%");
				//criteria.andSellerIdLike("%"+seckillOrder.getSellerId()+"%");
			}
			if(StringUtils.isNotBlank(seckillOrder.getStatus())){
				criteria.andLike("status","%"+seckillOrder.getStatus()+"%");
				//criteria.andStatusLike("%"+seckillOrder.getStatus()+"%");
			}
			if(StringUtils.isNotBlank(seckillOrder.getReceiverAddress())){
				criteria.andLike("receiverAddress","%"+seckillOrder.getReceiverAddress()+"%");
				//criteria.andReceiverAddressLike("%"+seckillOrder.getReceiverAddress()+"%");
			}
			if(StringUtils.isNotBlank(seckillOrder.getReceiverMobile())){
				criteria.andLike("receiverMobile","%"+seckillOrder.getReceiverMobile()+"%");
				//criteria.andReceiverMobileLike("%"+seckillOrder.getReceiverMobile()+"%");
			}
			if(StringUtils.isNotBlank(seckillOrder.getReceiver())){
				criteria.andLike("receiver","%"+seckillOrder.getReceiver()+"%");
				//criteria.andReceiverLike("%"+seckillOrder.getReceiver()+"%");
			}
			if(StringUtils.isNotBlank(seckillOrder.getTransactionId())){
				criteria.andLike("transactionId","%"+seckillOrder.getTransactionId()+"%");
				//criteria.andTransactionIdLike("%"+seckillOrder.getTransactionId()+"%");
			}
	
		}
        List<TbSeckillOrder> all = seckillOrderMapper.selectByExample(example);
        PageInfo<TbSeckillOrder> info = new PageInfo<TbSeckillOrder>(all);
        //序列化再反序列化
        String s = JSON.toJSONString(info);
        PageInfo<TbSeckillOrder> pageInfo = JSON.parseObject(s, PageInfo.class);

        return pageInfo;
    }

    @Autowired
	private RedisTemplate redisTemplate;

	@Autowired

	private TbSeckillGoodsMapper tbSeckillGoodsMapper;

	@Autowired
	private TbSeckillOrderMapper tbSeckillOrderMapper;

	@Autowired
	private CreateOrderThreadHandler handler;

    @Override
    public void submitOrder(Long id, String userId) {
		//1.先从redis中根据秒杀商品的ID 获取秒杀商品的数据


		/*TbSeckillGoods seckillGoods = (TbSeckillGoods) redisTemplate.boundHashOps(SysConstants.SEC_KILL_GOODS).get(id);


		if(seckillGoods==null || seckillGoods.getStockCount()<=0) {
			//2.判断 商品是否存在   如果商品不存在  或者 库存为0   说明商品已经卖完
			throw new RuntimeException("商品已经售罄");
		}*/


		//1.判断用户是否有存在未支付的订单  如果有
		if(redisTemplate.boundHashOps(SysConstants.SEC_KILL_ORDER).get(userId)!=null){
			throw new RuntimeException("有未支付的订单");
		}
		//2.判断如果用户已经在排队中 （订单正在创建）

		if(redisTemplate.boundHashOps(SysConstants.SEC_USER_QUEUE_FLAG_KEY).get(userId)!=null){
			throw new RuntimeException("正在排队，请稍等");
		}




		//3.从队列中弹出元素。  如果 元素为null 说明卖完了。
		Object seckillGoods11 = redisTemplate.boundListOps(SysConstants.SEC_KILL_GOODS_PREFIX + id).rightPop();
		if(seckillGoods11==null){
			throw new RuntimeException("商品已经售罄");
		}


		//将用户压入队列 进行排队
		redisTemplate.boundListOps(SysConstants.SEC_KILL_USER_ORDER_LIST).leftPush(new SeckillStatus(userId,id,SeckillStatus.SECKILL_queuing));


		//将用户存储一个正在排队的标识
		redisTemplate.boundHashOps(SysConstants.SEC_USER_QUEUE_FLAG_KEY).put(userId,id);

		//执行创建订单的方法
		handler.handlerCreateOrder();
    }

	@Override
	public TbSeckillOrder getOrderByUserId(String userId) {
		return (TbSeckillOrder) redisTemplate.boundHashOps(SysConstants.SEC_KILL_ORDER).get(userId);
	}

    @Override
    public void updateOrderStatus(String transaction_id, String userId) {
		//1.获取到redis中未支付订单
		TbSeckillOrder seckillOrder = getOrderByUserId(userId);
		if (seckillOrder != null) {
			//2.设置redis中的订单的状态为已支付
			seckillOrder.setStatus("1");
			seckillOrder.setPayTime(new Date());
			seckillOrder.setTransactionId(transaction_id);
			//3.将redis中的订单存储到mysql中
			tbSeckillOrderMapper.insert(seckillOrder);
			//4.redis中的订单删除。
			redisTemplate.boundHashOps(SysConstants.SEC_KILL_ORDER).delete(userId);
		}
    }

	@Override
	public void deleteOrder(String userId) {
		//2.删除redis中的该用户对应的未支付的订单
		TbSeckillOrder seckillOrder = getOrderByUserId(userId);
		//3.恢复库存
		Long seckillId = seckillOrder.getSeckillId();//订单对应的秒杀商品的ID

		TbSeckillGoods  tbSeckillGoods = (TbSeckillGoods) redisTemplate.boundHashOps(SysConstants.SEC_KILL_GOODS).get(seckillId);
		if(tbSeckillGoods==null){
			//从数据库中获取商品的数据
			TbSeckillGoods tbSeckillGoods1 = tbSeckillGoodsMapper.selectByPrimaryKey(seckillId);
			//恢复库存
			tbSeckillGoods1.setStockCount(tbSeckillGoods.getStockCount()+1);
			//存储redis中
			redisTemplate.boundHashOps(SysConstants.SEC_KILL_GOODS).put(seckillId,tbSeckillGoods1);
		}else{
			tbSeckillGoods.setStockCount(tbSeckillGoods.getStockCount()+1);
			redisTemplate.boundHashOps(SysConstants.SEC_KILL_GOODS).put(seckillId,tbSeckillGoods);
		}
		//4.恢复队列的元素

		redisTemplate.boundListOps(SysConstants.SEC_KILL_GOODS_PREFIX + seckillId).leftPush(seckillId);


	}


}
