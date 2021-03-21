# api-limit
### 基于RateLimiter的接口限流器
 <br>①打包到本地仓库或远程仓库
 ```
 -mvn clean install
 或
 -mvn clean deploy
```
 <br>②在项目中引入jar包
 ```xml
    <dependency>
        <groupId>com.cxming</groupId>
        <artifactId>api-limit</artifactId>
        <version>1.0.0.RELEASE</version>
    </dependency>
```
 <br>③装配并注册拦截器
 ```java
@Configuration
public class WebConfiguration implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(limitInterceptor());
    }
    @Bean
    public LimitInterceptor limitInterceptor() {
        return new LimitInterceptor();
    }
}
```
 <br>④在实际开发的Controller接口中加上@ApiLimit注解，即可根据设置的参数进行限流
 ```java
@RestController
public class DemoController {
    @ApiLimit(pps = 5)
    @RequestMapping("/api/test/limit")
    public Object test(){
        return null;
    }
}
```
 <br>⑤使用全局异常捕获OutOfLimitException或设置handle参数设置为自定义捕获的Exception
 ```java
@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler
    @ResponseBody
    public ResponseResult handleLimitException(Exception exception) {
        return new ResponseResult(ResponseEnum.LIMIT);
    }
}
```
 
