package com.wojtechnology.sunami;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.os.AsyncTask;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.text.Layout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
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
    private TextView mHintTitle;
    private TextView mHintArtist;
    private SeekBar mSeekBar;
    private LinearLayout mHint;
    private View mNextHintButton;
    private int mDraggingBorder;
    private int mVerticalRange;
    private MainActivity mContext;
    private boolean mIsOpen;
    private boolean mActive;
    private boolean mIsFirst;
    private boolean mAllowDrag;
    private boolean mIsDragging;
    private boolean mIsSeeking;
    private boolean mPlayIconActive;

    // Transport controls
    private Button mLastMainBut;
    private Button mPlayMainBut;
    private Button mNextMainBut;

    private TextView mRunningTime;
    private TextView mTotalTime;

    private ImageView mArtworkView;

    private OnClickListener mTogglePlayClickListener;
    private OnClickListener mCloseHintClickListener;

    private int mItemWidth;
    private int mScreenHeight;

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
                    mIsOpen = false;
                    updateDefaultLocation();
                }else if(mDraggingBorder == mVerticalRange){
                    mIsOpen = true;
                    updateDefaultLocation();
                }

            }
            if (state == ViewDragHelper.STATE_DRAGGING){
                // Nothing
            }
            mDraggingState = state;
        }

        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            mDraggingBorder = top;
            int height = mScreenHeight - mSongHint.getMeasuredHeight();
            float fraction = (float) top / (float) height;
            if (fraction >= 0.5f) {
                fraction = (fraction - 0.5f) * 2.0f;
                mPlayIconActive = true;
                mPlayHintButton.setOnClickListener(mTogglePlayClickListener);
                updatePlayIcon();
            } else {
                fraction = 1.0f - fraction * 2.0f;
                mPlayIconActive = false;
                mPlayHintButton.setOnClickListener(mCloseHintClickListener);
                mPlayHintButton.setBackground(mContext.getResources().getDrawable(R.drawable.ic_close_hint));
            }
            mPlayHintButton.setAlpha(fraction);
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
            if (mDraggingBorder == 0) {
                mIsOpen = false;
                return;
            }
            if (mDraggingBorder == rangeToCheck) {
                mIsOpen = true;
                return;
            }
            boolean settleToOpen = false;
            if (yvel > AUTO_OPEN_SPEED_LIMIT) {
                settleToOpen = true;
            } else if (yvel < -AUTO_OPEN_SPEED_LIMIT) {
                settleToOpen = false;
            } else if (mDraggingBorder > rangeToCheck / 2) {
                settleToOpen = true;
            } else if (mDraggingBorder < rangeToCheck / 2) {
                settleToOpen = false;
            }

            final int settleDestY = settleToOpen ? mVerticalRange : 0;
            mIsOpen = settleToOpen;

            if (mDragHelper.settleCapturedViewAt(0, settleDestY)) {
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
        mAllowDrag = true;
        mIsDragging = false;
        mIsSeeking = false;
        mPlayIconActive = true;
        mDraggingState = 0;

        mDraggable = (RelativeLayout) findViewById(R.id.draggable);
        mSongHint = (LinearLayout) findViewById(R.id.song_hint);
        mPlayHintButton = (Button) findViewById(R.id.play_hint_button);
        mHintTitle = (TextView) findViewById(R.id.hint_title);
        mHintArtist = (TextView) findViewById(R.id.hint_artist);
        mSeekBar = (SeekBar) findViewById(R.id.seek_bar);
        mHint = (LinearLayout) findViewById(R.id.hint);
        mNextHintButton = findViewById(R.id.next_hint_button);

        mLastMainBut = (Button) findViewById(R.id.last_main_button);
        mPlayMainBut = (Button) findViewById(R.id.play_main_button);
        mNextMainBut = (Button) findViewById(R.id.next_main_button);

        mRunningTime = (TextView) findViewById(R.id.running_time);
        mTotalTime = (TextView) findViewById(R.id.total_time);

        mArtworkView = (ImageView) findViewById(R.id.album_art_view);

        mLastMainBut.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mContext.mTheBrain.playLast();
            }
        });

        mNextMainBut.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mContext.mTheBrain.playNext();
            }
        });

        mPlayMainBut.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mContext.mTheBrain.togglePlay();
            }
        });

        mNextHintButton.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        MarginLayoutParams layoutParams = (MarginLayoutParams) mNextHintButton.getLayoutParams();
        mItemWidth = mNextHintButton.getMeasuredWidth() + 2 * layoutParams.leftMargin;

        updateScreenHeight();

        mTogglePlayClickListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                mContext.mTheBrain.togglePlay();
            }
        };

        mCloseHintClickListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                closeView();
            }
        };

        mPlayHintButton.setOnClickListener(mTogglePlayClickListener);

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && mIsSeeking) {
                    mRunningTime.setText(ListAdapter.displayTime(Integer.toString(progress)));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mIsSeeking = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mContext.updateMediaPlayerProgress(seekBar.getProgress());
                mIsSeeking = false;
            }
        });

        mSongHint.setOnTouchListener(new OnTouchListener() {
            private int mX;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_MOVE: {
                        if (mIsDragging) {
                            int diffX = (int) event.getX() - mX;
                            if (Math.abs(diffX) < mItemWidth) {
                                mHint.setX((float) diffX);
                            } else if (diffX >= mItemWidth) {
                                mHint.setX((float) mItemWidth);
                            } else if (diffX <= -mItemWidth) {
                                mHint.setX((float) -mItemWidth);
                            }
                            if (Math.abs(diffX) > mItemWidth / 8) {
                                mAllowDrag = false;
                            } else {
                                mAllowDrag = true;
                            }
                        }
                        break;
                    }
                    case MotionEvent.ACTION_DOWN: {
                        if (mIsOpen) {
                            mIsDragging = true;
                        }
                        mX = (int) event.getX();
                        break;
                    }
                    case MotionEvent.ACTION_CANCEL:
                    case MotionEvent.ACTION_UP: {
                        if (mIsDragging && (int) event.getX() - mX >= mItemWidth) {
                            mContext.mTheBrain.playLast();
                        } else if (mIsDragging && (int) event.getX() - mX <= -mItemWidth) {
                            mContext.mTheBrain.playNext();
                        } else if (mAllowDrag) {
                            if (mIsOpen) {
                                openView();
                            } else {
                                closeView();
                            }
                        }
                        mIsDragging = false;
                        break;
                    }
                    default: {
                        Log.e("MotionEvent", "Event: " + event.getAction());
                        break;
                    }
                }
                if (!mIsDragging) {
                    mHint.setX(0);
                    mAllowDrag = true;
                }
                return false;
            }
        });
    }

    private void updateScreenHeight() {
        Display display = mContext.getWindowManager().getDefaultDisplay();
        Window window = mContext.getWindow();
        Point size = new Point();
        Rect rectangle = new Rect();
        display.getSize(size);
        window.getDecorView().getWindowVisibleDisplayFrame(rectangle);
        mScreenHeight = size.y - rectangle.top;
    }

    public void setProgress(int progress) {
        if (!mIsSeeking) {
            mRunningTime.setText(ListAdapter.displayTime(Integer.toString(progress)));
            mSeekBar.setProgress(progress);
        }
    }

    public int updateDefaultLocation() {
        return updateDefaultLocation(mScreenHeight);
    }

    public int updateDefaultLocation(int screenHeight) {
        int height;
        if (mActive) {
            height = screenHeight - mSongHint.getMeasuredHeight();
        } else {
            height = screenHeight;
        }

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mDraggable.getLayoutParams();
        params.topMargin = mIsOpen ? height : 0;
        params.bottomMargin = mIsOpen ? -height : 0;
        mDraggable.setLayoutParams(params);

        if (!mIsFirst) {
            if (mIsOpen) {
                if (mDragHelper.smoothSlideViewTo(mDraggable, 0, height)) {
                    mDragHelper.continueSettling(true);
                }
            } else {
                if (mDragHelper.smoothSlideViewTo(mDraggable, 0, 0)) {
                    mDragHelper.continueSettling(true);
                }
            }
        }

        return height;
    }

    public void updatePlayIcon() {
        if (mPlayIconActive) {
            if (mContext.mTheBrain.isPlaying()) {
                mPlayHintButton.setBackgroundResource(R.drawable.ic_pause_hint);
            } else {
                mPlayHintButton.setBackgroundResource(R.drawable.ic_play_hint);
            }
        }
        if (mContext.mTheBrain.isPlaying()) {
            mPlayMainBut.setBackgroundResource(R.drawable.ic_pause_hint_white);
        } else {
            mPlayMainBut.setBackgroundResource(R.drawable.ic_play_hint_white);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        updateScreenHeight();
        mVerticalRange = h - mSongHint.getMeasuredHeight();
        if (!mIsFirst) {
            updateDefaultLocation(h);
        } else {
            mIsFirst = false;
        }
        super.onSizeChanged(w, h, oldw, oldh);
    }

    private boolean isDraggableTarget(MotionEvent event) {
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
            if (isDraggableTarget(event) && mDragHelper.shouldInterceptTouchEvent(event) && mAllowDrag) {
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

    public void playSong(FireMixtape song) {
        if (!mContext.drawerOpen()) {
            mActive = true;
        }
        mSeekBar.setProgress(0);
        if (song == null) {
            return;
        }
        new GetArtworkTask().execute(song);
        mSeekBar.setMax(Integer.parseInt(song.duration));
        mTotalTime.setText(ListAdapter.displayTime(song.duration));
        mHintTitle.setText(song.title);
        mHintArtist.setText(song.artist);
        updateDefaultLocation();
    }

    private class GetArtworkTask extends AsyncTask<FireMixtape, Integer, Void> {
        private Bitmap mBM;

        @Override
        protected Void doInBackground(FireMixtape... params) {
            FireMixtape song = params[0];

            if (song.isSoundcloud) {
                mBM = AlbumArtHelper.decodeBitmapFromURL(song.album_art_url);
            } else {
                mBM = AlbumArtHelper.decodeBitmapFromAlbumId(mContext,
                        Long.parseLong(song.album_id));
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            mArtworkView.setImageBitmap(mBM);
        }
    }

    public void hideSong() {
        mIsOpen = true;
        mActive = false;
        updateDefaultLocation();
    }

    public void showSong() {
        mActive = true;
        updateDefaultLocation();
    }

    public void openView() {
        mIsOpen = false;
        updateDefaultLocation();
    }

    public void closeView() {
        mIsOpen = true;
        updateDefaultLocation();
    }
}
