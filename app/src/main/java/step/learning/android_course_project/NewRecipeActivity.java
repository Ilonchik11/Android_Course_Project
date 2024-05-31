package step.learning.android_course_project;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Html;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.gson.Gson;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
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
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okio.BufferedSink;
import step.learning.android_course_project.models.Recipe;

public class NewRecipeActivity extends AppCompatActivity {
    private static final String URL = "https://web-course-project20240219151321.azurewebsites.net/";
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private LinearLayout resultsContainer;

    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageView imageView;
    private EditText etUser;
    private EditText etRecipeName;
    private EditText etRecipeDetails;
    private Button sendRecipeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_new_recipe);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        etUser = findViewById(R.id.user);
        etRecipeName = findViewById(R.id.recipe_name);
        etRecipeDetails = findViewById(R.id.recipe_details);
        sendRecipeButton = findViewById(R.id.send_button);
        sendRecipeButton.setOnClickListener(this::onSendRecipeButtonClick);

        imageView = findViewById(R.id.imageView);
        Button buttonSelectImage = findViewById(R.id.buttonSelectImage);
        buttonSelectImage.setOnClickListener(v -> getContent.launch("image/*"));

        resultsContainer = findViewById(R.id.results_container);
        searchRecipes();
    }

    private void onSendRecipeButtonClick(View view) {
        // Получаем значения из полей ввода
        String user = etUser.getText().toString();
        String recipeName = etRecipeName.getText().toString();
        String recipeDetails = etRecipeDetails.getText().toString();

        // Получаем Bitmap из ImageView
        BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
        Bitmap bitmap = drawable.getBitmap();

        // Сохраняем изображение в файл
        File imageFile = saveImageToFile(bitmap);

        // Отправляем данные на сервер
        sendRecipeData(user, recipeName, recipeDetails, imageFile);
    }

    private void sendRecipeData(String user, String recipeName, String recipeDetails, File imageFile) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    String urlString = "https://web-course-project20240219151321.azurewebsites.net/Home/NewRecipe";
                    URL url = new URL(urlString);
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    try {
                        urlConnection.setRequestMethod("POST");

                        urlConnection.setConnectTimeout(5000);
                        urlConnection.setReadTimeout(5000);

                        // Устанавливаем режим отправки данных формы
                        urlConnection.setRequestProperty("Content-Type", "multipart/form-data; boundary=--------------------------");

                        // Создаем тело запроса
                        String boundary = "--------------------------";
                        String separator = "--" + boundary + "\r\n";
                        String ending = "\r\n--" + boundary + "--\r\n";
                        OutputStream outputStream = urlConnection.getOutputStream();
                        PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, "UTF-8"), true);

                        // Добавляем поля формы
                        writer.append(separator);
                        writer.append("Content-Disposition: form-data; name=\"newrecipe-user\"\r\n\r\n");
                        writer.append(user + "\r\n");

                        writer.append(separator);
                        writer.append("Content-Disposition: form-data; name=\"newrecipe-name\"\r\n\r\n");
                        writer.append(recipeName + "\r\n");

                        writer.append(separator);
                        writer.append("Content-Disposition: form-data; name=\"newrecipe-info\"\r\n\r\n");
                        writer.append(recipeDetails + "\r\n");

                        // Добавляем файл
                        writer.append(separator);
                        writer.append("Content-Disposition: form-data; name=\"newrecipe-photo\"; filename=\"" + imageFile.getName() + "\"\r\n");
                        writer.append("Content-Type: image/jpeg\r\n\r\n");
                        writer.flush();

                        FileInputStream fileInputStream = new FileInputStream(imageFile);
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                        }
                        outputStream.flush();
                        fileInputStream.close();

                        // Завершаем запрос
                        writer.append(ending);
                        writer.flush();
                        writer.close();

                        // Получаем ответ от сервера
                        int responseCode = urlConnection.getResponseCode();
                        if (responseCode == HttpURLConnection.HTTP_OK) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(NewRecipeActivity.this, "Connection OK!", Toast.LENGTH_SHORT).show();
                                    finish();
                                    Intent intent = new Intent(NewRecipeActivity.this, NewRecipeActivity.class);
                                    startActivity(intent);
                                }
                            });
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(NewRecipeActivity.this, "Fail Connection!", Toast.LENGTH_SHORT).show();
                                    // Обработка ошибки
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
            // Создаем временный файл
            File tempFile = File.createTempFile("tempImage", ".jpg", getCacheDir());

            // Записываем изображение в файл
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

    private void searchRecipes() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    String urlString = URL + "/Home/NewRecipe";
                    URL url = new URL(urlString);
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    try {
                        urlConnection.setRequestMethod("POST");

                        urlConnection.setConnectTimeout(5000);
                        urlConnection.setReadTimeout(5000);

                        urlConnection.setDoOutput(true);

                        OutputStream outputStream = urlConnection.getOutputStream();
                        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                        writer.flush();
                        writer.close();
                        outputStream.close();

                        urlConnection.connect();

                        BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                        String inputLine;
                        StringBuilder response = new StringBuilder();
                        while ((inputLine = in.readLine()) != null) {
                            response.append(inputLine);
                        }
                        in.close();

                        int responseCode = urlConnection.getResponseCode();
                        if (responseCode == HttpURLConnection.HTTP_OK) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(NewRecipeActivity.this, "Connection OK!", Toast.LENGTH_SHORT).show();
                                    handleResponse(String.valueOf(response));
                                }
                            });
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(NewRecipeActivity.this, "Fail Connection!", Toast.LENGTH_SHORT).show();
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
    private void handleResponse(String htmlResponse) {
        List<Recipe> recipes = new ArrayList<>();
        // Log.d("HTML Response", htmlResponse);

        Pattern recipePattern = Pattern.compile("<div class=\"card border-info\".*?>.*?(<div class=\"card-footer\"><i class=\"bi bi-calendar-check-fill\"></i> .*?</div>)\\s*</div>", Pattern.DOTALL);
        Matcher recipeMatcher = recipePattern.matcher(htmlResponse);

        while (recipeMatcher.find()) {
            String recipeBlock = recipeMatcher.group().trim();
            // Log.d("Recipe HTML Block", recipeBlock);

            Pattern namePattern = Pattern.compile("<div class=\"card-header\"><b>(.*?)</b></div>", Pattern.DOTALL);
            Matcher nameMatcher = namePattern.matcher(recipeBlock);
            String name = "";
            if (nameMatcher.find()) {
                name = nameMatcher.group(1).trim();
            }
            // Log.d("Recipe Name", name);

            Pattern infoPattern = Pattern.compile("<p class=\"card-text\">(.*?)</p>", Pattern.DOTALL);
            Matcher infoMatcher = infoPattern.matcher(recipeBlock);
            String info = "";
            if (infoMatcher.find()) {
                info = infoMatcher.group(1).replaceAll("&#xD;&#xA;", "\n").trim();
            }
            // Log.d("Recipe Info", info);

            Pattern imagePattern = Pattern.compile("<img src=\"/images/(.*?)\" class=\"img-fluid\" alt=\"Recipe Image\">", Pattern.DOTALL);
            Matcher imageMatcher = imagePattern.matcher(recipeBlock);
            String imageUrl = "";
            if (imageMatcher.find()) {
                imageUrl = imageMatcher.group(1).trim();
            }
            // Log.d("Recipe Image URL", imageUrl);

            Pattern userPattern = Pattern.compile("<div class=\"card-footer\"><i class=\"bi bi-people-fill\"></i> (.*?)</div>", Pattern.DOTALL);
            Matcher userMatcher = userPattern.matcher(recipeBlock);
            String user = "Unknown"; // Default value
            if (userMatcher.find()) {
                String encodedUser = userMatcher.group(1).trim();
                user = Html.fromHtml(encodedUser, Html.FROM_HTML_MODE_LEGACY).toString();
            }
            // Log.d("Recipe User", user);

            Pattern datePattern = Pattern.compile("<div class=\"card-footer\"><i class=\"bi bi-calendar-check-fill\"></i> (.*?)</div>", Pattern.DOTALL);
            Matcher dateMatcher = datePattern.matcher(recipeBlock);
            String date = "Unknown"; // Default value
            if (dateMatcher.find()) {
                date = dateMatcher.group(1).trim();
            }
            // Log.d("Recipe Date", date);

            recipes.add(new Recipe(name, info, imageUrl, user, date));
        }

        if (!recipes.isEmpty()) {
            // displayToast("Рецепты найдены");
            // for (Recipe recipe : recipes) {
            //     Log.d("Recipe HTML", "Name: " + recipe.getNewRecipeName() + ", Info: " + recipe.getNewRecipeInfo());
            // }
            displayRecipes(recipes);
        } else {
            // displayToast("Рецепты не найдены");
            resultsContainer.removeAllViews();
            findViewById(R.id.no_results_text).setVisibility(View.VISIBLE);  // Показать TextView если рецепты не найдены
        }
    }

    private void displayRecipes(List<Recipe> recipes) {
        // Очистка контейнера перед отображением новых результатов
        resultsContainer.removeAllViews();

        if (recipes.isEmpty()) {
            resultsContainer.removeAllViews();
        } else {
            resultsContainer.setVisibility(View.VISIBLE);
            for (Recipe recipe : recipes) {
                View recipeView = getLayoutInflater().inflate(R.layout.recipe_item, null);

                TextView recipeName = recipeView.findViewById(R.id.recipe_name);
                TextView recipeInfo = recipeView.findViewById(R.id.recipe_info);
                ImageView recipeImage = recipeView.findViewById(R.id.recipe_image);
                TextView recipeUser = recipeView.findViewById(R.id.recipe_user);
                TextView recipeDate = recipeView.findViewById(R.id.recipe_date);

                recipeName.setText(recipe.getNewRecipeName());
                recipeInfo.setText(recipe.getNewRecipeInfo());
                String userName = recipe.getUserName();
                if (userName != null && !userName.isEmpty()) {
                    recipeUser.setText("User: " + userName);
                } else {
                    recipeUser.setText("User: Unknown");
                }
                String dateString = recipe.getNewRecipeDate();
                if (dateString != null && !dateString.isEmpty()) {
                    recipeDate.setText("Date: " + dateString);
                } else {
                    recipeDate.setText("Date: Unknown");
                }

                String imageUrl = URL + "images/" + recipe.getNewRecipeAvatarFileName();
                urlToImageView(imageUrl, recipeImage);
                resultsContainer.addView(recipeView);

                View separator = new View(this);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        20
                );
                separator.setLayoutParams(params);
                resultsContainer.addView(separator);
            }
        }
    }
    private void urlToImageView(String url, ImageView imageView) {
        CompletableFuture.supplyAsync( () -> {
                    try (java.io.InputStream is = new URL(url).openConnection().getInputStream()) {
                        return BitmapFactory.decodeStream(is);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }, executor )
                .thenAccept( imageView::setImageBitmap );
    }
}