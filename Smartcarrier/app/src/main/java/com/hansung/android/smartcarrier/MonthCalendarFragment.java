package com.hansung.android.smartlocker;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MonthCalendarFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MonthCalendarFragment extends Fragment {
    Calendar mCal; // 캘린더 선언
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    int year;
    int month;
    int day;
    String userID;
    String userPass;
    String  userName;
    String userAge;
    static ItemMonthAdapter adapt;
    private DBHelper mDbHelper;
    boolean go = false;
    private Button out;
    ArrayAdapter<String> adapter;
    // TODO: Rename and change types of parameters
    private int mParam1;
    private int mParam2;
    public MonthCalendarFragment() {
        // Required empty public constructor
    }

    public static MonthCalendarFragment newInstance(int year, int month) {
        MonthCalendarFragment fragment = new MonthCalendarFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PARAM1, year);
        args.putInt(ARG_PARAM2, month);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getInt(ARG_PARAM1);
            mParam2 = getArguments().getInt(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mDbHelper = new DBHelper(this.getContext());

        View rootView = inflater.inflate(R.layout.fragment_month_calendar, container, false);
        ArrayList<ItemMonth> dayList = new ArrayList<ItemMonth>();


        mCal = Calendar.getInstance();
        // 각 파라미터에 저장된 값을 넣어 Calendar 설정
        mCal.set(Integer.parseInt(String.valueOf(mParam1)), Integer.parseInt(String.valueOf(mParam2))-1, 1);
        // 해당 월의 1일이 될때까지 그리드뷰에 공백 삽입
        int dayNum = mCal.get(Calendar.DAY_OF_WEEK);
        for (int i = 1; i < dayNum; i++) {
            dayList.add(new ItemMonth(String.valueOf(mParam1), String.valueOf(mParam2), null, null, null));
        }
        int dayMax = mCal.getActualMaximum(Calendar.DAY_OF_MONTH);
        // 해당 월의 최대 일 수를 구하기 위해서 .getActualMaximum(Calendar.DAY_OF_MONTH) 함수를 사용함
        for (int i = 1; i < dayMax+1; i++){ // 최대 일 수만큼 dayList에 요소를 추가함
            Cursor cursor = mDbHelper.getDayUsersBySQL(String.valueOf(mParam1), String.valueOf(mParam2), String.valueOf(i));
            // 월별 달력에서 해당 일수마다 포인터를 이동시켜 데이터가 있으면 일별 칸에 스케줄 제목을 표시함
            if(cursor.getCount()>1){ // 해당 일의 스케줄이 2개 이상이면 최대 2개까지만 표시함
                cursor.moveToNext();
                String sche1 = cursor.getString(cursor.getColumnIndex(UserContract.Users.SCHEDULE_TITLE));
                cursor.moveToNext();
                String sche2 = cursor.getString(cursor.getColumnIndex(UserContract.Users.SCHEDULE_TITLE));
                dayList.add(new ItemMonth(String.valueOf(mParam1), String.valueOf(mParam2), String.valueOf(i), sche1, sche2));
            }
            else {
                if (cursor.moveToNext()) {
                    dayList.add(new ItemMonth(String.valueOf(mParam1), String.valueOf(mParam2),
                            String.valueOf(i), cursor.getString(cursor.getColumnIndex(UserContract.Users.SCHEDULE_TITLE)), null));
                } else {
                    dayList.add(new ItemMonth(String.valueOf(mParam1), String.valueOf(mParam2),
                            String.valueOf(i), null, null));
                }
            }
        }
        // 월간 달력은 총 42칸으로 구성되었고, 날짜를 표현하고 남은칸은 공백 삽입
        for (int i = dayList.size(); i < 42; i++){
            dayList.add(new ItemMonth(String.valueOf(mParam1), String.valueOf(mParam1), null, null, null));
        }
        //System.out.println(dayList);
        // id를 바탕으로 화면 레이아웃에 정의된 GridView 객체 로딩
        GridView gridview = rootView.findViewById(R.id.gridview);
        adapt = new ItemMonthAdapter(
                getActivity(),
                R.layout.item_month,
                dayList){
            @Override
            public View getView(int position, View convertView, ViewGroup parent){
                View view = super.getView(position,convertView,parent);
                // 각 칸의 색은 흰색으로 표현하였으며, 각 칸의 텍스트를 가운데로 정렬
                View tv_cell = (View) view.findViewById(R.id.month_item);
                //tv_cell.setBackgroundColor(Color.WHITE);
                // 그리드뷰로 화면을 꽉 채우기 위해, 날짜를 출력할 TextView의 높이를 그리드뷰의 높이 / 6으로 설정
                tv_cell.getLayoutParams().height = gridview.getHeight()/6;

                return tv_cell;
            }
        };
        // 어댑터를 GridView 객체에 연결
        gridview.setAdapter(adapt);
        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                // 일별 칸을 클릭했을 때, 스케줄이 한개 있으면 해당 날짜정보를 스케줄액티비티에 전송
                if(adapt.getText1(position)!= null&&(adapt.getText2(position) == null)){
                    Cursor cursor = mDbHelper.getDayUsersBySQL(String.valueOf(mParam1), String.valueOf(mParam2), String.valueOf(adapt.getDayNum(position)));
                    cursor.moveToNext();
                    Intent intent = new Intent(getActivity(), Schedule.class);
                    intent.putExtra("year", mParam1);
                    intent.putExtra("month", mParam2);
                    intent.putExtra("day", Integer.parseInt((String) adapt.getDayNum(position)));
                    intent.putExtra("hour", Integer.parseInt(cursor.getString(cursor.getColumnIndex(UserContract.Users.START_TIME))));
                    startActivity(intent);
                }
                // 스케줄이 두개 이상이면 다이얼로그에 스케줄 목록을 표시하고, 클릭하면 해당 스케줄의 데이터를 스케줄 액티비티에 전송
                else if((adapt.getText1(position) != null)&&(adapt.getText2(position) != null)) {
                    adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.select_dialog_singlechoice);

                    Cursor cursor = mDbHelper.getDayUsersBySQL(String.valueOf(mParam1),String.valueOf(mParam2), String.valueOf(adapt.getDayNum(position)));
                    while (cursor.moveToNext()) {
                        adapter.add(cursor.getString(cursor.getColumnIndex(UserContract.Users.SCHEDULE_TITLE)));
                    }
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            cursor.moveToPosition(adapter.getPosition(adapter.getItem(which)));
                            //System.out.println(adapter.getItem(which));
                            System.out.println(adapter.getPosition(adapter.getItem(which)));
                            Intent intent = new Intent(getActivity(), Schedule.class);
                            intent.putExtra("year", mParam1);
                            intent.putExtra("month", mParam2);
                            intent.putExtra("day", Integer.parseInt((String) adapt.getDayNum(position)));
                            intent.putExtra("hour", Integer.parseInt(cursor.getString(cursor.getColumnIndex(UserContract.Users.START_TIME))));
                            startActivity(intent);
                        }
                    });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }
                if(position >= dayNum-1 && position < dayMax+dayNum-1) {
                    day = position-(dayNum-2);
                    // 그리드뷰의 n번째 요소의 내용이 1일 이상이고, 해당 월의 최대일수 이하일 때 메시지를 출력함
                    Toast.makeText(getActivity(),
                            mParam1 + "." + mParam2 + "." + (position -(dayNum-2)),
                            Toast.LENGTH_SHORT).show();
                    go = true;
                }
            }
        });

        out = rootView.findViewById(R.id.out);

        //회원가입버튼
        out.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(),HomeActivity.class);
                startActivity(intent);
            }
        });
        // 플로팅 버튼 부분
        FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() { // 일별칸에 스케줄이 없을때 클릭하고 플로팅버튼을 누르면 스케줄 액티비티로 이동
            @Override
            public void onClick(View view) {
                userID = ((CalendarActivity) getActivity()).userID;
                userName = ((CalendarActivity) getActivity()).userName;
                userPass = ((CalendarActivity) getActivity()).userPass;
                userAge = ((CalendarActivity) getActivity()).userAge;

                Intent intent = new Intent(getActivity(), Schedule.class);
                    intent.putExtra("year", mParam1);
                    intent.putExtra("month", mParam2);
                    intent.putExtra("day", day);
                    intent.putExtra("userID", userID);
                    intent.putExtra("userName",userName);
                    intent.putExtra("userPass", userPass);
                    intent.putExtra("userAge", userAge);
                    startActivity(intent);

            }
        });
        return rootView;
    }

}