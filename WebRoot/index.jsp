<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%
String path = request.getContextPath();
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
%>
 
<!DOCTYPE HTML>
<html>
  <head>
    <base href="<%=basePath%>">
    <title>My WebSocket</title>
  </head>
   
  <body>
    <b>WebSocket Test</b><br/><br/>
    TIPS:
    <br>1、打开此页面，代表与WebSocket服务器建立连接成功。
    <br>2、在输入框内输入消息，点击 Send 按钮，消息会发送到WebSocket服务器。 
    <br>3、如果有多个客户端连接同一个WebSocket服务，那么其中一个客户端发送消息后， 服务器端会将消息回调给所有客户端，在客户端的Message List下显示
    <br>4、点击 Close 按钮，断开连接。
    <br><br>
    <input id="text" type="text" />
    <button onclick="send()">Send</button>
    <button onclick="closeWebSocket()">Close</button>
    
    <br/>
    <br/>
    <b>Message List...</b><br/>
    <div id="message"></div>
  </body>
   
  <script type="text/javascript">
      var websocket = null;
       
      //判断当前浏览器是否支持WebSocket
      if('WebSocket' in window){
          websocket = new WebSocket("ws://10.1.1.62:8080/websocket/websocket");
      }
      else{
          alert('Not support websocket')
      }
       
      //连接发生错误的回调方法
      websocket.onerror = function(){
          setMessageInnerHTML("error");
      };
       
      //连接成功建立的回调方法
      websocket.onopen = function(event){
          setMessageInnerHTML("open");
      }
       
      //接收到消息的回调方法
      websocket.onmessage = function(event){
          setMessageInnerHTML(event.data);
      }
       
      //连接关闭的回调方法
      websocket.onclose = function(){
          setMessageInnerHTML("close");
      }
       
      //监听窗口关闭事件，当窗口关闭时，主动去关闭websocket连接，防止连接还没断开就关闭窗口，server端会抛异常。
      window.onbeforeunload = function(){
          websocket.close();
      }
       
      //将消息显示在网页上
      function setMessageInnerHTML(innerHTML){
          document.getElementById('message').innerHTML += innerHTML + '<br/>';
      }
       
      //关闭连接
      function closeWebSocket(){
          websocket.close();
      }
       
      //发送消息
      function send(){
          var message = document.getElementById('text').value;
          websocket.send(message);
      }
  </script>
</html>