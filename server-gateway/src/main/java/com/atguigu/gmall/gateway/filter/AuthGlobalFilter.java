package com.atguigu.gmall.gateway.filter;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.result.ResultCodeEnum;
import com.atguigu.gmall.user.client.UserFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * @author 赵赳
 * @CreateTime: 2021/4/29 14:43
 * @Description: 网关过滤器
 */
@Component
public class AuthGlobalFilter implements GlobalFilter {

    AntPathMatcher pathMatcher = new AntPathMatcher();

    @Value("${authUrls.url}")
    String authUrls; //请求白名单

    @Autowired
    UserFeignClient userFeignClient;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        String uri = request.getURI().toString();
        String path = request.getPath().toString();

        //放行静态资源  //ico jpg css png js login
        if (path.contains("passport") || path.contains("login") || path.contains(".js") || path.contains(".png")
             || path.contains(".ico") || path.contains(".jpg") || uri.contains(".css") || uri.contains(".img")) {
            return chain.filter(exchange);
        }

        //拦截内部请求 黑名单
        if (pathMatcher.match("/**/inner/**", path)) {
            return out(response, ResultCodeEnum.PERMISSION);
        }

        //拦截任何请求
        String userTempId = "";
        Map<String, Object> resultMap = null;
        //获取token
        String token = getTokenOrTempId(request, "token");
        if (StringUtils.isEmpty(token)) {
            //用户没有登录，获取用户临时id
            userTempId = getTokenOrTempId(request, "userTempId");
        } else {
            //进行认证
            resultMap = userFeignClient.verify(token);
        }


        //鉴权白名单
        String[] split = authUrls.split(",");
        for (String authUrl : split) {
            if (uri.contains(authUrl)) {
                if (StringUtils.isEmpty(token) || !(Boolean) resultMap.get("isSuccess")) {
                    //鉴权失败
                    response.setStatusCode(HttpStatus.SEE_OTHER);
                    //重定向到登录页面
                    response.getHeaders().set(HttpHeaders.LOCATION, "http://passport.gmall.com/login.html?originUrl=" + uri);
                    return response.setComplete();
                }
            }
        }

        //鉴权成功，将用户id转发到下一个请求中
        if (resultMap != null && !StringUtils.isEmpty((String) resultMap.get("userId"))) {
            request.mutate().header("userId", (String) resultMap.get("userId"));
        }
        //鉴权失败，将临时用户id传递到下一个请求中
        if (!StringUtils.isEmpty(userTempId)) {
            request.mutate().header("userTempId", userTempId);
        }
        //刷新请求
        exchange.mutate().request(request);
        //放行
        return chain.filter(exchange);
    }

    /***
     *  获取token或用户临时id
     * @param request
     * @param name
     * @return
     */
    private String getTokenOrTempId(ServerHttpRequest request, String name) {
        String token = "";
        //同步请求获取信息
        MultiValueMap<String, HttpCookie> cookies = request.getCookies();
        if (cookies.size() > 0) {
            List<HttpCookie> tokens = cookies.get(name);
            if (tokens != null && tokens.size() > 0) {
                token = tokens.get(0).getValue();
            }
        }

        if (StringUtils.isEmpty(token)) {
            //异步请求获取token
            List<String> list = request.getHeaders().get(name);
            if (list != null && list.size() > 0) {
                token = list.get(0);
            }
        }

        return token;
    }


    //转换器
    private Mono<Void> out(ServerHttpResponse response, ResultCodeEnum resultCodeEnum) {
        Result<Object> result = Result.build(null, resultCodeEnum);
        byte[] bits = JSONObject.toJSONString(result).getBytes(StandardCharsets.UTF_8);
        DataBuffer wrap = response.bufferFactory().wrap(bits);
        response.getHeaders().add("Content-Type", "application/json;charset=UTF-8");
        return response.writeWith(Mono.just(wrap));
    }

}
