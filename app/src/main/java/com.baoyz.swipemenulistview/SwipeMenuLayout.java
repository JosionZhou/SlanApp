package com.baoyz.swipemenulistview;

import android.content.Context;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.widget.ScrollerCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Interpolator;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import com.sun.jna.platform.win32.WinError;

/* loaded from: D:\Downloads\dex-tools-2.1-20150601.060031-26\dex2jar-2.1-SNAPSHOT\classes.dex */
public class SwipeMenuLayout extends FrameLayout {
    private static final int CONTENT_VIEW_ID = 1;
    private static final int MENU_VIEW_ID = 2;
    private static final int STATE_CLOSE = 0;
    private static final int STATE_OPEN = 1;
    private int MAX_VELOCITYX;
    private int MIN_FLING;
    private boolean isFling;
    private int mBaseX;
    private Interpolator mCloseInterpolator;
    private ScrollerCompat mCloseScroller;
    private View mContentView;
    private int mDownX;
    private GestureDetectorCompat mGestureDetector;
    private GestureDetector.OnGestureListener mGestureListener;
    private SwipeMenuView mMenuView;
    private Interpolator mOpenInterpolator;
    private ScrollerCompat mOpenScroller;
    private int mSwipeDirection;
    private int position;
    private int state;

    public SwipeMenuLayout(View contentView, SwipeMenuView menuView) {
        this(contentView, menuView, null, null);
    }

    public SwipeMenuLayout(View contentView, SwipeMenuView menuView, Interpolator closeInterpolator, Interpolator openInterpolator) {
        super(contentView.getContext());
        this.state = 0;
        this.MIN_FLING = dp2px(15);
        this.MAX_VELOCITYX = -dp2px(500);
        this.mCloseInterpolator = closeInterpolator;
        this.mOpenInterpolator = openInterpolator;
        this.mContentView = contentView;
        this.mMenuView = menuView;
        this.mMenuView.setLayout(this);
        init();
    }

