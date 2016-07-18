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
package com.silentcircle.messaging.model.json;

import com.silentcircle.messaging.model.Contact;

import org.json.JSONException;
import org.json.JSONObject;


public class JSONContactAdapter extends JSONAdapter {

	public JSONObject adapt( Contact contact ) {
		JSONObject json = new JSONObject();
		try {
			json.put( "alias", contact.getAlias() );
			json.put( "device", contact.getDevice() );
			json.put( "username", contact.getUserId() );
			json.put( "display_name", contact.getDisplayName() );
		} catch( JSONException exception ) {
			// This should never happen because we control all of the keys.
		}
		return json;
	}

	public Contact adapt( JSONObject json ) {

		Contact contact = new Contact(getString(json, "username"));
		contact.setAlias(getString(json, "alias"));
		contact.setDevice(getString(json, "device"));
		contact.setDisplayName(getString(json, "display_name"));
		return contact;
	}
}