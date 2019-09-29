/**
 * Copyright 2019 Matthew Jones
 *
 * File: LongPressButton.java
 * Author: Matt Jones
 * Date: 2019.09.06
 * Desc: A view that shifts color when pressed and held until the action is performed.
 */

package zone.mattjones.consolepad;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

public class LongPressButton extends FrameLayout implements GestureDetector.OnGestureListener {
    /** A shared deceleration interpolator for all button animations to use. */
    private static final Interpolator SHARED_INTERPOLATOR = new DecelerateInterpolator();

    /** The amount of time the button needs to be held before triggering the action. */
    private static final int LONG_PRESS_DURATION_MS = 1500;

    /** The amount of the button should show a confirmation (darker color) before cleanup. */
    private static final int CONFIRM_DURATION_MS = 1500;

    /** The amount of acceptable motion in DP to continue a long-press. */
    private static final int SCROLL_SLOP_DP = 20;

    /** The conversion multiple from DP to PX. */
    private final float mDpToPx;

    /** Gesture detector for simplifying gesture recognition. */
    private final GestureDetector mGestureDetector;

    /** The listener that runs this button's action when completed. */
    private final Animator.AnimatorListener mAnimatorListener = new Animator.AnimatorListener() {
        /** Whether the animation was canceled. */
        private boolean mIsCanceled;

        @Override
        public void onAnimationStart(Animator animator) {
            mActiveGroup.setVisibility(View.VISIBLE);
        }

        @Override
        public void onAnimationEnd(Animator animator) {
            if (mIsCanceled) {
                cleanupAnimation();
                return;
            }
            if (mClickListener != null) mClickListener.onClick(LongPressButton.this);
            mActiveGroup.setBackgroundColor(
                    getResources().getColor(R.color.purple_secondary, null));
            mConfirmRunnable = () -> cleanupAnimation();
            postDelayed(() -> {
                mConfirmRunnable.run();
                mConfirmRunnable = null;
            }, CONFIRM_DURATION_MS);
        }

        @Override
        public void onAnimationCancel(Animator animator) {
            mIsCanceled = true;
        }

        @Override
        public void onAnimationRepeat(Animator animator) { }
    };

    /** Responsible for making the changes to the view when the animation ticks. */
    private ValueAnimator.AnimatorUpdateListener mAnimatorUpdateListener;

    /** The animator responsible for scrolling the background while the button is being held. */
    private ValueAnimator mScrollAnimator;

    /** The action triggered after the button is held for long enough. */
    private OnClickListener mClickListener;

    /** The view of that button that is shown while the button is not active. */
    private ViewGroup mInactiveGroup;

    /** The view of that button that is shown while the button is active. */
    private ViewGroup mActiveGroup;

    /** The dimensions of the view at the start of the animation. */
    private Rect mOriginalDimensions;

    /** The rect used to clip the active view as it animates across the screen. */
    private Rect mAnimatedClipRect;

    /** Wait to reset the background color after a confirmed, selection. */
    private Runnable mConfirmRunnable;

    /** Default constructor for use in XML. */
    public LongPressButton(Context context, AttributeSet atts) {
        super(context, atts);

        mDpToPx = getResources().getDisplayMetrics().density;
        mGestureDetector = new GestureDetector(this);

        mOriginalDimensions = new Rect();
        mAnimatedClipRect = new Rect(0, 0, 0, 0);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        // Tint the icons since they aren't by default.
        setIconColor((ImageView) findViewById(R.id.button_icon),
                getResources().getColor(R.color.icon_color_default));
        setIconColor((ImageView) findViewById(R.id.button_icon_active),
                getResources().getColor(R.color.icon_color_selected));

        mInactiveGroup = (ViewGroup) findViewById(R.id.inactive_button);
        addOnLayoutChangeListener(new OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(
                    View view, int l, int t, int r, int b, int ol, int ot, int or, int ob) {
                mOriginalDimensions.set(0, 0, r - l, b - t);

            }
        });

        mActiveGroup = (ViewGroup) findViewById(R.id.active_button);
        mActiveGroup.setClipBounds(mAnimatedClipRect);

