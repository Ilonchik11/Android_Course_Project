package step.learning.android_course_project;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RegistrationActivity extends AppCompatActivity {

    private static final String urlString = "https://web-course-project20240219151321.azurewebsites.net/Home/UserRegistration";
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageView imageView;
    private EditText etLogin;
    private EditText etEmail;
    private EditText etPassword;
    private EditText etPasswordRepeat;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registration);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        etLogin = findViewById(R.id.login);
        etEmail = findViewById(R.id.email);
        etPassword = findViewById(R.id.password);
        etPasswordRepeat = findViewById(R.id.password_repeat);

        imageView = findViewById(R.id.imageView);
        Button buttonSelectImage = findViewById(R.id.buttonSelectImage);
        buttonSelectImage.setOnClickListener(v -> getContent.launch("image/*"));

        Button registrationButton = findViewById(R.id.registration_button);
        registrationButton.setOnClickListener(this::onRegistrationButtonClick);
    }

    private void onRegistrationButtonClick(View view) {
        String login = etLogin.getText().toString();
        String email = etEmail.getText().toString();
        String password = etPassword.getText().toString();
        String passwordRepeat = etPasswordRepeat.getText().toString();
        if(!password.equals(passwordRepeat)) {
            Toast.makeText(RegistrationActivity.this, "Passwords are not equals!", Toast.LENGTH_SHORT).show();
        }

        BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
        Bitmap bitmap = drawable.getBitmap();
        File avatarFile = saveImageToFile(bitmap);

        sendRegistrationData(login, email, password, passwordRepeat, avatarFile);
    }
    private void sendRegistrationData(String login, String email, String password, String repeatPassword, File avatarFile) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(urlString);
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    try {
                        urlConnection.setRequestMethod("POST");
                        urlConnection.setConnectTimeout(5000);
                        urlConnection.setReadTimeout(5000);

                        String boundary = "--------------------------" + System.currentTimeMillis();
                        urlConnection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

                        OutputStream outputStream = urlConnection.getOutputStream();
                        PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, "UTF-8"), true);

                        String separator = "--" + boundary + "\r\n";
                        String ending = "\r\n--" + boundary + "--\r\n";

                        writer.append(separator);
                        writer.append("Content-Disposition: form-data; name=\"signup-login\"\r\n\r\n");
                        writer.append(login + "\r\n");

                        writer.append(separator);
                        writer.append("Content-Disposition: form-data; name=\"signup-email\"\r\n\r\n");
                        writer.append(email + "\r\n");

                        writer.append(separator);
                        writer.append("Content-Disposition: form-data; name=\"signup-password\"\r\n\r\n");
                        writer.append(password + "\r\n");

                        writer.append(separator);
                        writer.append("Content-Disposition: form-data; name=\"signup-repeat\"\r\n\r\n");
                        writer.append(repeatPassword + "\r\n");

                        writer.append(separator);
                        writer.append("Content-Disposition: form-data; name=\"signup-avatar\"; filename=\"" + avatarFile.getName() + "\"\r\n");
                        writer.append("Content-Type: image/jpeg\r\n\r\n");
                        writer.flush();

                        FileInputStream fileInputStream = new FileInputStream(avatarFile);
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                        }
                        outputStream.flush();
                        fileInputStream.close();

                        writer.append(ending);
                        writer.flush();
                        writer.close();

                        int responseCode = urlConnection.getResponseCode();
                        if (responseCode == HttpURLConnection.HTTP_OK) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    // Toast.makeText(RegistrationActivity.this, "Registration successful!", Toast.LENGTH_SHORT).show();
                                    finish();
                                    Intent intent = new Intent(RegistrationActivity.this, MainActivity.class);
                                    startActivity(intent);
                                }
                            });
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(RegistrationActivity.this, "Registration failed!", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    } finally {
                        urlConnection.disconnect();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    private File saveImageToFile(Bitmap bitmap) {
        try {
            File tempFile = File.createTempFile("tempImage", ".jpg", getCacheDir());
            FileOutputStream fos = new FileOutputStream(tempFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();

            return tempFile;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    private final ActivityResultLauncher<String> getContent = registerForActivityResult(new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    imageView.setImageURI(uri);
                }
            });

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            imageView.setImageURI(imageUri);
        }
    }
}