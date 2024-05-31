package step.learning.android_course_project.models;

public class Recipe {
    private String newRecipeName;
    private String newRecipeInfo;
    private String newRecipeAvatarFileName;
    private String userName;
    private String newRecipeDate;
    public Recipe(String name, String info, String imageUrl, String user, String date) {
        this.newRecipeName = name;
        this.newRecipeInfo = info;
        this.newRecipeAvatarFileName = imageUrl;
        this.userName = user;
        this.newRecipeDate = date;
    }

    public String getNewRecipeName() {
        return newRecipeName;
    }

    public void setNewRecipeName(String newRecipeName) {
        this.newRecipeName = newRecipeName;
    }

    public String getNewRecipeInfo() {
        return newRecipeInfo;
    }

    public void setNewRecipeInfo(String newRecipeInfo) {
        this.newRecipeInfo = newRecipeInfo;
    }

    public String getNewRecipeAvatarFileName() {
        return newRecipeAvatarFileName;
    }

    public void setNewRecipeAvatarFileName(String newRecipeAvatarFileName) {
        this.newRecipeAvatarFileName = newRecipeAvatarFileName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getNewRecipeDate() {
        return newRecipeDate;
    }

    public void setNewRecipeDate(String newRecipeDate) {
        this.newRecipeDate = newRecipeDate;
    }
}
