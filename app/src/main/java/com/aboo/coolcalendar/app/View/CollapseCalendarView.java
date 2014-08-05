package com.aboo.coolcalendar.app.View;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.aboo.coolcalendar.app.R;
import com.aboo.coolcalendar.app.View.manager.*;
import com.aboo.coolcalendar.app.View.widget.DayView;
import com.aboo.coolcalendar.app.View.widget.WeekView;
import org.apache.commons.lang3.StringUtils;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Created by Blaz Solar on 28/02/14.
 */
public class CollapseCalendarView extends LinearLayout implements View.OnClickListener {

    private static final String TAG = "CalendarView";

    @NonNull
    private final CalendarManager mManager;

    @NonNull
    private TextView mTitleView;
    @NonNull
    private ImageButton mPrev;
    @NonNull
    private ImageButton mNext;
    @NonNull
    private LinearLayout mWeeksView;

    @NonNull
    private final LayoutInflater mInflater;
    @NonNull
    private final RecycleBin mRecycleBin = new RecycleBin();

    @Nullable
    private OnDateSelect mListener;

    @NonNull
    private TextView mSelectionText;
    @NonNull
    private LinearLayout mHeader;

    @NonNull
    private ResizeManager mResizeManager;
    private DateTimeFormatter mHeaderFormat;
    private LocalDate mCurrentDate;

    private int mTouchDownX;
    private int mTouchDownY;
    private int mTouchUpX;
    private int mTouchUpY;

