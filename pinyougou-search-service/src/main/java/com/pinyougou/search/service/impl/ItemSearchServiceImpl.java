package com.pinyougou.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.pinyougou.es.dao.ItemDao;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.query.DeleteQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 描述
 *
 * @author 三国的包子
 * @version 1.0
 * @package com.pinyougou.search.service.impl *
 * @since 1.0
 */
@Service
public class ItemSearchServiceImpl implements ItemSearchService {

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    @Override
    public Map<String, Object> search(Map<String, Object> searchMap) {

        //1.获取map中的关键字参数的值  keywords
        String keywords = (String) searchMap.get("keywords");

        //2.创建查询对象的 构建对象
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();

        //3.设置查询的条件  matchQuery
//        queryBuilder.withIndices()
        // 使用多字段查询
        queryBuilder.withQuery(QueryBuilders.multiMatchQuery(keywords,"title","seller","brand","category"));


        //3.1 设置高亮显示的 域（字段） 设置前缀 后缀

        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.preTags("<em style=\"color:red\">").postTags("</em>");
        queryBuilder
                .withHighlightFields(new HighlightBuilder.Field("title"))
                .withHighlightBuilder(highlightBuilder);

        //3.2 设置聚合查询的条件  根据商品分类 聚合查询 （类似于sql的group by ）

        //terms 的参数 给聚合查询取一个名称
        // field("category") 指定分组的字段
        queryBuilder.addAggregation(AggregationBuilders.terms("category_group").field("category").size(50));


        //3.3 过滤查询
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        //3.3.1 商品分类过滤查询


        String category = (String) searchMap.get("category");
        if(StringUtils.isNotBlank(category)) {

            //fileter 就相当于must
            boolQueryBuilder.filter(QueryBuilders.termQuery("category", category));

        }

        //3.3.2  品牌的过滤查询
        String brand = (String) searchMap.get("brand");
        if(StringUtils.isNotBlank(brand)){
            boolQueryBuilder.filter(QueryBuilders.termQuery("brand",brand));
        }
        //3.3.3  规格的过滤查询
        Map<String,String> spec = (Map<String, String>) searchMap.get("spec");//{"网络":"移动3G","机身内存":"16G"}
        if(spec!=null){
            for (String key : spec.keySet()) {//
                boolQueryBuilder.filter(QueryBuilders.termQuery("specMap."+key+".keyword",spec.get(key)));
            }
        }

        //3.3.4  价格区间的过滤 范围查询

        String price = (String) searchMap.get("price");//   0-500   3000-*

        if(StringUtils.isNotBlank(price)){
            String[] split = price.split("-");

          if(split[1].equals("*")){
              // 大于某一个价格
              boolQueryBuilder.filter(QueryBuilders.rangeQuery("price").gte(split[0]));
          }else{
              //1=<price <=5
              boolQueryBuilder.filter(QueryBuilders.rangeQuery("price").from(split[0],true).to(split[1],true));
          }
        }


        queryBuilder.withFilter(boolQueryBuilder);


        //4.构建查询对象
        NativeSearchQuery searchQuery = queryBuilder.build();


        //4.1 设置分页
        Integer pageNo = (Integer) searchMap.get("pageNo");
        Integer pageSize = (Integer) searchMap.get("pageSize");
        if(pageNo==null) {pageNo=1;}
        if(pageSize==null){pageSize=40;}
        //第一个参数为 当前的页码，如果为0 表示第一页
        //第二个参数为 每页显示的行
        Pageable pageable = PageRequest.of(pageNo-1,pageSize);
        searchQuery.setPageable(pageable);

        //4.2 设置排序 按照价格来排序  定义两个变量  sortType:要排序的类型 ASC DESC  sortField:接收排序的字段 比如price  createtime

        String sortType = (String) searchMap.get("sortType");
        String sortField = (String) searchMap.get("sortField");

        if(StringUtils.isNotBlank(sortType) && StringUtils.isNotBlank(sortField)) {
            if(sortType.equals("ASC")) {
                Sort sort = new Sort(Sort.Direction.ASC, sortField);//order by price asc
                searchQuery.addSort(sort);
            }else if(sortType.equals("DESC")){
                Sort sort = new Sort(Sort.Direction.DESC, sortField);//order by price asc
                searchQuery.addSort(sort);
            }else{
                System.out.println("不排序");
            }
        }

        //5.执行查询
        AggregatedPage<TbItem> tbItems = elasticsearchTemplate.queryForPage(searchQuery, TbItem.class, new SearchResultMapper() {

            //自定义 数据 返回 获取高亮的数据 返回
            @Override
            public <T> AggregatedPage<T> mapResults(SearchResponse response, Class<T> clazz, Pageable pageable) {
                //1.获取结果集（有高亮的数据）

                List<T> content = new ArrayList<>();

                //2.获取分页的对象

                //3.获取查询的总记录数
                SearchHits hits = response.getHits();
                long totalHits = hits.getTotalHits();


                if(hits==null || hits.getTotalHits()<=0){
                    return new AggregatedPageImpl<T>(content);
                }

                //获取高亮的数据
                for (SearchHit hit : hits) {
                    String sourceAsString = hit.getSourceAsString();//获取到的每一个文档的数据的JSON字符串 不是高亮的
                    TbItem tbItem = JSON.parseObject(sourceAsString, TbItem.class);//转成 POJO
                    System.out.println(":::::"+sourceAsString);
                    Map<String, HighlightField> highlightFields = hit.getHighlightFields();
                    if(highlightFields!=null && highlightFields.get("title")!=null && highlightFields.get("title").getFragments()!=null) {
                        HighlightField highlightField = highlightFields.get("title");//获取 高亮字段为title的高亮的数据
                            Text[] fragments = highlightField.getFragments();//高亮的碎片 <em sytle
                            StringBuffer sb = new StringBuffer();
                            for (Text text : fragments) {
                                String string = text.string();//高亮的数据
                                sb.append(string);
                            }
                            tbItem.setTitle(sb.toString());

                            System.out.println(">>>>>" + tbItem.getTitle());

                            //将数据存储到集合中
                            content.add((T) tbItem);

                    }else{
                        content.add((T) tbItem);
                    }
                }

                //4.获取 聚合查询的对象（ 分组 统计 求平均值.....sum avg count groupby ）
                Aggregations aggregations = response.getAggregations();

                //5.获取深度分页的Id
                String scrollId = response.getScrollId();

                return new AggregatedPageImpl<T>(content,pageable,totalHits,aggregations,scrollId);
            }
        });

        //6.获取结果   封装到map返回
        Map<String,Object> resultMap = new HashMap<>();

        //6.1 获取分类 分组查询的结果
        StringTerms category_group = (StringTerms) tbItems.getAggregation("category_group");
        List<String> categoryList = new ArrayList<>();
        if (category_group != null) {
            //6.2 获取bukets
            List<StringTerms.Bucket> buckets = category_group.getBuckets();

            //6.3 获取bukets中的key

            for (StringTerms.Bucket bucket : buckets) {
                String keyAsString = bucket.getKeyAsString();//就是 商品分类的名称  手机  拼板电视
                //6.4 将key 存储到数组中
                categoryList.add(keyAsString);

            }

        }
        //6.5 将数组 存储到返回结果的Map中
        resultMap.put("categoryList",categoryList);

        System.out.println(category_group);

        //6.6 从redis中根据商品分类名称  获取规格的列表 和品牌的列表 返回map对象中  默认的情况下 获取第一个商品分类的品牌列表和规格列表


        //先获取到页面传递过来的商品分类
        if(StringUtils.isNotBlank(category)) {
            // 判断 商品分类是否有值 有值  根据分类查询品牌和规格
         Map map = searchBrandAndSpecList(category);//map里面已有了品牌列表 和规格列表  "brandList"----》[]  "specList":[]
         resultMap.putAll(map);
        }else{
            // 没有 获取默认的第一个商品分类的数据
            if(categoryList!=null && categoryList.size()>0) {
                Map map = searchBrandAndSpecList(categoryList.get(0));//map里面已有了品牌列表 和规格列表  "brandList"----》[]  "specList":[]
                resultMap.putAll(map);
            }else{
                resultMap.putAll(new HashMap<>());
            }
        }






        resultMap.put("rows",tbItems.getContent());//专门 存储查询到的当前的页的集合数据List<tbitem>
        resultMap.put("total",tbItems.getTotalElements());//总记录数
        System.out.println("总记录数:"+tbItems.getTotalElements());
        resultMap.put("totalPages",tbItems.getTotalPages());//总页数
        return resultMap;
    }

