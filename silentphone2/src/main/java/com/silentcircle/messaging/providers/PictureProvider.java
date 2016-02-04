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
package com.silentcircle.messaging.providers;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;

public class PictureProvider extends ContentProvider {

    public static final String JPG_FILE_NAME = "IMG.jpg";

    public static final Uri CONTENT_URI = Uri.parse("content://com.silentcircle.messaging.provider.picture/");
    private static final HashMap<String, String> MIME_TYPES = new HashMap<String, String>();

    static {
        MIME_TYPES.put(".jpg", "image/jpeg");
        MIME_TYPES.put(".jpeg", "image/jpeg");
    }

    @Override
    public int delete(Uri uri, String where, String[] whereArgs) {
        File file = new File(getContext().getFilesDir(), JPG_FILE_NAME);

        if(file.exists()) {
            file.delete();
        }

        return 0;
    }

    @Override
    public String getType(Uri uri) {

        String path = Uri.withAppendedPath(uri, JPG_FILE_NAME).toString();

        for (String extension : MIME_TYPES.keySet()) {

            if (path.endsWith(extension)) {

                return MIME_TYPES.get(extension);

            }

        }

        return null;

    }

    @Override
    public Uri insert(Uri uri, ContentValues initialValues) {

        throw new RuntimeException("Operation not supported");

    }

    @Override
    public boolean onCreate() {
        try {
            File mFile = new File(getContext().getFilesDir(), JPG_FILE_NAME);
            if (!mFile.exists()) {
                mFile.createNewFile();
            }
            getContext().getContentResolver().notifyChange(CONTENT_URI, null);
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;

        }

    }

    @Override
    public ParcelFileDescriptor openFile(Uri uri, String mode)

            throws FileNotFoundException {

        File f = new File(getContext().getFilesDir(), JPG_FILE_NAME);
        if (!f.exists()) {
            try {
                f.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (f.exists()) {

            return ParcelFileDescriptor.open(f,

                    ParcelFileDescriptor.MODE_READ_WRITE);

        }

        throw new FileNotFoundException(uri.getPath());

    }

    @Override
    public Cursor query(Uri url, String[] projection, String selection, String[] selectionArgs, String sort) {
        throw new RuntimeException("Operation not supported");
    }

    @Override
    public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
        throw new RuntimeException("Operation not supported");
    }

}
