package org.example.entity;

import lombok.Builder;
import lombok.Data;

/**
 * @author mavenr
 * @Classname TemplateInfo
 * @Description TODO
 * @Date 2022/10/18 15:09
 */
@Data
@Builder
public class TemplateInfo {

    /**
     * 文章标题
     */
    private String title;

    /**
     * 标题类型
     */
    private String titleType;

    /**
     * 位置
     */
    private Integer position;

    /**
     * 组别
     */
    private String group;

    /**
     * 推送日期
     */
    private String pushDate;

    /**
     * 推送人数
     */
    private Integer pushPeople;
}
