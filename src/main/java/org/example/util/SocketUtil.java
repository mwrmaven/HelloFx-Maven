package org.example.util;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

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
}
