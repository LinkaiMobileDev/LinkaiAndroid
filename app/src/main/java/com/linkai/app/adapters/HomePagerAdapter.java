package com.linkai.app.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.linkai.app.Fragments.ChatFragment;
import com.linkai.app.Fragments.ContactsFragment;
import com.linkai.app.Fragments.MyLinkaiFragment;
import com.linkai.app.R;
import com.linkai.app.libraries.Const;

/**
 * Created by LP1001 on 07-07-2016.
 */
public class HomePagerAdapter extends FragmentPagerAdapter {
    Fragment chatFragment,contactsFragment,myLinkaiFragment;
    public HomePagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                contactsFragment=new ContactsFragment();
                return contactsFragment;
            case 1:
                chatFragment=new ChatFragment();
                return chatFragment;
            case 2:
                myLinkaiFragment=new MyLinkaiFragment();
                return myLinkaiFragment;
            default://returns chat fragment
                chatFragment=new ChatFragment();
                return chatFragment;
        }
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return Const.CONTEXT.getResources().getString(R.string.home_pager_tab_contacts);
            case 1:
                return Const.CONTEXT.getResources().getString(R.string.home_pager_tab_chats);
            case 2:
                return Const.CONTEXT.getResources().getString(R.string.home_pager_tab_mylinkai);
        }
        return null;
    }


}
