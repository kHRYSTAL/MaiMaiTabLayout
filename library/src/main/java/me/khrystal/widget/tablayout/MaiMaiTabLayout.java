package me.khrystal.widget.tablayout;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * usage: 仿脉脉tabLayout
 * author: kHRYSTAL
 * create time: 19/8/9
 * update time:
 * email: 723526676@qq.com
 */
public class MaiMaiTabLayout extends HorizontalScrollView {

    // @formatter:off
    private static final int[] ATTRS = new int[]{
            android.R.attr.textSize,
            android.R.attr.textColor
    };
    // @formatter:on
    private LinearLayout.LayoutParams matchParentTabLayoutParams;
    private LinearLayout.LayoutParams defaultTabLayoutParams;
    private LinearLayout.LayoutParams expandedTabLayoutParams;

    private PageListener pageListener;
    public ViewPager.OnPageChangeListener delegatePageListener;

    private LinearLayout tabsContainer;
    private ViewPager pager;

    private int tabCount;

    private int currentPosition = 0;
    private int selectedPosition = 0;

    private Paint rectPaint;
    private Paint dividerPaint;

    private int indicatorColor = 0xFF666666;
    private int underlineColor = 0x1A000000;
    private int dividerColor = 0x1A000000;

    // 该属性表示里面的TAB是否均分整个PagerSlidingTabStrip控件的宽,
    // true是,false不均分,从左到右排列,默认false
    private boolean shouldExpand = false;
    private boolean textAllCaps = true;

    private int scrollOffset = 52;
    private int indicatorHeight = 8;
    private int underlineHeight = 2;
    private int dividerPaddingTopBottom = 12;
    private int tabPadding = 20;
    private int dividerWidth = 1;

    private int tabTextSize = 12;
    private int tabTextColor = 0xFF666666;
    private int selectedTabTextColor = 0xFF45c01a;
    private Typeface tabTypeface = null;
    private int tabTypefaceStyle = Typeface.NORMAL;

    private int lastScrollX = 0;

    private int tabBackgroundResId;
    private Locale locale;
    private boolean smoothScrollWhenClickTab = true;
    private List<TextView> tabViews = new ArrayList<>();
    private boolean mFadeEnabled = true;
    private float zoomMax = 0.3f;
    private float lastZoomMax;
    private State mState;

    private enum State {
        IDLE, GOING_LEFT, GOING_RIGHT
    }

    private int oldPage;

    private Paint mPaintTabText = new Paint();

    public MaiMaiTabLayout(Context context) {
        this(context, null);
    }

    public MaiMaiTabLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MaiMaiTabLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setFillViewport(true);
        setWillNotDraw(false);// 防止onDraw方法不执行

        tabsContainer = new LinearLayout(context);
        tabsContainer.setOrientation(LinearLayout.HORIZONTAL);
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        tabsContainer.setLayoutParams(params);
        addView(tabsContainer);

