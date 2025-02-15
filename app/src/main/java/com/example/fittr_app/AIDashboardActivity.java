package com.example.fittr_app;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;

public class AIDashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fittrai_dashboard);  // Set the XML layout file

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
