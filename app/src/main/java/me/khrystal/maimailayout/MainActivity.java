package me.khrystal.maimailayout;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import me.khrystal.widget.tablayout.MaiMaiTabLayout;

public class MainActivity extends AppCompatActivity implements AppBarLayout.OnOffsetChangedListener {

    private int[] colors = {
            Color.BLUE, Color.RED, Color.GREEN
    };

    AppBarLayout appBarLayout;
    private MaiMaiTabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SamplePagerAdapter vpa = new SamplePagerAdapter(new ArrayList<String>() {
            {
                this.add("好友");
                this.add("关注");
                this.add("职言");
            }
        });

        ViewPager pager = findViewById(R.id.viewpager);
        appBarLayout = findViewById(R.id.appbarLayout);
        appBarLayout.setOrientation(LinearLayout.VERTICAL);
        pager.setAdapter(vpa);
        tabLayout = findViewById(R.id.tabLayout);
        tabLayout.setViewPager(pager);
    }

    @Override
    protected void onResume() {
        super.onResume();
        appBarLayout.addOnOffsetChangedListener(MainActivity.this);
    }

    @Override
    public void onPause() {
        super.onPause();
        appBarLayout.removeOnOffsetChangedListener(MainActivity.this);
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int offset) {
        // 在 xml 中设置了最大偏移量是15dp(xhdpi 45px) tabLayout缩放比例最大为getZoomMax 0.4
        // 则 zoom / offset = 0.4 / 45px
        float offsetMax;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            offsetMax = tabLayout.getHeight() - tabLayout.getMinimumHeight();
        } else {
            offsetMax = dip2px(MainActivity.this, 15);
        }
        float zoom = Math.abs(offset) * tabLayout.getZoomMax() / offsetMax;
        tabLayout.updateSelectTabScale(zoom);
    }

    class SamplePagerAdapter extends PagerAdapter {

        private List<String> titles;

        public SamplePagerAdapter(List<String> titles) {
            this.titles = titles;
        }

        @Override
        public int getCount() {
            return titles.size();
        }

        @Override
        public int getItemPosition(@NonNull Object object) {
            View view = (View) object;
            return (int) view.getTag();
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return titles.get(position);
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
            return view == o;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            NestedScrollView scrollView = new NestedScrollView(MainActivity.this);
            TextView textView = new TextView(MainActivity.this);
            textView.setText(getPageTitle(position));
            textView.setGravity(Gravity.CENTER);
            textView.setTextSize(22);
            textView.setTextColor(Color.parseColor("#ffffff"));
            scrollView.setBackgroundColor(colors[position]);
            scrollView.setTag(position);
            scrollView.addView(textView);
            container.addView(scrollView);
            return scrollView;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView((View) object);
        }
    }

    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}
