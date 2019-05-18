package com.smile.calendar.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.smile.calendar.R;
import com.smile.calendar.manager.CalendarManager;
import com.smile.calendar.manager.ResizeManager;
import com.smile.calendar.module.Day;
import com.smile.calendar.module.EventModel;
import com.smile.calendar.module.Month;
import com.smile.calendar.module.Week;

import org.joda.time.LocalDate;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;


@SuppressLint({"SimpleDateFormat", "NewApi"})
public class CollapseCalendarView extends LinearLayout implements View.OnClickListener {

    public static final String TAG = "CalendarView";
    private CalendarManager mManager;
    private TextView mTitleView;
    private ImageButton mPrev;
    private ImageButton mNext;
    private LinearLayout mWeeksView;
    private final LayoutInflater mInflater;
    private final RecycleBin mRecycleBin = new RecycleBin();
    private OnDateSelect mListener;
    private TextView mSelectionText;
    private LinearLayout mHeader;
    private ResizeManager mResizeManager;
    private ImageView mIvPre;
    private ImageView mIvNext;
    private Animation left_in;
    private Animation right_in;
    private boolean initialized;
    private String[] weeks;
    private OnTitleClickListener titleClickListener;
    private JSONObject dataObject;                            //日历数据源
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    private boolean showChinaDay = true;                    //是否显示农历日期
    private String marksMap = "";
    public static boolean withMonthSchedule = true
    private List<EventModel> eventModels;

    public CollapseCalendarView(Context context) {
        this(context, null);
        onFinishInflate();
    }

    public void setWithMonthSchedule(boolean withSchedule) {
        this.withMonthSchedule = withSchedule;
        showChinaDay = false;
    }

    public void showChinaDay(boolean showChinaDay) {
        this.showChinaDay = showChinaDay;
        populateLayout();
    }


    public void setTitleClickListener(OnTitleClickListener titleClickListener) {
        this.titleClickListener = titleClickListener;
    }

