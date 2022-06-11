package com.hansung.android.smartlocker;

import android.database.Cursor;
import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.Calendar;


public class MonthViewFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    DBHelper dbHelper;
    Cursor cursor;

    String data_d;
    int data_year;
    int data_month;
    int data_day;
    int data_hour;
    int data_minute;
    int data_st;

    public MonthViewFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static MonthViewFragment newInstance(String param1, String param2) {
        MonthViewFragment fragment = new MonthViewFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //MonthCalendarAdapter에서 데이터 가져옴.
        // 뷰페이저를 선언하여 프래그먼트에 연결시킴
        View rootView = inflater.inflate(R.layout.fragment_month_view, container, false);
        ViewPager2 vpPager = rootView.findViewById(R.id.vpPager1);
        FragmentStateAdapter adapter = new MonthCalendarAdapter(this);
        int year = ((MonthCalendarAdapter) adapter).year;
        int month = ((MonthCalendarAdapter) adapter).month;
        vpPager.setAdapter(adapter);
        //초기 포지션 지정.
        vpPager.setCurrentItem(50);
        //초기 타이틀 설정.
        ((CalendarActivity)getActivity()).getSupportActionBar().setTitle(year + "년 " + (month+1) + "월");
        //화면 전환시 타이틀 설정.
        //초기 position값이 50이기 때문에 12의 배수인 60에 맞추기 위해 10을 더해주고 계산.
        vpPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                ((CalendarActivity) getActivity()).getSupportActionBar().setTitle((year + (month + position + 10) / 12 - 5) + "년 " + ((month + position + 10) % 12 ) + "월");

            }
        });
        return rootView;
    }
}