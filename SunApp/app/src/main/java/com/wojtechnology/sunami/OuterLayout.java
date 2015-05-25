package com.wojtechnology.sunami;

import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

/**
 * Created by wojtekswiderski on 15-05-24.
 */
public class OuterLayout extends RelativeLayout {
    private final double AUTO_OPEN_SPEED_LIMIT = 800.0;
    private int mDraggingState;
    private ViewDragHelper mDragHelper;
    private RelativeLayout mDraggable;
    private Button mDraggableButton;
    private int mDraggingBorder;
    private int mVerticalRange;
    private boolean mIsOpen;

    public OuterLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mDraggingState = 0;
        mIsOpen = true;
    }

    public class DragHelperCallback extends ViewDragHelper.Callback {

        @Override
        public void onViewDragStateChanged(int state) {
            if (state == mDraggingState) {
                return;
            }
            if((mDraggingState == ViewDragHelper.STATE_DRAGGING || mDraggingState == ViewDragHelper.STATE_SETTLING) &&
                    state == ViewDragHelper.STATE_IDLE){
                // View has stopped moving

                if(mDraggingBorder == 0){
                    onStopDraggingToClosed();
                }else if(mDraggingBorder == mVerticalRange){
                    mIsOpen = true;
                }

            }
            if (state == ViewDragHelper.STATE_DRAGGING){
                onStartDragging();
            }
            mDraggingState = state;
        }

        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            mDraggingBorder = top;
        }

        @Override
        public int getViewVerticalDragRange(View child) {
            return mVerticalRange;
        }

        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            //return (child.getId() == R.id.draggable);
            return true;
        }

        @Override
        public int clampViewPositionVertical(View child, int top, int dy) {
            final int topBound = getPaddingTop();
            final int bottomBound = mVerticalRange;
            return Math.min(Math.max(top, topBound), bottomBound);
        }

        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            final float rangeToCheck = mVerticalRange;
            if(mDraggingBorder == 0){
                mIsOpen = false;
                return;
            }
            if(mDraggingBorder == rangeToCheck){
                mIsOpen = true;
                return;
            }
            boolean settleToOpen = false;
            if (yvel > AUTO_OPEN_SPEED_LIMIT) {
                settleToOpen = true;
            }else if(yvel < -AUTO_OPEN_SPEED_LIMIT){
                settleToOpen = false;
            }else if(mDraggingBorder > rangeToCheck / 2){
                settleToOpen = true;
            }else if(mDraggingBorder < rangeToCheck / 2){
                settleToOpen = false;
            }

            final int settleDestY = settleToOpen ? mVerticalRange : 0;

            if(mDragHelper.settleCapturedViewAt(0, settleDestY)){
                ViewCompat.postInvalidateOnAnimation(OuterLayout.this);
            }
        }
    }

    @Override
    protected void onFinishInflate() {
        mDragHelper = ViewDragHelper.create(this, 1.0f, new DragHelperCallback());
        mIsOpen = true;
        mDraggingState = 0;
        //mDraggable = (RelativeLayout) findViewById(R.id.draggable);
        //mDraggableButton = (Button) findViewById(R.id.draggable_button);
        super.onFinishInflate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mVerticalRange = h - mDraggableButton.getMeasuredHeight();
        super.onSizeChanged(w, h, oldw, oldh);
    }

    private void onStopDraggingToClosed(){
        // To be implemented
    }

    private void onStartDragging(){
        // To be implemented
    }

    private boolean isDraggableTarget(MotionEvent event){
        int[] draggableLocation = new int[2];
        mDraggableButton.getLocationOnScreen(draggableLocation);
        int upperLimit = draggableLocation[1] + mDraggableButton.getMeasuredHeight();
        int lowerLimit = draggableLocation[1];
        int y = (int) event.getRawY();
        return (y > lowerLimit && y < upperLimit);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (isDraggableTarget(event) && mDragHelper.shouldInterceptTouchEvent(event)) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isDraggableTarget(event) || isMoving()) {
            mDragHelper.processTouchEvent(event);
            return true;
        } else {
            return super.onTouchEvent(event);
        }
    }

    @Override
    public void computeScroll() { // needed for automatic settling.
        if (mDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    public boolean isMoving() {
        return (mDraggingState == ViewDragHelper.STATE_DRAGGING ||
                mDraggingState == ViewDragHelper.STATE_SETTLING);
    }

    public boolean isOpen() {
        return mIsOpen;
    }

    public void showDisplay(){
        mDragHelper.smoothSlideViewTo(mDraggable, 0, mDraggable.getMeasuredHeight() - mDraggableButton.getMeasuredHeight());
    }
}