        mAnimatorUpdateListener = (animator) -> {
            float v = (float) animator.getAnimatedValue();
            mAnimatedClipRect.set(mOriginalDimensions.left, mOriginalDimensions.top,
                    (int) (mOriginalDimensions.right * v), mOriginalDimensions.bottom);
            mActiveGroup.setClipBounds(mAnimatedClipRect);
        };
    }

    @Override
    public void setOnClickListener(OnClickListener l) {
        // Don't set the normal click listener on this view by calling super.
        mClickListener = l;
    }

    /**
     * Interpolate between two colors.
     * @param start The start color.
     * @param end The end color.
     * @param f The fraction in range [0, 1].
     * @return The interpolated color.
     */
    private int interpolateColor(int start, int end, float f) {
        return Color.argb(
                (Color.alpha(start) * (1 - f) + Color.alpha(end) * f) / 255.f,
                (Color.red(start) * (1 - f) + Color.red(end) * f) / 255.f,
                (Color.green(start) * (1 - f) + Color.green(end) * f) / 255.f,
                (Color.blue(start) * (1 - f) + Color.blue(end) * f) / 255.f);
    }

    /** Cancel the running animation and reset state. */
    private void cleanupAnimation() {
        if (mScrollAnimator == null) return;
        mScrollAnimator.removeAllListeners();
        mScrollAnimator.cancel();
        mScrollAnimator = null;
        mActiveGroup.setVisibility(View.GONE);

        mAnimatedClipRect.set(0, 0, 0, 0);
        mActiveGroup.setBackgroundColor(
                getResources().getColor(R.color.purple_primary, null));
    }

    /**
     * Set the label for this button.
     * @param stringResourceId The resource ID of the string to use.
     */
    public void setButtonText(int stringResourceId) {
        ((TextView) findViewById(R.id.button_label)).setText(stringResourceId);
        ((TextView) findViewById(R.id.button_label_active)).setText(stringResourceId);
    }

    /**
     * Set this button's icon.
     * @param iconId The ID of the drawable for the button to display.
     */
    public void setButtonIcon(int iconId) {
        ImageView activeIcon = (ImageView) findViewById(R.id.button_icon_active);
        ImageView inactiveIcon = (ImageView) findViewById(R.id.button_icon);

        // Make sure to call mutate() on the drawables so Android doesn't try sharing a single
        // resource instance.
        activeIcon.setImageDrawable(getResources().getDrawable(iconId, null).mutate());
        setIconColor(activeIcon, getResources().getColor(R.color.icon_color_selected));


        inactiveIcon.setImageDrawable(getResources().getDrawable(iconId, null).mutate());
        setIconColor(inactiveIcon, getResources().getColor(R.color.icon_color_default));
    }

    /**
     * Set the color of the icon.
     * @param color The color to set the icon to. The original colors are overridden by this value.
     */
    private void setIconColor(ImageView iconView, int color) {
        iconView.setColorFilter(color, PorterDuff.Mode.SRC_IN);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (mConfirmRunnable != null) return false;
        handleMotionEvent(event);
        return mGestureDetector.onTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mConfirmRunnable != null) return false;
        handleMotionEvent(event);
        return mGestureDetector.onTouchEvent(event);
    }

    private void handleMotionEvent(MotionEvent event) {
        if (event.getActionMasked() == MotionEvent.ACTION_DOWN && mScrollAnimator == null) {
            mScrollAnimator = ValueAnimator.ofFloat(0, 1);
            mScrollAnimator.setDuration(LONG_PRESS_DURATION_MS);
            mScrollAnimator.addListener(mAnimatorListener);
            mScrollAnimator.addUpdateListener(mAnimatorUpdateListener);
            mScrollAnimator.setInterpolator(SHARED_INTERPOLATOR);
            mScrollAnimator.start();
        } else if (event.getActionMasked() == MotionEvent.ACTION_UP
                || event.getActionMasked() == MotionEvent.ACTION_CANCEL) {
            cleanupAnimation();
        }
    }

    @Override
    public boolean onDown(MotionEvent motionEvent) {
        return true;
    }

    @Override
    public void onShowPress(MotionEvent motionEvent) {
    }

    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent) {
        cleanupAnimation();
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float dx, float dy) {
        // Don't cancel if the movement was small enough.
        if (Math.abs(dx) < SCROLL_SLOP_DP * mDpToPx && Math.abs(dy) < SCROLL_SLOP_DP * mDpToPx) {
            return true;
        }
        cleanupAnimation();
        return false;
    }

    @Override
    public void onLongPress(MotionEvent motionEvent) {
    }

    @Override
    public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float vx, float vy) {
        cleanupAnimation();
        return false;
    }
}