    @Autowired
    private ItemDao itemDao;

    @Override
    public void updateIndex(List<TbItem> itemList) {
        //1.编写一个DAO
        //2.调用DAO的saveAll方法 更新到ES服务器中

        //循环遍历  将 规格的数据存储到specMap中
        for (TbItem tbItem : itemList) {
            String spec = tbItem.getSpec();//{"网络":"移动3G","机身内存":"32G"}
            Map map = JSON.parseObject(spec, Map.class);
            tbItem.setSpecMap(map);
        }
        itemDao.saveAll(itemList);
    }

    @Override
    public void deleteByIds(Long[] ids) {
        //delete from tb_item where goods_id in (1,2,3)
        DeleteQuery deletequery = new DeleteQuery();
        deletequery.setQuery(QueryBuilders.termsQuery("goodsId",ids));
        elasticsearchTemplate.delete(deletequery,TbItem.class);
    }

    @Autowired
    private RedisTemplate redisTemplate;

    private Map searchBrandAndSpecList(String category) {
        //1.根据商品分类的名称 获取模板的ID
        Long typeTemplateId = (Long) redisTemplate.boundHashOps("itemCat").get(category);
        //2.根据模板的ID 获取 品牌列表
        List<Map> brandList = (List<Map>) redisTemplate.boundHashOps("brandList").get(typeTemplateId);
        //3.根据模板的ID 获取 规格的列表
        List<Map> specList = (List<Map>) redisTemplate.boundHashOps("specList").get(typeTemplateId);
        //4.存到map中 返回
        Map brandAndSpeMap = new HashMap<>();
        brandAndSpeMap.put("brandList", brandList);
        brandAndSpeMap.put("specList", specList);
        return brandAndSpeMap;

    }
}
