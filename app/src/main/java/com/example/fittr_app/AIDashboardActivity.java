package com.example.fittr_app;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.content.Intent;

import android.widget.ImageButton;
import android.widget.TextView;

import com.example.fittr_app.R;

public class AIDashboardActivity extends AppCompatActivity {

    private int userId;
    private int formScore;
    private int stabilityScore;
    private int rangeOfMotionScore;

    private float caloriesBurnt;

    private String adviceText;

    private String futureAdvice;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fittrai_dashboard);  // Set the XML layout file

        Intent intent = getIntent();

        adviceText = intent.getStringExtra("summary_analysis");
        formScore = intent.getIntExtra("form_score", 0);
        stabilityScore = intent.getIntExtra("stability_score", 0);
        rangeOfMotionScore = intent.getIntExtra("range_of_motion_score", 0);
        futureAdvice = intent.getStringExtra("future_advice");
        caloriesBurnt = intent.getFloatExtra("calories_burnt", 0.0f);

        TextView adviceTextView = findViewById(R.id.adviceTextView);
        TextView formScoreTextView = findViewById(R.id.formScoreTextView);
        TextView stabilityScoreTextView = findViewById(R.id.stabilityScoreTextView);
        TextView rangeOfMotionScoreTextView = findViewById(R.id.rangeOfMotionScoreTextView);
        TextView futureAdviceTextView = findViewById(R.id.futureAdviceTextView);
        TextView caloriesBurntTextView = findViewById(R.id.caloriesBurntTextView);

        caloriesBurntTextView.setText(String.format("%.2f", caloriesBurnt));  // Format the calories burnt to 2 decimal places
        adviceTextView.setText(adviceText);
        formScoreTextView.setText(String.valueOf(formScore));
        stabilityScoreTextView.setText(String.valueOf(stabilityScore));
        rangeOfMotionScoreTextView.setText(String.valueOf(rangeOfMotionScore));
        futureAdviceTextView.setText(futureAdvice);

        ImageButton btnBack = findViewById(R.id.btnBack);  // Reference the back button
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Finish the activity and return to the previous one
                finish();
            }
        });
    }
}