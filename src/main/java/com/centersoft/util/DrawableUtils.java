package com.centersoft.util;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;

/**
 * Description
 * FileName       DrawableUtils
 * CopyRight      CenterSoft
 * Author         LH
 * Createdate     2016/11/23 上午9:25
 * ------------------------------
 * updateAuthor   <修改人员>
 * updateDate     <修改日期>
 * updateNeedNum  <需求单号>
 * updateContent  <修改内容>
 * ------------------------------
 */
public class DrawableUtils {

//    Random random=new Random();   //创建随机
//    int red = random.nextInt(200)+22;
//    int green = random.nextInt(200)+22;
//    int blue = random.nextInt(200)+22;
//    int color= Color.rgb(red, green, blue);//范围 0-255
//    GradientDrawable createShape = DrawableUtils.createShape(color); // 默认显示的图片
//    StateListDrawable createSelectorDrawable = DrawableUtils.createSelectorDrawable(pressedDrawable, createShape);// 创建状态选择器
//    textView.setBackgroundDrawable(createSelectorDrawable);

    public static GradientDrawable createShape(int color, int radius) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setCornerRadius(UiUtils.dip2px(radius));//设置4个角的弧度
        drawable.setColor(color);// 设置颜色
        drawable.setShape(GradientDrawable.RECTANGLE);
        return drawable;
    }

    public static StateListDrawable createSelectorDrawable(Drawable pressedDrawable, Drawable normalDrawable) {
//		<selector xmlns:android="http://schemas.android.com/apk/res/android"  android:enterFadeDuration="200">
//	    <item  android:state_pressed="true" android:drawable="@drawable/detail_btn_pressed"></item>
//	    <item  android:drawable="@drawable/detail_btn_normal"></item>
//	</selector>
        StateListDrawable stateListDrawable = new StateListDrawable();
        stateListDrawable.addState(new int[]{android.R.attr.state_pressed}, pressedDrawable);// 按下显示的图片
        stateListDrawable.addState(new int[]{}, normalDrawable);// 抬起显示的图片
        return stateListDrawable;
    }
}
