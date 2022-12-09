package org.example.util;

import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.List;

/**
 * @Classname SocketUtil
 * @Description 端口连接测试
 * @Date 2022/12/4 19:56
 * @author mavenr
 */
public class SocketUtil {

	/**
	 * 测试端口号是否在用
	 * @param host
	 * @param port
	 * @return
	 */
	public static boolean isAlive(String host, int port) {
		boolean flag = false;
		SocketAddress socketAddress = new InetSocketAddress(host, port);
		Socket socket = new Socket();
		// 设置超时时间
		int timeout = 3000;
		System.out.println("测试端口信息 host：" + host + "； port：" + port);
		try {
			socket.connect(socketAddress, timeout);
			socket.close();
			flag = true;
			System.out.println("host：" + host + "; port:" + port + " 端口已启用！");
		} catch (IOException e) {
			System.out.println("host：" + host + "; port:" + port + " 端口未启用！");
		}
		return flag;
	}

	/**
	 * 关闭端口集合
	 * @param ports 端口集合
	 */
	public static void killPorts(List<Integer> ports) {
		for (Integer p : ports) {
			kill(p);
		}
		System.out.println("端口清理程序执行完毕！");
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 关闭端口
	 * @param port
	 */
	public static void kill(int port) {
		System.out.println("要停止的端口号为：" + port);
		Runtime runtime = Runtime.getRuntime();
		// 查找进程号
		try {
			Process process;
			// 获取系统类型
			String osName = System.getProperty("os.name");
			String flag = "Mac";
			if (osName.startsWith("Windows")) {
				flag = "Windows";
				process = runtime.exec("cmd /c netstat -ano | findstr \"" + port + "\"");
			} else {
				process = runtime.exec("lsof -i:"+ port);
			}

			InputStream inputStream = process.getInputStream();
			String read = read(inputStream, "UTF-8", port, flag);
			if (StringUtils.isBlank(read)) {
				System.out.println("未查询到启动的端口号");
			} else {
				System.out.println("关闭进程id：" + read);
				// 关掉进程
				if (flag.equals("Mac")) {
					process = runtime.exec("kill " + read);
				} else {
					process = runtime.exec("cmd /c taskkill /f /t /pid " + read);
				}
			}
			System.out.println("端口 " + port + " 关闭成功！");

		} catch (Exception e) {
			System.out.println("端口 " + port + " 关闭失败！");
			throw new RuntimeException(e);
		}

	}

	/**
	 * 获取命令行执行后返回的数据
	 * @param in 命令行执行后的输入信息
	 * @param charset 输入信息的编码格式
	 * @param port 要关闭的端口号
	 * @param osName 系统名称
	 * @return
	 * @throws Exception
	 */
	private static String read(InputStream in, String charset, int port, String osName) throws Exception {
		BufferedReader reader = new BufferedReader(new InputStreamReader(in, charset));
		String line;
		while ((line = reader.readLine()) != null) {
			if ("Mac".equals(osName)) {
				if (line.contains("Google")) {
					String[] split = line.trim().split(" +");
					// 获取name列
					String nameStr = split[8];
					if (!nameStr.contains(":")) {
						return null;
					}
					String portStr = nameStr.substring(nameStr.indexOf(":") + 1);
					if (String.valueOf(port).equals(portStr)) {
						return split[1];
					}
				}
			} else {
				if (line.contains(String.valueOf(port))) {
					String[] split = line.trim().split(" +");
					// 获取name列
					String nameStr = split[1];
					if (!nameStr.contains(":")) {
						return null;
					}
					String portStr = nameStr.substring(nameStr.indexOf(":") + 1);
					if (String.valueOf(port).equals(portStr)) {
						return split[4];
					}
				}
			}
		}
		return null;
	}
}
