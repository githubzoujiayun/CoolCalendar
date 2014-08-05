package com.aboo.coolcalendar.app;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import com.aboo.coolcalendar.app.View.CollapseCalendarView;
import com.aboo.coolcalendar.app.View.manager.CalendarManager;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;


public class MainActivity extends Activity implements CollapseCalendarView.OnDateSelect {

    private CollapseCalendarView mCalendarView;
    private TextView mTitleTv;
    private DateTimeFormatter mHeaderFormat;
    private CalendarManager mCalendarManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mCalendarView = (CollapseCalendarView) findViewById(R.id.calendar);
//        mCalendarView.init(LocalDate.now(), LocalDate.now(), LocalDate.now().plusYears(1));
        mCalendarView.setListener(this);

        mTitleTv = (TextView) mCalendarView.findViewById(R.id.title);
        mHeaderFormat = DateTimeFormat.forPattern("MMMM yyyy");
        mCalendarManager = mCalendarView.getManager();
    }

    @Override
    public void onDateSelected(LocalDate date) {
        LocalDate currentDate = LocalDate.parse(mTitleTv.getText().toString(), mHeaderFormat);
        // 如果是当前月的日期才显示
        if (date.getYear() == currentDate.getYear()
                && date.getMonthOfYear() == currentDate.getMonthOfYear()){
            Toast.makeText(this, date.getYear() + "年"
                            + date.getMonthOfYear() + "月"
                            + date.getDayOfMonth() + "日",
                    Toast.LENGTH_SHORT).show();

            // 如果选中的日期不是当前月，则不给选中
            if(mCalendarManager.selectDay(date)){
                mCalendarView.populateLayout();
            }
        }
    }
}
