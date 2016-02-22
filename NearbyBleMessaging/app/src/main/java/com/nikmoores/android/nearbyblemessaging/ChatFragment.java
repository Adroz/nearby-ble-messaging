package com.nikmoores.android.nearbyblemessaging;

import android.app.Fragment;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.messages.Message;
import com.google.android.gms.nearby.messages.MessageListener;
import com.google.android.gms.nearby.messages.NearbyMessagesStatusCodes;
import com.google.android.gms.nearby.messages.PublishCallback;
import com.google.android.gms.nearby.messages.PublishOptions;
import com.google.android.gms.nearby.messages.Strategy;
import com.google.android.gms.nearby.messages.SubscribeCallback;
import com.google.android.gms.nearby.messages.SubscribeOptions;

import java.util.ArrayList;

/**
 * Fragment that allows for user to transmit messages to nearby devices, and to also receive
 * messages from them.
 * <p/>
 * Created by Nik on 21/02/2016.
 */
public class ChatFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String LOG_TAG = ChatFragment.class.getSimpleName();

    // Views.
    private EditText mMessageText;
    private Button mSendButton;

    private Message mMessagePacket;

    /**
     * A {@link MessageListener} to process incoming messages.
     */
    private MessageListener mMessageListener;

    /**
     * The string containing any unsent message. The message will be sent after the user agrees to
     * the Nearby permission.
     */
    private String mUnsentMessage;

    /**
     * Flag for resolving Nearby permission opt-in error. Used to prevent duplicate permission
     * dialog.
     */
    private boolean mResolvingNearbyError = false;

    // Messages list adapter and array
    private ArrayAdapter<String> mNearbyDevicesArrayAdapter;
    private final ArrayList<String> mNearbyDevicesArrayList = new ArrayList<>();

    private GoogleApiClient mGoogleApiClient;   // Google play services - for Nearby API

    public ChatFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_chat, container, false);

        mMessageText = (EditText) rootView.findViewById(R.id.message_edit_text);
        mSendButton = (Button) rootView.findViewById(R.id.send_button);
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mMessageText.getText().toString().length() != 0){
                    mSendButton.setEnabled(false);
                    publish(mMessageText.getText().toString());
                }
            }
        });

        final ListView nearbyDevicesListView = (ListView) rootView.findViewById(
                R.id.message_list_view);
        mNearbyDevicesArrayAdapter = new ArrayAdapter<>(getActivity().getApplicationContext(),
                android.R.layout.simple_list_item_1,
                mNearbyDevicesArrayList);
        nearbyDevicesListView.setAdapter(mNearbyDevicesArrayAdapter);

        setupMessageListener();
        mMessagePacket = new Message(new byte[0]);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();

        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addApi(Nearby.MESSAGES_API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onStop() {
        if (mGoogleApiClient.isConnected() && !getActivity().isChangingConfigurations()) {
            mGoogleApiClient.disconnect();

            unsubscribe();
            unpublish();
        }
        super.onStop();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(LOG_TAG, "GoogleApiClient connected");
        mSendButton.setEnabled(true);
        // Set the view's tag as a quick and dirty way of testing what happens with each connection state.
        mSendButton.setTag(ConnectionResult.SUCCESS);
        subscribe();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(LOG_TAG, "GoogleApiClient connection suspended, error: " + i);
        // Disable any UI elements that rely on the API until it is reconnected. In this point,
        // there is nothing to disable.
        mSendButton.setEnabled(false);
        mSendButton.setTag(i);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i(LOG_TAG, "GoogleApiClient connection failed.");
        // Not handled as of yet. GoogleApiClient onConnectionFailed guideline found at:
        // https://developers.google.com/android/guides/api-client#handle_connection_failures
        mSendButton.setEnabled(false);
        mSendButton.setTag(connectionResult.getErrorCode());
    }

    /**
     * Attempts to publish message to nearby BLE devices.
     * Publishes device information to nearby devices. If not successful, attempts to resolve any
     * error related to Nearby permissions by displaying an opt-in dialog. Registers a callback
     * that updates the UI when the publication expires.
     */
    private void publish(String messageToSend) {
        // Unpublish existing
        unpublish();

        mUnsentMessage = messageToSend;

        // Cannot proceed without a connected GoogleApiClient.
        if (connectToApiClient()) {

            PublishOptions options = new PublishOptions.Builder()
                    .setStrategy(Strategy.DEFAULT)
                    .setCallback(new PublishCallback() {
                        @Override
                        public void onExpired() {
                            super.onExpired();
                            Log.i(LOG_TAG, "No longer publishing");
                        }
                    }).build();

            // Let the user know that their message has been shortened if it's too long.
            if (messageToSend.length() >= Message.MAX_CONTENT_SIZE_BYTES) {
                Toast.makeText(getActivity(), "Your message was too long and has been truncated.",
                        Toast.LENGTH_SHORT).show();
                messageToSend = messageToSend.substring(0, Message.MAX_CONTENT_SIZE_BYTES);
            }

            // Start with anonymous username. TODO: Add ability to change display name
            final String finalMessage = "Anonymous: " + messageToSend;
            mMessagePacket = new Message(finalMessage.getBytes());

            Nearby.Messages.publish(mGoogleApiClient, mMessagePacket, options)
                    .setResultCallback(new ResultCallback<Status>() {

                        @Override
                        public void onResult(@NonNull Status status) {
                            if (status.isSuccess()) {
                                mNearbyDevicesArrayAdapter.add(finalMessage);
                                mMessageText.setText("");
                                mUnsentMessage = "";
                            } else {
                                handleNearbyError(status);
                            }

                            mSendButton.setEnabled(true);
                        }
                    });
        }
    }

    /**
     * Method attempts to stop publishing message data (if running).device information to nearby devices. If successful, resets state. If not
     * successful, attempts to resolve any error related to Nearby permissions by displaying an
     * opt-in dialog.
     */
    private void unpublish() {
        // Cannot proceed without a connected GoogleApiClient. Reconnect and execute the pending
        // task in onConnected().
        if (connectToApiClient()) {
            // Use last message packet, as required by the Nearby API, to unpublish.
            // There are no issues if unpublish was to fail, so don't listen.
            Nearby.Messages.unpublish(mGoogleApiClient, mMessagePacket);
        }
    }

    public void publishExisting() {
        if (!mUnsentMessage.equals("")) {
            publish(mUnsentMessage);
        }
    }

    /**
     * Subscribe to messages from nearby BLE devices. If unsuccessful due to Nearby permissions not
     * agreed to, display dialog.
     */
    private void subscribe() {
        // Cannot proceed without a connected GoogleApiClient. Reconnect and execute the pending
        // task in onConnected().
        if (connectToApiClient()) {
            SubscribeOptions options = new SubscribeOptions.Builder()
                    .setStrategy(Strategy.DEFAULT) // Only use BLE.
                    .setCallback(new SubscribeCallback() {
                        @Override
                        public void onExpired() {
                            super.onExpired();
                            // For now, do nothing - will want to be notified so we can sub again
                            Log.i(LOG_TAG, "No longer subscribing");
                        }
                    }).build();

            Nearby.Messages.subscribe(mGoogleApiClient, mMessageListener, options)
                    .setResultCallback(new ResultCallback<Status>() {

                        @Override
                        public void onResult(@NonNull Status status) {
                            if (!status.isSuccess()) {
                                handleNearbyError(status);
                            }
                        }
                    });
        }
    }

    /**
     * Method ends subscription to incoming messages.
     */
    private void unsubscribe() {
        // Cannot proceed without a connected GoogleApiClient. Reconnect and execute the pending
        // task in onConnected().
        if (connectToApiClient()) {
            Nearby.Messages.unsubscribe(mGoogleApiClient, mMessageListener)
                    .setResultCallback(new ResultCallback<Status>() {

                        @Override
                        public void onResult(Status status) {
                            if (!status.isSuccess()) {
                                handleNearbyError(status);
                            }
                        }
                    });
        }
    }

    private void setupMessageListener() {
        mMessageListener = new MessageListener() {
            @Override
            public void onFound(Message message) {
                // There are two main ways of handling sending/receiving messages:
                // 1) Match a timestamp so that repeated messages are ignored (but allowing for
                //    newly connected devices to receive the messages.
                // 2) Alter the sending service to only broadcast for a short interval (but that
                //    might mean that other devices miss the message altogether).

                // Basic way of checking if the message received is a duplicate. Not advised for
                // when three people are talking, as it could result in a loop of repeated incoming
                // messages.
                // TODO: add timestamps, and timestamp checks.
                String messageIn = new String(message.getContent());
                int arrayCount = mNearbyDevicesArrayAdapter.getCount();
                if (arrayCount > 0) {
                    if (mNearbyDevicesArrayAdapter.getItem(arrayCount - 1).equals(messageIn)) {
                        return;
                    }
                }
                mNearbyDevicesArrayAdapter.add(new String(message.getContent()));
            }
        };
    }

    public boolean connectToApiClient() {
        if (!mGoogleApiClient.isConnected()) {
            if (!mGoogleApiClient.isConnecting()) {
                mGoogleApiClient.connect();
                return false;
            }
        }
        return true;
    }

    protected void setResolvingNearbyError(boolean state) {
        mResolvingNearbyError = state;
    }

    /**
     * Handles errors associated with Nearby permission requests.
     *
     * @param status The error status.
     */
    private void handleNearbyError(Status status) {
        if (status.getStatusCode() == NearbyMessagesStatusCodes.APP_NOT_OPTED_IN) {
            if (!mResolvingNearbyError) {
                try {
                    mResolvingNearbyError = true;
                    status.startResolutionForResult(getActivity(), MainActivity.RESOLVE_ERROR_REQUEST);

                } catch (IntentSender.SendIntentException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
