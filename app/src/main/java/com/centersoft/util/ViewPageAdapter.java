package com.centersoft.util;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

/**
 * Created by liudong on 2018/3/15.
 */

public class ViewPageAdapter extends FragmentPagerAdapter {

    private List<Fragment> lists;
    private String[] mTitles;

    public ViewPageAdapter(FragmentManager fm, List<Fragment> li, String[] mt) {
        super(fm);
        this.lists = li;
        this.mTitles = mt;
    }

    @Override
    public Fragment getItem(int arg0) {
        return lists.get(arg0);
    }

    @Override
    public int getCount() {
        return lists.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if (mTitles.length == 0) {
            return "";
        }
        return mTitles[position];
    }
}
