package com.newspace.websocket.client;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.handshake.ServerHandshake;

public class ClientDemo2 extends WebSocketClient{

	public static void main( String[] args ) throws URISyntaxException, InterruptedException {
		
		ExecutorService pool = Executors.newCachedThreadPool();
		
		/*
		 * 这里模拟 [4] 个客户端向服务器发送请求，模拟一桌麻将的四个玩家
		 * 其中玩家的ID为  [5-8]，桌号为 [B]
		 * 服务器接收到客户端的请求后，判断桌号，然后推送一条信息给此桌子上的其它玩家。（模拟出牌时，桌子上的所有玩家都可以看到出牌玩家所出的牌面数据）
		 */
		for(int i = 5; i <= 8; i++)
		{
			final int id = i;
			Runnable run = new Runnable() {
				
				@Override
				public void run() {
					try
					{
						send(id);
						
					} catch (URISyntaxException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}
			};
			
			pool.execute(run);
		}
		pool.shutdown();
		
		
		
	}
	
	/**
	 * @description 初始化连接，并发送数据。 每成功初始化一个连接，代表一个客户端与服务器建立连接成功，然后就可以随意发送消息
	 *              这里测试建立连接成功后， 发送一条消息， 告诉服务器桌号和用户ID，例如：{"tableNo":"B","userId":5}
	 * @param i
	 * @throws URISyntaxException
	 * @throws InterruptedException
	 */
	public static void send(int i) throws URISyntaxException, InterruptedException
	{
		ClientDemo2 c = new ClientDemo2( new URI( "ws://10.1.1.62:8080/websocket/websocket" )); 
		
		c.connect();
		Thread.sleep(1000);
			
		String tableNo = "A";
		if(i > 4)
		{
			tableNo = "B";
		}
		
		String message = "{\"tableNo\":\""+tableNo+"\",\"userId\":"+i+"}";
		System.out.println(String.format("桌号[%s],用户[%s]发送请求数据...", tableNo,i));
		c.send(message);
	}
	
	public ClientDemo2( URI serverUri , Draft draft ) {
		super( serverUri, draft );
	}
	
	public ClientDemo2(URI serverUri) {
		super(serverUri);
	}

	@Override
	public void onOpen(ServerHandshake arg0) {
		System.out.println( "opened connection..." );
		
	}
	
	@Override
	public void onClose(int code, String reason, boolean remote) {
		System.out.println( "Connection closed by " + ( remote ? "remote peer" : "us" ) );
		
	}

	@Override
	public void onError(Exception ex) {
		ex.printStackTrace();
		
	}

	@Override
	public void onMessage(String message) {
		System.out.println( "received: " + message );
		
	}



}
