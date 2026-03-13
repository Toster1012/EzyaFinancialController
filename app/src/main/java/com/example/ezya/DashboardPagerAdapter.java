package com.example.ezya;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class DashboardPagerAdapter extends FragmentStateAdapter {

    private final String period;

    public DashboardPagerAdapter(@NonNull FragmentActivity activity, String period) {
        super(activity);
        this.period = period;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) return IncomeFragment.newInstance(period);
        return ExpenseFragment.newInstance(period);
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}