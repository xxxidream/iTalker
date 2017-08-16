package net.qiujuer.web.italker.push.bean.db;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 用户关系的model
 * 用于用户直接进行好友关系的实现
 */
@Entity
@Table(name="TB_USER_FOLLOW")
public class UserFollow {
    //这是个主键
    @Id
    @PrimaryKeyJoinColumn
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid",strategy = "uuid2" )
    @Column(updatable = false,nullable = false)
    private String id;

    //定义一个发起人，你关注某人，这里就是你
    //多对1 ，你可以关注很多人，你的每一次关注都是一条记录
    //你可以创建很多个关注的信息，所以是多对1
    //这里的多对一是:User 对应多个UserFollow
    //optional 不可选必须存储，一天关注记录一定要有一个关注人
    @ManyToOne(optional = false)
    @JoinColumn(name = "originId")
    private User origin;
    @Column(nullable = false,updatable = false,insertable = false)
    private String originId;

    //定义关注的目标，你关注的人
    @JoinColumn(name = "targetId")
    @ManyToOne(optional = false)
    private User target;

    @Column(nullable = false,updatable = false,insertable = false)
    private String targetId;

    //别名，也就是对target的备注
    @Column
    private String alias;

    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime createAt = LocalDateTime.now();

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updateAt = LocalDateTime.now();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public User getOrigin() {
        return origin;
    }

    public void setOrigin(User origin) {
        this.origin = origin;
    }

    public String getOriginId() {
        return originId;
    }

    public void setOriginId(String originId) {
        this.originId = originId;
    }

    public User getTarget() {
        return target;
    }

    public void setTarget(User target) {
        this.target = target;
    }

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public LocalDateTime getCreateAt() {
        return createAt;
    }

    public void setCreateAt(LocalDateTime createAt) {
        this.createAt = createAt;
    }

    public LocalDateTime getUpdateAt() {
        return updateAt;
    }

    public void setUpdateAt(LocalDateTime updateAt) {
        this.updateAt = updateAt;
    }
}
