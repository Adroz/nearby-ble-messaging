package com.nikmoores.android.nearbyblemessaging;

import android.app.FragmentManager;
import android.test.ActivityInstrumentationTestCase2;
import android.test.UiThreadTest;
import android.widget.Button;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

/**
 * Instrumentation tests for the connection, sending and receiving of the Nearby Messages API.
 *
 * Created by Nik on 22/02/2016.
 */
public class ChatFragmentTests extends ActivityInstrumentationTestCase2<MainActivity> {

    public ChatFragmentTests() {
        super(MainActivity.class);
    }

    private ChatFragment chatFragment;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        MainActivity activity = getActivity();
        assertNotNull("Error: Activity doesn't exist", activity);

        FragmentManager fm = getActivity().getFragmentManager();
        chatFragment = (ChatFragment) fm.findFragmentByTag(MainActivity.CHAT_FRAGMENT_TAG);
        assertNotNull("Error: Fragment doesn't exist", chatFragment);
    }


    /**
     * The user should be notified if the GoogleApiClient instance is connected, suspended, or
     * failed to connect.
     */
    @UiThreadTest
    public void testApiConnectionStates() {
        final Button sendButton = (Button) getActivity().findViewById(R.id.send_button);

        // Test for failed connection. For this dummy program, we don't care what the error is, as
        // long as its code is displayed to the user.
        ConnectionResult testResult = new ConnectionResult(ConnectionResult.API_UNAVAILABLE);
        chatFragment.onConnectionFailed(testResult);
        assertFalse("Error: Send button should be disabled if not connected", sendButton.isEnabled());
        assertEquals("Error: Connection error doesn't equal input error",
                testResult.getErrorCode(),
                (int) sendButton.getTag());

        chatFragment.onConnectionSuspended(GoogleApiClient.ConnectionCallbacks.CAUSE_NETWORK_LOST);
        assertFalse("Error: Send button should be disabled if not connected", sendButton.isEnabled());
        assertEquals("Error: Connection error doesn't equal input error",
                GoogleApiClient.ConnectionCallbacks.CAUSE_NETWORK_LOST,
                (int) sendButton.getTag());

        chatFragment.onConnected(null);
        assertTrue("Error: Send button should be enabled when connected", sendButton.isEnabled());
        assertEquals("Error: Connection result should be SUCCESS (0)",
                ConnectionResult.SUCCESS,
                (int) sendButton.getTag());

    }
}