    public CollapseCalendarView(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.calendarViewStyle);
    }

    public void setArrayData(JSONObject jsonObject) {
        this.dataObject = jsonObject;
    }

    public interface OnTitleClickListener {
        void onTitleClick();
    }

    @SuppressLint("NewApi")
    public CollapseCalendarView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        weeks = getResources().getStringArray(R.array.weeks);
        mInflater = LayoutInflater.from(context);
        mResizeManager = new ResizeManager(this, !withMonthSchedule);
        int resourceLayout = withMonthSchedule ? R.layout.calendar_month_layout_with_schedule : R.layout.calendar_month_layout;
        inflate(context, resourceLayout, this);
        setOrientation(VERTICAL);
        setBackgroundColor(getResources().getColor(R.color.activity_bg_color));
        mIvPre = new ImageView(getContext());
        mIvNext = new ImageView(getContext());
        RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, android.widget.RelativeLayout.LayoutParams.WRAP_CONTENT);
        mIvPre.setLayoutParams(param);
        mIvNext.setLayoutParams(param);
        initAnim();
    }


    private void initAnim() {
        left_in = AnimationUtils.makeInAnimation(getContext(), true);
        right_in = AnimationUtils.makeInAnimation(getContext(), false);
    }


    public void init(CalendarManager manager) {
        if (manager != null) {
            mManager = manager;
            if (mListener != null) {
                mListener.onDateSelected(mManager.getSelectedDay());
            }
            populateLayout();

        }
    }


    public void changeDate(String date) {
        if (date.compareTo(mManager.getSelectedDay().toString()) > 0) {
            this.setAnimation(right_in);
            right_in.start();
        } else if (date.compareTo(mManager.getSelectedDay().toString()) < 0) {
            this.setAnimation(left_in);
            left_in.start();
        }
        try {
            mManager.init(LocalDate.fromDateFields(sdf.parse(date)));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        init(mManager);
    }


    public CalendarManager getManager() {
        return mManager;
    }

    @Override
    public void onClick(View v) {
    /*	if (mManager != null) {
            int id = v.getId();
			if (id == R.id.prev) {
				prev();
			} else if (id == R.id.next) {
				next();
			} else if (id == R.id.title) {
				if (titleClickListener != null) {
					titleClickListener.onTitleClick();
				}
			}
		}*/
    }

    @SuppressLint("WrongCall")
    @Override
    protected void dispatchDraw(Canvas canvas) {
        mResizeManager.onDraw();
        super.dispatchDraw(canvas);
    }


    public CalendarManager.State getState() {
        if (mManager != null) {
            return mManager.getState();
        } else {
            return null;
        }
    }

    public void setDateSelectListener(OnDateSelect listener) {
        mListener = listener;
    }


    public void setTitle(String text) {
        if (TextUtils.isEmpty(text)) {
            mHeader.setVisibility(View.VISIBLE);
            mSelectionText.setVisibility(View.GONE);
        } else {
            mHeader.setVisibility(View.GONE);
            mSelectionText.setVisibility(View.VISIBLE);
            mSelectionText.setText(text);
        }
    }

    /**
     * 显示日历自带标题
     */
    public void showHeader() {
        mHeader.setVisibility(View.VISIBLE);
    }

    /**
     * 隐藏日历自带标题
     */
    public void hideHeader() {
        mHeader.setVisibility(View.GONE);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return mResizeManager.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {


        super.onTouchEvent(event);
        return mResizeManager.onTouchEvent(event);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
//        解决日历和pager左右滑动的冲突
        getParent().requestDisallowInterceptTouchEvent(true);
        return super.dispatchTouchEvent(ev);
    }

    /**
     * 周-月
     */
    public void weekToMonth() {
        if (mManager.getState() == CalendarManager.State.WEEK) {
            mManager.toggleView();
        }
    }

    /**
     * 月-周
     */
    public void monthToWeek() {
        if (mManager.getState() == CalendarManager.State.MONTH) {
            mManager.toggleView();
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mTitleView = (TextView) findViewById(R.id.title);
        mTitleView.setOnClickListener(this);
        mPrev = (ImageButton) findViewById(R.id.prev);
        mNext = (ImageButton) findViewById(R.id.next);
        mWeeksView = (LinearLayout) findViewById(R.id.weeks);
        mHeader = (LinearLayout) findViewById(R.id.header);
        mSelectionText = (TextView) findViewById(R.id.selection_title);
        mPrev.setOnClickListener(this);
        mNext.setOnClickListener(this);
        populateLayout();
    }


    public synchronized void populateLayout() {
        if (mManager != null) {
            populateDays();
            if (mPrev != null) {
                mPrev.setEnabled(mManager.hasPrev());
                mNext.setEnabled(mManager.hasNext());
                mTitleView.setText(mManager.getHeaderText());
                if (mManager.getState() == CalendarManager.State.MONTH) {
                    populateMonthLayout((Month) mManager.getUnits());
                } else {
                    populateWeekLayout((Week) mManager.getUnits());
                    mManager.weekChanged();
                }
            }
        }
    }

    /**
     * 刷新日历Month View
     *
     * @param month 月份
     */
    private void populateMonthLayout(Month month) {
        List<Week> weeks = month.getWeeks();
        int cnt = weeks.size();
        for (int i = 0; i < cnt; i++) {
            populateWeekLayout(weeks.get(i), withMonthSchedule ? getWeekViewWithSchedule(i) : getWeekView(i));
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


    /**
     * 翻转到前一页
     */
    public void prev() {
        if (mManager.prev()) {
            populateLayout();
        }
        if (mListener != null) {
            //执行选中回调
            mListener.onDateSelected(mManager.getSelectedDay());
        }
        this.setAnimation(left_in);
        left_in.start();
    }

    /**
     * 翻转到下一页
     */
    public void next() {
        if (mManager.next()) {
            populateLayout();
        }
        if (mListener != null) {
            //执行选中回调
            mListener.onDateSelected(mManager.getSelectedDay());
        }
        this.setAnimation(right_in);
        right_in.start();
    }


    public LinearLayout getWeeksView() {
        return mWeeksView;
    }


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

    private WeekViewWithSchedule getWeekViewWithSchedule(int index) {
        int cnt = mWeeksView.getChildCount();

        if (cnt < index + 1) {
            for (int i = cnt; i < index + 1; i++) {
                View view = getView();
                mWeeksView.addView(view);
            }
        }
        return (WeekViewWithSchedule) mWeeksView.getChildAt(index);
    }

    private View getView() {
        View view = mRecycleBin.recycleView();
        if (view == null) {
            int resourceLayout = withMonthSchedule ? R.layout.calendar_week_layout_with_scehdule : R.layout.calendar_week_layout;
            view = mInflater.inflate(resourceLayout, this, false);
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

        public View recycleView() {
            return mViews.poll();
        }

        public void addView(View view) {
            mViews.add(view);
        }
    }

    public interface OnDateSelect {
        void onDateSelected(LocalDate date);
    }


    public void addMarks(String dateString) {
        marksMap = dateString;
//        刷新数据
        populateLayout();
    }

    /**
     * 在日历上做一组标记
     *
     */
    public void clearMarks() {
        marksMap = "";
//        刷新数据
        populateLayout();
    }

    public void setEvent(List<EventModel> eventModels) {
        this.eventModels = eventModels;
        populateLayout();
    }

}
