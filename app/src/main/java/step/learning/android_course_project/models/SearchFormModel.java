package step.learning.android_course_project.models;

import com.google.gson.annotations.SerializedName;

public class SearchFormModel {
    @SerializedName("search-fragment")
    private String searchFragment;

    public SearchFormModel(String searchFragment) {
        this.searchFragment = searchFragment;
    }
}
