package net.qiujuer.web.italker.push.factory;

import com.google.common.base.Strings;
import net.qiujuer.web.italker.push.bean.db.User;
import net.qiujuer.web.italker.push.utils.Hib;
import net.qiujuer.web.italker.push.utils.TextUtil;
import org.hibernate.Session;

import java.util.List;
import java.util.UUID;

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
}
