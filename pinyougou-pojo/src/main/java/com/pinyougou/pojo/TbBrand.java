package com.pinyougou.pojo;

import javax.persistence.*;
import java.io.Serializable;

/**
 * 描述：添加字段品牌审核状态
 *
 * @author 苏红霖[添加]
 * @date 2019.6.13
 */
@Table(name = "tb_brand")
public class TbBrand implements Serializable {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 品牌名称
     */
    @Column(name = "name")
    private String name;

    /**
     * 品牌首字母
     */
    @Column(name = "first_char")
    private String firstChar;

    /**
     * 品牌审核状态
     */
    @Column(name = "status")
    private String status;

    /**
     * 商家ID
     */
    @Column(name = "seller_id")
    private String sellerId;

    private static final long serialVersionUID = 1L;

    /**
     * @return id
     */
    public Long getId() {
        return id;
    }

    /**
     * @param id
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * 获取品牌名称
     *
     * @return name - 品牌名称
     */
    public String getName() {
        return name;
    }

    /**
     * 设置品牌名称
     *
     * @param name 品牌名称
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 获取品牌首字母
     *
     * @return first_char - 品牌首字母
     */
    public String getFirstChar() {
        return firstChar;
    }

    /**
     * 设置品牌首字母
     *
     * @param firstChar 品牌首字母
     */
    public void setFirstChar(String firstChar) {
        this.firstChar = firstChar;
    }

    /**
     * 获取品牌审核状态
     *
     * @return
     */
    public String getStatus() {
        return status;
    }

    /**
     * 设置品牌审核状态
     *
     * @param status
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * 获取商家登陆名
     *
     * @return - 商家ID
     */
    public String getSellerId() {
        return sellerId;
    }

    /**
     * 设置商家登陆名
     *
     * @param sellerId - 商家ID
     */
    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }
}