package com.pinyougou.es.service;

/**
 * 描述
 *
 * @author 三国的包子
 * @version 1.0
 * @package com.pinyougou.es.service *
 * @since 1.0
 */
public interface ItemService {

    //查询数据库的数据导入到E是服务器中
    public void importDBToEs();

    //新增或更新es中的商品
    public void updateEsGoods(Long[] ids);

    //删除es中的商品
    public void deleteEsGoods(Long[] ids);

    //通过spu的id获取sku的id
    public Long[] getItemIds(Long[] ids);
}
