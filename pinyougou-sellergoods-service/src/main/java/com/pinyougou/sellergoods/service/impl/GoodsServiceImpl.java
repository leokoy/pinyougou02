package com.pinyougou.sellergoods.service.impl;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.pinyougou.mapper.*;
import com.pinyougou.pojo.*;
import entity.Goods;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.apache.commons.lang3.StringUtils;
import com.pinyougou.core.service.CoreServiceImpl;

import tk.mybatis.mapper.entity.Example;

import com.pinyougou.sellergoods.service.GoodsService;


/**
 * 服务实现层
 *
 * @author Administrator
 */
@Service
public class GoodsServiceImpl extends CoreServiceImpl<TbGoods> implements GoodsService {

    @Autowired
    private TbGoodsMapper goodsMapper;


    @Autowired
    private TbGoodsDescMapper goodsDescMapper;


    @Autowired
    public GoodsServiceImpl(TbGoodsMapper goodsMapper) {
        super(goodsMapper, TbGoods.class);
        this.goodsMapper = goodsMapper;
    }


    @Override
    public PageInfo<TbGoods> findPage(Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        List<TbGoods> all = goodsMapper.selectAll();
        PageInfo<TbGoods> info = new PageInfo<TbGoods>(all);

        //序列化再反序列化
        String s = JSON.toJSONString(info);
        PageInfo<TbGoods> pageInfo = JSON.parseObject(s, PageInfo.class);
        return pageInfo;
    }


    @Override
    public PageInfo<TbGoods> findPage(Integer pageNo, Integer pageSize, TbGoods goods) {
        PageHelper.startPage(pageNo, pageSize);

        Example example = new Example(TbGoods.class);
        Example.Criteria criteria = example.createCriteria();

        //排除 已删除的数据

        criteria.andEqualTo("isDelete", false);


        if (goods != null) {
            if (StringUtils.isNotBlank(goods.getSellerId())) {
                criteria.andEqualTo("sellerId", goods.getSellerId());//qiandu  qiandu123
                //criteria.andSellerIdLike("%"+goods.getSellerId()+"%");
            }
            if (StringUtils.isNotBlank(goods.getGoodsName())) {
                criteria.andLike("goodsName", "%" + goods.getGoodsName() + "%");
                //criteria.andGoodsNameLike("%"+goods.getGoodsName()+"%");
            }
            if (StringUtils.isNotBlank(goods.getAuditStatus())) {
                criteria.andEqualTo("auditStatus", goods.getAuditStatus());
            }
            if (StringUtils.isNotBlank(goods.getIsMarketable())) {
                criteria.andLike("isMarketable", "%" + goods.getIsMarketable() + "%");
                //criteria.andIsMarketableLike("%"+goods.getIsMarketable()+"%");
            }
            if (StringUtils.isNotBlank(goods.getCaption())) {
                criteria.andLike("caption", "%" + goods.getCaption() + "%");
                //criteria.andCaptionLike("%"+goods.getCaption()+"%");
            }
            if (StringUtils.isNotBlank(goods.getSmallPic())) {
                criteria.andLike("smallPic", "%" + goods.getSmallPic() + "%");
                //criteria.andSmallPicLike("%"+goods.getSmallPic()+"%");
            }
            if (StringUtils.isNotBlank(goods.getIsEnableSpec())) {
                criteria.andLike("isEnableSpec", "%" + goods.getIsEnableSpec() + "%");
                //criteria.andIsEnableSpecLike("%"+goods.getIsEnableSpec()+"%");
            }

        }
        List<TbGoods> all = goodsMapper.selectByExample(example);
        PageInfo<TbGoods> info = new PageInfo<TbGoods>(all);
        //序列化再反序列化
        String s = JSON.toJSONString(info);
        PageInfo<TbGoods> pageInfo = JSON.parseObject(s, PageInfo.class);

        return pageInfo;
    }


    @Autowired
    private TbItemMapper itemMapper;

