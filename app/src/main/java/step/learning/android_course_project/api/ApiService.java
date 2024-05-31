package step.learning.android_course_project.api;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;
import step.learning.android_course_project.models.LoginRequest;
import step.learning.android_course_project.models.UserEntity;

public interface ApiService {
    @GET("api/auth/Authenticate")
    Call<LoginRequest> authenticate(@Query("login") String login, @Query("password") String password);

}
/* @POST("RegisterUser")
    Call<UserEntity> registerUser(@Body UserEntity user);
    azure-mobile-services = "3.5.1"
    azure-mobile = { group = "com.microsoft.azure", name = "azure-mobile-android", version.ref = "azure-mobile-services" }*/