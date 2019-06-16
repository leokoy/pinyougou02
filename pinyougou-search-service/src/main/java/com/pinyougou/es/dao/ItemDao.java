package com.pinyougou.es.dao;

import com.pinyougou.pojo.TbItem;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * 对 pinyougou的下的类型为item 下的所有的文档进行基本的CRUD操作
 *
 * @author 三国的包子
 * @version 1.0
 * @package com.pinyougou.es.dao *
 * @since 1.0
 */
public interface ItemDao extends ElasticsearchRepository<TbItem,Long> {
}
