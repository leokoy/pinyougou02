package com.pinyougou.pojo;

import javax.persistence.*;
import java.io.Serializable;

@Table(name = "tb_type_template")
public class TbTypeTemplate implements Serializable {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 模板名称
     */
    @Column(name = "name")
    private String name;

    /**
     * 关联规格
     */
    @Column(name = "spec_ids")
    private String specIds;

    /**
     * 关联品牌
     */
    @Column(name = "brand_ids")
    private String brandIds;

    /**
     * 自定义属性
     */
    @Column(name = "custom_attribute_items")
    private String customAttributeItems;

    /**
     * 审核状态
     */
    @Column(name = "template_status")
    private String templateStatus;

    /**
     * 商家ID
     */
    @Column(name = "sellerId")
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
     * 获取模板名称
     *
     * @return name - 模板名称
     */
    public String getName() {
        return name;
    }

    /**
     * 设置模板名称
     *
     * @param name 模板名称
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 获取关联规格
     *
     * @return spec_ids - 关联规格
     */
    public String getSpecIds() {
        return specIds;
    }

    /**
     * 设置关联规格
     *
     * @param specIds 关联规格
     */
    public void setSpecIds(String specIds) {
        this.specIds = specIds;
    }

    /**
     * 获取关联品牌
     *
     * @return brand_ids - 关联品牌
     */
    public String getBrandIds() {
        return brandIds;
    }

    /**
     * 设置关联品牌
     *
     * @param brandIds 关联品牌
     */
    public void setBrandIds(String brandIds) {
        this.brandIds = brandIds;
    }

    /**
     * 获取自定义属性
     *
     * @return custom_attribute_items - 自定义属性
     */
    public String getCustomAttributeItems() {
        return customAttributeItems;
    }

    /**
     * 设置自定义属性
     *
     * @param customAttributeItems 自定义属性
     */
    public void setCustomAttributeItems(String customAttributeItems) {
        this.customAttributeItems = customAttributeItems;
    }

    /**
     * 获取审核状态
     *
     * @return - 审核状态
     */
    public String getTemplateStatus() {
        return templateStatus;
    }

    /**
     * 设置审核状态
     *
     * @param templateStatus 审核状态
     */
    public void setTemplateStatus(String templateStatus) {
        this.templateStatus = templateStatus;
    }

    /**
     * 获取商家ID
     * @return - 商家ID
     */
    public String getSellerId() {
        return sellerId;
    }

    /**
     * 设置商家ID
     * @param sellerId 商家ID
     */
    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }
}