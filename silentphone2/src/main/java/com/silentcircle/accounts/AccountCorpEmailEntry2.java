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

package com.silentcircle.accounts;


import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.net.http.SslCertificate;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ConsoleMessage;
import android.webkit.CookieManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebViewDatabase;

import com.silentcircle.silentphone2.R;
import com.silentcircle.silentphone2.activities.ProvisioningActivity;
import com.silentcircle.silentphone2.util.ConfigurationUtilities;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;


/**
 * A simple {@link Fragment} subclass.
 */
public class AccountCorpEmailEntry2 extends Fragment {

    public static final String TAG="AccountCorpEmailEntry2";
    public static final String ADFS_PATH = "adfs_path";
    public static final String CAN_DO_USERNAME = "can_do_username";
    public static final String DOMAIN = "domain";
    public static final String AUTH_CODE = "auth_code";
    public static final String REDIRECT_URI = "redirect_uri";

    private static final String CODE = "code=";
    private static final String ERROR = "error=";

    private AuthenticatorActivity mParent;

    private WebView ssoWebView;
    private WebChromeObserver ssoWebViewChromeObserver;
    private WebViewObserver ssoWebViewObserver;

    private boolean dummyLoaded = false;
    private boolean realLoaded = false;
    private boolean canDoUsername = false;

    private String startPath;
    private String username;
    private String redirectScheme;
    private String redirectAuthority;
    private String redirectPath;


    public static AccountCorpEmailEntry2 newInstance(Bundle args) {
        AccountCorpEmailEntry2 f = new AccountCorpEmailEntry2();
        f.setArguments(args);
        return f;
    }

    public AccountCorpEmailEntry2() {
        // Required empty public constructor
    }

    private class WebChromeObserver extends WebChromeClient {
        @Override
        public boolean onConsoleMessage (ConsoleMessage consoleMessage) {
            Log.w(TAG, "ChromeClient message: " + consoleMessage.toString());
            return true;
        }
    }

    private class WebViewObserver extends WebViewClient {
        @Override
        public void onReceivedSslError (WebView view, SslErrorHandler handler, SslError error) {
            Log.e(TAG, "Received SSL Error: " +  error);
            SslCertificate sslc = error.getCertificate();

            Log.w(TAG, "Who's it for: " + sslc.getIssuedTo().toString());

            String message = null;
            switch (error.getPrimaryError())
            {
                case SslError.SSL_DATE_INVALID:
                    message = getString(R.string.provisioning_ssl_date_invalid);
                    break;
                case SslError.SSL_EXPIRED:
                    message = getString(R.string.provisioning_ssl_expired);
                    break;
                case SslError.SSL_IDMISMATCH:
                    message = getString(R.string.provisioning_ssl_idmismatch);
                    break;
                case SslError.SSL_NOTYETVALID:
                    message = getString(R.string.provisioning_ssl_notyetvalid);
                    break;
                case SslError.SSL_UNTRUSTED:
                    message = getString(R.string.provisioning_ssl_untrusted);
                    break;
            }
            mParent.showInputInfo(getString(R.string.redirect_ssl_error, sslc.getIssuedTo().toString()
                                    + (TextUtils.isEmpty(message) ? "" : ": " + message)));
            //Toast.makeText(mParent.getApplicationContext(),
            //        "Big fat warning: SSL error on "+sslc.getIssuedTo().toString()+", see logcat; bypass for demo purposes", Toast.LENGTH_LONG).show();

            handler.proceed();
        }

        @Override
        @SuppressWarnings("deprecation")
        public void onReceivedError (WebView view, int errorCode, String description, String failingUrl) {
            Log.w(TAG, description + ", url= " + failingUrl + " code= " + errorCode);

        }

