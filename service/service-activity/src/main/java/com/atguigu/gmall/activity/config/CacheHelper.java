package com.atguigu.gmall.activity.config;

import org.aspectj.weaver.ast.Var;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 赵赳
 * @CreateTime: 2021/5/11 16:10
 * @Description: 存储库存状态，服务器共享资源，使用并发集合ConcurrentHashMap
 */
public class CacheHelper {

    public static final ConcurrentHashMap<String,String> map = new ConcurrentHashMap<>();

    public static void put(String key,String value){
        map.put(key,value);
    }

    public static String get(String key){
        return map.get(key);
    }
}
