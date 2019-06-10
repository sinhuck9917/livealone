package com.example.livealone4.Adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.example.livealone4.Fragments.HiringFragment;
import com.example.livealone4.Fragments.LiveAloneFragment;
import com.example.livealone4.Fragments.MyPageFragment;


public class ViewPagerAdapter extends FragmentStatePagerAdapter {

    HiringFragment hiringFragment;
    LiveAloneFragment liveAloneFragment;
    MyPageFragment myPageFragment;
    Context context;

    public ViewPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        hiringFragment = new HiringFragment();
        liveAloneFragment = new LiveAloneFragment();
        myPageFragment = new MyPageFragment();
        this.context = context;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){

            case 0 :
                return hiringFragment;
            case 1 :
                return liveAloneFragment;
            case 2 :
                return myPageFragment;
            default:
                return null;

        }
    }



    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    @Override
    public int getCount() {
        return 3;
    }
}
