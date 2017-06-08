package com.newspace.websocket.server;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import com.newspace.websocket.server.param.Message;
import com.newspace.websocket.utils.JsonUtils;
 
/**
 * @description websocket实现类
 * @author huqili
 * @date 2017年4月6日
 * 下面注解用来指定一个URI，客户端可以通过这个URI来连接到WebSocket。类似Servlet的注解mapping。无需在web.xml中配置。
 */
@ServerEndpoint("/websocket")
public class WebSocketServer{
    //静态变量，用来记录当前在线连接数。应该把它设计成线程安全的。
    private static int onlineCount = 0;
     
    //concurrent包的线程安全Set，用来存放每个客户端对应的MyWebSocket对象。若要实现服务端与单一客户端通信的话，可以使用Map来存放，其中Key可以为用户标识
    private static CopyOnWriteArraySet<WebSocketServer> webSocketSet = new CopyOnWriteArraySet<WebSocketServer>();
    
    /*
     * ConcurrentHashMap：使用锁分离技术允许多个修改操作并发进行。
     * CopyOnWriteArraySet：使用读写分离技术允许我们进行并发的读， 而不需要加锁
     * Map的key存放桌号，Set存放当前桌子上的玩家ID
     */
    private static ConcurrentHashMap<String, ConcurrentHashMap<WebSocketServer,String>> map = new ConcurrentHashMap<String, ConcurrentHashMap<WebSocketServer,String>>();
     
    //与某个客户端的连接会话，需要通过它来给客户端发送数据
    private Session session;
    
     
    /**
     * 连接建立成功调用的方法
     * @param session  可选的参数。session为与某个客户端的连接会话，需要通过它来给客户端发送数据
     */
    @OnOpen
    public void onOpen(Session session){
        this.session = session;
        webSocketSet.add(this);     //加入set中
        addOnlineCount();           //在线数加1
        System.out.println(String.format("有新连接加入！ID为：[%s],当前在线人数为：[%s]",session.getId(),getOnlineCount()));
    }
     
    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose(Session session){
        webSocketSet.remove(this);  //从set中删除
        subOnlineCount();           //在线数减1    
        System.out.println(String.format("有一连接关闭！ID为：[%s],当前在线人数为：[%s]",session.getId(),getOnlineCount()));
    }
     
    /**
     * 收到客户端消息后调用的方法
     * @param message 客户端发送过来的消息
     * @param session 可选的参数
     */
    @OnMessage
    public void onMessage(String json, Session session) {
        System.out.println(String.format("来自客户端[%s]的消息：%s", session.getId(),json));
         
        Message message = JsonUtils.fromJson(json, Message.class);
        
        String tableNo = message.getTableNo();
        int userId = message.getUserId();
        
        //如果集合中已经有此桌号信息，则直接将此客户端（玩家）加入到当前桌子下
        if(map.containsKey(tableNo))
        {
        	map.get(tableNo).put(this, String.valueOf(userId));
        }
        else
        {
        	ConcurrentHashMap<WebSocketServer,String> map2 = new ConcurrentHashMap<>();
        	map2.put(this, String.valueOf(userId));
        	map.put(tableNo, map2);
        }
        
        
        //群发消息
        broadcast(tableNo,userId,json);
    }
     
    /**
     * 发生错误时调用
     * @param session
     * @param error
     */
    @OnError
    public void onError(Session session, Throwable error){
        System.out.println("发生错误");
        error.printStackTrace();
    }
     

    /**
     * 将消息回调给对应桌子的所有玩家
     * @param map
     */
    private static void broadcast(String tableNo,int userId,String json) 
    {
    	
    	
        for (WebSocketServer client : map.get(tableNo).keySet())
        {
            try 
            {
                synchronized (client) 
                {
                    client.session.getBasicRemote().sendText(String.format("hi,tableNo:[%s],UserId:[%s],SessionID:[%s],the message is:[%s] from [%s]", tableNo,map.get(tableNo).get(client),client.session.getId(),json,userId));
                }
            }
            catch (IOException e)
            {
            	webSocketSet.remove(client);
                try
                {
                    client.session.close();
                }
                catch (IOException e1) { 
                	
                }
                broadcast(tableNo,userId,json);
            }
        }
    }
    
    public static synchronized int getOnlineCount() {
        return onlineCount;
    }
 
    public static synchronized void addOnlineCount() {
        WebSocketServer.onlineCount++;
    }
     
    public static synchronized void subOnlineCount() {
        WebSocketServer.onlineCount--;
    }
}