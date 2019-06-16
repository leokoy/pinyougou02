package com.pinyougou.sellergoods.service;
import java.util.List;
import java.util.Map;

import com.pinyougou.pojo.TbTypeTemplate;

import com.github.pagehelper.PageInfo;
import com.pinyougou.core.service.CoreService;
/**
 * 服务层接口
 * @author Administrator
 *
 */
public interface TypeTemplateService extends CoreService<TbTypeTemplate> {
	
	
	
	/**
	 * 返回分页列表
	 * @return
	 */
	 PageInfo<TbTypeTemplate> findPage(Integer pageNo, Integer pageSize);
	
	

	/**
	 * 分页
	 * @param pageNo 当前页 码
	 * @param pageSize 每页记录数
	 * @return
	 */
	PageInfo<TbTypeTemplate> findPage(Integer pageNo, Integer pageSize, TbTypeTemplate TypeTemplate);

	//根据模板的ID  获取模板的对象 中的规格的数据 拼接成格式：[{id:27,'text':'网络',options:[{},{}]},{}]
    List<Map> findSpecList(Long typeTmplateId);

	/**
	 * 描述：批量修改模板审核状态[苏红霖]2019.6.14
	 *
	 * @param ids    模板id
	 * @param status 审核状态
	 * @return 修改结果
	 */
    void updateStatus(Long[] ids, String status);

}
