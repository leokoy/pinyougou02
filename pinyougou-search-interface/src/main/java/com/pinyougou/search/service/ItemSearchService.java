package com.pinyougou.search.service;

import com.pinyougou.pojo.TbItem;

import java.util.List;
import java.util.Map; /**
 * 描述
 *
 * @author 三国的包子
 * @version 1.0
 * @package com.pinyougou.search.service *
 * @since 1.0
 */
public interface ItemSearchService {
    /**
     * 根据搜索的条件 map 封装  执行ES的查询 返回一个结果集（map）
     * @param searchMap
     * @return
     */
    Map<String,Object> search(Map<String, Object> searchMap);

    /**
     * 根据SKU的数据列表  将其更新到ES服务器中
     * @param itemList
     */
    void updateIndex(List<TbItem> itemList);

    void deleteByIds(Long[] ids);

}
