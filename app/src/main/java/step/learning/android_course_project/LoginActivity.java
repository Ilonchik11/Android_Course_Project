package step.learning.android_course_project;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import step.learning.android_course_project.api.ApiService;
import step.learning.android_course_project.api.RetrofitClient;
import step.learning.android_course_project.models.LoginRequest;
import step.learning.android_course_project.models.UserEntity;

public class LoginActivity extends AppCompatActivity {

    private EditText loginEditText;
    private EditText passwordEditText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        loginEditText = findViewById(R.id.login);
        passwordEditText = findViewById(R.id.password);
        Button loginButton = findViewById(R.id.login_button);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            // Получение значений логина и пароля из полей ввода
                            String login = loginEditText.getText().toString();
                            String password = passwordEditText.getText().toString();

                            // URL вашего сервера
                            String urlString = "https://web-course-project20240219151321.azurewebsites.net/api/auth";
                            URL url = new URL(urlString);

                            // Формирование URL с параметрами логина и пароля
                            String urlStringWithParams = urlString + "?login=" + URLEncoder.encode(login, "UTF-8") +
                                    "&password=" + URLEncoder.encode(password, "UTF-8");
                            URL urlWithParams = new URL(urlStringWithParams);

                            // Создание соединения
                            HttpURLConnection urlConnection = (HttpURLConnection) urlWithParams.openConnection();
                            try {
                                // Установка метода запроса (GET в данном случае)
                                urlConnection.setRequestMethod("GET");

                                // Установка таймаута соединения
                                urlConnection.setConnectTimeout(5000); // 5 секунд
                                urlConnection.setReadTimeout(5000); // 5 секунд

                                // Подключение к серверу и отправка запроса
                                urlConnection.connect();

                                // Проверка успешности ответа (код 200 обозначает успешный запрос)
                                int responseCode = urlConnection.getResponseCode();
                                if (responseCode == HttpURLConnection.HTTP_OK) {
                                    // В случае успешного входа перенаправляем на главную страницу
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            // Получаем экземпляр SharedPreferences
                                            SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);

                                            // Редактируем SharedPreferences, указывая, что пользователь авторизован
                                            SharedPreferences.Editor editor = sharedPreferences.edit();
                                            editor.putBoolean("is_authenticated", true);
                                            editor.apply();

                                            Toast.makeText(LoginActivity.this, "Вход выполнен успешно", Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                            startActivity(intent);
                                            finish(); // Закрываем LoginActivity
                                        }
                                    });
                                } else {
                                    // Вывод сообщения об ошибке
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(LoginActivity.this, "Вход отклонен", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            } finally {
                                // Закрытие соединения после завершения запроса
                                urlConnection.disconnect();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });

        findViewById(R.id.login_btn_registration).setOnClickListener(this::onRegistrationButtonClick);
    }

    private void onRegistrationButtonClick(View view) {
        Intent intent = new Intent(this, RegistrationActivity.class);
        startActivity(intent);
    }
}