/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.silentcircle.common.util;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Matrix;
import android.graphics.Outline;
import android.graphics.Point;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.text.Spannable;
import android.text.style.URLSpan;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.silentcircle.messaging.activities.ChooserBuilder;
import com.silentcircle.messaging.util.IOUtils;
import com.silentcircle.silentphone2.R;

import java.io.File;
import java.io.IOException;

/**
 * Provides static functions to work with views
 */
public class ViewUtil {

    private static final String TAG = ViewUtil.class.getSimpleName();

    public static final int ROTATE_90 = 90;
    public static final int ROTATE_180 = 180;
    public static final int ROTATE_270 = 270;

    private ViewUtil() {
    }

    /**
     * Returns the width as specified in the LayoutParams
     *
     * @throws IllegalStateException Thrown if the view's width is unknown before a layout pass
     *                               s
     */
    public static int getConstantPreLayoutWidth(View view) {
        // We haven't been layed out yet, so get the size from the LayoutParams
        final ViewGroup.LayoutParams p = view.getLayoutParams();
        if (p.width < 0) {
            throw new IllegalStateException("Expecting view's width to be a constant rather " +
                    "than a result of the layout pass");
        }
        return p.width;
    }

    /**
     * Returns a boolean indicating whether or not the view's layout direction is RTL
     *
     * @param view - A valid view
     * @return True if the view's layout direction is RTL
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static boolean isViewLayoutRtl(View view) {
        return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) &&
                (view.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL);
    }

    @SuppressLint("NewApi")
    private static final ViewOutlineProvider OVAL_OUTLINE_PROVIDER;
    @SuppressLint("NewApi")
    private static final ViewOutlineProvider RECT_OUTLINE_PROVIDER;
    static {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            OVAL_OUTLINE_PROVIDER = new ViewOutlineProvider() {
                @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void getOutline(View view, Outline outline) {
                    outline.setOval(0, 0, view.getWidth(), view.getHeight());
                }
            };
            RECT_OUTLINE_PROVIDER = new ViewOutlineProvider() {
                @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void getOutline(View view, Outline outline) {
                    outline.setRect(0, 0, view.getWidth(), view.getHeight());
                }
            };
        }
        else {
            OVAL_OUTLINE_PROVIDER = RECT_OUTLINE_PROVIDER = null;
        }
    }

    /**
     * Adds a rectangular outline to a view. This can be useful when you want to add a shadow
     * to a transparent view. See b/16856049.
     * @param view view that the outline is added to
     * @param res The resources file.
     */
    public static void addRectangularOutlineProvider(View view, Resources res) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            view.setOutlineProvider(RECT_OUTLINE_PROVIDER);
    }

    /**
     * Configures the floating action button, clipping it to a circle and setting its translation z.
     * @param view The float action button's view.
     * @param res The resources file.
     */
    public static void setupFloatingActionButton(View view, Resources res) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            view.setOutlineProvider(OVAL_OUTLINE_PROVIDER);
            view.setTranslationZ(res.getDimensionPixelSize(R.dimen.floating_action_button_translation_z));
        }
    }

    /**
     * Adds padding to the bottom of the given {@link android.widget.ListView} so that the floating action button
     * does not obscure any content.
     *
     * @param listView to add the padding to
     * @param res valid resources object
     */
    public static void addBottomPaddingToListViewForFab(ListView listView, Resources res) {
        final int fabPadding = res.getDimensionPixelSize(
                R.dimen.floating_action_button_list_bottom_padding);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
            listView.setPaddingRelative(listView.getPaddingStart(), listView.getPaddingTop(),
                    listView.getPaddingEnd(), listView.getPaddingBottom() + fabPadding);
        else
            listView.setPadding(listView.getPaddingLeft(), listView.getPaddingTop(),
                    listView.getPaddingRight(), listView.getPaddingBottom() + fabPadding);

        listView.setClipToPadding(false);
    }

    /**
     * Sets view's opacity.
     *
     * @param view View to set opacity to.
     * @param alpha The opacity of the view.
     *
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static void setAlpha(View view, float alpha) {
        if (view != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            view.setAlpha(alpha);
        }
    }

    /**
     * Set drawable on the left of text view.
     *
     * @param view Text view to set drawable to.
     * @param drawableResourceID Drawable id to set to text view.
     */
    public static void setDrawableStart(TextView view, int drawableResourceID) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            setDrawableLeft(view, drawableResourceID);
        } else {
            setDrawableStartForReal(view, drawableResourceID);
        }
    }

    private static void setDrawableLeft(TextView view, int drawableResourceID) {
        view.setCompoundDrawablesWithIntrinsicBounds(drawableResourceID, 0, 0, 0);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private static void setDrawableStartForReal(TextView view, int drawableResourceID) {
        view.setCompoundDrawablesRelativeWithIntrinsicBounds(drawableResourceID, 0, 0, 0);
    }


    public static Intent createIntentForLinks(TextView view) {

        CharSequence text = view.getText();

        if (text instanceof Spannable) {

            Spannable stext = (Spannable) text;
            URLSpan[] spans = stext.getSpans(0, stext.length(), URLSpan.class);

            if (spans != null && spans.length > 0) {
                ChooserBuilder chooser = new ChooserBuilder(view.getContext());
                chooser.label(R.string.view);
                for (URLSpan span : spans) {
                    String url = span.getURL();
                    CharSequence label = stext.subSequence(stext.getSpanStart(span), stext.getSpanEnd(span));
                    chooser.intent(new Intent(Intent.ACTION_VIEW, Uri.parse(url)), label);
                }
                return chooser.build();

            }
        }
        return null;
    }

    /**
     * Returns screen dimensions in passed Point structure
     */
    public static void getScreenDimensions(final Context context, final Point size) {
        if (context == null || size == null) {
            return;
        }

        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        display.getSize(size);
    }

    /**
     * Sets state (enabled/disabled) for viewGroup and its children.
     *
     * @param viewGroup ViewGroup for which to set state.
     * @param enabled State to set.
     */
    public static void setEnabled(final ViewGroup viewGroup, boolean enabled) {
        viewGroup.setEnabled(enabled);
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View view = viewGroup.getChildAt(i);
            view.setEnabled(enabled);
        }
    }

    /**
     * Finds JPEG image rotation flags from exif and returns matrix to be used to rotate image.
     *
     * @param fileName JPEG image file name.
     *
     * @return Matrix to use in image rotate transformation or null if parsing failed.
     */
    public static Matrix getRotationMatrixFromExif(final String fileName) {
        Matrix matrix = null;
        try {
            ExifInterface exif = new ExifInterface(fileName);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            int rotate = 0;

            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = ROTATE_90;
                    break;

                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = ROTATE_180;
                    break;

                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = ROTATE_270;
                    break;
            }

            if (rotate != 0) {
                matrix = new Matrix();
                matrix.preRotate(rotate);
            }
        }
        catch (IOException e) {
            Log.i(TAG, "Failed to determine image flags from file " + fileName);
        }
        return matrix;
    }

    public static Matrix getRotationMatrixFromExif(final Context context, final Uri uri) {
        Matrix matrix = null;
        File tmpFile = IOUtils.writeUriContentToTempFile(context, uri);
        if (tmpFile != null){
            matrix = ViewUtil.getRotationMatrixFromExif(tmpFile.getAbsolutePath());
            tmpFile.delete();
        }
        return matrix;
    }

    public static void animateImageChange(final Context context, final ImageView imageView,
            final int newImage) {
        final Animation animOut = AnimationUtils.loadAnimation(context, android.R.anim.fade_out);
        final Animation animIn  = AnimationUtils.loadAnimation(context, android.R.anim.fade_in);

        animOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                imageView.setImageResource(newImage);
                animIn.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                    }
                });
                imageView.startAnimation(animIn);
            }
        });
        imageView.startAnimation(animOut);
    }
}
