package com.pinyougou.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pinyougou.core.service.CoreServiceImpl;
import com.pinyougou.mapper.TbBrandMapper;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.sellergoods.service.BrandService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

import java.util.Arrays;
import java.util.List;


/**
 * 服务实现层
 * 苏红霖 2019.6.14
 * @author Administrator
 */
@Service
public class BrandServiceImpl extends CoreServiceImpl<TbBrand> implements BrandService {

    @Autowired
    private TbBrandMapper brandMapper;

    @Autowired
    public BrandServiceImpl(TbBrandMapper brandMapper) {
        super(brandMapper, TbBrand.class);
        this.brandMapper = brandMapper;
    }

    /**
     *
     * @param pageNo   当前页 码
     * @param pageSize 每页记录数
     * @param brand
     * @return
     */
    @Override
    public PageInfo<TbBrand> findPage(Integer pageNo, Integer pageSize, TbBrand brand) {
        PageHelper.startPage(pageNo, pageSize);

        Example example = new Example(TbBrand.class);
        Example.Criteria criteria = example.createCriteria();

        if (brand != null) {
            if (StringUtils.isNotBlank(brand.getName())) {
                criteria.andLike("name", "%" + brand.getName() + "%");
                //criteria.andNameLike("%"+brand.getName()+"%");
            }
            if (StringUtils.isNotBlank(brand.getFirstChar())) {
                criteria.andLike("firstChar", "%" + brand.getFirstChar() + "%");
                //criteria.andFirstCharLike("%"+brand.getFirstChar()+"%");
            }
            //品牌审核
            if (StringUtils.isNoneBlank(brand.getStatus())) {
                //查询审核通过品牌列列表
                if ("1".equals(brand.getStatus())) {
                    criteria.andEqualTo("status", brand.getStatus());
                } else {
                    //查询未审核 审核未通过品牌列表
                    criteria.andNotEqualTo("status", 1);
                }
            }
            //筛选商家申请的品牌列表
            if (StringUtils.isNoneBlank(brand.getSellerId())){
                criteria.andEqualTo("sellerId", brand.getSellerId());
            }


        }
        List<TbBrand> all = brandMapper.selectByExample(example);
        PageInfo<TbBrand> info = new PageInfo<TbBrand>(all);
        //序列化再反序列化
        String s = JSON.toJSONString(info);
        PageInfo<TbBrand> pageInfo = JSON.parseObject(s, PageInfo.class);

        return pageInfo;
    }

    /**
     * 描述：批量修改品牌审核状态[苏红霖]
     *
     * @param ids    品牌id
     * @param status 审核状态
     * @return 修改结果
     */
    @Override
    public void updateStatus(Long[] ids, String status) {
        //update tb_brand set status = ? where id in(?,?,?)

        //设置条件
        Example example = new Example(TbBrand.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andIn("id", Arrays.asList(ids));

        //更新状态
        TbBrand tbBrand = new TbBrand();
        tbBrand.setStatus(status);
        brandMapper.updateByExampleSelective(tbBrand, example);


    }
}
