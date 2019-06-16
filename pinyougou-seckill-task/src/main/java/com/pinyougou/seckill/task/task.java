package com.pinyougou.seckill.task;

import com.pinyougou.common.pojo.SysConstants;
import com.pinyougou.mapper.TbSeckillGoodsMapper;
import com.pinyougou.pojo.TbSeckillGoods;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * 描述
 *
 * @author 三国的包子
 * @version 1.0
 * @package com.pinyougou.seckill *
 * @since 1.0
 */
@Component
public class task {

    @Autowired
    private TbSeckillGoodsMapper seckillGoodsMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 定时 吧数据库的数据 推送到redis中
     * corn:表达式用于指定何时去执行该方法。
     */
    @Scheduled(cron = "0/5 * * * * ?")
    public void pushGoods(){
        //1.查询数据库

        Example exmaple = new Example(TbSeckillGoods.class);
        Example.Criteria criteria = exmaple.createCriteria();
        //状态为1
        criteria.andEqualTo("status","1");

        //当前的时间再活动范围内   开始时间<当前的时间<结束时间
        Date date = new Date();
        criteria.andLessThan("startTime",date);
        criteria.andGreaterThan("endTime",date);
        //剩余库存大于0
        criteria.andGreaterThan("stockCount",0);

        //排除 已经在redis存在的商品   select * from where id not in (12,3,4)
        Set<Long> keys = redisTemplate.boundHashOps(SysConstants.SEC_KILL_GOODS).keys();//获取到的是所有的field的集合

        if(keys!=null && keys.size()>0) {
            System.out.println("asfdasfdasfasf");
            criteria.andNotIn("id", keys);
        }

        List<TbSeckillGoods> goods = seckillGoodsMapper.selectByExample(exmaple);

        //2.将数据推送到redis中

        for (TbSeckillGoods good : goods) {
            pushGoodsList(good);
            redisTemplate.boundHashOps(SysConstants.SEC_KILL_GOODS).put(good.getId(),good);//  bigke  field   value
        }

        System.out.println(new Date());
    }

    //将每一个商品 创建出一个队列 队列的元素的个数 由商品的库存决定
    private void pushGoodsList(TbSeckillGoods good){
        for (Integer i = 0; i < good.getStockCount(); i++) {
            redisTemplate.boundListOps(SysConstants.SEC_KILL_GOODS_PREFIX+good.getId()).leftPush(good.getId());
        }
    }
}
