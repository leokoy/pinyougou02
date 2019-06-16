package com.pinyougou.sellergoods.service;

import java.util.List;

import com.pinyougou.pojo.TbSpecification;

import com.github.pagehelper.PageInfo;
import com.pinyougou.core.service.CoreService;
import entity.Specification;

/**
 * 服务层接口
 *
 * @author Administrator
 */
public interface SpecificationService extends CoreService<TbSpecification> {


    //新增的方法
    void add(Specification record);

    /**
     * 返回分页列表
     *
     * @return
     */
    PageInfo<TbSpecification> findPage(Integer pageNo, Integer pageSize);


    /**
     * 分页
     *
     * @param pageNo   当前页 码
     * @param pageSize 每页记录数
     * @return
     */
    PageInfo<TbSpecification> findPage(Integer pageNo, Integer pageSize, TbSpecification Specification);

    /**
     * 描述：批量修改规格审核状态[苏红霖]
     *
     * @param ids    规格id
     * @param status 审核状态
     * @return 修改结果
     */
    void updateStatus(Long[] ids, String status);

    /**
     * 描述：根据规格ID,删除规格选项表
     *
     * @param ids 规格ID
     */
    void deleteOption(Long[] ids);
}
