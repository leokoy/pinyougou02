package com.pinyougou.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pinyougou.core.service.CoreServiceImpl;
import com.pinyougou.mapper.TbSpecificationOptionMapper;
import com.pinyougou.mapper.TbTypeTemplateMapper;
import com.pinyougou.pojo.TbSpecificationOption;
import com.pinyougou.pojo.TbTypeTemplate;
import com.pinyougou.sellergoods.service.TypeTemplateService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import tk.mybatis.mapper.entity.Example;

import java.util.Arrays;
import java.util.List;
import java.util.Map;


/**
 * 服务实现层
 *
 * @author Administrator
 */
@Service
public class TypeTemplateServiceImpl extends CoreServiceImpl<TbTypeTemplate> implements TypeTemplateService {


    private TbTypeTemplateMapper typeTemplateMapper;

    @Autowired
    private TbSpecificationOptionMapper optionMapper;

    @Autowired
    public TypeTemplateServiceImpl(TbTypeTemplateMapper typeTemplateMapper) {
        super(typeTemplateMapper, TbTypeTemplate.class);
        this.typeTemplateMapper = typeTemplateMapper;
    }


    @Override
    public PageInfo<TbTypeTemplate> findPage(Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        List<TbTypeTemplate> all = typeTemplateMapper.selectAll();
        PageInfo<TbTypeTemplate> info = new PageInfo<TbTypeTemplate>(all);

        //序列化再反序列化
        String s = JSON.toJSONString(info);
        PageInfo<TbTypeTemplate> pageInfo = JSON.parseObject(s, PageInfo.class);
        return pageInfo;
    }


    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 描述：添加审核搜索条件,修改redis储存[苏红霖 2019.6.14]
     *
     * @param pageNo       当前页 码
     * @param pageSize     每页记录数
     * @param typeTemplate
     * @return
     */
    @Override
    public PageInfo<TbTypeTemplate> findPage(Integer pageNo, Integer pageSize, TbTypeTemplate typeTemplate) {
        PageHelper.startPage(pageNo, pageSize);

        Example example = new Example(TbTypeTemplate.class);
        Example.Criteria criteria = example.createCriteria();

        if (typeTemplate != null) {
            if (StringUtils.isNotBlank(typeTemplate.getName())) {
                criteria.andLike("name", "%" + typeTemplate.getName() + "%");
                //criteria.andNameLike("%"+typeTemplate.getName()+"%");
            }
            if (StringUtils.isNotBlank(typeTemplate.getSpecIds())) {
                criteria.andLike("specIds", "%" + typeTemplate.getSpecIds() + "%");
                //criteria.andSpecIdsLike("%"+typeTemplate.getSpecIds()+"%");
            }
            if (StringUtils.isNotBlank(typeTemplate.getBrandIds())) {
                criteria.andLike("brandIds", "%" + typeTemplate.getBrandIds() + "%");
                //criteria.andBrandIdsLike("%"+typeTemplate.getBrandIds()+"%");
            }
            if (StringUtils.isNotBlank(typeTemplate.getCustomAttributeItems())) {
                criteria.andLike("customAttributeItems", "%" + typeTemplate.getCustomAttributeItems() + "%");
                //criteria.andCustomAttributeItemsLike("%"+typeTemplate.getCustomAttributeItems()+"%");
            }
            //模板表审核状态查询
            if (StringUtils.isNoneBlank(typeTemplate.getTemplateStatus())) {
                //审核通过
                if ("1".equals(typeTemplate.getTemplateStatus())) {
                    criteria.andEqualTo("templateStatus", typeTemplate.getTemplateStatus());
                } else {
                    //审核未通过
                    criteria.andNotEqualTo("templateStatus", 1);
                }
            }
            //查询该商家的模板ID
            if (StringUtils.isNoneBlank(typeTemplate.getSellerId())) {
                criteria.andEqualTo("sellerId", typeTemplate.getSellerId());
            }

        }
        List<TbTypeTemplate> all = typeTemplateMapper.selectByExample(example);
        PageInfo<TbTypeTemplate> info = new PageInfo<TbTypeTemplate>(all);
        //序列化再反序列化
        String s = JSON.toJSONString(info);
        PageInfo<TbTypeTemplate> pageInfo = JSON.parseObject(s, PageInfo.class);

        //加入缓存

        //获取所有的模板的数据
        /*List<TbTypeTemplate> typeTemplateList = this.findAll();*/

        Example example1 = new Example(TbTypeTemplate.class);
        Example.Criteria criteria1 = example1.createCriteria();
        criteria1.andEqualTo("templateStatus", "1");
        List<TbTypeTemplate> typeTemplateList = typeTemplateMapper.selectByExample(example1);
        //循环遍历 模板的数据 存储redis中

        for (TbTypeTemplate tbTypeTemplate : typeTemplateList) {
            String brandIds = tbTypeTemplate.getBrandIds();//[{id:,text:"华为"}]
            List<Map> maps = JSON.parseArray(brandIds, Map.class);
            //品牌列表的存储
            redisTemplate.boundHashOps("brandList").put(tbTypeTemplate.getId(), maps);
            //规格的列表存储
            List<Map> specList = findSpecList(tbTypeTemplate.getId());//[{id:27,'text':'网络',options:[{},{}]},{}]
            //String specIds = tbTypeTemplate.getSpecIds();//[{id:,text:'网络'}]=====>[{id,text:"网络","options":[{},{}]}   ]
            redisTemplate.boundHashOps("specList").put(tbTypeTemplate.getId(), specList);
        }


        return pageInfo;
    }

    //根据模板的ID  获取模板的对象 中的规格的数据 拼接成格式：[{id:27,'text':'网络',options:[{},{}]},{}]
    @Override
    public List<Map> findSpecList(Long typeTmplateId) {
        //1.获取模板对象
        TbTypeTemplate typeTemplate = typeTemplateMapper.selectByPrimaryKey(typeTmplateId);
        //2.获取模板对象中规格列表数据字符串
        String specIds = typeTemplate.getSpecIds();//[{"id":27,"text":"网络"},{"id":32,"text":"机身内存"}]

        List<Map> maps = JSON.parseArray(specIds, Map.class);

        //3.根据规格的ID  获取规格的选项的列表
        for (Map map : maps) {
            //map  =====>{"id":27,"text":"网络"}

            Integer id = (Integer) map.get("id");//获取到规格的ID

            // select * from tb_option where spec_id = 27
            TbSpecificationOption option = new TbSpecificationOption();
            option.setSpecId(Long.valueOf(id));
            List<TbSpecificationOption> options = optionMapper.select(option);
            //4.拼接数据
            map.put("options", options);  //{"id":27,"text":"网络",options:[{id:1,optionName:"移动4G"}]}

        }
        //5.返回拼接后的数据
        return maps;
    }

    /**
     * 描述：根据模板ID修改模板审核状态[苏红霖 2019.6.14]
     *
     * @param ids    模板id
     * @param status 审核状态
     */
    @Override
    public void updateStatus(Long[] ids, String status) {
        //设置条件
        Example example = new Example(TbTypeTemplate.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("id", Arrays.asList(ids));

        //更新状态
        TbTypeTemplate tbTypeTemplate = new TbTypeTemplate();
        tbTypeTemplate.setTemplateStatus(status);
        typeTemplateMapper.updateByExampleSelective(tbTypeTemplate, example);
    }

}
