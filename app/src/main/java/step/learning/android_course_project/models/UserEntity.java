package step.learning.android_course_project.models;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class UserEntity {
    @SerializedName("id")
    private String id;

    @SerializedName("partitionKey")
    private String partitionKey;

    private String login;
    private String email;
    private String password;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    private String passwordSalt;
    private String passwordDk;
    private String avatar;
    private String newUserAvatarFileName;
    private Date registerDt;
    private Date deleteDt;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPartitionKey() {
        return partitionKey;
    }

    public void setPartitionKey(String partitionKey) {
        this.partitionKey = partitionKey;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordSalt() {
        return passwordSalt;
    }

    public void setPasswordSalt(String passwordSalt) {
        this.passwordSalt = passwordSalt;
    }

    public String getPasswordDk() {
        return passwordDk;
    }

    public void setPasswordDk(String passwordDk) {
        this.passwordDk = passwordDk;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getNewUserAvatarFileName() {
        return newUserAvatarFileName;
    }

    public void setNewUserAvatarFileName(String newUserAvatarFileName) {
        this.newUserAvatarFileName = newUserAvatarFileName;
    }

    public Date getRegisterDt() {
        return registerDt;
    }

    public void setRegisterDt(Date registerDt) {
        this.registerDt = registerDt;
    }

    public Date getDeleteDt() {
        return deleteDt;
    }

    public void setDeleteDt(Date deleteDt) {
        this.deleteDt = deleteDt;
    }
}
