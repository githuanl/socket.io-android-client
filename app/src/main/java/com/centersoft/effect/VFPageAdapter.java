package com.centersoft.effect;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

/**
 * Created by Administrator on 2016/10/17.
 */
public class VFPageAdapter extends FragmentPagerAdapter {

    private List<Fragment> lists;
    private String[] mTitles;

    public VFPageAdapter(FragmentManager fm, List<Fragment> li, String[] mt) {
        super(fm);
        this.lists = li;
        this.mTitles = mt;
    }

    public VFPageAdapter(FragmentManager fm) {
        super(fm);
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

