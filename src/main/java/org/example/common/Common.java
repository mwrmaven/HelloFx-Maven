package org.example.common;

/**
 * @Author mavenr
 * @Classname  Common
 * @Description 常量类
 * @Date 2021/9/16 12:37 上午
 */
public class Common {

    // 顶部的按钮1
    public static final String TOP_BUTTON_1 = "批量修改文件名";

    // 顶部的按钮2
    public static final String TOP_BUTTON_2 = "批量递增标识修改文件名";

    // 顶部的按钮3
    public static final String TOP_BUTTON_3 = "比较两个文件内容";

    // 顶部的按钮4
    public static final String TOP_BUTTON_4 = "文件中的文本行排序";

    // 顶部的按钮5
    public static final String TOP_BUTTON_5 = "URL编码批量转换";

    // 顶部的按钮
    public static final String TOP_BUTTON_6 = "下载公众号文章中音视频";

    // 软件窗口的标题
    public static final String STAGE_TITLE = "文件批处理工具";

    // 批量递增标识修改文件名，文件名修改的类型的前置lable
    public static final String EDIT_TYPE_LABLE = "文件名中插入或替换: ";
    // 批量递增标识修改文件名，文件名修改的类型1
    public static final String EDIT_TYPE_1 = "后置插入";
    // 批量递增标识修改文件名，文件名修改的类型2
    public static final String EDIT_TYPE_2 = "前置插入";
    // 批量递增标识修改文件名，文件名修改的类型3
    public static final String EDIT_TYPE_3 = "替换字符";
    // 批量递增标识修改文件名，文件名修改的类型3时的提示信息
    public static final String EDIT_TYPE_3_PROMPT_TEXT = "请输入旧字符";


    // 根据模板文件创建文件时的提示信息
    public static final String CREATE_TYPE_1_PROMPT_TEXT = "请输入生成文件的个数";
    // 批量递增标识修改文件名，创建文件的方式的前置lable
    public static final String CREATE_TYPE_LABLE = "根据模板文件创建文件或只替换文件名: ";
    // 创建文件的方式1
    public static final String CREATE_TYPE_1 = "根据模板文件创建文件";
    // 创建文件的方式2
    public static final String CREATE_TYPE_2 = "只替换文件名";

    // 标识符的类型
    public static final String NUMBER = "NUMBER";
    public static final String TIME = "TIME";

    // 时间的步长单位
    public static final String DAY = "天";
    public static final String WEEK = "周";
    public static final String MONTH = "月";
    public static final String YEAR = "年";

    // 文件格式
    public static final String TXT = "TXT格式文件";
    public static final String EXCEL = "EXCEL格式文件";

    // 编码或者解码
    public static final String DECODE = "URL解码";
    public static final String ENCODE = "URL编码";

    // url编码转换功能中的常量
    public static final String ONEURL = "单个url转换";
    public static final String ONEFILE = "单个文件中url批量转换";
    public static final String ONEFOLDER = "文件夹下所有文件中url批量转换";

    // 指定url列，或根据某列先生成url
    public static final String FIXCOL = "指定URL列";
    public static final String FROMCOL = "根据某列生成URL";


}