    private SwipeMenuLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.state = 0;
        this.MIN_FLING = dp2px(15);
        this.MAX_VELOCITYX = -dp2px(500);
    }

    private SwipeMenuLayout(Context context) {
        super(context);
        this.state = 0;
        this.MIN_FLING = dp2px(15);
        this.MAX_VELOCITYX = -dp2px(500);
    }

    public int getPosition() {
        return this.position;
    }

    public void setPosition(int position) {
        this.position = position;
        this.mMenuView.setPosition(position);
    }

    public void setSwipeDirection(int swipeDirection) {
        this.mSwipeDirection = swipeDirection;
    }

    private void init() {
        setLayoutParams(new AbsListView.LayoutParams(-1, -2));
        this.mGestureListener = new GestureDetector.SimpleOnGestureListener() { // from class: com.baoyz.swipemenulistview.SwipeMenuLayout.1
            @Override // android.view.GestureDetector.SimpleOnGestureListener, android.view.GestureDetector.OnGestureListener
            public boolean onDown(MotionEvent e) {
                SwipeMenuLayout.this.isFling = false;
                return true;
            }

            @Override // android.view.GestureDetector.SimpleOnGestureListener, android.view.GestureDetector.OnGestureListener
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (Math.abs(e1.getX() - e2.getX()) > SwipeMenuLayout.this.MIN_FLING && velocityX < SwipeMenuLayout.this.MAX_VELOCITYX) {
                    SwipeMenuLayout.this.isFling = true;
                }
                return super.onFling(e1, e2, velocityX, velocityY);
            }
        };
        this.mGestureDetector = new GestureDetectorCompat(getContext(), this.mGestureListener);
        if (this.mCloseInterpolator != null) {
            this.mCloseScroller = ScrollerCompat.create(getContext(), this.mCloseInterpolator);
        } else {
            this.mCloseScroller = ScrollerCompat.create(getContext());
        }
        if (this.mOpenInterpolator != null) {
            this.mOpenScroller = ScrollerCompat.create(getContext(), this.mOpenInterpolator);
        } else {
            this.mOpenScroller = ScrollerCompat.create(getContext());
        }
        LayoutParams contentParams = new LayoutParams(-1, -2);
        this.mContentView.setLayoutParams(contentParams);
        if (this.mContentView.getId() < 1) {
            this.mContentView.setId(1);
        }
        this.mMenuView.setId(2);
        this.mMenuView.setLayoutParams(new LayoutParams(-2, -2));
        addView(this.mContentView);
        addView(this.mMenuView);
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override // android.view.View
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    public boolean onSwipe(MotionEvent event) {
        this.mGestureDetector.onTouchEvent(event);
        int action = event.getAction();
        if (action == 0) {
            this.mDownX = (int) event.getX();
            this.isFling = false;
        } else if (action != 1) {
            if (action == 2) {
                int dis = (int) (this.mDownX - event.getX());
                if (this.state == 1) {
                    dis += this.mMenuView.getWidth() * this.mSwipeDirection;
                }
                swipe(dis);
            }
        } else if ((this.isFling || Math.abs(this.mDownX - event.getX()) > this.mMenuView.getWidth() / 2) && Math.signum(this.mDownX - event.getX()) == this.mSwipeDirection) {
            smoothOpenMenu();
        } else {
            smoothCloseMenu();
            return false;
        }
        return true;
    }

    public boolean isOpen() {
        return this.state == 1;
    }

    @Override // android.view.View
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }

    private void swipe(int dis) {
        if (Math.signum(dis) != this.mSwipeDirection) {
            dis = 0;
        } else if (Math.abs(dis) > this.mMenuView.getWidth()) {
            dis = this.mMenuView.getWidth() * this.mSwipeDirection;
        }
        View view = this.mContentView;
        view.layout(-dis, view.getTop(), this.mContentView.getWidth() - dis, getMeasuredHeight());
        if (this.mSwipeDirection == 1) {
            this.mMenuView.layout(this.mContentView.getWidth() - dis, this.mMenuView.getTop(), (this.mContentView.getWidth() + this.mMenuView.getWidth()) - dis, this.mMenuView.getBottom());
        } else {
            SwipeMenuView swipeMenuView = this.mMenuView;
            swipeMenuView.layout((-swipeMenuView.getWidth()) - dis, this.mMenuView.getTop(), -dis, this.mMenuView.getBottom());
        }
    }

    @Override // android.view.View
    public void computeScroll() {
        if (this.state == 1) {
            if (this.mOpenScroller.computeScrollOffset()) {
                swipe(this.mOpenScroller.getCurrX() * this.mSwipeDirection);
                postInvalidate();
                return;
            }
            return;
        }
        if (this.mCloseScroller.computeScrollOffset()) {
            swipe((this.mBaseX - this.mCloseScroller.getCurrX()) * this.mSwipeDirection);
            postInvalidate();
        }
    }

    public void smoothCloseMenu() {
        this.state = 0;
        if (this.mSwipeDirection == 1) {
            this.mBaseX = -this.mContentView.getLeft();
            this.mCloseScroller.startScroll(0, 0, this.mMenuView.getWidth(), 0, WinError.ERROR_FAIL_NOACTION_REBOOT);
        } else {
            this.mBaseX = this.mMenuView.getRight();
            this.mCloseScroller.startScroll(0, 0, this.mMenuView.getWidth(), 0, WinError.ERROR_FAIL_NOACTION_REBOOT);
        }
        postInvalidate();
    }

    public void smoothOpenMenu() {
        this.state = 1;
        if (this.mSwipeDirection == 1) {
            this.mOpenScroller.startScroll(-this.mContentView.getLeft(), 0, this.mMenuView.getWidth(), 0, WinError.ERROR_FAIL_NOACTION_REBOOT);
        } else {
            this.mOpenScroller.startScroll(this.mContentView.getLeft(), 0, this.mMenuView.getWidth(), 0, WinError.ERROR_FAIL_NOACTION_REBOOT);
        }
        postInvalidate();
    }

    public void closeMenu() {
        if (this.mCloseScroller.computeScrollOffset()) {
            this.mCloseScroller.abortAnimation();
        }
        if (this.state == 1) {
            this.state = 0;
            swipe(0);
        }
    }

    public void openMenu() {
        if (this.state == 0) {
            this.state = 1;
            swipe(this.mMenuView.getWidth() * this.mSwipeDirection);
        }
    }

    public View getContentView() {
        return this.mContentView;
    }

    public SwipeMenuView getMenuView() {
        return this.mMenuView;
    }

    private int dp2px(int dp) {
        return (int) TypedValue.applyDimension(1, dp, getContext().getResources().getDisplayMetrics());
    }

    @Override // android.widget.FrameLayout, android.view.View
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        this.mMenuView.measure(MeasureSpec.makeMeasureSpec(0, 0), MeasureSpec.makeMeasureSpec(getMeasuredHeight(), 1073741824));
    }

    @Override // android.widget.FrameLayout, android.view.ViewGroup, android.view.View
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        this.mContentView.layout(0, 0, getMeasuredWidth(), this.mContentView.getMeasuredHeight());
        if (this.mSwipeDirection == 1) {
            this.mMenuView.layout(getMeasuredWidth(), 0, getMeasuredWidth() + this.mMenuView.getMeasuredWidth(), this.mContentView.getMeasuredHeight());
        } else {
            SwipeMenuView swipeMenuView = this.mMenuView;
            swipeMenuView.layout(-swipeMenuView.getMeasuredWidth(), 0, 0, this.mContentView.getMeasuredHeight());
        }
    }

    public void setMenuHeight(int measuredHeight) {
        Log.i("byz", "pos = " + this.position + ", height = " + measuredHeight);
        LayoutParams params = (LayoutParams) this.mMenuView.getLayoutParams();
        if (params.height != measuredHeight) {
            params.height = measuredHeight;
            SwipeMenuView swipeMenuView = this.mMenuView;
            swipeMenuView.setLayoutParams(swipeMenuView.getLayoutParams());
        }
    }
}