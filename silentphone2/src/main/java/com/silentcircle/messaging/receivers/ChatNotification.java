/*
Copyright (C) 2016, Silent Circle, LLC.  All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Any redistribution, use, or modification is done solely for personal
      benefit and not for any commercial purpose or for monetary gain
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the name Silent Circle nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL SILENT CIRCLE, LLC BE LIABLE FOR ANY
DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package com.silentcircle.messaging.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Log;

import com.silentcircle.common.list.ContactEntry;
import com.silentcircle.contacts.ContactsUtils;
import com.silentcircle.messaging.services.AxoMessaging;
import com.silentcircle.messaging.util.ContactsCache;
import com.silentcircle.messaging.util.ConversationUtils;
import com.silentcircle.messaging.util.Extra;
import com.silentcircle.messaging.util.Notifications;
import com.silentcircle.silentphone2.R;

/**
 * Receiver for chat message updates.
 *
 * This receiver is intended to receive notifications about chat message updates and arrival
 * and show notification, when a new chat message arrives but application is in background.
 */
public class ChatNotification extends BroadcastReceiver implements AxoMessaging.AxoMessagingStateCallback {

    private static final String TAG = ChatNotification.class.getSimpleName();

    private Intent mLastNotificationIntent;
    private Context mContext;

    /*
     * Receive update on chat message and act on it.
     */
    @Override
    public void onReceive(Context context, Intent intent) {

        mLastNotificationIntent = null;
        mContext = null;

        AxoMessaging axoMessaging = AxoMessaging.getInstance(context.getApplicationContext());
        boolean axoRegistered = axoMessaging.isRegistered();
        if (!axoRegistered) {
            Log.d(TAG, "Axolotl not yet registered, wait for it before showing notification.");
            mLastNotificationIntent = intent;
            mContext = context;
            axoMessaging.addStateChangeListener(this);
        }
        else {
            sendMessageNotification(context, intent);
        }
    }

    @Override
    public void axoRegistrationStateChange(boolean registered) {
        Log.d(TAG, "Axolotl state: " + registered + ", intent: " + mLastNotificationIntent);
        if (registered && mLastNotificationIntent != null && mContext != null) {
            sendMessageNotification(mContext, mLastNotificationIntent);

            AxoMessaging axoMessaging = AxoMessaging.getInstance(mContext.getApplicationContext());
            axoMessaging.removeStateChangeListener(this);
            mLastNotificationIntent = null;
            mContext = null;
        }
    }

    private void sendMessageNotification(Context context, Intent intent) {
        String conversationPartnerId = Extra.PARTNER.from(intent);

        // create intent used to launch conversation activity.
        Intent messagingIntent = ContactsUtils.getMessagingIntent(conversationPartnerId, context);

        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher_st);

        /* Show notification.
         *
         * Leave message empty for now. It could be passed in intent from AxoMessaging but
         * that does not seem to be secure.
         */
        Notifications.sendMessageNotification(context, messagingIntent, bitmap);
    }
}