        @Override
        @TargetApi(Build.VERSION_CODES.M)
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Log.w(TAG, error.getDescription().toString() + ", url= " + request.getUrl().toString() + " code= " + error.getErrorCode());
            }
        }

        @Override
        public void onLoadResource(WebView wv, String sUrl) {
            Log.d("loadResource", sUrl);
        }

        @Override
        public void onPageFinished(WebView wv, String sUrl) {
            if (ConfigurationUtilities.mTrace) Log.d(TAG, "pageFinished: " + sUrl);

            if(!dummyLoaded) {
                dummyLoaded = true;
                if(canDoUsername) {
                    try {
                        wv.loadUrl(startPath + "&username="+ URLEncoder.encode(username, "UTF-8"));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                } else {
                    wv.loadUrl(startPath);
                }
            } else {
                if(!realLoaded) {
                    realLoaded = true;
                }
            }
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView wv, String sUrl) {
            if (ConfigurationUtilities.mTrace) Log.d("WebViewObserver", "url=" + sUrl);

            URI localUri = null;
            try {
                localUri = new URI(URLDecoder.decode(sUrl, "utf-8"));
            } catch (URISyntaxException e) {
                Log.e(TAG, "Cannot parse URI", e);
            } catch (UnsupportedEncodingException ignore) {
            }
            if (localUri == null)
                return false;   // cannot decode this URI --- let webview try it?? Or bail (back-step) out with error

            if (redirectScheme.equals(localUri.getScheme()) && redirectAuthority.equals(localUri.getAuthority())) {
                if (!TextUtils.isEmpty(redirectPath) && !redirectPath.equals(localUri.getPath())) {
                    mParent.showInputInfo(getString(R.string.redirect_uri_mismatch, sUrl));
                    mParent.backStep();
                    return true;
                }
                //Toast.makeText(mParent.getApplicationContext(), "Got my redirect", Toast.LENGTH_LONG).show();
                String query = localUri.getQuery();
                String code;
                if (!TextUtils.isEmpty(query)) {
                    int idx = query.indexOf(CODE);
                    if (idx < 0) {
                        checkAndGetError(query);
                        mParent.backStep();
                        return true;
                    }
                    code = query.substring(idx + CODE.length());
                    idx = code.indexOf('&');
                    if (idx > 0)
                        code = code.substring(0, idx);
                }
                else {
                    mParent.showInputInfo(getString(R.string.redirect_uri_query_missing, sUrl));
                    mParent.backStep();
                    return true;
                }
                mParent.accountCorpEmailEntry3(username, code);
                return true;
            }
            else {
                return false;
            }
        }
    }

    private void checkAndGetError(String query) {
        int idx = query.indexOf(ERROR);
        if (idx < 0) {
            mParent.showInputInfo(getString(R.string.redirect_uri_malformed));
            return;
        }
        String code = query.substring(idx + ERROR.length());
        idx = code.indexOf('&');
        if (idx > 0)
            code = code.substring(0, idx);
        mParent.showInputInfo(getString(R.string.redirect_uri_error, code));
    }

    @Override
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @SuppressWarnings("deprecation")
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View retView = inflater.inflate(R.layout.provisioning_corp_account_email_entry2, container, false);
        if (retView == null) {
            return null;
        }
        ssoWebView = (WebView)retView.findViewById(R.id.webView);

        WebSettings ws = ssoWebView.getSettings();

        ws.setJavaScriptEnabled(true);

        ssoWebViewObserver = new WebViewObserver();
        ssoWebViewChromeObserver = new WebChromeObserver();

        ssoWebView.setWebViewClient(ssoWebViewObserver);
        ssoWebView.setWebChromeClient(ssoWebViewChromeObserver);

        // Can't use removeAllCookies with callback as it's not in API 16
        CookieManager cm = android.webkit.CookieManager.getInstance();
        if (cm.hasCookies()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                cm.removeAllCookies(null);
            else
                cm.removeAllCookie();
        }

        WebViewDatabase wvd = WebViewDatabase.getInstance(mParent.getApplicationContext());
        wvd.clearFormData();
        wvd.clearHttpAuthUsernamePassword();

        WebStorage.getInstance().deleteAllData();

        ssoWebView.clearFormData();
        ssoWebView.clearCache(true);

        return retView;
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        commonOnAttach(getActivity());
    }

    /*
     * Deprecated on API 23
     * Use onAttachToContext instead
     */
    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        commonOnAttach(activity);
    }

    private void commonOnAttach(Activity activity) {
        try {
            mParent = (AuthenticatorActivity) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must be AuthenticatorActivity.");
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        dummyLoaded = false;
        realLoaded = false;

        Bundle args = getArguments();

        canDoUsername = args.getBoolean(CAN_DO_USERNAME);
        startPath = args.getString(ADFS_PATH);
        username = args.getString(ProvisioningActivity.USERNAME);
        try {
            URI redirectUri = new URI(URLDecoder.decode(args.getString(REDIRECT_URI), "utf-8"));
            redirectScheme = redirectUri.getScheme();
            redirectAuthority = redirectUri.getAuthority();
            redirectPath = redirectUri.getPath();
        } catch (URISyntaxException e) {
            Log.e(TAG, "Server provided a wrong URI " + args.getString(REDIRECT_URI));
        } catch (UnsupportedEncodingException ignore) { }

        if (TextUtils.isEmpty(redirectScheme) || TextUtils.isEmpty(redirectAuthority)) {
            mParent.showInputInfo(getString(R.string.redirect_wrong_uri, args.getString(REDIRECT_URI)));
            mParent.backStep();
            return;
        }

        // Why am I doing this? Regardless of the cookie/cache clearing stuff above
        // it still caches something that makes it not load. This helps.

        ssoWebView.loadData("<html></html>", "text/html", null);
    }
}

