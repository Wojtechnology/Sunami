package com.wojtechnology.sunami;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

/**
 * Created by wojtekswiderski on 15-05-24.
 */
public class OuterLayout extends RelativeLayout {
    private final double AUTO_OPEN_SPEED_LIMIT = 800.0;
    private int mDraggingState;
    private ViewDragHelper mDragHelper;
    private RelativeLayout mDraggable;
    private LinearLayout mSongHint;
    private Button mPlayHintButton;
    private TextView mHintTitle;
    private TextView mHintArtist;
    private int mDraggingBorder;
    private int mVerticalRange;
    private Context context;
    private boolean mIsOpen;
    private boolean mActive;
    private boolean mPlaying;
    private boolean mIsFirst;

    public OuterLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
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
            mIsOpen = settleToOpen ? true : false;

            if(mDragHelper.settleCapturedViewAt(0, settleDestY)){
                ViewCompat.postInvalidateOnAnimation(OuterLayout.this);
            }
        }
    }

    @Override
    protected void onFinishInflate() {
        mDragHelper = ViewDragHelper.create(this, 1.0f, new DragHelperCallback());
        mIsOpen = true;
        mIsFirst = true;
        mDraggingState = 0;

        mDraggable = (RelativeLayout) findViewById(R.id.draggable);
        mSongHint = (LinearLayout) findViewById(R.id.song_hint);
        mPlayHintButton = (Button) findViewById(R.id.play_hint_button);
        mHintTitle = (TextView) findViewById(R.id.hint_title);
        mHintArtist = (TextView) findViewById(R.id.hint_artist);

        mPlayHintButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(((MainActivity) context).isPlaying()){
                    ((MainActivity) context).pausePlay();
                    v.setBackgroundResource(R.drawable.ic_play_hint);
                }else{
                    ((MainActivity) context).resumePlay();
                    v.setBackgroundResource(R.drawable.ic_pause_hint);
                }
            }
        });

        if(((MainActivity) context).isPlaying()){
            playSong(new FireMixtape(context));
        }else{
            mActive = false;
        }
        updateDefaultLocation();
        super.onFinishInflate();
    }

    public int updateDefaultLocation(){
        Display display = ((Activity)context).getWindowManager().getDefaultDisplay();
        Window window = ((Activity)context).getWindow();
        Point size = new Point();
        Rect rectangle = new Rect();
        display.getSize(size);
        window.getDecorView().getWindowVisibleDisplayFrame(rectangle);
        return updateDefaultLocation(size.y - rectangle.top);
    }

    public int updateDefaultLocation(int screenHeight){
        int height;
        if(mActive){
            height = screenHeight - mSongHint.getMeasuredHeight();
        }else {
            height = screenHeight;
        }

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mDraggable.getLayoutParams();
        params.topMargin = height;
        params.bottomMargin = -height;
        mDraggable.setLayoutParams(params);

        if(!mIsFirst){
            if(mIsOpen) {
                if (mDragHelper.smoothSlideViewTo(mDraggable, 0, height)) {
                    mDragHelper.continueSettling(true);
                }
            }else{
                mDraggable.setTop(1);
                if (mDragHelper.smoothSlideViewTo(mDraggable, 0, 0)) {
                    mDragHelper.continueSettling(true);
                }
            }
        }

        return height;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mVerticalRange = h - mSongHint.getMeasuredHeight();
        if(!mIsFirst) {
            updateDefaultLocation(h);
        }else{
            mIsFirst = false;
        }
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
        mSongHint.getLocationOnScreen(draggableLocation);
        int upperLimit = draggableLocation[1] + mSongHint.getMeasuredHeight();
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

    public void playSong(FireMixtape song){
        mActive = true;
        mPlayHintButton.setBackgroundResource(R.drawable.ic_pause_hint);
        mHintTitle.setText(song.title);
        mHintArtist.setText(song.artist);

        updateDefaultLocation();
    }

    public void hideSong(){
        mIsOpen = true;
        mActive = false;
        updateDefaultLocation();
    }

    public void showSong(){
        mActive = true;
        updateDefaultLocation();
    }
}
