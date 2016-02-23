package com.nikmoores.android.nearbyblemessaging;

import com.google.android.gms.nearby.messages.Message;
import com.google.gson.Gson;

import java.nio.charset.Charset;

/**
 * Method containing the basic settings for sending and receiving messages. Each message contains a
 * unique instance identifier, a username, timestamp, and message.
 * <p/>
 * Created by Nik on 23/02/2016.
 */
public class NearbyMessage {
    private static final Gson gson = new Gson();

    /**
     * The unique app instance identifier that will be used to tell all the communicating devices
     * apart. It's important as it is used to prevent reading multiple repeat messages (while the
     * sending device broadcasts the message several times). For future releases it can be used to
     * block unwanted users in your chat window and could be used to update attached ListViews to
     * reflect username and user color changes, etc.
     */
    private final String mInstanceId;
    private String mUsername;
    private long mTimestamp;
    private String mMessageBody;

    public NearbyMessage(String instanceId, String username) {
        this.mInstanceId = instanceId;
        this.mUsername = username;
        this.mTimestamp = 0L;
        this.mMessageBody = "";
        // TODO: Could add color, etc.
    }

    protected String getInstanceId() {
        return mInstanceId;
    }

    protected String getUsername() {
        return mUsername;
    }

    protected void setUsername(String username) {
        mUsername = username;
    }

    protected long getTimestamp() {
        return mTimestamp;
    }

    protected void setTimestamp(long timestamp) {
        mTimestamp = timestamp;
    }

    protected String getMessageBody() {
        return mMessageBody;
    }

    protected void setMessageBody(String messageBody) {
        mMessageBody = messageBody;
    }


    /**
     * Converts a NearbyMessage into a Message.
     *
     * @param nearbyMessage The NearbyMessage object to be converted to a Message.
     * @return The constructed Message.
     */
    public static Message getMessage(NearbyMessage nearbyMessage) {
        return new Message(gson.toJson(nearbyMessage).getBytes(Charset.forName("UTF-8")));
    }

    /**
     * Converts a Message into a NearbyMessage.
     *
     * @param message The Message to convert.
     * @return The NearbyMessage object.
     */
    public static NearbyMessage getNearbyMessage(Message message) {
        String messageString = new String(message.getContent()).trim();
        return gson.fromJson(new String((messageString.getBytes(Charset.forName("UTF-8")))),
                NearbyMessage.class);
    }
}
