package com.newspace.websocket.utils;

import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

/**
 * {@link JsonUtils.java} 
 * @description 用于操作json的工具类，使用gson来进行json字符串和实体类对象之间的转换
 * 				备注：2017年6月8日导入github
 * @author huqili
 * @since JDK1.8
 * @date 2016年6月1日
 * 
 */
public class JsonUtils
{
	/**
	 * 一般的Gson对象,默认不转义特殊字符
	 * 
	 */
	private final static Gson gson = new GsonBuilder().disableHtmlEscaping().create();

	/**
	 * 只操作带有@Exponse注解的字段 的 Gson对象(默认不转义特殊字符)
	 */
	private final static Gson exposeGson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().disableHtmlEscaping().create();
	
	/**
	 * 特殊的Gson对象（disableHtmlEscaping()方法不转义特殊字符，比如&<>等；setPrettyPrinting()方法可以将json格式化输出）
	 */
	private final static Gson specialGson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

	/**
	 * 将json字符串转换成对象
	 * @param json json字符串
	 * @param clazz 实体类的Class对象
	 * @return T 实体类对象
	 */
	public static <T> T fromJson(String json, Class<T> clazz)
	{
		return gson.fromJson(json, clazz);
	}

	/**
	 * 从流中读取json并转换成对象
	 * @param reader 输入json的流
	 * @param clazz 实体类的Class对象
	 * @return T 实体类对象
	 */
	public static <T> T fromJson(Reader reader, Class<T> clazz)
	{
		return gson.fromJson(reader, clazz);
	}

	/**
	 * 将实体类对象转化成json字符串
	 * @param obj 实体类对象
	 * @return String json格式字符串
	 */
	public static String toJson(Object obj)
	{
		return gson.toJson(obj);
	}

	/**
	 * 将实体类对象转化成json字符串
	 * @param obj 实体类对象
	 * @param clazz 实体类的Class对象
	 * @return String json格式字符串
	 */
	public static String toJson(Object obj, Class<?> clazz)
	{
		return gson.toJson(obj, clazz);
	}

	/**
	 * 将实体类中转换成json字符串（仅转换带有@Exponse注解的字段）
	 * @param obj 实体对象
	 * @return String json格式字符串
	 */
	public static String toJsonWithExpose(Object obj)
	{
		return exposeGson.toJson(obj);
	}
	
	/**
	 * 将实体类转化成特殊的json格式（不转义特殊字符，并格式化输出）
	 * @param obj
	 * @return
	 */
	public static String toJsonWithSpecial(Object obj)
	{
		return specialGson.toJson(obj);
	}
	
	/**
	 * 根据json字符串返回Map对象
	 * 
	 * @param json
	 * @return
	 */
	public static Map<String, Object> json2Map(String json) 
	{
		return JsonUtils.toMap(JsonUtils.parse2JsonObject(json));  
	}
	
	/**
	 * 根据json字符串返回List集合
	 * 
	 * @param json
	 * @return
	 */
	public static List<Object> json2List(String json)
	{
		return JsonUtils.toList(JsonUtils.parse2JsonArray(json));
		
	}
	

	/**
	 * 将json串转换成指定类型的List集合
	 * @param json
	 * @param clazz list元素对象的类型
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> List<T> json2List(String json,Class<T> clazz) throws ClassNotFoundException
	{
		List<Object> list = gson.fromJson(json,new TypeToken<List<Object>>(){}.getType());
		List<T> _list = new ArrayList<T>();
		Class<T> classT = (Class<T>) Class.forName(clazz.getName()); 
		for(Object obj:list){
			/*
			 * 括号里边toJson是因为obj对象的结构为HashLinkTreeMap
			 * 若不转换为json串，直接用toString 方法，当json 中有属性为空时，转换失败
			 */
			 T perT = gson.fromJson(gson.toJson(obj), classT);
			_list.add(perT);
		}
		return _list;
	}

	/**
	 * 获取JsonObject
	 * @param json
	 * @return
	 */
	public static JsonObject parse2JsonObject(String json) {
		JsonParser parser = new JsonParser();
		JsonObject jsonObj = parser.parse(json).getAsJsonObject();
		return jsonObj;
	}

	/**
	 * 获取JsonArray
	 * @param json
	 * @return
	 */
	public static JsonArray parse2JsonArray(String json)
	{
		JsonParser parser = new JsonParser();
		JsonArray jsonArray = parser.parse(json).getAsJsonArray();
		return jsonArray;
	}
	
	/**
	 * 将JSONObjec对象转换成Map-List集合
	 * 
	 * @param json
	 * @return
	 */
	public static Map<String, Object> toMap(JsonObject json) {
		Map<String, Object> map = new HashMap<String, Object>();
		Set<Entry<String, JsonElement>> entrySet = json.entrySet();
		for (Iterator<Entry<String, JsonElement>> iter = entrySet.iterator(); iter
				.hasNext();) {
			Entry<String, JsonElement> entry = iter.next();
			String key = entry.getKey();
			Object value = entry.getValue();
			if (value instanceof JsonArray)
				map.put((String) key, toList((JsonArray) value));
			else if (value instanceof JsonObject)
				map.put((String) key, toMap((JsonObject) value));
			else
				map.put((String) key, value);
		}
		return map;
	}

	/**
	 * 将JSONArray对象转换成List集合
	 * 
	 * @param json
	 * @return
	 */
	public static List<Object> toList(JsonArray json) {
		List<Object> list = new ArrayList<Object>();
		for (int i = 0; i < json.size(); i++) {
			Object value = json.get(i);
			if (value instanceof JsonArray) {
				list.add(toList((JsonArray) value));
			} else if (value instanceof JsonObject) {
				list.add(toMap((JsonObject) value));
			} else {
				list.add(value);
			}
		}
		return list;
	}
}