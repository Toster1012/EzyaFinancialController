package com.example.ezya;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.ezya.databinding.ActivityWelcomeBinding;
import java.util.concurrent.Executors;

public class WelcomeActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "ezya_prefs";
    private static final String KEY_START_TIME = "start_time";
    private static final String KEY_PERIOD = "selected_period";

    private ActivityWelcomeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWelcomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        checkExistingData();
        binding.startButton.setOnClickListener(v -> onStartButtonClicked());
    }

    private void checkExistingData() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        long startTime = prefs.getLong(KEY_START_TIME, 0);
        String savedPeriod = prefs.getString(KEY_PERIOD, null);

        if (startTime == 0 || savedPeriod == null) return;

        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            double totalIncome = db.categoryDao().getTotalIncomeByPeriod(savedPeriod);

            if (totalIncome > 0) {
                runOnUiThread(() -> {
                    Intent intent = new Intent(this, DashboardActivity.class);
                    intent.putExtra("period", savedPeriod);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                });
            }
        });
    }

    private void onStartButtonClicked() {
        Intent intent = new Intent(this, BudgetActivity.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startActivity(intent);
            overrideActivityTransition(OVERRIDE_TRANSITION_OPEN,
                    android.R.anim.fade_in, android.R.anim.fade_out);
        } else {
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}