    public CollapseCalendarView(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.calendarViewStyle);
    }

    public CollapseCalendarView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        mManager = new CalendarManager(LocalDate.now(), CalendarManager.State.WEEK, null, null);
        mInflater = LayoutInflater.from(context);

        mResizeManager = new ResizeManager(this);

        inflate(context, R.layout.calendar_layout, this);

        setOrientation(VERTICAL);

        mHeaderFormat = DateTimeFormat.forPattern("MMMM yyyy");
    }

    public void init(@NonNull LocalDate date, @Nullable LocalDate minDate, @Nullable LocalDate maxDate) {
        mManager.init(date, minDate, maxDate);
        populateLayout();
        if (mListener != null) {
            mListener.onDateSelected(date);
        }
    }

    @NonNull
    public CalendarManager getManager() {
        return mManager;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.prev) {
            if (mManager.prev()) {
                populateLayout();
            }
        } else if (id == R.id.next) {
            if (mManager.next()) {
                populateLayout();
            }
        }
    }

    @Override
    protected void dispatchDraw(@NonNull Canvas canvas) {
        mResizeManager.onDraw();

        super.dispatchDraw(canvas);
    }

    public CalendarManager.State getState() {
        return mManager.getState();
    }

    public void setListener(@Nullable OnDateSelect listener) {
        mListener = listener;
    }

    public void setTitle(@Nullable String text) {
        if (StringUtils.isEmpty(text)) {
            mHeader.setVisibility(View.VISIBLE);
            mSelectionText.setVisibility(View.GONE);
        } else {
            mHeader.setVisibility(View.GONE);
            mSelectionText.setVisibility(View.VISIBLE);
            mSelectionText.setText(text);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return mResizeManager.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        super.onTouchEvent(event);


        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mTouchDownX = (int) event.getX();
                mTouchDownY = (int) event.getY();
                Log.e("按下00", mTouchDownX + "=======" + mTouchDownY);
                break;

            case MotionEvent.ACTION_UP:
                mTouchUpX = (int) event.getX();
                mTouchUpY = (int) event.getY();

                int deltaX = mTouchUpX - mTouchDownX;
                int deltaY = mTouchUpY - mTouchDownY;

                Log.e("弹起11", mTouchUpX + "=======" + mTouchUpY);
                Log.e("滑动=====", deltaX + "=======" + deltaY);

                // 如果滑动在Y轴上的距离太大，取消左右滑动
                if (Math.abs(deltaY) > 100) {
                    break;
                }

                // 左滑
                if (deltaX > 80) {
                    if (mManager.prev()) {
                        populateLayout();
                    }
                    break;
                }

                // 右滑
                if (deltaX < -80) {
                    if (mManager.next()) {
                        populateLayout();
                    }
                }

                break;
        }

        return mResizeManager.onTouchEvent(event);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mTitleView = (TextView) findViewById(R.id.title);
        mPrev = (ImageButton) findViewById(R.id.prev);
        mNext = (ImageButton) findViewById(R.id.next);
        mWeeksView = (LinearLayout) findViewById(R.id.weeks);

        mHeader = (LinearLayout) findViewById(R.id.header);
        mSelectionText = (TextView) findViewById(R.id.selection_title);

        mPrev.setOnClickListener(this);
        mNext.setOnClickListener(this);

        populateDays();
        populateLayout();
    }

    private void populateDays() {

        DateTimeFormatter formatter = DateTimeFormat.forPattern("E");

        LinearLayout layout = (LinearLayout) findViewById(R.id.days);

        LocalDate date = LocalDate.now().withDayOfWeek(DateTimeConstants.MONDAY);
        for (int i = 0; i < 7; i++) {
            TextView textView = (TextView) layout.getChildAt(i);
            textView.setText(date.toString(formatter));

            date = date.plusDays(1);
        }

    }

    public void populateLayout() {

        mPrev.setEnabled(mManager.hasPrev());
        mNext.setEnabled(mManager.hasNext());

        mTitleView.setText(mManager.getHeaderText());

        // 每更新一次则更新一次当前日期
        mCurrentDate = LocalDate.parse(mTitleView.getText().toString(), mHeaderFormat);

        if (mManager.getState() == CalendarManager.State.MONTH) {
            populateMonthLayout((Month) mManager.getUnits());
        } else {
            populateWeekLayout((Week) mManager.getUnits());
        }

    }

    private void populateMonthLayout(Month month) {

        List<Week> weeks = month.getWeeks();
        int cnt = weeks.size();
        for (int i = 0; i < cnt; i++) {
            WeekView weekView = getWeekView(i);
            populateWeekLayout(weeks.get(i), weekView);
        }

        int childCnt = mWeeksView.getChildCount();
        if (cnt < childCnt) {
            for (int i = cnt; i < childCnt; i++) {
                cacheView(i);
            }
        }

    }

    private void populateWeekLayout(Week week) {
        WeekView weekView = getWeekView(0);
        populateWeekLayout(week, weekView);

        int cnt = mWeeksView.getChildCount();
        if (cnt > 1) {
            for (int i = cnt - 1; i > 0; i--) {
                cacheView(i);
            }
        }
    }

    //初始化每一个空格
    private void populateWeekLayout(@NonNull Week week, @NonNull WeekView weekView) {

        List<Day> days = week.getDays();
        LocalDate tempDate;
        for (int i = 0; i < 7; i++) {
            final Day day = days.get(i);
            DayView dayView = (DayView) weekView.getChildAt(i);

            dayView.setText(day.getText());
            dayView.setSelected(day.isSelected());
            dayView.setCurrent(day.isCurrent());

            tempDate = day.getDate();
            if (mCurrentDate.getYear() == tempDate.getYear()
                    && mCurrentDate.getMonthOfYear() == tempDate.getMonthOfYear()) {
                dayView.setTextColor(getResources().getColorStateList(R.color.text_calendar));
            } else {
                dayView.setTextColor(getResources().getColorStateList(R.color.text_calendar_not_current));
            }

            boolean enables = day.isEnabled();
            dayView.setEnabled(enables);

            if (enables) {
                dayView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        LocalDate date = day.getDate();
//                        if (mManager.selectDay(date)) {
//                            populateLayout();
                        if (mListener != null) {
                            mListener.onDateSelected(date);
                        }
//                        }
                    }
                });
            } else {
                dayView.setOnClickListener(null);
            }
        }

    }

    @NonNull
    public LinearLayout getWeeksView() {
        return mWeeksView;
    }

    @NonNull
    private WeekView getWeekView(int index) {
        int cnt = mWeeksView.getChildCount();

        if (cnt < index + 1) {
            for (int i = cnt; i < index + 1; i++) {
                View view = getView();
                mWeeksView.addView(view);
            }
        }

        return (WeekView) mWeeksView.getChildAt(index);
    }

    private View getView() {
        View view = mRecycleBin.recycleView();
        if (view == null) {
            view = mInflater.inflate(R.layout.week_layout, this, false);
        } else {
            view.setVisibility(View.VISIBLE);
        }
        return view;
    }

    private void cacheView(int index) {
        View view = mWeeksView.getChildAt(index);
        if (view != null) {
            mWeeksView.removeViewAt(index);
            mRecycleBin.addView(view);
        }
    }

    public LocalDate getSelectedDate() {
        return mManager.getSelectedDay();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        mResizeManager.recycle();
    }

    private class RecycleBin {

        private final Queue<View> mViews = new LinkedList<View>();

        @Nullable
        public View recycleView() {
            return mViews.poll();
        }

        public void addView(@NonNull View view) {
            mViews.add(view);
        }

    }

    public interface OnDateSelect {
        public void onDateSelected(LocalDate date);
    }

}
