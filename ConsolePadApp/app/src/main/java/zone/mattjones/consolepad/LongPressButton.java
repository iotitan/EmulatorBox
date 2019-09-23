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
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;

public class LongPressButton extends LinearLayout implements GestureDetector.OnGestureListener {
    /** A shared deceleration interpolator for all button animations to use. */
    private static final DecelerateInterpolator SHARED_INTERPOLATOR = new DecelerateInterpolator();

    /** The amount of time the button needs to be held before triggering the action. */
    private static final int LONG_PRESS_DURATION_MS = 1500;

    /** The amount of acceptable motion in DP to continue a long-press. */
    private static final int SCROLL_SLOP_DP = 25;

    /** The conversion multiple from DP to PX. */
    private final float mDpToPx;

    /** Gesture detector for simplifying gesture recognition. */
    private final GestureDetector mGestureDetector;

    /** The listener that runs this button's action when completed. */
    private final Animator.AnimatorListener mAnimatorListener = new Animator.AnimatorListener() {
        /** Whether the animation was canceled. */
        private boolean mIsCanceled;

        @Override
        public void onAnimationStart(Animator animator) { }

        @Override
        public void onAnimationEnd(Animator animator) {
            cleanupAnimation();
            if (mIsCanceled) return;
            if (mClickListener != null) mClickListener.onClick(LongPressButton.this);
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

    /** The target background color when the button is being pressed. */
    private final int mTargetBackgroundColor;

    /** The target test color when the button is being pressed. */
    private final int mTargetTextColor;

    /** The view containing the text for this button. */
    private TextView mLabelView;

    /**
     * The animator responsible for transitioning the background color and eventually triggering
     * the action.
     */
    private ValueAnimator mColorAnimator;

    /** The initial background color of the view. */
    private int mStartBackgroundColor;

    /** The initial color of the text for this button. */
    private int mStartTextColor;

    /** The action triggered after the button is held for long enough. */
    private OnClickListener mClickListener;

    /** Default constructor for use in XML. */
    public LongPressButton(Context context, AttributeSet atts) {
        super(context, atts);
        mTargetBackgroundColor = getContext().getResources().getColor(R.color.colorPrimary);
        mTargetTextColor = getContext().getResources().getColor(R.color.icon_color);
        mDpToPx = context.getResources().getDisplayMetrics().density;
        mGestureDetector = new GestureDetector(this);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mLabelView = (TextView) findViewById(R.id.button_label);

        mAnimatorUpdateListener = (animator) -> {
            float v = (float) animator.getAnimatedValue();
            interpolateColor(mStartBackgroundColor, mTargetBackgroundColor, v);
            setBackgroundColor(interpolateColor(mStartBackgroundColor, mTargetBackgroundColor, v));
            mLabelView.setTextColor(interpolateColor(mStartTextColor, mTargetTextColor, v));
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
        if (mColorAnimator == null) return;
        mColorAnimator.removeAllListeners();
        mColorAnimator.cancel();
        mColorAnimator = null;
        setBackgroundColor(mStartBackgroundColor);
        mLabelView.setTextColor(mStartTextColor);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        handleMotionEvent(event);
        return mGestureDetector.onTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        handleMotionEvent(event);
        return mGestureDetector.onTouchEvent(event);
    }

    private void handleMotionEvent(MotionEvent event) {
        if (event.getActionMasked() == MotionEvent.ACTION_DOWN && mColorAnimator == null) {
            mStartBackgroundColor = Color.TRANSPARENT;
            if (getBackground() instanceof ColorDrawable) {
                mStartBackgroundColor = ((ColorDrawable) getBackground()).getColor();
            }

            mStartTextColor = mLabelView.getCurrentTextColor();

            mColorAnimator = ValueAnimator.ofFloat(0, 1);
            mColorAnimator.setDuration(LONG_PRESS_DURATION_MS);
            mColorAnimator.addListener(mAnimatorListener);
            mColorAnimator.addUpdateListener(mAnimatorUpdateListener);
            mColorAnimator.setInterpolator(SHARED_INTERPOLATOR);
            mColorAnimator.start();
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
        if (dx < SCROLL_SLOP_DP * mDpToPx && dy < SCROLL_SLOP_DP * mDpToPx) return true;
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
