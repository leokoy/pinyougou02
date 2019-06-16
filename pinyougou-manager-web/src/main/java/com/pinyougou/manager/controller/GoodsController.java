package com.pinyougou.manager.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageInfo;
import com.pinyougou.common.pojo.MessageInfo;
import com.pinyougou.page.service.ItemPageService;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;
import com.pinyougou.sellergoods.service.GoodsService;
import entity.Goods;
import entity.Result;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.xml.stream.events.EndDocument;
import java.util.List;

/**
 * 商品controller
 * @author Administrator
 *
 */
@RestController
@RequestMapping("/goods")
public class GoodsController {

	@Reference
	private GoodsService goodsService;

	@Reference
	private ItemSearchService itemSearchService;


	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findAll")
	public List<TbGoods> findAll(){
		return goodsService.findAll();
	}
	
	
	
	@RequestMapping("/findPage")
    public PageInfo<TbGoods> findPage(@RequestParam(value = "pageNo", defaultValue = "1", required = true) Integer pageNo,
                                      @RequestParam(value = "pageSize", defaultValue = "10", required = true) Integer pageSize) {
        return goodsService.findPage(pageNo, pageSize);
    }
	
	/**
	 * 增加
	 * @param goods
	 * @return
	 */
	@RequestMapping("/add")
	public Result add(@RequestBody Goods goods){
		try {
			//获取到商家的ID
			String sellerId = SecurityContextHolder.getContext().getAuthentication().getName();
			goods.getGoods().setSellerId(sellerId);
			goodsService.add(goods);
			return new Result(true, "增加成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "增加失败");
		}
	}
	
	/**
	 * 修改
	 * @param goods
	 * @return
	 */
	@RequestMapping("/update")
	public Result update(@RequestBody Goods goods){
		try {
			goodsService.update(goods);
			return new Result(true, "修改成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "修改失败");
		}
	}	
	
	/**
	 * 获取实体
	 * @param id
	 * @return
	 */
	@RequestMapping("/findOne/{id}")
	public Goods findOne(@PathVariable(value = "id") Long id){
		return goodsService.findOne(id);		
	}
	
	/**
	 * 批量删除
	 * @param ids
	 * @return
	 */
	@RequestMapping("/delete")
	public Result delete(@RequestBody Long[] ids){
		try {
			//删除数据
			goodsService.delete(ids);


			//将这些SKU的额数据从ES中移除掉    es中类似于有 这个SQL delete from tb_item where goods_id in (1,2,3)
			//根据SPU的ID数组  删除ES中goodsId 为这些数组值的 文档数据

			//itemSearchService.deleteByIds(ids);


			MessageInfo info = new MessageInfo("Goods_Topic","goods_delete_tag","deleteStatus",ids,MessageInfo.METHOD_DELETE);


			String s = JSON.toJSONString(info);
			Message message = new Message(info.getTopic(),info.getTags(),info.getKeys(),s.getBytes());


			SendResult send = producer.send(message);
			System.out.println(send);

			return new Result(true, "删除成功"); 
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "删除失败");
		}
	}
	
	

	@RequestMapping("/search")
    public PageInfo<TbGoods> findPage(@RequestParam(value = "pageNo", defaultValue = "1", required = true) Integer pageNo,
                                      @RequestParam(value = "pageSize", defaultValue = "10", required = true) Integer pageSize,
                                      @RequestBody TbGoods goods) {
		//加一个商家的条件

        return goodsService.findPage(pageNo, pageSize, goods);
    }




	@Autowired
	private DefaultMQProducer producer;

	//审核商品  更新状态的值
	@RequestMapping("/updateStatus")
	public Result updateStatus(@RequestBody Long[] ids,String status){
		try {
			goodsService.updateStatus(ids,status);
			if("1".equals(status)){

			/*	//1. 获取被审核的先通过SPU 获取到SKU的商品的数据
				//List<TbItem> itemList = goodsService.findTbItemListByIds(ids);
					//2. 将被省的SKU的商品商品的数据  更新到 ES中。
					//2.1 引入search的服务
					//2.2调用更新索引的方法
				//itemSearchService.updateIndex(itemList);

				//3 生成静态的页面
				for (Long id : ids) {
					//根据SPU的id 查询数据库中的额数据  生成静态页面
					itemPageService.genItemHtml(id);
				}*/
				//发送消息
				List<TbItem> itemList = goodsService.findTbItemListByIds(ids);

				MessageInfo info = new MessageInfo("Goods_Topic","goods_update_tag","updateStatus",itemList,MessageInfo.METHOD_UPDATE);


				String s = JSON.toJSONString(info);
				Message message = new Message(info.getTopic(),info.getTags(),info.getKeys(),s.getBytes());

				SendResult send = producer.send(message);
				System.out.println(send);

			}

			return new Result(true,"更新成");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false,"更新失败");
		}
	}
	
}
