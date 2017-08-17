package net.qiujuer.web.italker.push.service;

import com.google.common.base.Strings;
import net.qiujuer.web.italker.push.bean.api.account.AccountRepModel;
import net.qiujuer.web.italker.push.bean.api.account.LoginModel;
import net.qiujuer.web.italker.push.bean.api.account.RegisterModel;
import net.qiujuer.web.italker.push.bean.api.base.ResponseModel;
import net.qiujuer.web.italker.push.bean.card.UserCard;
import net.qiujuer.web.italker.push.bean.db.User;
import net.qiujuer.web.italker.push.factory.UserFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/account")
public class AccountService extends BaseService{
    @POST
    @Path("/login")
    //指定请求与返回的响应体为JSON
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseModel<AccountRepModel> login(LoginModel model){
        if(!LoginModel.check(model)){
            return ResponseModel.buildParameterError();
        }
        User user = UserFactory.login(model.getAccount(),model.getPassword());
        if(user!=null){
            //如果有携带pushId
            if(!Strings.isNullOrEmpty(model.getPushId())){
                return bind(user,model.getPushId());
            }
            AccountRepModel rspModel = new AccountRepModel(user);
            return ResponseModel.buildOk(rspModel);
        }else{
            return ResponseModel.buildLoginError();
        }
    }

    @POST
    @Path("/register")
    //指定请求与返回的响应体为JSON
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseModel<AccountRepModel> register(RegisterModel model){
        if(!RegisterModel.check(model)){
            return ResponseModel.buildParameterError();
        }
        User user = UserFactory.findByPhone(model.getAccount().trim());
        if(user!=null) {
            return ResponseModel.buildHaveAccountError();
        }

        user = UserFactory.findByName(model.getName().trim());
        if(user!=null) {
            return ResponseModel.buildHaveNameError();
        }

        //开始注册逻辑
        user = UserFactory.register(model.getAccount(),
                model.getPassword(),
                model.getName());
        if(user!=null){
            //如果有携带pushId
            if(!Strings.isNullOrEmpty(model.getPushId())){
                return bind(user,model.getPushId());
            }
            AccountRepModel rspModel = new AccountRepModel(user);
            return ResponseModel.buildOk(rspModel);
        }else{
            return ResponseModel.buildRegisterError();
        }
    }


    @POST
    @Path("/bind/{pushId}")
    //指定请求与返回的响应体为JSON
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    //从请求头中获取token字段
    //pushid 从url资质中获取
    public ResponseModel<AccountRepModel> bind(@PathParam("pushId") String pushId){
        if(Strings.isNullOrEmpty(pushId) ){
            return ResponseModel.buildParameterError();
        }
        User self  = getSelf();
        return bind(self,pushId);
    }

    private ResponseModel<AccountRepModel> bind(User self,String pushId) {
        //绑定pushId
        User user = UserFactory.bindPushId(self,pushId);
        if(user == null)
            return ResponseModel.buildServiceError();
            AccountRepModel rspModel = new AccountRepModel(user,true);
            return ResponseModel.buildOk(rspModel);
    }
}
