package com.nikmoores.android.nearbyblemessaging;

import android.app.FragmentManager;
import android.test.ActivityInstrumentationTestCase2;
import android.test.UiThreadTest;
import android.widget.Button;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

/**
 * Instrumentation tests for the connection, sending and receiving of the Nearby Messages API.
 * <p/>
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
                chatFragment.getConnectionStatus());

        chatFragment.onConnectionSuspended(GoogleApiClient.ConnectionCallbacks.CAUSE_NETWORK_LOST);
        assertFalse("Error: Send button should be disabled if not connected", sendButton.isEnabled());
        assertEquals("Error: Connection error doesn't equal input error",
                GoogleApiClient.ConnectionCallbacks.CAUSE_NETWORK_LOST,
                chatFragment.getConnectionStatus());

        chatFragment.onConnected(null);
        assertTrue("Error: Send button should be enabled when connected", sendButton.isEnabled());
        assertEquals("Error: Connection result should be SUCCESS (0)",
                ConnectionResult.SUCCESS,
                chatFragment.getConnectionStatus());

    }

    /**
     * Tests that the NearbyMessage is implemented properly. Check for setting and getting correctly
     * and for translating between NearMessages and Messages.
     */
    public void testNearbyMessageImplementation() {
        NearbyMessage nearbyMessage1 = new NearbyMessage("123", "Nik", 1234567L, "Message Body!");

        // Test InstanceID in == InstanceID out
        assertEquals("Error: the InstanceID returned was not as expected",
                "123", nearbyMessage1.getInstanceId());

        // Test that the Username/Timestamp/MessageBody can be altered
        assertEquals("Error: the username returned was not as expected",
                "Nik", nearbyMessage1.getUsername());
        nearbyMessage1.setUsername("Ben");
        assertEquals("Error: the username was not altered was not as expected",
                "Ben", nearbyMessage1.getUsername());

        // Test that NearbyMessage->Message->NearbyMessage is as expected.
        NearbyMessage nearbyMessage2 = NearbyMessage.getNearbyMessage(
                NearbyMessage.getMessage(nearbyMessage1));

        assertEquals("Error: InstanceID not converted correctly",
                nearbyMessage1.getInstanceId(), nearbyMessage2.getInstanceId());
        assertEquals("Error: Username not converted correctly",
                nearbyMessage1.getUsername(), nearbyMessage2.getUsername());
        assertEquals("Error: Timestamp not converted correctly",
                nearbyMessage1.getTimestamp(), nearbyMessage2.getTimestamp());
        assertEquals("Error: Message not converted correctly",
                nearbyMessage1.getMessageBody(), nearbyMessage2.getMessageBody());
    }
}
