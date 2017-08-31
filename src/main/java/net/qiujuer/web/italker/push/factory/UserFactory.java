package net.qiujuer.web.italker.push.factory;

import com.google.common.base.Strings;
import net.qiujuer.web.italker.push.bean.db.User;
import net.qiujuer.web.italker.push.bean.db.UserFollow;
import net.qiujuer.web.italker.push.utils.Hib;
import net.qiujuer.web.italker.push.utils.TextUtil;
import org.hibernate.Session;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class UserFactory {

    // 通过Phone找到User
    public static User findByPhone(String phone) {
        return Hib.query(session -> (User) session
                .createQuery("from User where phone=:inPhone")
                .setParameter("inPhone", phone)
                .uniqueResult());
    }
    // 通过Name找到User
    public static User findByName(String name) {
        return Hib.query(session -> (User) session
                .createQuery("from User where name=:inName")
                .setParameter("inName", name)
                .uniqueResult());
    }
    // 通过id找到User
    public static User findById(String id) {
        return Hib.query(session -> session.get(User.class,id));
    }

    // 通过Token找到User
    public static User findByToken(String token) {
        return Hib.query(session -> (User) session
                .createQuery("from User where token=:inToken")
                .setParameter("inToken", token)
                .uniqueResult());
    }

    public static User bindPushId(User user,String pushId){
        if (Strings.isNullOrEmpty(pushId))
            return null;
        //第一步查询是否有其他用户绑定了这个设备
        //取消绑定，避免推送混乱
        // 查询的列表不能包括自己
        Hib.queryOnly(session ->{
            List<User> userList = (List<User>) session
                    .createQuery("from User where lower(pushId)=:pushId and id!=:userId")
                    .setParameter("pushId", pushId.toLowerCase())
                    .setParameter("userId", user.getId())
                    .list();
            for(User u : userList){
                u.setPushId(null);
                session.saveOrUpdate(u);
            }
        });
        if(pushId.equalsIgnoreCase(user.getPushId())){
            return user;
        }else{
            //如果当前账户之前的设备id，和需要绑定的不同
            //那么需要单点登录，让之前的设备退出账户
            //给之前的设备推送一条退出信息
            if(Strings.isNullOrEmpty(user.getPushId())){
                //TODO 给之前的设备推送一条退出信息
            }
            user.setPushId(pushId);
            //更新新的设备id
            return update(user);
        }
    }

    public static User login(String account,String password){
        final String accountStr = account.trim();
        final String encodePassword = encodePassword(password);
        User user = Hib.query(session -> (User) session
                .createQuery("from User where phone=:phone and password= :password")
                .setParameter("phone", accountStr)
                .setParameter("password", encodePassword)
                .uniqueResult());
        if (user!=null) {
            user = login(user);
        };
        return user;
    }

    /**
     * 注册
     * @param account
     * @param password
     * @param name
     * @return
     */
    public static User register(String account,String password,String name){

        account = account.trim();
        password = encodePassword(password);

        User user = createUser(account,password,name);
        if(user!=null){
            user = login(user);
        }
        return user;

    }

    private static User createUser(String account,String password,String name){
        User user = new User();
        user.setName(name);
        user.setPassword(password);
        user.setPhone(account);

        return Hib.query(session ->{
            session.save(user);
            return user;
        });
    }
    private static User login(User user){
        String newToken = UUID.randomUUID().toString();
        newToken = TextUtil.encodeBase64(newToken);
        user.setToken(newToken);
        return update(user);
    }
    private static String encodePassword(String password){
        password = password.trim();
        password = TextUtil.getMD5(password);

        return TextUtil.encodeBase64(password);
    }

    public static User update(User user){
       return Hib.query(session -> {
            session.saveOrUpdate(user);
            return user;
        });
    }

    /**
     * 获取联系人的列表
     * @param self User
     * @return List<User>
     */
    public static List<User> contacts(User self){
        return Hib.query(session -> {
            //重新加载一次用户信息到self,和当前的session绑定
           session.load(self,self.getId());
           //获取我关注的人
           Set<UserFollow> flows = self.getFollowing();

           //java8
           return flows.stream()
                   .map(UserFollow::getTarget)
                   .collect(Collectors.toList());
        });
    }

    /**
     * 关注人的操作
     * @param origin 发起者
     * @param target 被关注的人
     * @param alias 备注名
     * @return 被关注的人的信息
     */
    public static User follow(final User origin,final User target,final String alias){
        UserFollow follow = getUserFollow(origin,target);
        if (follow!=null){
            return follow.getTarget();
        }
       return Hib.query(session -> {
            //想要操作懒加载的数据，需要重新load一次
           session.load(origin,origin.getId());
           session.load(target,target.getId());
           //我关注人的时候，同时他也关注我
            //需要添加两条数据
           UserFollow originFollow = new UserFollow();
            originFollow.setOrigin(origin);
            originFollow.setTarget(target);
            originFollow.setAlias(alias);

            UserFollow targetFollow = new UserFollow();
            targetFollow.setOrigin(target);
            targetFollow.setTarget(origin);

            //保存数据库操作
            session.save(originFollow);
            session.save(targetFollow);
            return target;
        });
    }

    /**
     * 查询两个人是否已经关注
     * @param origin 发起者
     * @param target 被关注人
     * @return 返回中间类UserFollow
     */
    public static UserFollow getUserFollow(final User origin,final User target){
        return Hib.query(session -> (UserFollow) session.createQuery("from UserFollow where originId = :originId and targetId = :targetId")
                .setParameter("originId",origin.getId())
                .setParameter("targetId",target.getId())
                .setMaxResults(1)
                .uniqueResult());
    }

    /**
     *
     * 搜索联系人的实现
     * @param name 查询的name，允许为空 ，为了简化分页，只返回20条数据
     * @return
     */
    public static List<User> search(String name) {
        if (Strings.isNullOrEmpty(name))
            name="";//保证不能为null 的情况
        final String searchName = "%"+ name +"%";
        return Hib.query(session -> {
           //name 忽略大小写，并且使用like查询，并且头像和描述不能为空
           return (List<User>) session.createQuery("from User where lower(name) like :name and portrait is not null and description is not null ")
                   .setParameter("name",searchName)
                   .setMaxResults(20)
                   .list();
        });
    }
}
