package net.qiujuer.web.italker.push.service;

import net.qiujuer.web.italker.push.bean.api.account.RegisterModel;
import net.qiujuer.web.italker.push.bean.card.UserCard;
import net.qiujuer.web.italker.push.bean.db.User;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/account")
public class AccountService {
    @POST
    @Path("/register")
    //指定请求与返回的响应体为JSON
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public UserCard register(RegisterModel model){
        UserCard card = new UserCard();
        card.setName(model.getName());
        card.setFollow(true);
        return card;
    }
}
