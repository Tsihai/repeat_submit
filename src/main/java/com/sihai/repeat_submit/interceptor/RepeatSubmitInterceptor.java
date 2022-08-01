package com.sihai.repeat_submit.interceptor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sihai.repeat_submit.annotation.RepeatSubmit;
import com.sihai.repeat_submit.redis.RedisCache;
import com.sihai.repeat_submit.request.RepeatableReadRequestWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class RepeatSubmitInterceptor implements HandlerInterceptor {

    // 重复参数
    public static final String REPEAT_PARAMS = "repeat_params";
    // 重复时间
    public static final String REPEAT_TIME = "repeat_time";
    public static final String REPEAT_SUBMIT_KEY = "repeat_submit_key";
    // 授权标识
    public static final String HEADER = "Authorization";

    @Autowired
    RedisCache redisCache;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 从request对象中得到提交的数据
        // request.getReader() 对返回内容的封装，可以让调用者更方便字符内容的处理
        // System.out.println("request.getReader().readLine() = " + request.getReader().readLine());

        // HandlerMethod: 封装的方法
        if (handler instanceof HandlerMethod){
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            Method method = handlerMethod.getMethod();
            RepeatSubmit repeatSubmit = method.getAnnotation(RepeatSubmit.class);
            if (repeatSubmit != null){
                // 判断是否需要拦截
                if(isRepeaSumit(request, repeatSubmit)){
                    Map<String, Object> map = new HashMap<>();
                    map.put("status", 500);
                    map.put("message", repeatSubmit.message());
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write(new ObjectMapper().writeValueAsString(map));
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 判断是否重复提交，返回true表示重复提交，false表示不重复提交
     * @param request
     * @param repeatSubmit
     * @return
     */
    private boolean isRepeaSumit(HttpServletRequest request, RepeatSubmit repeatSubmit) {
        // 请求参数字符串
        String nowParams = "";
        if (request instanceof RepeatableReadRequestWrapper){
            try {
                nowParams = ((RepeatableReadRequestWrapper) request).getReader().readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // 否则说明请求参数是 key, value的格式
        if (StringUtils.isEmpty(nowParams)){
            try {
                nowParams = new ObjectMapper().writeValueAsString(request.getParameterMap());
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
        Map<String, Object> nowDataMap = new HashMap<>();
        nowDataMap.put(REPEAT_PARAMS, nowParams);
        nowDataMap.put(REPEAT_TIME, System.currentTimeMillis());
        String requestURI = request.getRequestURI();
        String header = request.getHeader(HEADER);
        String cacheKey = REPEAT_SUBMIT_KEY + requestURI + header.replace("Bearer ", "");
        Object cacheObject = redisCache.getCacheObject(cacheKey);
        if (cacheObject != null){
            Map<String, Object> map = (Map<String, Object>) cacheObject;
            if (compareParms(map, nowDataMap)&&compareTime(map, nowDataMap,repeatSubmit.interval())){
                return true;
            }
        }
        redisCache.setCacheObject(cacheKey, nowDataMap, repeatSubmit.interval(), TimeUnit.MILLISECONDS);
        return false;
    }

    /**
     * 比较两个参数时间，返回true表示重复提交，false表示不重复提交
     * @param map
     * @param nowDataMap
     * @param interval
     * @return
     */
    private boolean compareTime(Map<String, Object> map, Map<String, Object> nowDataMap, int interval) {
        // redis中的时间
        Long time1 = (Long) map.get(REPEAT_TIME);
        // 当前时间
        Long time2 = (Long) nowDataMap.get(REPEAT_TIME);
        // 如果间隔时间太短
        if ((time2 - time1) < interval){
            return true;
        }
        return false;
    }


    /**
     * 比较参数是否一致
     * @param map
     * @param nowDataMap
     * @return
     */
    private boolean compareParms(Map<String, Object> map, Map<String, Object> nowDataMap) {
        String nowParams = (String) nowDataMap.get(REPEAT_PARAMS);
        String dataParams = (String) nowDataMap.get(REPEAT_PARAMS);
        return nowParams.equals(dataParams);
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }
}
