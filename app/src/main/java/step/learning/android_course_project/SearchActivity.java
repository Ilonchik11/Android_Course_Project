package step.learning.android_course_project;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.DocumentsContract;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import step.learning.android_course_project.models.Recipe;


public class SearchActivity extends AppCompatActivity {
    private static final String URL = "https://web-course-project20240219151321.azurewebsites.net/";
    private static final String urlString = "https://web-course-project20240219151321.azurewebsites.net/Home/Search";
    private EditText searchInput;
    private Button searchButton;
    private TextView noResultsText;
    private LinearLayout resultsContainer;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        searchInput = findViewById(R.id.search_input);
        searchButton = findViewById(R.id.search_button);
        noResultsText = findViewById(R.id.no_results_text);
        resultsContainer = findViewById(R.id.results_container);

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String query = searchInput.getText().toString();
                if (!query.isEmpty()) {
                    searchRecipes(query);
                } else {
                    Toast.makeText(SearchActivity.this, "Enter search query", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void searchRecipes(String query) {
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

                        urlConnection.setDoOutput(true);
                        String params = "search-fragment=" + URLEncoder.encode(query, "UTF-8");

                        OutputStream outputStream = urlConnection.getOutputStream();
                        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                        writer.write(params);
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
                                    // Toast.makeText(SearchActivity.this, "Connection OK!", Toast.LENGTH_SHORT).show();
                                    handleResponse(String.valueOf(response));
                                }
                            });
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(SearchActivity.this, "Fail Connection!", Toast.LENGTH_SHORT).show();
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

        Pattern recipePattern = Pattern.compile("<div class=\"card border-info\".*?>.*?(<div class=\"card-footer\"><i class=\"bi bi-calendar-check-fill\"></i> .*?</div>)\\s*</div>", Pattern.DOTALL);
        Matcher recipeMatcher = recipePattern.matcher(htmlResponse);

        while (recipeMatcher.find()) {
            String recipeBlock = recipeMatcher.group().trim();

            Pattern namePattern = Pattern.compile("<div class=\"card-header\"><b>(.*?)</b></div>", Pattern.DOTALL);
            Matcher nameMatcher = namePattern.matcher(recipeBlock);
            String name = "";
            if (nameMatcher.find()) {
                name = nameMatcher.group(1).trim();
            }

            Pattern infoPattern = Pattern.compile("<p class=\"card-text\">(.*?)</p>", Pattern.DOTALL);
            Matcher infoMatcher = infoPattern.matcher(recipeBlock);
            String info = "";
            if (infoMatcher.find()) {
                info = infoMatcher.group(1).replaceAll("&#xD;&#xA;", "\n").trim();
            }

            Pattern imagePattern = Pattern.compile("<img src=\"/images/(.*?)\" class=\"img-fluid\" alt=\"Recipe Image\">", Pattern.DOTALL);
            Matcher imageMatcher = imagePattern.matcher(recipeBlock);
            String imageUrl = "";
            if (imageMatcher.find()) {
                imageUrl = imageMatcher.group(1).trim();
            }

            Pattern userPattern = Pattern.compile("<div class=\"card-footer\"><i class=\"bi bi-people-fill\"></i> (.*?)</div>", Pattern.DOTALL);
            Matcher userMatcher = userPattern.matcher(recipeBlock);
            String user = "Unknown"; // Default value
            if (userMatcher.find()) {
                String encodedUser = userMatcher.group(1).trim();
                user = Html.fromHtml(encodedUser, Html.FROM_HTML_MODE_LEGACY).toString();
            }

            Pattern datePattern = Pattern.compile("<div class=\"card-footer\"><i class=\"bi bi-calendar-check-fill\"></i> (.*?)</div>", Pattern.DOTALL);
            Matcher dateMatcher = datePattern.matcher(recipeBlock);
            String date = "Unknown"; // Default value
            if (dateMatcher.find()) {
                date = dateMatcher.group(1).trim();
            }

            recipes.add(new Recipe(name, info, imageUrl, user, date));
        }

        if (!recipes.isEmpty()) {
            displayRecipes(recipes);
        } else {
            resultsContainer.removeAllViews();
            findViewById(R.id.no_results_text).setVisibility(View.VISIBLE);  // Показать TextView если рецепты не найдены
        }
    }

    private void displayRecipes(List<Recipe> recipes) {
        resultsContainer.removeAllViews();

        if (recipes.isEmpty()) {
            resultsContainer.removeAllViews();
            resultsContainer.setVisibility(View.GONE);
        } else {
            resultsContainer.setVisibility(View.VISIBLE);
            noResultsText.setVisibility(View.GONE);

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
