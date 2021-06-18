package com.atguigu.gmall.cache;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author 赵赳
 * @CreateTime: 2021/4/20 18:06
 * @Description:
 */
@Component
@Aspect
public class GmallCacheAspect {


    @Autowired
    RedisTemplate redisTemplate;

    @Around("@annotation(com.atguigu.gmall.cache.GmallCache)")
    public Object aopProxy(ProceedingJoinPoint point) {

        Object proceed = null;

        //获取相关参数
        MethodSignature signature = (MethodSignature) point.getSignature();
        //代理方法参数
        Object[] args = point.getArgs();
        //返回类型，存储类型
        Class returnType = signature.getReturnType();
        //方法名
        String name = signature.getMethod().getName();
        //获取注解
        GmallCache annotation = signature.getMethod().getAnnotation(GmallCache.class);
        String prefix = annotation.prefix();

        //redis 的key
        String redisKey = name;
        if (null != args && args.length > 0) {
            for (Object arg : args) {
                redisKey = redisKey + ":" + arg;
            }
        }

        //执行代理操作
        //从redis中查询数据
        proceed = redisTemplate.opsForValue().get(redisKey);

        if (proceed == null) {
            String uuid = UUID.randomUUID().toString().replaceAll("-", "");
            //加入分布式锁 设置1秒过期
            Boolean ok = redisTemplate.opsForValue().setIfAbsent(redisKey + ":lock", uuid, 1, TimeUnit.SECONDS);
            if (ok) { //加锁成功
                // 查数据库
                try {
                    //执行代理方法,查询数据库，获取返回数据
                    proceed = point.proceed();
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
                if (proceed != null) {
                    //存储到redis中
                    redisTemplate.opsForValue().set(redisKey, proceed);
                } else {
                    try {
                        Object o = returnType.newInstance();
                        //如果取不存在的key则加入缓存请求等待10秒
                        redisTemplate.opsForValue().set(redisKey, o, 10, TimeUnit.SECONDS);
                    } catch (InstantiationException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }

            // lua脚本释放锁
            String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
            // 设置lua脚本返回的数据类型
            DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
            // 设置lua脚本返回类型为Long
            redisScript.setResultType(Long.class);
            redisScript.setScriptText(script);
            redisTemplate.execute(redisScript, Arrays.asList(redisKey + ":lock"), uuid);
        } else {
            // 自旋
//                try {
//                    Thread.sleep(2000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
            return redisTemplate.opsForValue().get(redisKey);
        }
        return proceed;
    }
}