    @Autowired
    private TbItemCatMapper itemCatMapper;

    @Autowired
    private TbSellerMapper sellerMapper;

    @Autowired
    private TbBrandMapper brandMapper;

    @Override
    public void add(Goods goods) {
        //1.获取SPU的数据
        TbGoods tbGoods = goods.getGoods();
        tbGoods.setAuditStatus("0");//默认就是未审核的状态
        tbGoods.setIsDelete(false);//不删除的状态
        //2.获取SPU对应的描述的数据
        TbGoodsDesc goodsDesc = goods.getGoodsDesc();

        //3.获取SKU的列表数据
        List<TbItem> itemList = goods.getItemList();

        //4.插入到三个表中
        goodsMapper.insert(tbGoods);


        //设置描述表的主键
        goodsDesc.setGoodsId(tbGoods.getId());

        goodsDescMapper.insert(goodsDesc);
        saveItems(tbGoods, goodsDesc, itemList);


    }

    private void saveItems(TbGoods tbGoods, TbGoodsDesc goodsDesc, List<TbItem> itemList) {
        //如果启用规格
        if ("1".equals(tbGoods.getIsEnableSpec())) {
            //TODO
            for (TbItem tbItem : itemList) {


                //1设置标题  由 spu的名 + " "+ 规格的选项名
                String title = tbGoods.getGoodsName();
                String spec = tbItem.getSpec();//{ "网络": "移动4G", "机身内存": "32G" }
                Map<String, String> map = JSON.parseObject(spec, Map.class);

                for (String key : map.keySet()) {
                    title += " " + map.get(key);
                }
                tbItem.setTitle(title);

                //设置图片
                String itemImages = goodsDesc.getItemImages();//[{"color":"hognse","url":"http://192.168.25.133/group1/M00/00/05/wKgZhVzdfPOASSyAAANdC6JX9KA574.jpg"}]
                List<Map> maps = JSON.parseArray(itemImages, Map.class);
                String url = maps.get(0).get("url").toString();
                tbItem.setImage(url);
                //设置商品的分类的ID
                TbItemCat cat = itemCatMapper.selectByPrimaryKey(tbGoods.getCategory3Id());
                tbItem.setCategoryid(cat.getId());
                tbItem.setCategory(cat.getName());
                //设置时间
                tbItem.setCreateTime(new Date());
                tbItem.setUpdateTime(tbItem.getCreateTime());

                //设置GOODS_ID
                tbItem.setGoodsId(tbGoods.getId());

                //设置sellerid
                TbSeller tbSeller = sellerMapper.selectByPrimaryKey(tbGoods.getSellerId());
                tbItem.setSellerId(tbGoods.getSellerId());
                tbItem.setSeller(tbSeller.getNickName());

                //品牌名称

                TbBrand brand = brandMapper.selectByPrimaryKey(tbGoods.getBrandId());

                tbItem.setBrand(brand.getName());

                itemMapper.insert(tbItem);
            }
        } else {
            //如果不启用规格  SKU 还是需要有 单品
            TbItem tbItem = new TbItem();

            //补充属性的值
            tbItem.setTitle(tbGoods.getGoodsName());//SPU名
            //价格
            tbItem.setPrice(tbGoods.getPrice());

            tbItem.setNum(999);//默认的库存

            tbItem.setStatus("1");//默认是正常的
            tbItem.setIsDefault("1");//默认展示
            tbItem.setGoodsId(tbGoods.getId());//SPUde ID

            tbItem.setSpec("{}");//null值

            //设置图片
            String itemImages = goodsDesc.getItemImages();//[{"color":"hognse","url":"http://192.168.25.133/group1/M00/00/05/wKgZhVzdfPOASSyAAANdC6JX9KA574.jpg"}]
            List<Map> maps = JSON.parseArray(itemImages, Map.class);
            String url = maps.get(0).get("url").toString();
            tbItem.setImage(url);

            //设置商品的分类的ID
            TbItemCat cat = itemCatMapper.selectByPrimaryKey(tbGoods.getCategory3Id());
            tbItem.setCategoryid(cat.getId());
            tbItem.setCategory(cat.getName());
            //设置sellerid
            TbSeller tbSeller = sellerMapper.selectByPrimaryKey(tbGoods.getSellerId());
            tbItem.setSellerId(tbGoods.getSellerId());
            tbItem.setSeller(tbSeller.getNickName());

            //设置时间
            tbItem.setCreateTime(new Date());
            tbItem.setUpdateTime(tbItem.getCreateTime());

            //品牌名称

            TbBrand brand = brandMapper.selectByPrimaryKey(tbGoods.getBrandId());

            tbItem.setBrand(brand.getName());

            itemMapper.insert(tbItem);


        }
    }

