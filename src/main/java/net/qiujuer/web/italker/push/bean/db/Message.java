package net.qiujuer.web.italker.push.bean.db;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 消息的model 对应数据库
 */
@Entity
@Table(name="TB_MESSAGE")
public class Message {

    private static final int TYPE_STR = 1;//字符串类型
    private static final int TYPE_PIC = 2;//图片类型
    private static final int TYPE_FILE = 3;//文件类型
    private static final int TYPE_AUDIO = 4;//语音类型

    //这是个主键
    @Id
    @PrimaryKeyJoinColumn
    //这里不自动生成uuid,id由代码写入，有客户端负责生成，
    //避免复杂的映射关系
    // @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid",strategy = "uuid2" )
    @Column(updatable = false,nullable = false)
    private String id;

    @Column(nullable = false,columnDefinition = "TEXT")
    private String content;

    @Column
    private String attach;

    //消息类型
    @Column(nullable = false)
    private int type;

    //发送者
    //多个消息对应着一个发送者
    @JoinColumn(name="senderId")
    @ManyToOne(optional = false)
    private User sender;
    //这个字段仅仅是为了对应sender的数据库字段senderId
    @Column(nullable = false,updatable = false,insertable = false)
    private String senderId;

    //可以为空
    //多个消息对应着一个接收者
    @ManyToOne
    @JoinColumn(name="receiverId")
    private User receiver;
    @Column(updatable = false,insertable = false)
    private String receiverId;


    //一个群可以接受多个消息
    @ManyToOne
    @JoinColumn(name="groupId")
    private Group group;
    @Column(updatable = false,insertable = false)
    private String groupId;


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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getAttach() {
        return attach;
    }

    public void setAttach(String attach) {
        this.attach = attach;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public User getSender() {
        return sender;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }

    public User getReceiver() {
        return receiver;
    }

    public void setReceiver(User receiver) {
        this.receiver = receiver;
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

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }
}
