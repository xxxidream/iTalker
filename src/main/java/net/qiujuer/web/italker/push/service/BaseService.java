package net.qiujuer.web.italker.push.service;

import net.qiujuer.web.italker.push.bean.db.User;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;

public class BaseService {

    //添加一个上下文注解，该注解会给securityContext赋值
    //具体的值为我们拦截器中所返回的securityContext
    @Context
    protected SecurityContext securityContext;

    protected User getSelf(){
        return (User)securityContext.getUserPrincipal();
    }

}
