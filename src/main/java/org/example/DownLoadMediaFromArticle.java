package org.example;

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
 * @Author mavenr
 * @Classname  DownLoadMediaFromArticle
 * @Description 从公众号文章中下载音频视频
 * @Date 2021/10/29 10:11 下午
 */
public class DownLoadMediaFromArticle {

    private Unit unit = new Unit();

    /**
     * 根据公众号文章的url获取文章中包含的音频、视频
     * @param primaryStage
     * @param width
     * @param height
     */
    public AnchorPane download(Stage primaryStage, double width, double height) {
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
        List<Node> nodes = unit.chooseFolder(primaryStage, width, null);
        HBox foldH = new HBox();
        foldH.setAlignment(Pos.CENTER_LEFT);
        foldH.setSpacing(10);
        foldH.getChildren().add(foldUrl);
        foldH.getChildren().addAll(nodes);

        // 执行按钮
        HBox buttonH = new HBox();
        buttonH.setAlignment(Pos.CENTER_LEFT);
        // 按钮
        Button button = new Button("抓取");
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
                    Elements mpvoiceList = document.getElementsByTag("mpvoice");
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
                                        || param.contains("mid") || param.contains("idx"))
                                getVideoUrl = getVideoUrl + "&" + param;
                            }
                        }
                    }

                    // 用来获取video地址的请求
                    Elements mpvid = document.getElementsByAttribute("data-mpvid");
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
                    List<String> videoUrls = new ArrayList<>();
                    videoUrls.add(videoUrl);
                    System.out.println("视频地址：" + videoUrl);
                    StringBuilder sb = new StringBuilder();
                    sb.append("视频地址：" + videoUrl).append("\n");
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
