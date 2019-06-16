package com.pinyougou.es.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.pinyougou.es.dao.ItemDao;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.es.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;

import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 描述
 *
 * @author 三国的包子
 * @version 1.0
 * @package com.pinyougou.service.impl *
 * @since 1.0
 */

@Service
public class ItemServiceImpl implements ItemService {

    @Autowired
    private ItemDao dao;

    @Autowired
    private TbItemMapper itemMapper;

    @Override
    public void importDBToEs() {
        //1.查询数据库中的所有符合条件的SKU的数据
        TbItem tbitem = new TbItem();
        tbitem.setStatus("1");//正常的商品
        List<TbItem> tbItems = itemMapper.select(tbitem);

        //将规格的数据保存到map中

        for (TbItem tbItem : tbItems) {
            String spec = tbItem.getSpec();//  {"机身内存":"16G","网络":"联通3G"}
            Map<String,String> map = JSON.parseObject(spec, Map.class);
            tbItem.setSpecMap(map);
        }

        //2.保存所有的数据到 es服务器中
        dao.saveAll(tbItems);
    }

    @Override
    public void updateEsGoods(Long[] ids) {
        Example example = new Example(TbItem.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andIn("id", Arrays.asList(ids));
        List<TbItem> list = itemMapper.selectByExample(example);
        dao.saveAll(list);
    }

    @Override
    public void deleteEsGoods(Long[] ids) {
        Example example = new Example(TbItem.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andIn("id", Arrays.asList(ids));
        List<TbItem> list = itemMapper.selectByExample(example);
        Object[] objects = list.toArray();
        dao.deleteAll(list);
    }

    @Override
    public Long[] getItemIds(Long[] ids) {
        Example example = new Example(TbItem.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andIn("goodsId",Arrays.asList(ids));
        List<TbItem> list = itemMapper.selectByExample(example);
        ArrayList<Long> idsList = new ArrayList<>();
        for (TbItem tbItem : list) {
            idsList.add(tbItem.getId());
        }
        return idsList.toArray(new Long[]{});
    }
}
