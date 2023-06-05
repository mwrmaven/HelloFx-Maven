package org.example.entity;

/**
 * @author mavenr
 * @Classname NickNameAndComment
 * @Description TODO
 * @Date 2023/6/5 14:33
 */
public class NickNameAndComment {
    private String nickName;
    private String comment;

    public NickNameAndComment(String nickName, String comment) {
        this.nickName = nickName;
        this.comment = comment;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }
    public String getNickName() {
        return this.nickName;
    }
    public void setComment(String comment) {
        this.comment = comment;
    }
    public String getComment() {
        return this.comment;
    }
    @Override
    public String toString() {
        return "{nick=" + this.nickName + ";comment=" + this.comment + "}";
    }
}
