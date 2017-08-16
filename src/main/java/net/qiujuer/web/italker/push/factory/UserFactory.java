package net.qiujuer.web.italker.push.factory;

import net.qiujuer.web.italker.push.bean.db.User;
import net.qiujuer.web.italker.push.utils.Hib;
import net.qiujuer.web.italker.push.utils.TextUtil;
import org.hibernate.Session;

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

        User user = new User();
        user.setName(name);
        user.setPassword(password);
        user.setPhone(account);

        //保存
        Session session = Hib.session();
        session.beginTransaction();
        try{
            session.save(user);
        }catch (Exception e){
            session.getTransaction().rollback();
            return null;
        }
        session.getTransaction().commit();

        return user;

    }

    private static String encodePassword(String password){
        password = password.trim();
        password = TextUtil.getMD5(password);

        return TextUtil.encodeBase64(password);
    }
}
