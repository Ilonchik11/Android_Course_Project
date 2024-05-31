package step.learning.android_course_project;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        boolean isAuthenticated = sharedPreferences.getBoolean("is_authenticated", false);

        Button loginButton = findViewById(R.id.main_btn_login);
        Button logoutButton = findViewById(R.id.main_btn_logout);
        Button searchButton = findViewById(R.id.main_btn_search);
        Button newRecipeButton = findViewById(R.id.main_btn_new_recipe);
        Button feedbackButton = findViewById(R.id.main_btn_feedback);
        // Если пользователь авторизован, делаем кнопки видимыми, иначе скрываем
        if (isAuthenticated) {
            loginButton.setVisibility(View.GONE);
            logoutButton.setVisibility(View.VISIBLE);
            searchButton.setVisibility(View.VISIBLE);
            newRecipeButton.setVisibility(View.VISIBLE);
            feedbackButton.setVisibility(View.VISIBLE);
        } else {
            loginButton.setVisibility(View.VISIBLE);
            logoutButton.setVisibility(View.GONE);
            searchButton.setVisibility(View.GONE);
            newRecipeButton.setVisibility(View.GONE);
            feedbackButton.setVisibility(View.GONE);
        }

        findViewById(R.id.main_btn_home).setOnClickListener(this::onHomeButtonClick);
        findViewById(R.id.main_btn_privacy).setOnClickListener(this::onPrivacyButtonClick);
        findViewById(R.id.main_btn_search).setOnClickListener(this::onSearchButtonClick);
        findViewById(R.id.main_btn_new_recipe).setOnClickListener(this::onNewRecipeButtonClick);
        findViewById(R.id.main_btn_feedback).setOnClickListener(this::onFeedbackButtonClick);

        loginButton.setOnClickListener(this::onLoginButtonClick);
        logoutButton.setOnClickListener(this::onLogoutButtonClick);
    }
    private void onHomeButtonClick(View view) {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
    }
    private void onPrivacyButtonClick(View view) {
        Intent intent = new Intent(this, PrivacyActivity.class);
        startActivity(intent);
    }
    private void onSearchButtonClick(View view) {
        Intent intent = new Intent(this, SearchActivity.class);
        startActivity(intent);
    }
    private void onNewRecipeButtonClick(View view) {
        Intent intent = new Intent(this, NewRecipeActivity.class);
        startActivity(intent);
    }
    private void onFeedbackButtonClick(View view) {
        Intent intent = new Intent(this, FeedbackActivity.class);
        startActivity(intent);
    }
    private void onLoginButtonClick(View view) {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }
    private void onLogoutButtonClick(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure that you want to log out?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                performLogout();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }
    private void performLogout() {
        SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("is_authenticated", false);
        editor.apply();

        finish();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}