    @Override
    public Goods findOne(Long id) {
        //1.根据商品的ID 获取SPU的数据
        TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
        //2.根据商品的ID 获取描述的数据
        TbGoodsDesc tbGoodsDesc = goodsDescMapper.selectByPrimaryKey(id);
        //3.根据商品的ID 获取SKU的列表数据
        //select * from tb_item wehre goodid=1
        TbItem tbItem = new TbItem();
        tbItem.setGoodsId(id);
        List<TbItem> itemList = itemMapper.select(tbItem);
        //4.返回组合对象
        Goods goods = new Goods();
        goods.setGoods(tbGoods);
        goods.setGoodsDesc(tbGoodsDesc);
        goods.setItemList(itemList);
        return goods;
    }

    @Override
    public void update(Goods goods) {
        //1.获取SPU的数据 更新SPU的数据
        TbGoods tbGoods = goods.getGoods();
        //
        tbGoods.setAuditStatus("0");//更新的时候要重新设置回0
        goodsMapper.updateByPrimaryKey(tbGoods);
        //2.获取描述的数据 更新SPU描述的数据
        TbGoodsDesc goodsDesc = goods.getGoodsDesc();
        goodsDescMapper.updateByPrimaryKey(goodsDesc);

        //3.获取Sku的数据 更新Sku的数据  先删除原本数据库的该SPU下的SKU  再添加页面传递过来的SKU列表
        //delete from tb_item where goods_id = 1
        Example example = new Example(TbItem.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("goodsId", tbGoods.getId());
        itemMapper.deleteByExample(example);

        //新增
        List<TbItem> itemList = goods.getItemList();
        saveItems(tbGoods, goodsDesc, itemList);
    }

    @Override
    public void updateStatus(Long[] ids, String status) {
        // update tb_goods  set audit_status=1 where id in(1,2,3)

        Example exmaple = new Example(TbGoods.class);
        Example.Criteria criteria = exmaple.createCriteria();
        criteria.andIn("id", Arrays.asList(ids));

        //要更新后的值
        TbGoods tbGoods = new TbGoods();
        tbGoods.setAuditStatus(status);
        goodsMapper.updateByExampleSelective(tbGoods, exmaple);
    }

    @Override
    public List<TbItem> findTbItemListByIds(Long[] ids) {

        //select * from tb_item where goods_id in (1,2,3)
        Example example = new Example(TbItem.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andIn("goodsId", Arrays.asList(ids));
        criteria.andEqualTo("status", 1);
        return itemMapper.selectByExample(example);
    }

    @Override
    //逻辑删除  update tb-goods set is_delete=1 where id in (1,2,3)
    public void delete(Object[] ids) {
        Example exmaple = new Example(TbGoods.class);
        Example.Criteria criteria = exmaple.createCriteria();
        criteria.andIn("id", Arrays.asList(ids));

        //要更新后的值
        TbGoods tbGoods = new TbGoods();
        tbGoods.setIsDelete(true);
        goodsMapper.updateByExampleSelective(tbGoods, exmaple);
    }
}
