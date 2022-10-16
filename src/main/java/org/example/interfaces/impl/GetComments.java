package org.example.interfaces.impl;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.interfaces.Function;
import org.example.util.Unit;

import java.util.List;

/**
 * @Classname GetComments
 * @Description 获取公众号文章的评论数
 * @Date 2022/10/16 20:03
 * @author mavenr
 */
public class GetComments implements Function {

	private final Unit unit = new Unit();

	@Override
	public String tabName() {
		return "微信文章评论数";
	}

	@Override
	public String tabStyle() {
		String style = "-fx-font-weight: bold; " +
				"-fx-background-radius: 10 10 0 0; " +
				"-fx-focus-color: transparent; -fx-text-base-color: white; " +
				"-fx-background-color: #9068be;  -fx-pref-height: 30; ";
		return style;
	}

	@Override
	public AnchorPane tabPane(Stage stage, double width, double h) {
		AnchorPane ap = new AnchorPane();

		VBox all = new VBox();
		all.setPadding(new Insets(10));
		all.setSpacing(10);
		ap.getChildren().add(all);

		// 第一行 详情模板
		HBox line1 = new HBox();
		line1.setAlignment(Pos.CENTER_LEFT);
		line1.setSpacing(10);
		List<Node> getDetailsTemplate = unit.chooseFile(stage, width, "详情模板");
		line1.getChildren().addAll(getDetailsTemplate.get(0), getDetailsTemplate.get(1));

		// 第二行 要整理的数据文件
		HBox line2 = new HBox();
		line2.setAlignment(Pos.CENTER_LEFT);
		line2.setSpacing(10);
		List<Node> getDataFile = unit.chooseFile(stage, width, "数据文件");
		line2.getChildren().addAll(getDataFile.get(0), getDataFile.get(1));

		// 第三行 输入 cookie
		HBox line3 = new HBox();
		line3.setAlignment(Pos.CENTER_LEFT);
		line3.setSpacing(10);
		List<Node> getCookie = unit.newInputText(width, "请输入cookie : ", 100);
		line3.getChildren().addAll(getCookie.get(0), getCookie.get(1));

		// 第四行 输入 token
		HBox line4 = new HBox();
		line4.setAlignment(Pos.CENTER_LEFT);
		line4.setSpacing(10);
		List<Node> getToken = unit.newInputText(width, "请输入token : ", 100);
		line4.getChildren().addAll(getToken.get(0), getToken.get(1));

		all.getChildren().addAll(line1, line2, line3, line4);
		return ap;
	}
}
