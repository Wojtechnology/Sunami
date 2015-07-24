package com.wojtechnology.sunami;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

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
    private Button mNextHintButton;
    private TextView mHintTitle;
    private TextView mHintArtist;
    private SeekBar mSeekBar;
    private int mDraggingBorder;
    private int mVerticalRange;
    private MainActivity mContext;
    private boolean mIsOpen;
    private boolean mActive;
    private boolean mIsFirst;

    public OuterLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = (MainActivity) context;
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
            mIsOpen = settleToOpen;

            if(mDragHelper.settleCapturedViewAt(0, settleDestY)){
                ViewCompat.postInvalidateOnAnimation(OuterLayout.this);
            }
        }
    }

    @Override
    protected void onFinishInflate() {
        mDragHelper = ViewDragHelper.create(this, 2.0f, new DragHelperCallback());
        mIsOpen = true;
        mIsFirst = true;
        mActive = false;
        mDraggingState = 0;

        mDraggable = (RelativeLayout) findViewById(R.id.draggable);
        mSongHint = (LinearLayout) findViewById(R.id.song_hint);
        mPlayHintButton = (Button) findViewById(R.id.play_hint_button);
        mNextHintButton = (Button) findViewById(R.id.next_hint_button);
        mHintTitle = (TextView) findViewById(R.id.hint_title);
        mHintArtist = (TextView) findViewById(R.id.hint_artist);
        mSeekBar = (SeekBar) findViewById(R.id.seek_bar);

        mPlayHintButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mContext.mTheBrain.togglePlay();
            }
        });

        mNextHintButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mContext.mTheBrain.playNext();
            }
        });

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {}

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mContext.updateMediaPlayerProgress(seekBar.getProgress());
            }
        });

        mDraggable.setOnTouchListener(new OnTouchListener() {
            private int mX;
            private boolean isDragging;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        isDragging = true;
                        mX = (int) event.getX();
                        break;
                    }
                    case MotionEvent.ACTION_UP: {
                        if (isDragging && (int) event.getX() - mX > mDraggable.getMeasuredWidth() / 2) {
                            mContext.mTheBrain.playLast();
                        }
                        isDragging = false;
                        break;
                    }
                    default: {
                        break;
                    }
                }
                return false;
            }
        });

        updateDefaultLocation();
        super.onFinishInflate();
    }

    // Only happens when playing new song
    public void setMaxProgress(int max) {
    }

    public void setProgress(int progress) {
        mSeekBar.setProgress(progress);
    }

    public int updateDefaultLocation(){
        Display display = mContext.getWindowManager().getDefaultDisplay();
        Window window = mContext.getWindow();
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

    public void updatePlayIcon(){
        if (mContext.mTheBrain.isPlaying()) {
            mPlayHintButton.setBackgroundResource(R.drawable.ic_pause_hint);
        } else {
            mPlayHintButton.setBackgroundResource(R.drawable.ic_play_hint);
        }
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
        try {
            if (isDraggableTarget(event) && mDragHelper.shouldInterceptTouchEvent(event)) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
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
        if (!mContext.drawerOpen()) {
            mActive = true;
        }
        mSeekBar.setProgress(0);
        if (song == null){
            return;
        }
        mSeekBar.setMax(Integer.parseInt(song.duration));
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
