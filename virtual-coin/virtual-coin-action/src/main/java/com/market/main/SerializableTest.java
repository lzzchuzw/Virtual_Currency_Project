package com.market.main;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import org.apache.commons.io.output.ByteArrayOutputStream;

import com.market.pojo.TradingPlatform;
import com.market.service.ITradingPlatformService;
import com.market.service.imp.TradingPlatformService;
import com.utils.jedis.SerializableUtils;

import redis.clients.jedis.Jedis;

public class SerializableTest {
	
public static final String FILE_PATH = "F:/testFolder/serializable/";
	
	public static void main(String[] args) {
		//saveAndReadObjectFromJedis("Okex");
		//testSerializableListObject();
		testSerializableListObject2();
	}
    /**
     * 
    * @Title: saveAndReadObjectFromJedis
    * @Description: 向redis中保留和读取Object 序列化
    * @param name void
    * @author leisure
    * @date 2018年6月4日下午3:52:19
     */
	public static void saveAndReadObjectFromJedis(String name) {
		ITradingPlatformService tradingPlatformService = new TradingPlatformService();
		TradingPlatform tp = tradingPlatformService.getObject("where o.name = '"+name+"'");
		String filePath = FILE_PATH+tp.getName()+".txt";
		/*File file = new File(filePath);
		if(!file.exists()) {
			System.out.println("file not exists.....");
			return ;
		}*/
		FileOutputStream fos = null;
		ObjectOutputStream oos = null;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos2 = null;
		
		try {
			fos = new FileOutputStream(filePath);
			oos = new ObjectOutputStream(fos);
			oos2 = new ObjectOutputStream(baos);
		    
			oos.writeObject(tp);
			oos2.writeObject(tp);
			
			oos.flush();
			oos2.flush();
		} catch (FileNotFoundException e) {
			
			e.printStackTrace();
		} catch (IOException e) {
			
			e.printStackTrace();
		}finally {
			
			try {
				oos.close();
				fos.close();
				oos2.close();
			} catch (IOException e1) {
				
				e1.printStackTrace();
			}
			
		}
		
		
        //读取保存的对象
		FileInputStream fis = null;
		ObjectInputStream ois = null;
		ObjectInputStream ois2 = null;
		try {
			fis = new FileInputStream(filePath);
			ois = new ObjectInputStream(fis);
			System.out.println("输出内存中序列化的对象");
			System.out.println(new String(baos.toByteArray()));
			ois2 = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
			TradingPlatform saveTradingPlatform = (TradingPlatform) ois.readObject();
			TradingPlatform saveTradingPlatform2 = (TradingPlatform) ois2.readObject();
			outputObject(saveTradingPlatform);
			System.out.println("输出内存中序列化的对象");
			outputObject(saveTradingPlatform2);
			//System.out.println();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	public static void testSerializableListObject() {
		ITradingPlatformService tradingPlatformService = new TradingPlatformService();
		List<TradingPlatform> tpList = tradingPlatformService.findObjects(" where 1=1");
		System.out.println("tpList.size = "+tpList.size());
		serializableListObject(tpList);
	}
	
	public static void testSerializableListObject2() {
		ITradingPlatformService tradingPlatformService = new TradingPlatformService();
		List<TradingPlatform> tpList = tradingPlatformService.findObjects(" where 1=1");
		System.out.println("tpList.size = "+tpList.size());
		SerializableUtils<List<TradingPlatform>> su = new SerializableUtils<List<TradingPlatform>>();
		byte[] bytes = su.serializableEncode(tpList);
			
		
		 //连接本地的 Redis 服务
        Jedis jedis = new Jedis("192.168.5.150",6379);
        System.out.println("连接成功");
        //查看服务是否运行
        System.out.println("服务正在运行: "+jedis.ping());
        try {
			//jedis.set("tp_jedis",new String(baos.toByteArray(),"utf-8"));
			jedis.set("tp_jedis".getBytes(),bytes);
		} catch (Exception e) {
			e.printStackTrace();
		}
        byte[] tt = jedis.get("tp_jedis".getBytes());
        
        List<TradingPlatform> list = su.serializableDecode(tt);
		for(int index=0;index<list.size();index++) {
			TradingPlatform tp = list.get(index);
			outputObject(tp);
		}
	}
	/**
	 * 
	* @Title: serializableListObject
	* @Description: 序列化list
	* @param tpList
	* @return String
	* @author leisure
	* @date 2018年6月4日下午5:14:06
	 */
	public static String serializableListObject(List<TradingPlatform> tpList) {
		if(null==tpList || 0==tpList.size()) {
			return null;
		}
		String filePath = FILE_PATH + "list"+".txt";
		TradingPlatform[] tpArray = new TradingPlatform[tpList.size()];
		tpList.toArray(tpArray);
		System.out.println("tpArray.lenth = "+tpArray.length);
		FileOutputStream fos = null;
		ObjectOutputStream oos = null;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos2 = null;
		try {
			fos = new FileOutputStream(filePath);
			oos = new ObjectOutputStream(fos);
            oos2 = new ObjectOutputStream(baos);
		    
			oos.writeObject(tpArray);
			oos2.writeObject(tpArray);
			
			oos.flush();
			oos2.flush();
		} catch (IOException e) {
		
			e.printStackTrace();
		}finally {
			try {
				oos.close();
				fos.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		
		 //连接本地的 Redis 服务
        Jedis jedis = new Jedis("localhost");
        System.out.println("连接成功");
        //查看服务是否运行
        System.out.println("服务正在运行: "+jedis.ping());
        try {
			//jedis.set("tp_jedis",new String(baos.toByteArray(),"utf-8"));
			jedis.set("tp_jedis".getBytes(),baos.toByteArray());
		} catch (Exception e) {
			e.printStackTrace();
		}
        byte[] tt = jedis.get("tp_jedis".getBytes());
        
        //System.out.println("m_s = "+m_s);
		 //读取保存的对象
		FileInputStream fis = null;
		ObjectInputStream ois = null;
		ObjectInputStream ois2 = null;
		try {
			fis = new FileInputStream(filePath);
			ois = new ObjectInputStream(fis);
			//ois2 = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
			ois2 = new ObjectInputStream(new ByteArrayInputStream(tt));
			//TradingPlatform[] saveTradingPlatformArray = (TradingPlatform[]) ois.readObject();
			TradingPlatform[] saveTradingPlatformArray2 = (TradingPlatform[]) ois2.readObject();
			/*for(int i=0;i<saveTradingPlatformArray.length;i++) {
				System.out.println(" 第 = "+i+" 个保存的TradingPlatform对象======>");
				outputObject(saveTradingPlatformArray[i]);
			}*/
			for(int i=0;i<saveTradingPlatformArray2.length;i++) {
				System.out.println(" 第 = "+i+" 个保存的TradingPlatform对象======>");
				outputObject(saveTradingPlatformArray2[i]);
			}
		} catch (IOException e) {
		
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("输出内存中序列化的对象");
		return filePath;
	}
	
	
	
	/**
	 * 
	* @Title: outputObject
	* @Description: 输出对象
	* @param tp void
	* @author leisure
	* @date 2018年6月4日下午4:59:32
	 */
	public static void outputObject(TradingPlatform tp) {
		Field[] fields = TradingPlatform.class.getDeclaredFields();
		System.out.println("fields.lenth = "+fields.length);
		for(int index=0;index<fields.length;index++) {
			Field field = fields[index];
			String fieldName = field.getName();
			
			String getMethodName = "get"+fieldName.substring(0, 1).toUpperCase()+fieldName.substring(1);
			
			/*Method[] methods = TradingPlatform.class.getMethods();
			for(int i=0;i<methods.length;i++) {
				Method method = methods[i];
				if(method.getName().contains("get")) {
					System.out.println("i = "+i+"----getMethod name = "+method.getName());
					System.out.println(method.invoke(saveTradingPlatform, null));
				}
			}*/
			
			Method method;
			try {
				method = TradingPlatform.class.getDeclaredMethod(getMethodName, null);
				String s = String.valueOf(method.invoke(tp, null));
				System.out.println("getMethodName = "+getMethodName+"--- = "+s);
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
		}
	}
	
	

}
