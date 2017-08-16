package net.qiujuer.web.italker.push;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import java.util.logging.Logger;

import net.qiujuer.web.italker.push.provider.GsonProvider;
import net.qiujuer.web.italker.push.service.AccountService;
import org.glassfish.jersey.server.ResourceConfig;

public class Application extends ResourceConfig{
    public Application(){
        //注册逻辑处理的包名
//        packages("net.qiujuer.web.italker.push.service.service");
        packages(AccountService.class.getPackage().getName());

        //注册Json解析器
//        register(JacksonJsonProvider.class);
        register(GsonProvider.class);
        //注册日志打印输出
        register(Logger.class);
    }
}
