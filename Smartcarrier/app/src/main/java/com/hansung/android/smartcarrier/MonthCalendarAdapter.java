package com.hansung.android.smartlocker;

import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.Calendar;

public class MonthCalendarAdapter extends FragmentStateAdapter {
    private static int NUM_ITEMS=100;
    public MonthCalendarAdapter(@NonNull MonthViewFragment fragmentActivity) {
        super(fragmentActivity);
    }

    Calendar cal = Calendar.getInstance();
    int year = cal.get(Calendar.YEAR);
    int month = cal.get(Calendar.MONTH);
    int day;
    public Fragment createFragment(int position) {
        return MonthCalendarFragment.newInstance(year + ((month + position + 10) / 12 - 5), (month + position + 10) % 12);
    }

    @Override
    public int getItemCount() {
        return NUM_ITEMS;
    }
}

