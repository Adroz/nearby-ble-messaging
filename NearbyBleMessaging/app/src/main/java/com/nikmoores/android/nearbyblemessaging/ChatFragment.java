package com.nikmoores.android.nearbyblemessaging;

import android.app.Fragment;
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

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.nearby.Nearby;

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

        final ListView nearbyDevicesListView = (ListView) rootView.findViewById(
                R.id.message_list_view);
        mNearbyDevicesArrayAdapter = new ArrayAdapter<>(getActivity().getApplicationContext(),
                android.R.layout.simple_list_item_1,
                mNearbyDevicesArrayList);
        nearbyDevicesListView.setAdapter(mNearbyDevicesArrayAdapter);

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
        }
        super.onStop();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(LOG_TAG, "GoogleApiClient connected");
        mSendButton.setEnabled(true);
        // Set the view's tag as a quick and dirty way of testing what happens with each connection state.
        mSendButton.setTag(ConnectionResult.SUCCESS);
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
}
