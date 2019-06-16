package com.pinyougou.page.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.mapper.TbGoodsDescMapper;
import com.pinyougou.mapper.TbGoodsMapper;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.page.service.ItemPageService;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbGoodsDesc;
import com.pinyougou.pojo.TbItem;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;
import tk.mybatis.mapper.entity.Example;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 描述
 *
 * @author 三国的包子
 * @version 1.0
 * @package com.pinyougou.page.service.impl *
 * @since 1.0
 */
@Service
public class ItemPageServiceImpl implements ItemPageService {

    @Autowired
    private TbGoodsMapper goodsMapper;

    @Autowired
    private TbGoodsDescMapper goodsDescMapper;

    @Autowired
    private FreeMarkerConfigurer configurer;

    @Autowired
    private TbItemMapper itemMapper;

    @Override
    public void genItemHtml(Long id) {
        //1.根据SPU的ID  获取到SPU的信息（包括 描述的信息）
        TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);

        TbGoodsDesc tbGoodsDesc = goodsDescMapper.selectByPrimaryKey(id);

        //2.调用freeamrker的方法  输出静态页面( 模板 + 数据集 =html)
        genHTML("item.ftl",tbGoods,tbGoodsDesc);
    }

    @Override
    public void deleteByIds(Long[] longs) {
        for (Long aLong : longs) {
            try {
                FileUtils.forceDelete(new File("F:\\freemarker\\"+aLong+".html"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void genHTML(String templateName,TbGoods tbGoods, TbGoodsDesc tbGoodsDesc) {
        FileWriter writer=null;
        try {
            //1.创建模板文件
            //2.创建configuration对象
            //4.构建文件所在的目录以及字符编码

            Configuration configuration = configurer.getConfiguration();
            //5.加载模板文件获取到模板对象，准备数据集
            Template template = configuration.getTemplate(templateName);

            Map map = new HashMap<>();
            map.put("tbGoods",tbGoods);
            map.put("tbGoodsDesc",tbGoodsDesc);

            //根据SPU的ID 查询该SPU下的符合条件的所有的SKU的列表数据
            //select * from tb_item where goods_id=1 and status=1 order by is_default desc
            Example exmaple = new Example(TbItem.class);
            Example.Criteria criteria = exmaple.createCriteria();
            criteria.andEqualTo("goodsId",tbGoods.getId());
            criteria.andEqualTo("status",1);

            exmaple.setOrderByClause("is_default desc");//order by
            List<TbItem> skuList = itemMapper.selectByExample(exmaple);
            //存储到静态页面中
            map.put("skuList",skuList);


            //6.创建(写)文件流  输出
           writer = new FileWriter(new File("F:\\freemarker\\"+tbGoods.getId()+".html"));
            template.process(map,writer);
            //7.关闭流
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if(writer!=null){
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
