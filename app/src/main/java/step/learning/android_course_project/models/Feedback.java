package step.learning.android_course_project.models;

public class Feedback {
    private String UserName;
    private String UserContact;
    private String FeedbackDetails;
    private String FeedbackDate;

    public Feedback(String userName, String userContact, String feedbackDetails, String feedbackDate) {
        UserName = userName;
        UserContact = userContact;
        FeedbackDetails = feedbackDetails;
        FeedbackDate = feedbackDate;
    }

    public Feedback(String userName, String feedbackDetails, String feedbackDate) {
        UserName = userName;
        FeedbackDetails = feedbackDetails;
        FeedbackDate = feedbackDate;
    }

    public String getUserName() {
        return UserName;
    }

    public void setUserName(String userName) {
        UserName = userName;
    }

    public String getUserContact() {
        return UserContact;
    }

    public void setUserContact(String userContact) {
        UserContact = userContact;
    }

    public String getFeedbackDetails() {
        return FeedbackDetails;
    }

    public void setFeedbackDetails(String feedbackDetails) {
        FeedbackDetails = feedbackDetails;
    }

    public String getFeedbackDate() {
        return FeedbackDate;
    }

    public void setFeedbackDate(String feedbackDate) {
        FeedbackDate = feedbackDate;
    }
}
