package com.yeuyt.tagsview;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * 标签列表控件
 */
public class TagsView extends ViewGroup implements View.OnClickListener {

    private int marginX;//标签水平间距
    private int marginY;//标签竖直间距

    //标签textview的属性
    private int tagsTextPaddingX;
    private int tagsTextPaddingY;
    private float tagsTextSize;
    //ColorStateList能够把xml中定义的颜色应用到view上，主要是文字颜色selector
    private ColorStateList tagsTextColor;
    private int tagsTextBackground;

    //保存childView的位置
    private static final int TAG_POSITION = R.id.tag_position;

    //点击监听
    private OnChildClickListener mChildClickListener;
    //是否可以选中子view，默认不可选中
    private boolean selected = false;


    public TagsView(Context context) {
        super(context);
        init(context, null);
    }

    public TagsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public TagsView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private  void init(Context context, AttributeSet set) {
        TypedArray array = context.obtainStyledAttributes(set, R.styleable.TagsViewGroup);
        if (array != null) {
            marginX = array.getDimensionPixelOffset(R.styleable.TagsViewGroup_marginX, 10);
            marginY = array.getDimensionPixelOffset(R.styleable.TagsViewGroup_marginY, 10);

            tagsTextPaddingX = array.getDimensionPixelOffset(R.styleable.TagsViewGroup_tagsTextPaddingX, 0);
            tagsTextPaddingY = array.getDimensionPixelOffset(R.styleable.TagsViewGroup_tagsTextPaddingY, 0);

            tagsTextSize = array.getDimension(R.styleable.TagsViewGroup_tagsTextSize, 18);//默认18sp
            tagsTextColor = array.getColorStateList(R.styleable.TagsViewGroup_tagsTextColor);
            tagsTextBackground = array.getResourceId(R.styleable.TagsViewGroup_tagsTextBackground, 0);
            array.recycle();
        }

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // 获得它的父容器为它设置的测量模式和大小
        int measureWidth = MeasureSpec.getSize(widthMeasureSpec);
        int measureHeight = MeasureSpec.getSize(heightMeasureSpec);
        //int modeWidth = MeasureSpec.getMode(widthMeasureSpec);
        //   int modeHeight = MeasureSpec.getMode(heightMeasureSpec);

        int count = getChildCount();

        int sumHeight = getPaddingTop();//测量之后的总高度，初始高度考虑了paddingTop
        int currentWidth = getPaddingLeft();//当前行的目前宽度,考虑了paddingLeft
        int maxWidth = 0;//所有行中的最宽的那行
        int leftLocation = 0;//当前子view的左边位置
        for(int i=0; i<count; i++) {
            View child = getChildAt(i);

            // 测量每一个child的宽和高
            measureChild(child, widthMeasureSpec, heightMeasureSpec);
            int childWidth = child.getMeasuredWidth();
            int childHeight = child.getMeasuredHeight();

            //不换行执行的操作
            if(currentWidth + childWidth <= measureWidth-getPaddingRight()) {
                leftLocation = currentWidth;
                currentWidth += childWidth+marginX;
                maxWidth = Math.max(maxWidth, currentWidth);//更新maxWidth
            } else {//如果加入当前child，超出最大宽度，开启新行
                currentWidth = getPaddingLeft();//当前行的宽度置为初始值
                sumHeight += childHeight+marginY;//高度叠加
            }
            //输入的left, top, right, bottom 一定要与Location的构造参数对应
            child.setTag(new Location(leftLocation, currentWidth-marginY, sumHeight, sumHeight+childHeight));

            if(i == count -1) {
                //初始sumHeight并没有考虑第一行的高度，所以最后要再叠加一次
                sumHeight += childHeight;
                //考虑paddingBottom
                sumHeight += getPaddingBottom();

                //考虑paddingRight,并减掉多余的一个marginX
                maxWidth += getPaddingRight()-marginX;
            }
        }
        measureWidth = resolveSize(maxWidth, measureWidth);
        measureHeight = resolveSize(sumHeight, measureHeight);
        setMeasuredDimension(measureWidth, measureHeight);

//        setMeasuredDimension((modeWidth == MeasureSpec.EXACTLY) ? measureWidth : maxWidth,
//                (modeHeight == MeasureSpec.EXACTLY) ? measureHeight : sumHeight);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int count = getChildCount();
        for(int i=0; i<count; i++) {
            View child = getChildAt(i);

            Location location = (Location)child.getTag();
            child.layout(location.left, location.top, location.right, location.bottom);
        }
    }
    public void setTags(String[] strings) {
        setTags(strings, false);
    }

    public void setTags(String[] strings, boolean selected) {
        this.selected = selected;
        for(int i=0; i<strings.length; i++) {
            TextView textView = new TextView(getContext());
            textView.setText(strings[i]);
            textView.setPadding(tagsTextPaddingX, tagsTextPaddingY, tagsTextPaddingX, tagsTextPaddingY);
            textView.setTextSize(tagsTextSize);

            //默认未选中灰色，选中为绿色
            textView.setTextColor(tagsTextColor!=null ? tagsTextColor :
                getResources().getColorStateList(R.color.selector_tags_text_color, null));

            textView.setBackgroundResource(tagsTextBackground!=0 ? tagsTextBackground :
                R.drawable.selector_tags_text_background);//默认背景

            //android要求key必须为resource id
            textView.setTag(TAG_POSITION, i);
            textView.setOnClickListener(this);

            addView(textView);
        }
    }
    public void clearAllTags() {
        removeAllViews();
    }

    @Override
    public void onClick(View v) {
        if (selected)
            v.setSelected(!v.isSelected());
        if (mChildClickListener != null) {
            mChildClickListener.onChildClick(v, (int)v.getTag(TAG_POSITION));
        }
    }

    /**
     * 设置点击监听
     */
    public void setOnChildClickListener(OnChildClickListener listener) {
        mChildClickListener = listener;
    }

    public interface OnChildClickListener {
        void onChildClick(View view, int position);
    }



    private class Location {
        int left;
        int right;
        int top;
        int bottom;

        public Location(int left, int right, int top, int bottom) {
            this.left = left;
            this.right = right;
            this.top = top;
            this.bottom = bottom;
        }
    }
}
