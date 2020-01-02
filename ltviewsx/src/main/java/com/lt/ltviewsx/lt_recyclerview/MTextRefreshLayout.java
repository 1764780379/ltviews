package com.lt.ltviewsx.lt_recyclerview;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.lt.ltviewsx.R;

import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * 创    建:  lt  2018/5/23--18:42
 * 作    用:  文字的下拉刷新
 * 注意事项:
 */

public class MTextRefreshLayout extends LtRefreshLayout {

    TextView tv;
    TextView tvDate;
    ImageView iv;
    ObjectAnimator oa;
    String date;
    SimpleDateFormat sdf;

    public MTextRefreshLayout(@NonNull Context context) {
        this(context, null);
    }

    public MTextRefreshLayout(@NonNull Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MTextRefreshLayout(@NonNull Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        sdf = new SimpleDateFormat("M-d H:m");
        date = getDate();
    }

    @Override
    protected void onStatus(int status) {
        switch (status) {
            case STATE_REFRESH_DOWN:
                //下拉中
                tv.setText(R.string.down_refresh);
                iv.setVisibility(View.VISIBLE);
                iv.setImageResource(R.drawable.lt_arrow);
                //设置动画时间
                oa = ObjectAnimator.ofFloat(iv, "rotation", iv.getRotation(), 0)
                        .setDuration(animationTime);
                oa.setRepeatCount(0);//设置动画执行的次数
                oa.start();//开始动画
                break;
            case STATE_REFRESH_RELEASE:
                //松开刷新
                tv.setText(R.string.release_refresh_now);
                iv.setVisibility(View.VISIBLE);
                iv.setImageResource(R.drawable.lt_arrow);
                oa = ObjectAnimator.ofFloat(iv, "rotation", iv.getRotation(), 180)
                        .setDuration(animationTime);//设置动画时间
                oa.setRepeatCount(0);//设置动画执行的次数
                oa.start();//开始动画
                break;
            case STATE_REFRESHING:
                //刷新中
                tv.setText(R.string.refresh);
                iv.setVisibility(View.VISIBLE);
                iv.setImageResource(R.drawable.lt_loading);
                oa = ObjectAnimator.ofFloat(iv, "rotation", 0, 360)
                        .setDuration(animationTime << 2);//设置动画时间
                oa.setInterpolator(new LinearInterpolator());
                oa.setRepeatCount(ObjectAnimator.INFINITE);//设置动画执行的次数,这个是无限
                oa.start();//开始动画
                break;
            case STATE_REFRESH_FINISH:
                //刷新完成
                date = setDate();
                tv.setText(R.string.refresh_finish);
                tvDate.setText(getContext().getString(R.string.last_update) + date);
                iv.clearAnimation();
                oa.cancel();
                iv.setVisibility(View.INVISIBLE);
                iv.setImageResource(R.drawable.lt_arrow);
                break;
            case STATE_BACK:
                //刷新头回到重新隐藏了回去,此时重置箭头的动画
                oa = ObjectAnimator.ofFloat(iv, "rotation", iv.getRotation(), 0)
                        .setDuration(animationTime);
                oa.setRepeatCount(0);//设置动画执行的次数
                oa.start();//开始动画
                break;
            default:
                break;
        }
    }

    @Override
    protected void onProgress(float y) {

    }

    @Override
    protected View getRefreshView() {
        View view = View.inflate(getContext(), R.layout.lt_refresh_view, null);
        tv = (TextView) view.findViewById(R.id.tv_lt_refresh);
        tvDate = (TextView) view.findViewById(R.id.tv_lt_date);
        iv = (ImageView) view.findViewById(R.id.iv_lt_refresh);
        tvDate.setText(getContext().getString(R.string.last_update) + date);
        return view;
    }

    String getDate() {
        SharedPreferences preference = getContext().getSharedPreferences("lt_rv",
                Context.MODE_PRIVATE);
        return preference.getString("lt_date", "");
    }

    String setDate() {
        String value = sdf.format(new Date());
        SharedPreferences preference = getContext().getSharedPreferences("lt_rv",
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preference.edit();
        editor.putString("lt_date", value);
        editor.apply();
        return value;
    }
}
