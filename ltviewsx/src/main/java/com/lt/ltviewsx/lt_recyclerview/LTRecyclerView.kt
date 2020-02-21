package com.lt.ltviewsx.lt_recyclerview

import android.content.Context
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lt.ltviewsx.R
import com.lt.ltviewsx.lt_listener.OnNoItemListener
import com.lt.ltviewsx.lt_listener.OnUpAndDownListener
import com.lt.ltviewsx.utils.nullSize
import java.lang.reflect.InvocationTargetException

/**
 * 所在包名:  com.lt.ltrecyclerview
 * 创建日期:  2017/4/6--17:10
 * 作   用:   以RecyclerView为基础,可以上拉加载和下拉刷新
 * 使用方法:
 * 注意事项:
 */
open class LTRecyclerView
@JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : FrameLayout(context, attrs, defStyleAttr) {

    /**
     * 获得自定义控件中的RefreshLayout
     */
    val refreshLayout: BaseRefreshLayout
    /**
     * 获取到自定义控件中包含的RecyclerView
     */
    val recyclerView = RecyclerView(context)
    /**
     * 获取适配器对象
     */
    var adapter: RecyclerView.Adapter<*>? = null
        private set
    /**
     * 获取线性多列布局的管理者
     */
    val layoutManager = GridLayoutManager(context, 1)
    /**
     * 获取没有条目时展示的View
     */
    var noItemView: View? = null
        private set
    private var onUpAndDownListener: OnUpAndDownListener? = null

    init {
        //添加下拉刷新
        refreshLayout = try {
            thisRefreshLayout()
        } catch (e: Exception) {
            throw RuntimeException("请将刷新的类继承ViewGroup类或子类并实现BaseRefreshLayout接口")
        }
        val lp = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        recyclerView.layoutParams = lp
        refreshLayout.setLayoutParams(lp)
        refreshLayout.setOnRefreshListener {
            //刷新时,添加下拉刷新回调
            onUpAndDownListener?.down()
        }
        refreshLayout.addView(recyclerView)
        this.addView(refreshLayout as ViewGroup)
        //设置自定义属性
        val a = context.obtainStyledAttributes(attrs, R.styleable.LTRecyclerView)
        //没有条目时的文字
        val noItemText = a.getString(R.styleable.LTRecyclerView_noItemText)
        if (!TextUtils.isEmpty(noItemText)) {
            setNoItemText(noItemText)
        }
        //没有条目时的布局或图片,或字符串,或颜色...
        val noItemViewId = a.getResourceId(R.styleable.LTRecyclerView_noItemView, 0)
        if (noItemViewId > 0) {
            when (context.resources.getResourceTypeName(noItemViewId)) {
                "layout" -> setNoItemView(View.inflate(context, noItemViewId, null))
                "mipmap", "drawable" -> {
                    val noItemView = ImageView(context)
                    noItemView.setImageResource(noItemViewId)
                    setNoItemView(noItemView)
                }
                "string" -> setNoItemText(resources.getString(noItemViewId))
                "color" -> {
                    val noItemViewIv = ImageView(context)
                    noItemViewIv.setBackgroundResource(noItemViewId)
                    setNoItemView(noItemViewIv, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
                }
            }
        }
        //分割线高度
        val dHeight = a.getDimension(R.styleable.LTRecyclerView_dividerHeight, 0f)
        //分割线颜色
        val dColor = a.getColor(R.styleable.LTRecyclerView_dividerColor, -0x2a2a2b)
        if (dHeight != 0f) {
            addItemDecoration_line(dHeight.toInt(), dColor)
        }
        //分割线图片
        val drawable = a.getDrawable(R.styleable.LTRecyclerView_dividerDrawable)
        drawable?.let { addItemDecoration_drawable(it) }
        a.recycle() //使用完进行回收
        layoutManager.orientation = LinearLayoutManager.VERTICAL //设置为竖直的 GridView
        recyclerView.layoutManager = layoutManager //ViewGroup 管理者设置给 view
        //添加上拉加载,这个是滚动的监听
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    onUpAndDownListener ?: return //没有回调就没必要走
                    if (refreshLayout.isRefreshing) return
                    adapter ?: return  //如果没有适配器
                    if (adapter is LtAdapter<*> && !(adapter as LtAdapter<*>).refreshViewIsHaveData)//如果已标记为没数据
                        return
                    if (layoutManager.findLastVisibleItemPosition() + 1 == adapter!!.itemCount) { //如果是最后一个条目,表示是上拉加载
                        onUpAndDownListener?.up()
                    }
                }
            }
        })
    }

    /**
     * 类内部获取下拉刷新的View,可以继承并重写该方法来实现项目内不同的下拉刷新效果
     */
    @Throws(NoSuchMethodException::class, IllegalAccessException::class, InvocationTargetException::class, InstantiationException::class)
    protected open fun thisRefreshLayout(): BaseRefreshLayout {
        return when (val clazz = LtRecyclerViewManager.refreshLayoutClazz) {
            MSwipeRefreshLayout::class.java -> MSwipeRefreshLayout(context)
            MTextRefreshLayout::class.java -> MTextRefreshLayout(context)
            else -> clazz!!.getConstructor(Context::class.java).newInstance(context) as BaseRefreshLayout
        }
    }

    /**
     * 设置一行展示多少列
     */
    fun setSpanCount(spanCount: Int): LTRecyclerView {
        layoutManager.spanCount = spanCount
        return this
    }

    /**
     * 添加上拉和下拉的回调接口
     *
     * @param onUpAndDownListener 回调接口
     */
    fun setOnUpAndDownListener(onUpAndDownListener: OnUpAndDownListener?): LTRecyclerView {
        this.onUpAndDownListener = onUpAndDownListener
        return this
    }

    /**
     * 设置RecyclerView的适配器
     *
     * @param adapter 继承自RecyclerView.Adapter的适配器
     */
    fun setAdapter(adapter: RecyclerView.Adapter<*>): LTRecyclerView {
        this.adapter = adapter
        if (adapter is LtAdapter<*>)
            adapter.addOnNoItemListener(object : OnNoItemListener {
                override fun noItem() { //没有条目时隐藏rl,然后展示没条目时的布局
                    recyclerView.visibility = View.INVISIBLE
                    noItemView?.visibility = View.VISIBLE
                }

                override fun haveItem() { //有条目了就显示rv,并且隐藏noItemView
                    recyclerView.visibility = View.VISIBLE
                    noItemView?.visibility = View.GONE
                }
            })
        recyclerView.adapter = adapter
        if (adapter is LtAdapter<*>)
            if (adapter.getLtItemCount() == 0
                    && (!adapter.headersIsItem || adapter.headList.nullSize() == 0)
                    && (!adapter.tailsIsItem || adapter.tailList.nullSize() == 0))
                recyclerView.visibility = View.INVISIBLE
        return this
    }

    /**
     * 设置没有条目时展示的View,默认是居中的
     */
    fun setNoItemView(view: View): LTRecyclerView {
        val lp = LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        lp.gravity = Gravity.CENTER
        return setNoItemView(view, lp)
    }

    fun setNoItemView(view: View, layoutParams: ViewGroup.LayoutParams): LTRecyclerView {
        if (noItemView != null) removeView(noItemView)
        noItemView = view
        view.layoutParams = layoutParams
        if (adapter == null) {
            view.visibility = View.VISIBLE
        } else {
            if (adapter is LtAdapter<*> && (adapter as LtAdapter<*>).getLtItemCount() == 0) {
                view.visibility = View.VISIBLE
            } else if (adapter?.itemCount == 0) {
                view.visibility = View.VISIBLE
            } else {
                view.visibility = View.INVISIBLE
            }
        }
        this.addView(view)
        return this
    }

    fun setNoItemText(text: String): LTRecyclerView {
        val tv = TextView(context)
        tv.setTextColor(LtRecyclerViewManager.noItemTextColor)
        tv.text = text
        return setNoItemView(tv)
    }

    /**
     * 刷新rv的布局
     */
    fun notifyDataSetChanged(): LTRecyclerView {
        adapter?.notifyDataSetChanged()
        return this
    }

    /**
     * 判断适配器是否为空
     */
    fun adapterIsNull(): Boolean = adapter == null

    /**
     * 设置是否上拉加载
     */
    fun setBottomRefresh(b: Boolean): LTRecyclerView {
        (adapter as? LtAdapter<*>)?.setRefresh(b)
        return this
    }

    /**
     * 设置是否下拉刷新
     */
    fun setTopRefresh(b: Boolean): LTRecyclerView {
        refreshLayout.isRefreshing = b
        return this
    }

    /**
     * 设置是否上拉加载(底部)和下拉刷新(顶部)
     */
    fun setRefresh(top: Boolean, bottom: Boolean): LTRecyclerView {
        return setTopRefresh(top).setBottomRefresh(bottom)
    }

    /**
     * 添加分割线
     */
    @JvmOverloads
    fun addItemDecoration_line(px: Int = 2, color: Int = -0x2a2a2b): LTRecyclerView {
        recyclerView.addItemDecoration(LtDivider(recyclerView, px, color))
        return this
    }

    fun addItemDecoration_drawable(resId: Int): LTRecyclerView {
        return addItemDecoration_drawable(ContextCompat.getDrawable(context, resId)!!)
    }

    fun addItemDecoration_drawable(drawable: Drawable): LTRecyclerView {
        recyclerView.addItemDecoration(LtDivider(recyclerView, drawable))
        return this
    }
}