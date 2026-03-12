package com.example.ezya;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.ezya.databinding.ActivityWelcomeBinding;

public class WelcomeActivity extends AppCompatActivity {

    private ActivityWelcomeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWelcomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.startButton.setOnClickListener(v -> onStartButtonClicked());
    }

    private void onStartButtonClicked() {
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}