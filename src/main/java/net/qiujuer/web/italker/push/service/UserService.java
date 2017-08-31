package net.qiujuer.web.italker.push.service;

import com.google.common.base.Strings;
import net.qiujuer.web.italker.push.bean.api.account.AccountRepModel;
import net.qiujuer.web.italker.push.bean.api.base.ResponseModel;
import net.qiujuer.web.italker.push.bean.api.user.UpdateInfoModel;
import net.qiujuer.web.italker.push.bean.card.UserCard;
import net.qiujuer.web.italker.push.bean.db.User;
import net.qiujuer.web.italker.push.factory.UserFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户信息处理
 */
//127.0.0.1/api/user/...
@Path("/user")
public class UserService extends BaseService {

    //用户修改信息接口
    //返回自己的个人信息
    @PUT
    //@Path("")//127.0.0.1/api/user  不需要写，就是当前目录
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseModel<UserCard> update(@HeaderParam("token") String token, UpdateInfoModel model) {
        if (Strings.isNullOrEmpty(token) || !UpdateInfoModel.check(model)) {
            return ResponseModel.buildParameterError();
        }
        User self = getSelf();

        self = model.updateToUser(self);
        self = UserFactory.update(self);
        UserCard card = new UserCard(self, true);
        return ResponseModel.buildOk(card);

    }

    @GET
    @Path("/contact")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseModel<List<UserCard>> contact() {
        User self = getSelf();
        List<User> users = UserFactory.contacts(self);
        List<UserCard> userCards = users.stream()
                .map(user -> new UserCard(user, true))
                .collect(Collectors.toList());
        return ResponseModel.buildOk(userCards);
    }

    @PUT//修改类使用put
    @Path("/follow/{followId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseModel<UserCard> follow(@PathParam("followId") String followId) {
        User self = getSelf();

        //不能关注自己
        if (self.getId().equalsIgnoreCase(followId) || Strings.isNullOrEmpty(followId)) {
            return ResponseModel.buildParameterError();
        }
        //找到我要关注的人
        User followUser = UserFactory.findById(followId);
        if (followUser == null) {
            //未找到人
            return ResponseModel.buildNotFoundUserError(null);
        }

        followUser = UserFactory.follow(self, followUser, null);
        if (followId == null) {
            return ResponseModel.buildServiceError();
        }
        //TODO 通知我关注的人我关注了他
        //返回关注的人的信息
        return ResponseModel.buildOk(new UserCard(followUser, true));
    }

    //获取某人的信息
    @GET
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseModel<UserCard> getUser(@PathParam("id") String id) {
        if (Strings.isNullOrEmpty(id)) {
            return ResponseModel.buildParameterError();
        }
        User self = getSelf();
        if (self.getId().equalsIgnoreCase(id)) {
            return ResponseModel.buildOk(new UserCard(self, true));
        }

        User user = UserFactory.findById(id);
        if (user == null) {
            return ResponseModel.buildNotFoundUserError(null);
        }
        boolean isFollow = UserFactory.getUserFollow(self, user) != null;

        return ResponseModel.buildOk(new UserCard(user, isFollow));
    }

    //搜索人的接口
    @GET
    @Path("/search/{name:(.*)?}")//名字为任意字符，可以为空
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseModel<List<UserCard>> search(@DefaultValue("") @PathParam("name") String name) {
        User self = getSelf();
        List<User> searchUsers = UserFactory.search(name);
        //把查询的人封装为UserCard
        //判断这些人是否已经有我关注的人
        //如果有，则返回的关注状态中应该已经设置好状态

        //拿出我的联系人
        final List<User> contacts = UserFactory.contacts(self);
        List<UserCard> userCards = searchUsers.stream()
                .map(user -> {
                    //判断这个人是否是我自己,或者是我的联系人中的
                    boolean isFollow = user.getId().equalsIgnoreCase(self.getId())
                            || contacts.stream().anyMatch(contactUser -> contactUser.getId().equalsIgnoreCase(user.getId()));
                    return new UserCard(user, isFollow);
                })
                .collect(Collectors.toList());
        return ResponseModel.buildOk(userCards);
    }
}
