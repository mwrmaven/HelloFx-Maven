package org.example.interfaces.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.example.common.Common;
import org.example.util.Unit;
import org.example.button.BatchButton;
import org.example.interfaces.Function;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author mavenr
 * @Classname DownLoadMediaFromArticle
 * @Description 下载公众号文章中音视频
 * @Date 2021/12/10 10:41
 */
public class DownLoadMediaFromArticle implements Function {

    private Unit unit = new Unit();

    @Override
    public String tabName() {
        return Common.TOP_BUTTON_6;
    }

    @Override
    public String tabStyle() {
        String style = "-fx-font-weight: bold; " +
                "-fx-background-radius: 10 10 0 0; " +
                "-fx-focus-color: transparent; -fx-text-base-color: white; " +
                "-fx-background-color: #fa6e57;  -fx-pref-height: 30; ";
        return style;
    }

    /**
     * 根据公众号文章的url获取文章中包含的音频、视频
     * @param stage 场景
     * @param width 屏幕宽度
     * @param h 窗口高度
     * @return
     */
    @Override
    public AnchorPane tabPane(Stage stage, double width, double h) {
        AnchorPane ap = new AnchorPane();

        // 竖向布局
        VBox vBox = new VBox();
        vBox.setSpacing(10);
        vBox.setPadding(new Insets(10));

        // 横向布局，输入公众号文章的url
        HBox hBox = new HBox();
        hBox.setSpacing(10);
        hBox.setAlignment(Pos.CENTER_LEFT);
        // 标签
        Label urlLabel = new Label("请输入公众号文章的url：");
        TextField tf = new TextField();
        tf.setPrefWidth(width / 2);
        hBox.getChildren().addAll(urlLabel, tf);

        // 音视频下载地址
        Label foldUrl = new Label("请选择音视频保存的位置：");
        List<Node> nodes = unit.chooseFolder(stage, width, null);
        HBox foldH = new HBox();
        foldH.setAlignment(Pos.CENTER_LEFT);
        foldH.setSpacing(10);
        foldH.getChildren().add(foldUrl);
        foldH.getChildren().addAll(nodes);

        // 执行按钮
        HBox buttonH = new HBox();
        buttonH.setAlignment(Pos.CENTER_LEFT);
        // 按钮
        BatchButton batchButton = new BatchButton();
        batchButton.setText("抓取");
        Button button = batchButton.createInstance();

        buttonH.getChildren().add(button);

        // 结果显示区域
        TextArea area = new TextArea();

        // 点击按钮触发事件
        button.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                List<String> voiceIds = new ArrayList<>();
                List<String> voiceUrls = new ArrayList<>();
                // 获取文章url
                String url = tf.getText();
                System.out.println("文章路径" + url);
                try {
                    // 获取文章中的音视频链接
                    Document document = Jsoup.connect(url).get();
                    // 用来获取video地址的请求
                    Elements mpvid = document.getElementsByAttribute("data-mpvid");
                    Elements mpvoiceList = document.getElementsByTag("mpvoice");
                    StringBuilder sb = new StringBuilder();
                    List<String> videoUrls = new ArrayList<>();

                    if (CollectionUtils.isEmpty(mpvoiceList) && CollectionUtils.isEmpty(mpvid)) {
                        area.setText("未获取到音视频！");
                    }
                    if (CollectionUtils.isNotEmpty(mpvoiceList)) {
                        voiceIds = mpvoiceList.stream().map(item -> item.attr("voice_encode_fileid")).collect(Collectors.toList());
                    }
                    String voiceBaseUrl = "https://res.wx.qq.com/voice/getvoice?mediaid=";

                    String getVideoUrl = "https://mp.weixin.qq.com/mp/videoplayer?action=get_mp_video_play_url&preview=0";

                    Elements metaList = document.getElementsByTag("meta");
                    for (Element e : metaList) {
                        if ("og:url".equals(e.attr("property"))) {
                            String content = e.attr("content");
                            content = content.substring(content.indexOf("?") + 1);
                            String[] split = content.split("&");
                            for (String param : split) {
                                if (param.contains("__biz")
                                        || param.contains("mid") || param.contains("idx")) {
                                    getVideoUrl = getVideoUrl + "&" + param;
                                }
                            }
                        }
                    }

                    if (CollectionUtils.isEmpty(mpvid)) {
                        area.setText("未获取到视频！");
                    } else {
                        getVideoUrl = getVideoUrl + "&vid=" + mpvid.get(0).attr("data-mpvid");
                        getVideoUrl = getVideoUrl + "&uin=";
                        getVideoUrl = getVideoUrl + "&key=";
                        getVideoUrl = getVideoUrl + "&pass_ticket=";
                        getVideoUrl = getVideoUrl + "&wxtoken=777";
                        getVideoUrl = getVideoUrl + "&devicetype=";
                        getVideoUrl = getVideoUrl + "&clientversion=";
                        getVideoUrl = getVideoUrl + "&appmsg_token=";
                        getVideoUrl = getVideoUrl + "&x5=0";
                        getVideoUrl = getVideoUrl + "&f=json";

                        // 请求获取video url
                        Connection.Response response = Jsoup.connect(getVideoUrl)
                                .ignoreContentType(true)
                                .execute();
                        String videoUrlBody = response.body();
                        JSONObject urlJson = JSONObject.parseObject(videoUrlBody);
                        JSONArray url_info = urlJson.getJSONArray("url_info");
                        String videoUrl;
                        JSONObject jsonObject = url_info.getJSONObject(0);
                        videoUrl = jsonObject.getString("url");
                        videoUrls.add(videoUrl);
                        System.out.println("视频地址：" + videoUrl);
                        sb.append("视频地址：" + videoUrl).append("\n");
                    }

                    for (String id : voiceIds) {
                        String vr = voiceBaseUrl + id;
                        voiceUrls.add(vr);
                        sb.append("音频地址：" + vr).append("\n");
                    }

                    area.setText(sb.toString());

                    // 下载路径
                    String folder = ((TextField) foldH.getChildren().get(foldH.getChildren().size() - 1)).getText();
                    if (!folder.endsWith("/")) {
                        folder += "/";
                    }
                    int num = 1;
                    // 下载音视频
                    for (String voice : voiceUrls) {
                        Connection.Response response1 = Jsoup.connect(voice)
                                .maxBodySize(0)
                                .timeout(0)
                                .ignoreContentType(true).execute();
                        FileUtils.writeByteArrayToFile(new File(folder + num + ".mp3"), response1.bodyAsBytes());
                        num++;
                    }

                    for (String video : videoUrls) {
                        Connection.Response response1 = Jsoup.connect(video)
                                .maxBodySize(0)
                                .timeout(0)
                                .ignoreContentType(true).execute();
                        FileUtils.writeByteArrayToFile(new File(folder + num + ".mp4"), response1.bodyAsBytes());
                        num++;
                    }

                    sb.append("下载完成");
                    area.setText(sb.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        vBox.getChildren().addAll(hBox, foldH, buttonH, area);
        ap.getChildren().add(vBox);
        return ap;
    }
}
