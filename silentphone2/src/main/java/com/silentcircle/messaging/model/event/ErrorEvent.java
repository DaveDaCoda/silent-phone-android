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
package com.silentcircle.messaging.model.event;

import com.silentcircle.messaging.model.MessageErrorCodes;
import com.silentcircle.messaging.util.IOUtils;

public class ErrorEvent extends Event {
    @MessageErrorCodes.MessageErrorCode private int error;

    private String mSender;
    private String mDeviceId;
    private String mMessageId;
    private String mSendToDevId;
    private boolean mDuplicate;
    private byte[] mMessageText;

    public ErrorEvent() {
        this(MessageErrorCodes.SUCCESS);
    }

    public ErrorEvent(@MessageErrorCodes.MessageErrorCode int errorCode) {
        error = errorCode;
    }

    public void clear() {
        super.clear();
        removeError();
    }

    @MessageErrorCodes.MessageErrorCode public int getError() {
        return this.error;
    }

    public void removeError() {
        this.error = MessageErrorCodes.SUCCESS;
    }

    public void setError(@MessageErrorCodes.MessageErrorCode int errorCode) {
        this.error = errorCode;
    }

    public void setSender(final String sender) {
        mSender = sender;
    }

    public void setDeviceId(final String deviceId) {
        mDeviceId = deviceId;
    }

    public String getSender() {
        return mSender;
    }

    public String getDeviceId() {
        return mDeviceId;
    }

    public void setMessageId(String messageId) {
        mMessageId = messageId;
    }

    public String getMessageId() {
        return mMessageId;
    }

    public void setMessageText(byte[] text) {
        mMessageText = text;
    }

    public void setMessageText(CharSequence text) {
        this.setMessageText(IOUtils.toByteArray(text));
    }

    public String getMessageText() {
        return toString(this.getMessageTextAsByteArray());
    }

    public byte[] getMessageTextAsByteArray() {
        return mMessageText;
    }

    public void setSentToDevId(final String devId) {
        mSendToDevId = devId;
    }

    public String getSentToDevId() {
        return mSendToDevId;
    }

    public boolean isDuplicate() {
        return mDuplicate;
    }

    public void setDuplicate(boolean duplicate) {
        this.mDuplicate = duplicate;
    }

    public String toFormattedString() {
        String result = super.toFormattedString()
                + "Error: " + getError() + "\n"
                + "Sender: " + getSender() + "\n"
                + "Device id: " + getDeviceId() + "\n"
                + "Message id: " + getMessageId() + "\n"
                + "Sent to device: " + getSentToDevId() + "\n"
                + "Is duplicate: " + isDuplicate();
        return result;
    }
}
