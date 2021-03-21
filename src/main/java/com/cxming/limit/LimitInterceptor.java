package com.cxming.limit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.util.concurrent.RateLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Constructor;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * @author caixiaoming
 * @create 2021-03-21 23:57
 */
public class LimitInterceptor implements HandlerInterceptor {

    private final static Logger LOGGER = LoggerFactory.getLogger(LimitInterceptor.class);

    private static final String PROPERTY_PREFIX = "limit.";

    private static Cache<String, RateLimiter> limiterCache;

    @Value("#{T(java.lang.Long).parseLong('${limit.expire.sesonds:3600}')}")
    private long limitExpireSeconds;

    @Autowired
    private Environment environment;

    @PostConstruct
    public void init() {
        limiterCache = CacheBuilder.newBuilder()
                .expireAfterAccess(limitExpireSeconds, TimeUnit.SECONDS)
                .build();
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (handler.getClass().isAssignableFrom(HandlerMethod.class)) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            ApiLimit limit = handlerMethod.getMethodAnnotation(ApiLimit.class);
            if (limit == null) {
                return true;
            }
            final String method = handlerMethod.getBeanType().getName().concat(".").concat(handlerMethod.getMethod().getName());
            //qps顺序：配置 > 代码，配置修改可以实时生效
            final long pps = Long.valueOf(environment.getProperty(PROPERTY_PREFIX.concat(method), String.valueOf(limit.pps())));
            String cacheKey = method.concat(".").concat(String.valueOf(pps));
            RateLimiter rateLimiter = limiterCache.get(cacheKey, new Callable<RateLimiter>() {
                @Override
                public RateLimiter call() throws Exception {
                    LOGGER.info("Initialize RateLimiter of {}, pps = {}.", method, pps);
                    return RateLimiter.create(pps);
                }
            });
            //判断是否阻塞
            if (limit.block()) {
                rateLimiter.acquire();
                return true;
            } else {
                if (rateLimiter.tryAcquire()) {
                    return true;
                } else {
                    Constructor<? extends Exception> constructor = limit.handle().getDeclaredConstructor(String.class);
                    throw constructor.newInstance(String.format("Request method[%s] pps out of limit[%s]!", method, pps));
                }
            }
        }
        return true;
    }
}
