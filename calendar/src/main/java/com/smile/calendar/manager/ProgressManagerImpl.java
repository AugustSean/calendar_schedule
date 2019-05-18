package com.smile.calendar.manager;

import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import com.smile.calendar.holder.AbstractViewHolder;
import com.smile.calendar.holder.SizeViewHolder;
import com.smile.calendar.holder.StubViewHolder;
import com.smile.calendar.view.CollapseCalendarView;

public class ProgressManagerImpl extends ProgressManager {

    public ProgressManagerImpl(@NonNull CollapseCalendarView calendarView, boolean fromMonth) {
        super(calendarView,fromMonth);

        if (!fromMonth) {
            initMonthView();
        }
    }

    @Override
    public void finish(final boolean expanded) {

        mCalendarView.post(new Runnable() { // to prevent flickering
            @Override
            public void run() {
                mCalendarView.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;

                for (AbstractViewHolder view : mViews) {
                    view.onFinish(true);
                }

                if (!expanded) {
                    CalendarManager manager = mCalendarView.getManager();
                    if (mFromMonth) {
                        manager.toggleView();
                    }
                    mCalendarView.populateLayout();
                }
            }
        });
    }

    private void initMonthView() {

        mCalendarHolder = new SizeViewHolder(mCalendarView.getHeight(), 0);
        mCalendarHolder.setView(mCalendarView);
        mCalendarHolder.setDelay(0);
        mCalendarHolder.setDuration(1);


        mCalendarView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                mCalendarView.getViewTreeObserver().removeOnPreDrawListener(this);

                mCalendarHolder.setMaxHeight(mCalendarView.getHeight());

                mCalendarView.getLayoutParams().height = mCalendarHolder.getMinHeight();

                initializeChildren();

                setInitialized(true);

                return false;
            }
        });
    }


    private void initializeChildren() {

        mViews = new AbstractViewHolder[childCount];
        for (int i = 0; i < childCount; i++) {

            View view = mWeeksView.getChildAt(i);

            int activeIndex = getActiveIndex();

            AbstractViewHolder holder;
            if (i == activeIndex) {
                holder = new StubViewHolder();
            } else {
                SizeViewHolder tmpHolder = new SizeViewHolder(0, view.getHeight());

                final int duration = mWeeksHolder.getMaxHeight() - view.getHeight();

                if (i < activeIndex) {
                    tmpHolder.setDelay(view.getTop() * 1.0f / duration);
                } else {
                    tmpHolder.setDelay((view.getTop() - view.getHeight()) * 1.0f / duration);
                }
                tmpHolder.setDuration(view.getHeight() * 1.0f / duration);

                holder = tmpHolder;

                view.setVisibility(View.GONE);
            }

            holder.setView(view);

            mViews[i] = holder;
        }

    }

}
