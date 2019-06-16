package com.pinyougou.sellergoods.service;
import java.util.List;
import com.pinyougou.pojo.TbGoods;

import com.github.pagehelper.PageInfo;
import com.pinyougou.core.service.CoreService;
import com.pinyougou.pojo.TbItem;
import entity.Goods;
/**
 * 服务层接口
 * @author Administrator
 *
 */
public interface GoodsService extends CoreService<TbGoods> {
	
	
	
	/**
	 * 返回分页列表
	 * @return
	 */
	 PageInfo<TbGoods> findPage(Integer pageNo, Integer pageSize);
	
	

	/**
	 * 分页
	 * @param pageNo 当前页 码
	 * @param pageSize 每页记录数
	 * @return
	 */
	PageInfo<TbGoods> findPage(Integer pageNo, Integer pageSize, TbGoods Goods);

	/**
	 * 接收组合对象 获取SPU  和描述  和 SKU列表
	 * @param goods
	 */
	void add(Goods goods);

	public Goods findOne(Long id);

	//更新 注意  商品的主键 一定要有
	void update(Goods goods);

    void updateStatus(Long[] ids, String status);

	/**
	 * 根据SPU的ID 的数组 查询 该SPU的下的所有的SKU的列表
	 * @param ids
	 * @return
	 */
	List<TbItem> findTbItemListByIds(Long[] ids);

}
