package org.example.entity;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author mavenr
 * @Classname CommentInfo
 * @Description TODO
 * @Date 2022/10/18 11:24
 */
@Data
@Builder
public class CommentInfo {

    /**
     * 内容标题
     */
    private String title;

    /**
     * 发表时间
     */
    private String pushDate;

    /**
     * 总阅读人数(图文阅读人数)
     */
    private Integer allReadPeople;

    /**
     * 总分享人数(分享人数)
     */
    private Integer allSharePeople;

    /**
     * 送达人数
     */
    private Integer pushPeople;

    /**
     * 阅读完成率
     */
    private String completeReadRate;

    /**
     * 评论数
     */
    private Integer commentNum;

    /**
     * 网页链接
     */
    private String url;

}
