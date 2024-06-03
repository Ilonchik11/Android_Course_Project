package step.learning.android_course_project;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import step.learning.android_course_project.models.Feedback;
import step.learning.android_course_project.models.Recipe;

public class FeedbackActivity extends AppCompatActivity {

    private static final String urlString = "https://web-course-project20240219151321.azurewebsites.net/Home/Feedback";
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private LinearLayout resultsContainer;
    private EditText etUserName;
    private EditText etNameContact;
    private EditText etFeedbackDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_feedback);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        etUserName = findViewById(R.id.user_name);
        etNameContact = findViewById(R.id.user_contact);
        etFeedbackDetails = findViewById(R.id.feedback_details);
        Button sendFeedbackButton = findViewById(R.id.send_feedback_button);
        sendFeedbackButton.setOnClickListener(this::onSendFeedbackButtonClick);

        resultsContainer = findViewById(R.id.results_container);

        searchFeedbacks();
    }

    private void onSendFeedbackButtonClick(View view) {
        String userName = etUserName.getText().toString();
        String nameContact = etNameContact.getText().toString();
        String feedbackDetails = etFeedbackDetails.getText().toString();

        sendFeedbackData(userName, nameContact, feedbackDetails);
    }

    private void sendFeedbackData(String user, String contact, String feedbackText) {
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

                        urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                        String urlParameters = "feedback-user=" + URLEncoder.encode(user, "UTF-8") +
                                "&feedback-contact=" + URLEncoder.encode(contact, "UTF-8") +
                                "&feedback-text=" + URLEncoder.encode(feedbackText, "UTF-8");

                        urlConnection.setDoOutput(true);
                        OutputStream outputStream = urlConnection.getOutputStream();
                        outputStream.write(urlParameters.getBytes("UTF-8"));
                        outputStream.flush();
                        outputStream.close();

                        int responseCode = urlConnection.getResponseCode();
                        if (responseCode == HttpURLConnection.HTTP_OK) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    // Toast.makeText(FeedbackActivity.this, "Connection OK!", Toast.LENGTH_SHORT).show();
                                    finish();
                                    Intent intent = new Intent(FeedbackActivity.this, FeedbackActivity.class);
                                    startActivity(intent);
                                }
                            });
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(FeedbackActivity.this, "Fail Connection!", Toast.LENGTH_SHORT).show();
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


    private void searchFeedbacks() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    java.net.URL url = new URL(urlString);
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
                                    // Toast.makeText(FeedbackActivity.this, "Connection OK!", Toast.LENGTH_SHORT).show();
                                    handleResponse(String.valueOf(response));
                                }
                            });
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(FeedbackActivity.this, "Fail Connection!", Toast.LENGTH_SHORT).show();
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
        List<Feedback> feedbacks = new ArrayList<>();

        Pattern feedbackPattern = Pattern.compile(
                "<div class=\"card border-info\".*?>.*?(<div class=\"card-footer\">.*?</div>)\\s*</div>",
                Pattern.DOTALL
        );
        Matcher recipeMatcher = feedbackPattern.matcher(htmlResponse);

        while (recipeMatcher.find()) {
            String feedbackBlock = recipeMatcher.group().trim();

            Pattern namePattern = Pattern.compile("<div class=\"card-header\"><b>(.*?)</b></div>", Pattern.DOTALL);
            Matcher nameMatcher = namePattern.matcher(feedbackBlock);
            String userName = "";
            if (nameMatcher.find()) {
                String encodedUser = nameMatcher.group(1).trim();
                userName = Html.fromHtml(encodedUser, Html.FROM_HTML_MODE_LEGACY).toString();
            }

            Pattern infoPattern = Pattern.compile("<p class=\"card-text\">(.*?)</p>", Pattern.DOTALL);
            Matcher infoMatcher = infoPattern.matcher(feedbackBlock);
            String feedbackInfo = "";
            if (infoMatcher.find()) {
                feedbackInfo = infoMatcher.group(1).replaceAll("&#xD;&#xA;", "\n").trim();
            }

            Pattern datePattern = Pattern.compile("<div class=\"card-footer\">(.*?)</div>", Pattern.DOTALL);
            Matcher dateMatcher = datePattern.matcher(feedbackBlock);
            String feedbackDate = "Unknown";
            if (dateMatcher.find()) {
                feedbackDate = dateMatcher.group(1).trim();
            }

            feedbacks.add(new Feedback(userName, feedbackInfo, feedbackDate));
        }

        if (!feedbacks.isEmpty()) {
            displayFeedbacks(feedbacks);
        } else {
            resultsContainer.removeAllViews();
            findViewById(R.id.no_results_text).setVisibility(View.VISIBLE);
        }
    }

    private void displayFeedbacks(List<Feedback> feedbacks) {
        resultsContainer.removeAllViews();

        if (feedbacks.isEmpty()) {
            resultsContainer.removeAllViews();
            resultsContainer.setVisibility(View.GONE);
        } else {
            resultsContainer.setVisibility(View.VISIBLE);
            for (Feedback feedback : feedbacks) {
                View feedbackView = getLayoutInflater().inflate(R.layout.feedback_item, null);

                TextView userName = feedbackView.findViewById(R.id.feedback_user);
                TextView feedbackText = feedbackView.findViewById(R.id.feedback_text);
                TextView feedbackDate = feedbackView.findViewById(R.id.feedback_date);

                userName.setText(feedback.getUserName());
                feedbackText.setText(feedback.getFeedbackDetails());
                feedbackDate.setText(feedback.getFeedbackDate());
                resultsContainer.addView(feedbackView);

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
}