        //设置默认值
        DisplayMetrics dm = getResources().getDisplayMetrics();
        scrollOffset = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, scrollOffset, dm);
        indicatorHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, indicatorHeight, dm);
        underlineHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, underlineHeight, dm);
        dividerPaddingTopBottom = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dividerPaddingTopBottom, dm);
        tabPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, tabPadding, dm);
        dividerWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dividerWidth, dm);
        tabTextSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, tabTextSize, dm);

        // get system attrs (android:textSize and android:textColor)
        TypedArray a = context.obtainStyledAttributes(attrs, ATTRS);
        tabTextSize = a.getDimensionPixelSize(0, tabTextSize);
        tabTextColor = a.getColor(1, tabTextColor);
        a.recycle();

        // get custom attrs
        a = context.obtainStyledAttributes(attrs, R.styleable.MaiMaiTabLayout);
        indicatorColor = a.getColor(R.styleable.MaiMaiTabLayout_indicatorColor, indicatorColor);
        underlineColor = a.getColor(R.styleable.MaiMaiTabLayout_underlineColor, underlineColor);
        dividerColor = a.getColor(R.styleable.MaiMaiTabLayout_dividerColor, dividerColor);
        indicatorHeight = a.getDimensionPixelSize(R.styleable.MaiMaiTabLayout_indicatorHeight, indicatorHeight);
        underlineHeight = a.getDimensionPixelSize(R.styleable.MaiMaiTabLayout_underlineHeight, underlineHeight);
        dividerPaddingTopBottom = a.getDimensionPixelSize(R.styleable.MaiMaiTabLayout_dividerPaddingTopBottom, dividerPaddingTopBottom);
        tabPadding = a.getDimensionPixelSize(R.styleable.MaiMaiTabLayout_tabPaddingLeftRight, tabPadding);
        tabBackgroundResId = a.getResourceId(R.styleable.MaiMaiTabLayout_tabBackground, tabBackgroundResId);
        shouldExpand = a.getBoolean(R.styleable.MaiMaiTabLayout_shouldExpand, shouldExpand);
        scrollOffset = a.getDimensionPixelSize(R.styleable.MaiMaiTabLayout_scrollOffset, scrollOffset);
        textAllCaps = a.getBoolean(R.styleable.MaiMaiTabLayout_allCaps, textAllCaps);
        selectedTabTextColor = a.getColor(R.styleable.MaiMaiTabLayout_textSelectedColor, selectedTabTextColor);
        zoomMax = a.getFloat(R.styleable.MaiMaiTabLayout_scaleZoomMax, zoomMax);
        lastZoomMax = zoomMax;
        smoothScrollWhenClickTab = a.getBoolean(R.styleable.MaiMaiTabLayout_smoothScrollWhenClickTab, smoothScrollWhenClickTab);
        tabTextColor = a.getColor(R.styleable.MaiMaiTabLayout_tabTextColor, tabTextColor);
        selectedTabTextColor = a.getColor(R.styleable.MaiMaiTabLayout_selectedTabTextColor, selectedTabTextColor);
        tabTextSize = a.getDimensionPixelSize(R.styleable.MaiMaiTabLayout_tabTextSize, tabTextSize);
        a.recycle();

        rectPaint = new Paint();
        rectPaint.setAntiAlias(true);
        rectPaint.setStyle(Paint.Style.FILL);

        dividerPaint = new Paint();
        dividerPaint.setAntiAlias(true);
        dividerPaint.setStrokeWidth(dividerWidth);

        defaultTabLayoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
        matchParentTabLayoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        expandedTabLayoutParams = new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, 1.0f);

        if (locale == null) {
            locale = getResources().getConfiguration().locale;
        }
        pageListener = new PageListener();

        mPaintTabText.setAntiAlias(true);
    }

    /**
     * 关联ViewPager
     *
     * @param pager pager
     */
    public void setViewPager(ViewPager pager) {
        this.pager = pager;
        if (this.pager.getAdapter() == null) {
            throw new IllegalStateException("ViewPager does not have adapter instance.");
        }
        this.pager.addOnPageChangeListener(pageListener);
        this.notifyDataSetChanged();
    }

    public void setOnPageChangeListener(ViewPager.OnPageChangeListener listener) {
        this.delegatePageListener = listener;
    }

    private void notifyDataSetChanged() {
        tabsContainer.removeAllViews();
        tabCount = pager.getAdapter().getCount();
        for (int i = 0; i < tabCount; i++) {
            addTextTab(i, pager.getAdapter().getPageTitle(i).toString());
        }
        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {
                getViewTreeObserver().removeGlobalOnLayoutListener(this);
                currentPosition = pager.getCurrentItem();
                scrollToChild(currentPosition, 0);
                updateTabStyles();
            }
        });
    }

    private void addTextTab(final int position, String title) {
        TextView tab = new TextView(getContext());
        tab.setText(title);
        tab.setGravity(Gravity.LEFT | Gravity.BOTTOM);
        tab.setSingleLine();
        tab.setIncludeFontPadding(false);

        addTab(position, tab);
    }

    private void addTab(final int position, View tab) {
        TitleView titleView = new TitleView(getContext());
        titleView.setPadding(0, 0, tabPadding, 0);
        titleView.addView(tab, 0, matchParentTabLayoutParams);
        tabsContainer.addView(titleView, position, shouldExpand ? expandedTabLayoutParams : defaultTabLayoutParams);

        titleView.setDoubleSingleClickListener(new TitleView.DoubleSingleClickListener() {
            @Override
            public void onDoubleTap(MotionEvent e) {
                //cb
                if (mOnPagerTitleItemClickListener != null) {
                    mOnPagerTitleItemClickListener.onDoubleClickItem(position);
                }
            }

            @Override
            public void onSingleTapConfirmed(MotionEvent e) {
                changeSelectPosition(position);
                //cb
                if (mOnPagerTitleItemClickListener != null) {
                    mOnPagerTitleItemClickListener.onSingleClickItem(position);
                }
                requestLayout();
            }
        });
        ((TextView) tab).setTextColor(tabTextColor);
        tabViews.add(position, (TextView) tab);
    }

    private void changeSelectPosition(int position) {
        mFadeEnabled = false;
        selectedPosition = position;
        //set old view statue
        tabViews.get(oldPosition).setTextColor(tabTextColor);
        tabViews.get(oldPosition).setTextSize(TypedValue.COMPLEX_UNIT_PX, tabTextSize);

        //set new view statue
        tabViews.get(position).setTextColor(selectedTabTextColor);
        tabViews.get(position).setTextSize(TypedValue.COMPLEX_UNIT_PX, tabTextSize * (1 + zoomMax));
        oldPosition = selectedPosition;
        pager.setCurrentItem(position, smoothScrollWhenClickTab);
        currentPosition = position;
        scrollToChild(position, 0);//滚动HorizontalScrollView
    }

    private void updateTabStyles() {
        for (int i = 0; i < tabCount; i++) {
            FrameLayout frameLayout = (FrameLayout) tabsContainer.getChildAt(i);
            frameLayout.setBackgroundResource(tabBackgroundResId);

            for (int j = 0; j < frameLayout.getChildCount(); j++) {
                View v = frameLayout.getChildAt(j);
                if (v instanceof TextView) {
                    TextView tab = (TextView) v;
                    tab.setTextSize(TypedValue.COMPLEX_UNIT_PX, tabTextSize);
                    tab.setTypeface(tabTypeface, tabTypefaceStyle);
                    if (j == 0) {
                        tab.setTextColor(tabTextColor);
                    } else {
                        tab.setTextColor(selectedTabTextColor);
                    }
                    tabViews.get(i).setTextColor(tabTextColor);
                    //set normal  Scale
                    tabViews.get(i).setTextSize(TypedValue.COMPLEX_UNIT_PX, tabTextSize);

                    // setAllCaps() is only available from API 14, so the upper case is made manually if we are on a
                    // pre-ICS-build
                    if (textAllCaps) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                            tab.setAllCaps(true);
                        } else {
                            tab.setText(tab.getText().toString().toUpperCase(locale));
                        }
                    }
                    if (i == selectedPosition) {
                        tabViews.get(i).setTextColor(selectedTabTextColor);
                        //set select  Scale
                        tabViews.get(i).setTextSize(TypedValue.COMPLEX_UNIT_PX, tabTextSize * (1 + zoomMax));
                    }
                    requestLayout();
                }
            }
        }
    }

    private void scrollToChild(int position, int offset) {
        if (tabCount == 0) {
            return;
        }
        int newScrollX = tabsContainer.getChildAt(position).getLeft() + offset;

        if (position > 0 || offset > 0) {
            newScrollX -= scrollOffset;
        }

        if (newScrollX != lastScrollX) {
            lastScrollX = newScrollX;
            // smoothScrollTo(newScrollX, 0);
            int k = tabsContainer.getChildAt(position).getMeasuredWidth();
            int l = tabsContainer.getChildAt(position).getLeft() + offset;
            int i2 = l + k / 2 - this.getMeasuredWidth() / 2;
            smoothScrollTo(i2, 0);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (isInEditMode() || tabCount == 0) {
            return;
        }
        final int height = getHeight();
        // draw underline
        rectPaint.setColor(underlineColor);
        canvas.drawRect(0, height - underlineHeight, tabsContainer.getWidth(), height, rectPaint);
        // draw indicator line
        rectPaint.setColor(indicatorColor);
        // draw divider
        dividerPaint.setColor(dividerColor);
        for (int i = 0; i < tabCount - 1; i++) {
            View tab = tabsContainer.getChildAt(i);
            canvas.drawLine(tab.getRight(), dividerPaddingTopBottom, tab.getRight(), height - dividerPaddingTopBottom, dividerPaint);
        }
    }

    private int oldPosition = 0;

    private class PageListener implements ViewPager.OnPageChangeListener {

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            currentPosition = position;
            if (tabsContainer != null && tabsContainer.getChildAt(position) != null) {
                scrollToChild(position, (int) (positionOffset * tabsContainer.getChildAt(position).getWidth()));
            }

            invalidate();

            if (delegatePageListener != null) {
                delegatePageListener.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }

            if (mState == State.IDLE && positionOffset > 0) {
                oldPage = pager.getCurrentItem();
                mState = position == oldPage ? State.GOING_RIGHT : State.GOING_LEFT;
            }
            boolean goingRight = position == oldPage;
            if (mState == State.GOING_RIGHT && !goingRight)
                mState = State.GOING_LEFT;
            else if (mState == State.GOING_LEFT && goingRight)
                mState = State.GOING_RIGHT;


            float effectOffset = isSmall(positionOffset) ? 0 : positionOffset;


            View mLeft = tabsContainer.getChildAt(position);
            View mRight = tabsContainer.getChildAt(position + 1);


            if (effectOffset == 0) {
                mState = State.IDLE;
            }

            if (mFadeEnabled)
                animateFadeScale(mLeft, mRight, effectOffset, position);

        }

        @Override
        public void onPageScrollStateChanged(int state) {
            if (state == ViewPager.SCROLL_STATE_IDLE) {
                scrollToChild(pager.getCurrentItem(), 0);
                mFadeEnabled = true;
            }
            if (delegatePageListener != null) {
                delegatePageListener.onPageScrollStateChanged(state);
            }
        }

        @Override
        public void onPageSelected(int position) {
            selectedPosition = position;

            //set old view statue
            tabViews.get(oldPosition).setTextColor(tabTextColor);
            //set new view statue
            tabViews.get(position).setTextColor(selectedTabTextColor);
            if (delegatePageListener != null) {
                delegatePageListener.onPageSelected(position);
            }
            oldPosition = selectedPosition;
        }
    }

    // use method replace viewpager#setCurrentItem(position)
    public void select(int position) {
        changeSelectPosition(position);
    }

    public void setIndicatorColor(int indicatorColor) {
        this.indicatorColor = indicatorColor;
        invalidate();
    }

    public void setIndicatorColorResource(int resId) {
        this.indicatorColor = getResources().getColor(resId);
        invalidate();
    }

    public int getIndicatorColor() {
        return this.indicatorColor;
    }

    public void setIndicatorHeight(int indicatorLineHeightDp) {
        this.indicatorHeight = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, indicatorLineHeightDp, getResources().getDisplayMetrics());
        invalidate();
    }

    public int getIndicatorHeight() {
        return indicatorHeight;
    }

    public void setUnderlineColor(int underlineColor) {
        this.underlineColor = underlineColor;
        invalidate();
    }

    public void setUnderlineColorResource(int resId) {
        this.underlineColor = getResources().getColor(resId);
        invalidate();
    }

    public int getUnderlineColor() {
        return underlineColor;
    }

    public void setDividerColor(int dividerColor) {
        this.dividerColor = dividerColor;
        invalidate();
    }

    public void setDividerColorResource(int resId) {
        this.dividerColor = getResources().getColor(resId);
        invalidate();
    }

    public int getDividerColor() {
        return dividerColor;
    }

    public void setUnderlineHeight(int underlineHeightDp) {
        this.underlineHeight = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, underlineHeightDp, getResources().getDisplayMetrics());
        invalidate();
    }

    public int getUnderlineHeight() {
        return underlineHeight;
    }

    public void setDividerPaddingTopBottom(int dividerPaddingDp) {
        this.dividerPaddingTopBottom = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dividerPaddingDp, getResources().getDisplayMetrics());
        invalidate();
    }

    public int getDividerPaddingTopBottom() {
        return dividerPaddingTopBottom;
    }

    public void setScrollOffset(int scrollOffsetPx) {
        this.scrollOffset = scrollOffsetPx;
        invalidate();
    }

    public int getScrollOffset() {
        return scrollOffset;
    }

    public void setShouldExpand(boolean shouldExpand) {
        this.shouldExpand = shouldExpand;
        notifyDataSetChanged();
    }

    public boolean getShouldExpand() {
        return shouldExpand;
    }

    public boolean isTextAllCaps() {
        return textAllCaps;
    }

    public void setAllCaps(boolean textAllCaps) {
        this.textAllCaps = textAllCaps;
    }

    public void setTextSize(int textSizeSp) {
        this.tabTextSize = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP, textSizeSp, getResources().getDisplayMetrics());
        updateTabStyles();
    }

    public int getTextSize() {
        return tabTextSize;
    }

    public void setTextColor(int textColor) {
        this.tabTextColor = textColor;
        updateTabStyles();
    }

    public void setTextColorResource(int resId) {
        this.tabTextColor = getResources().getColor(resId);
        updateTabStyles();
    }

    public int getTextColor() {
        return tabTextColor;
    }

    public void setSelectedTextColor(int textColor) {
        this.selectedTabTextColor = textColor;
        updateTabStyles();
    }

    public void setSelectedTextColorResource(int resId) {
        this.selectedTabTextColor = getResources().getColor(resId);
        updateTabStyles();
    }

    public int getSelectedTextColor() {
        return selectedTabTextColor;
    }

    public void setTypeface(Typeface typeface, int style) {
        this.tabTypeface = typeface;
        this.tabTypefaceStyle = style;
        updateTabStyles();
    }

    public void setTabBackground(int resId) {
        this.tabBackgroundResId = resId;
        updateTabStyles();
    }

    public int getTabBackground() {
        return tabBackgroundResId;
    }

    public void setTabPaddingLeftRight(int paddingDp) {
        this.tabPadding = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, paddingDp, getResources().getDisplayMetrics());
        updateTabStyles();
    }

    public int getTabPaddingLeftRight() {
        return tabPadding;
    }

    public boolean isSmoothScrollWhenClickTab() {
        return smoothScrollWhenClickTab;
    }

    public void setSmoothScrollWhenClickTab(boolean smoothScrollWhenClickTab) {
        this.smoothScrollWhenClickTab = smoothScrollWhenClickTab;
    }

    public void updateSelectTabScale(float zoom) {
        if (zoomMax == lastZoomMax - zoom)
            return;
        zoomMax = lastZoomMax - zoom;
        tabViews.get(selectedPosition).setTextSize(TypedValue.COMPLEX_UNIT_PX, tabTextSize * (1 + zoomMax));
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());
        currentPosition = savedState.currentPosition;
        requestLayout();
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState savedState = new SavedState(superState);
        savedState.currentPosition = currentPosition;
        return savedState;
    }

    static class SavedState extends BaseSavedState {
        int currentPosition;

        public SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            currentPosition = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(currentPosition);
        }

        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }


    public void setFadeEnabled(boolean enabled) {
        mFadeEnabled = enabled;
    }

    public boolean getFadeEnabled() {
        return mFadeEnabled;
    }

    public float getCurrentZoomMax() {
        return zoomMax;
    }

    public void setZoomMax(float zoomMax) {
        this.lastZoomMax = zoomMax;
    }

    public float getZoomMax() {
        return lastZoomMax;
    }

    private boolean isSmall(float positionOffset) {
        return Math.abs(positionOffset) < 0.0001;
    }


    protected void animateFadeScale(View left, View right, float positionOffset, int position) {
        if (mState != State.IDLE) {
            if (left != null) {
                float mScale = 1 + zoomMax - zoomMax * positionOffset;
                tabViews.get(position).setTextSize(TypedValue.COMPLEX_UNIT_PX, tabTextSize * mScale);
            }
            if (right != null) {
                float mScale = 1 + zoomMax * positionOffset;
                tabViews.get(position + 1).setTextSize(TypedValue.COMPLEX_UNIT_PX, tabTextSize * mScale);
            }
            requestLayout();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    private OnPagerTitleItemClickListener mOnPagerTitleItemClickListener;

    public interface OnPagerTitleItemClickListener {
        /**
         * @param position position
         */
        void onSingleClickItem(int position);

        /**
         * @param position position
         */
        void onDoubleClickItem(int position);
    }

    /***
     * @param mOnPagerTitleItemClickListener mOnPagerTitleItemClickListener
     */
    public void setOnPagerTitleItemClickListener(OnPagerTitleItemClickListener mOnPagerTitleItemClickListener) {
        this.mOnPagerTitleItemClickListener = mOnPagerTitleItemClickListener;
    }
}
