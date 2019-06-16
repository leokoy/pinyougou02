package com.pinyougou.sellergoods.service.impl;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.apache.commons.lang3.StringUtils;
import com.pinyougou.core.service.CoreServiceImpl;

import org.springframework.data.redis.core.RedisTemplate;
import tk.mybatis.mapper.entity.Example;

import com.pinyougou.mapper.TbItemCatMapper;
import com.pinyougou.pojo.TbItemCat;

import com.pinyougou.sellergoods.service.ItemCatService;


/**
 * 服务实现层
 *
 * @author Administrator
 */
@Service
public class ItemCatServiceImpl extends CoreServiceImpl<TbItemCat> implements ItemCatService {

    //add  update  delete  select
    @Autowired
    private TbItemCatMapper itemCatMapper;

    @Autowired
    public ItemCatServiceImpl(TbItemCatMapper itemCatMapper) {
        super(itemCatMapper, TbItemCat.class);
        this.itemCatMapper = itemCatMapper;
    }


    @Override
    public PageInfo<TbItemCat> findPage(Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        List<TbItemCat> all = itemCatMapper.selectAll();
        PageInfo<TbItemCat> info = new PageInfo<TbItemCat>(all);

        //序列化再反序列化
        String s = JSON.toJSONString(info);
        PageInfo<TbItemCat> pageInfo = JSON.parseObject(s, PageInfo.class);
        return pageInfo;
    }


    @Override
    public PageInfo<TbItemCat> findPage(Integer pageNo, Integer pageSize, TbItemCat itemCat) {
        PageHelper.startPage(pageNo, pageSize);

        Example example = new Example(TbItemCat.class);
        Example.Criteria criteria = example.createCriteria();

        if (itemCat != null) {
            if (StringUtils.isNotBlank(itemCat.getName())) {
                criteria.andLike("name", "%" + itemCat.getName() + "%");
                //criteria.andNameLike("%"+itemCat.getName()+"%");
            }
            //分类审核查询
            if (StringUtils.isNoneBlank(itemCat.getStatus())) {
                    //查询未审核 审核未通过分类列表
                    criteria.andNotEqualTo("status", 1);
            }
            //筛选商家申请的分类列表
            if (StringUtils.isNoneBlank(itemCat.getSellerId())){
                criteria.andEqualTo("sellerId", itemCat.getSellerId());
            }

        }
        List<TbItemCat> all = itemCatMapper.selectByExample(example);
        PageInfo<TbItemCat> info = new PageInfo<TbItemCat>(all);
        //序列化再反序列化
        String s = JSON.toJSONString(info);
        PageInfo<TbItemCat> pageInfo = JSON.parseObject(s, PageInfo.class);

        return pageInfo;
    }

    //新建之后调用该方法
    //修改之后调用该方法
    //删除之后调用该方法

    //1.加入redis的依赖
    //2.配置redis的配置文件
    //3.注入redisTemplate 调用方法

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public List<TbItemCat> findByParentId(Long parentId) {
        TbItemCat itemCat = new TbItemCat();
        itemCat.setParentId(parentId);
        List<TbItemCat> select = itemCatMapper.select(itemCat);

        //
        TbItemCat tbItemCat = new TbItemCat();
        tbItemCat.setStatus("1");
        List<TbItemCat> all = itemCatMapper.select(tbItemCat);
        for (TbItemCat cat : all) {
            redisTemplate.boundHashOps("itemCat").put(cat.getName(),cat.getTypeId());
        }
        return select;
    }

    /**
     * 描述：批量修改分类审核状态[苏红霖]2019.6.15
     *
     * @param ids    分类id
     * @param status 审核状态
     * @return 修改结果
     */
    @Override
    public void updateStfatus(Long[] ids, String status) {
        //update tb_brand set status = ? where id in(?,?,?)

        //设置条件
        Example example = new Example(TbItemCat.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andIn("id", Arrays.asList(ids));

        //更新状态
        TbItemCat tbItemCat = new TbItemCat();
        tbItemCat.setStatus(status);
        itemCatMapper.updateByExampleSelective(tbItemCat, example);

    }
}
