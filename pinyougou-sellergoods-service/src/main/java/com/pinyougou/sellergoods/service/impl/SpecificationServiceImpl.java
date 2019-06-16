package com.pinyougou.sellergoods.service.impl;

import java.util.Arrays;
import java.util.List;

import com.pinyougou.mapper.TbSpecificationOptionMapper;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.pojo.TbSpecificationOption;
import entity.Specification;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.apache.commons.lang3.StringUtils;
import com.pinyougou.core.service.CoreServiceImpl;

import tk.mybatis.mapper.entity.Example;

import com.pinyougou.mapper.TbSpecificationMapper;
import com.pinyougou.pojo.TbSpecification;

import com.pinyougou.sellergoods.service.SpecificationService;


/**
 * 服务实现层[苏红霖 2019.6.14]
 *
 * @author Administrator
 */
@Service
public class SpecificationServiceImpl extends CoreServiceImpl<TbSpecification> implements SpecificationService {

    @Autowired
    private TbSpecificationMapper specificationMapper;

    @Autowired
    private TbSpecificationOptionMapper optionMapper;

    @Autowired
    public SpecificationServiceImpl(TbSpecificationMapper specificationMapper) {
        super(specificationMapper, TbSpecification.class);
        this.specificationMapper = specificationMapper;
    }

    @Override
    public void add(Specification record) {
        //1.获取规格的数据
        TbSpecification specification = record.getSpecification();
        //2.获取规格的选项的列表数据
        List<TbSpecificationOption> optionList = record.getOptionList();
        //3.插入到 两个表中
        //如果是用通用的mapper  添加了主键 注解  @id  自动获取主键的值 放入到 pojo中的ID 属性中
        specificationMapper.insert(specification);

        for (TbSpecificationOption tbSpecificationOption : optionList) {
            tbSpecificationOption.setSpecId(specification.getId());//设置规格的id
            optionMapper.insert(tbSpecificationOption);
        }


    }

    @Override
    public PageInfo<TbSpecification> findPage(Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        List<TbSpecification> all = specificationMapper.selectAll();
        PageInfo<TbSpecification> info = new PageInfo<TbSpecification>(all);

        //序列化再反序列化
        String s = JSON.toJSONString(info);
        PageInfo<TbSpecification> pageInfo = JSON.parseObject(s, PageInfo.class);
        return pageInfo;
    }


    @Override
    public PageInfo<TbSpecification> findPage(Integer pageNo, Integer pageSize, TbSpecification specification) {
        PageHelper.startPage(pageNo, pageSize);

        Example example = new Example(TbSpecification.class);
        Example.Criteria criteria = example.createCriteria();

        if (specification != null) {
            if (StringUtils.isNotBlank(specification.getSpecName())) {
                criteria.andLike("specName", "%" + specification.getSpecName() + "%");
                //criteria.andSpecNameLike("%"+specification.getSpecName()+"%");
            }
            //规格表审核状态查询
            if (StringUtils.isNoneBlank(specification.getStatus())) {
                //审核通过
                if ("1".equals(specification.getStatus())) {
                    criteria.andEqualTo("status", specification.getStatus());
                } else {
                    //审核未通过
                    criteria.andNotEqualTo("status", 1);
                }
            }
            //查询该商家提交的规格
            if (StringUtils.isNoneBlank(specification.getSellerId())){
                criteria.andEqualTo("sellerId", specification.getSellerId());
            }

        }
        List<TbSpecification> all = specificationMapper.selectByExample(example);
        PageInfo<TbSpecification> info = new PageInfo<TbSpecification>(all);
        //序列化再反序列化
        String s = JSON.toJSONString(info);
        PageInfo<TbSpecification> pageInfo = JSON.parseObject(s, PageInfo.class);

        return pageInfo;
    }

    /**
     * 描述：批量修改规格审核状态[苏红霖]
     *
     * @param ids    规格id
     * @param status 审核状态
     * @return 修改结果
     */
    @Override
    public void updateStatus(Long[] ids, String status) {
        //update tb_specifcation set status = ? where id in(?,?,?)

        //设置条件
        Example example = new Example(TbSpecificationOption.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andIn("id", Arrays.asList(ids));

        //更新状态
        TbSpecification tbSpecification = new TbSpecification();
        tbSpecification.setStatus(status);
        specificationMapper.updateByExampleSelective(tbSpecification, example);
    }

    /**
     * 描述：根据规格ID,删除规格选项表
     *
     * @param ids   规格ID
     */
    @Override
    public void deleteOption(Long[] ids) {
        Example example = new Example(TbSpecificationOption.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andIn("specId",Arrays.asList(ids));
        optionMapper.deleteByExample(example);
    }
}
