package entity;

import java.io.Serializable;

/***
 *
 * @Author ChenYj
 * @date 2019/6/14 10:35
 */
public class Order implements Serializable {
    private Long market;//销售额
    private String name;//商品名称
    private Long marketNum;//销售数量

    public Long getMarket() {
        return market;
    }

    public void setMarket(Long market) {
        this.market = market;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getMarketNum() {
        return marketNum;
    }

    public void setMarketNum(Long marketNum) {
        this.marketNum = marketNum;
    }
}
