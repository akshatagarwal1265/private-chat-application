package com.akshatagarwal1265.test1;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by Akshat on 15-09-2017.
 */

class SectionsPagerAdapter extends FragmentPagerAdapter
{
    public SectionsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {

        switch(position)
        {
            case 0:
                LoginChatFragment loginChatFragment = new LoginChatFragment();
                return loginChatFragment;
            case 1:
                SignupChatFragment signupChatFragment = new SignupChatFragment();
                return signupChatFragment;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {

        switch(position)
        {
            case 0:
                return "Enter Duo";
            case 1:
                return "Make Duo";
            default:
                return null;
        }
    }
}