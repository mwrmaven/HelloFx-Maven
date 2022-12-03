package org.example.entity;

import lombok.Builder;
import lombok.Data;

/**
 * @Classname ArticleLink
 * @Description 草稿箱文章链接（文章名 + url）
 * @Date 2022/12/3 15:30
 * @author mavenr
 */
@Data
@Builder
public class ArticleLink {

	/**
	 * 文章名称
	 */
	private String title;

	/**
	 * 文章链接
	 */
	private String url;
}
