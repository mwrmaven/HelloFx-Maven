package org.example.util;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.example.entity.ProcessInfo;
import org.example.init.Config;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author mavenr
 * @Classname Unit
 * @Description 通用的组件单元
 * @Date 2021/8/30 16:28
 */
public class Unit {

    /**
     * 选择文件夹组件集合
     * @param primaryStage
     * @param width
     * @return
     */
    public List<Node> chooseFolder(Stage primaryStage, double width, String buttonText) {
        // 选择文件夹/文件路径，并输入到文本框
        TextField text = new TextField();
        // 输入框中禁止编辑
        text.setDisable(true);
        text.setPrefWidth(width / 2 - 200);

        // 点击按钮，选择文件夹
        Button buttonFileChoose = new Button("选择文件夹路径");
        if (buttonText != null && !"".equals(buttonText.replaceAll(" ", ""))) {
            buttonFileChoose.setText(buttonText);
        }
        buttonFileChoose.setPrefWidth(120);
        buttonFileChoose.setAlignment(Pos.CENTER);
        buttonFileChoose.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                DirectoryChooser dc = new DirectoryChooser();
                File file = dc.showDialog(primaryStage);
                String path = file.getPath();
                text.setText(path);
            }
        });

        ArrayList<Node> nodes = new ArrayList<>();
        nodes.add(buttonFileChoose);
        nodes.add(text);

        // 设置button样式
        String style = "-fx-background-color: #FFD700; -fx-background-radius: 4";
        buttonFileChoose.setStyle(style);

        return nodes;
    }

    /**
     * 选择文件组件集合
     * @param primaryStage
     * @param width
     * @param buttonText
     * @return
     */
    public List<Node> chooseFile(Stage primaryStage, double width, String buttonText) {
        // 选择文件夹/文件路径，并输入到文本框
        TextField text = new TextField();
        // 输入框中禁止编辑
        text.setDisable(true);
        text.setPrefWidth(width / 2 - 200);

        // 点击按钮，选择文件夹
        Button buttonFileChoose = new Button("选择文件路径");
        if (buttonText != null && !"".equals(buttonText.replaceAll(" ", ""))) {
            buttonFileChoose.setText(buttonText);
        }
        buttonFileChoose.setPrefWidth(100);
        buttonFileChoose.setAlignment(Pos.CENTER);
        buttonFileChoose.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                String baseDir = Config.get("baseDir");
                FileChooser fc = new FileChooser();
                if (StringUtils.isNotBlank(baseDir)) {
                    File baseFile = new File(baseDir);
                    if (baseFile.exists()) {
                        fc.setInitialDirectory(baseFile);
                    } else {
                        fc.setInitialDirectory(new File("./"));
                    }
                }
                File file = fc.showOpenDialog(primaryStage);
                if (file == null) {
                    return;
                }
                String path = file.getPath();
                text.setText(path);
                Config.set("baseDir", file.getParent());
            }
        });

        ArrayList<Node> nodes = new ArrayList<>();
        nodes.add(buttonFileChoose);
        nodes.add(text);

        // 设置button样式
        String style = "-fx-background-color: #ffcc33; -fx-background-radius: 4";
        buttonFileChoose.setStyle(style);

        return nodes;
    }

    /**
     * 输入文本的组件集合
     * @param primaryStage
     * @param width
     * @param buttonText
     * @return
     */
    @Deprecated
    public List<Node> inputText(Stage primaryStage, double width, String buttonText) {
        Label label = new Label();
        if (StringUtils.isNotEmpty(buttonText)) {
            label.setText(buttonText);
        } else {
            label.setText("请输入文本");
        }

        // 输入文本
        TextField text = new TextField();
        text.setPrefWidth(width / 2 - 200);

        ArrayList<Node> nodes = new ArrayList<>();
        nodes.add(label);
        nodes.add(text);

        return nodes;
    }

    /**
     * 输入文本的组件的集合
     * @param width 框体的宽度
     * @param lText label的文本
     * @param labelWidth label的宽度
     * @return
     */
    public List<Node> newInputText(double width, String lText, double labelWidth) {
        Label label = new Label();
        label.setPrefWidth(labelWidth);
        if (StringUtils.isNotEmpty(lText)) {
            label.setText(lText);
        } else {
            label.setText("请输入文本");
        }

        // 输入文本
        TextField text = new TextField();
        text.setPrefWidth(width / 2 - 200);

        ArrayList<Node> nodes = new ArrayList<>();
        nodes.add(label);
        nodes.add(text);

        return nodes;
    }

    /**
     * 文本框失去焦点的保存操作
     * @param field
     * @param configText
     * @param configKey
     */
    public void loseFocuseSave(TextField field, String configText, String configKey) {
        field.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                // 判断内容改变，则保存内容
                if (!configText.equals(field.getText())) {
                    // 设置配置文件
                    Config.set(configKey, field.getText());
                }
            }
        });
    }

    /**
     * 启动浏览器
     * @param chromePath
     * @param ta
     */
    public void startChrome(String chromePath, TextArea ta) {
        if (StringUtils.isBlank(chromePath)) {
            ta.setText("请输入chrome浏览器的启动器类的文件路径！");
            return;
        }
        // 获取文件工具的路径
        String currentPath = System.getProperty("user.dir");
        // chrome测试数据存放路径
        String chromeTestPath= currentPath + File.separator + "chromeTest";
        // 启动chrome调试
        ta.appendText("\nchromePath = " + chromePath);
        // 查看端口是否被占用，如果被占用则先停掉端口再启动 区分 windows 和 mac ，命令行也是
        boolean alive = SocketUtil.isAlive("127.0.0.1", 9527);
        if (alive) {
            ta.setText("测试浏览器已启动，请在任务栏中点击浏览器图标创建新窗口！");
            return;
        }
        String[] cmd = new String[3];
        cmd[0] = chromePath;
        cmd[1] = "--remote-debugging-port=9527";
        cmd[2] = "--user-data-dir=" + chromeTestPath;
        try {
            Runtime.getRuntime().exec(cmd);
            ta.appendText("\nchrome浏览器远程调试模式启动成功！");
        } catch (IOException e) {
            System.out.println(e.getMessage());
            ta.appendText("\nchrome浏览器远程调试模式启动失败，请联系技术人员！");
        }
    }

    /**
     * 对象转list方法
     * @param obj 对象
     * @param clazz list集合中的元素的类
     * @return
     * @param <T>
     */
    public <T> List<T> objToList(Object obj, Class<T> clazz) {
        List<T> list = new ArrayList<T>();
        if (obj instanceof List<?>) {
            for (Object o : (List<?>) obj) {
                list.add(clazz.cast(o));
            }
            return list;
        }
        return null;
    }

    public String getCellValue(Cell cell) {
        if (cell == null || cell.getCellType() == CellType.BLANK
                || cell.getCellType() == CellType._NONE || cell.getCellType() == CellType.ERROR) {
            return "";
        } else if (cell.getCellType() == CellType.STRING) {
            return cell.getStringCellValue();
        } else if (cell.getCellType() == CellType.BOOLEAN) {
            boolean booleanCellValue = cell.getBooleanCellValue();
            return String.valueOf(booleanCellValue);
        } else if (cell.getCellType() == CellType.NUMERIC) {
            double numericCellValue = cell.getNumericCellValue();
            // 去掉double末尾的0
            BigDecimal bd = new BigDecimal(numericCellValue);
            BigDecimal noZeroBigDecimal = bd.stripTrailingZeros();
            // 不使用科学计数法
            String value = noZeroBigDecimal.toPlainString();
            return String.valueOf(value);
        } else if (cell.getCellType() == CellType.FORMULA) {
            return cell.getStringCellValue();
        } else {
            return "";
        }
    }

    /**
     * 获取所有进程（当前使用windows系统）
     * @return
     */
    public static List<ProcessInfo> getProcessList() {
        // 创建系统进程，查询当前运行的 chromedriver.exe的进程
        ProcessBuilder pb = new ProcessBuilder("tasklist");
        List<ProcessInfo> processInfoList = new ArrayList<>();
        try {
            Process p = pb.start();
            BufferedReader out = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8));

            String line;
            boolean flag = true;
            List<Integer> blackIndex = new ArrayList<>();
            while ((line = out.readLine()) != null) {
                System.out.println("PROCESS: " + line);
                // 不为空则，打印出pid
                if (CollectionUtils.isNotEmpty(blackIndex)) {
                    String pid = line.substring(blackIndex.get(0), blackIndex.get(1)).trim();
//                    System.out.println("pid:" + pid + "<<<<<<");
                    ProcessInfo pi = ProcessInfo.builder()
                            .info(line)
                            .pid(pid)
                            .build();
                    processInfoList.add(pi);
                }
                // 判断进程名是否以 chromedriver 开始
                int index = -1;
                int start = 0;
                if (flag && line.matches("=+ =+ =+ =+ =+")) {
                    // 获取空格位置
                    while ((index = line.indexOf(" ", start)) != -1) {
                        blackIndex.add(index);
                        start = index + 1;
                    }
                    flag = false;
                }
            }


        } catch (Exception e) {
            System.out.println("chromedriver进程关闭失败");
            throw new RuntimeException(e);
        }
        return processInfoList;
    }

    /**
     * 将配置文件中的Unicode 转 utf-8 汉字
     * @param 原始字符串
     * @return  转换后的格式的字符串
     */
    public String unicodeToChina(String str) {
        Charset set = Charset.forName("UTF-16");
        Pattern p = Pattern.compile("\\\\u([0-9a-fA-F]{4})");
        Matcher m = p.matcher( str );
        int start = 0 ;
        int start2 = 0 ;
        StringBuffer sb = new StringBuffer();
        while( m.find( start ) ) {
            start2 = m.start() ;
            if( start2 > start ){
                String seg = str.substring(start, start2) ;
                sb.append( seg );
            }
            String code = m.group( 1 );
            int i = Integer.valueOf( code , 16 );
            byte[] bb = new byte[ 4 ] ;
            bb[ 0 ] = (byte) ((i >> 8) & 0xFF );
            bb[ 1 ] = (byte) ( i & 0xFF ) ;
            ByteBuffer b = ByteBuffer.wrap(bb);
            sb.append( String.valueOf( set.decode(b) ).trim() );
            start = m.end() ;
        }
        start2 = str.length() ;
        if( start2 > start ){
            String seg = str.substring(start, start2) ;
            sb.append( seg );
        }
        return sb.toString() ;
    }

}
