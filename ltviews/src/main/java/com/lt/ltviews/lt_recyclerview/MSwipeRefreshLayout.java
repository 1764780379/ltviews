package com.lt.ltviews.lt_recyclerview;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;

import com.lt.ltviews.R;

/**
 * 创    建:  lt  2018/5/23--17:52
 * 作    用:  兼容的下拉刷新控件
 * 注意事项:
 */

public class MSwipeRefreshLayout extends SwipeRefreshLayout implements BaseRefreshLayout {

    public MSwipeRefreshLayout(@NonNull Context context) {
        this(context, null);
    }

    public MSwipeRefreshLayout(@NonNull Context context, AttributeSet attrs) {
        super(context, attrs);
        setColorSchemeResources(R.color.colorAccent);
    }
}
