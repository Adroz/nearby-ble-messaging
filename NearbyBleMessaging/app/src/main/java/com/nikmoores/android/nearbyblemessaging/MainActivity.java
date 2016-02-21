package com.nikmoores.android.nearbyblemessaging;

import android.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {
    private static final String CHAT_FRAGMENT_TAG = "chat_fragment_tag";

    private ChatFragment mChatFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FragmentManager fm = getFragmentManager();
        mChatFragment = (ChatFragment) fm.findFragmentByTag(CHAT_FRAGMENT_TAG);

        if (mChatFragment == null) {
            mChatFragment = new ChatFragment();
            fm.beginTransaction().add(R.id.container, mChatFragment, CHAT_FRAGMENT_TAG).commit();
        }
    }
}
