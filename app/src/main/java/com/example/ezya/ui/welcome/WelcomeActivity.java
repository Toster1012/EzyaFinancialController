package com.example.ezya.ui.welcome;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import com.example.ezya.App;
import com.example.ezya.R;
import com.example.ezya.base.BaseActivity;
import com.example.ezya.ui.budget.BudgetActivity;
import com.example.ezya.ui.dashboard.DashboardActivity;
import java.util.concurrent.Executors;

public class WelcomeActivity extends BaseActivity {

    private static final String PREFS_NAME = "ezya_prefs";
    private static final String KEY_START_TIME = "start_time";
    private static final String KEY_PERIOD = "selected_period";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        long startTime = prefs.getLong(KEY_START_TIME, 0);
        String period = prefs.getString(KEY_PERIOD, null);

        if (startTime > 0 && period != null) {
            Executors.newSingleThreadExecutor().execute(() -> {
                double totalIncome = App.from(this)
                        .container.categoryRepository
                        .getTotalIncomeByPeriod(period);
                runOnUiThread(() -> {
                    if (totalIncome > 0) {
                        Intent intent = new Intent(this, DashboardActivity.class);
                        intent.putExtra("period", period);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                                | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    } else {
                        Intent intent = new Intent(this, BudgetActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                                | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                    finish();
                });
            });
            return;
        }

        setContentView(R.layout.activity_welcome);

        findViewById(R.id.startButton).setOnClickListener(v ->
                startActivity(new Intent(this, BudgetActivity.class)));

        findViewById(R.id.helpButton).setOnClickListener(v -> showHelpDialog());
    }

    private void showHelpDialog() {
        View dialogView = LayoutInflater.from(this)
                .inflate(R.layout.dialog_help, null);

        new AlertDialog.Builder(this, R.style.DarkAlertDialog)
                .setView(dialogView)
                .setPositiveButton("Понятно", null)
                .show();
    }
}