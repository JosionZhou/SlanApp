package com.bigmercu.cBox;

import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.Checkable;
import com.sun.jna.platform.win32.WinError;
import com.sun.jna.platform.win32.Winspool;

/* loaded from: D:\Downloads\dex-tools-2.1-20150601.060031-26\dex2jar-2.1-SNAPSHOT\classes.dex */
public class CheckBox extends View implements Checkable {
    private static final String TAG = CheckBox.class.getSimpleName();
    private int AnimationValue;
    private int AnimationValue1;
    private float AnimationValue2;
    private float AnimationValue3;
    private float AnimationValue4;
    private int AnimationValueBorder;
    private int Duration;
    private int blueAfter;
    private int blueBefore;
    private String boxText;
    private float cAnimationValue;
    private float cAnimationValue1;
    private int cAnimationValue2;
    private ValueAnimator cValueAnimator;
    private ValueAnimator cValueAnimator1;
    private ValueAnimator cValueAnimator2;
    private boolean checked;
    private int greenAfter;
    private int greenBefore;
    private int hSize;
    private int hStart;
    private boolean isCircle;
    private boolean isHook;
    private boolean isShowBorder;
    private Path mDst;
    private Path mDstBorder;
    private OnCheckedChangeListener mOnCheckedChangeListener;
    private Paint mPaint;
    private Paint mPaintAfter;
    private int mPaintColor;
    private Paint mPaintText;
    private Path mPath;
    private Path mPathBorder;
    private int mScreenWidth;
    private int mSreenHeight;
    private float pathLenth;
    private float pathLenthBorder;
    private PathMeasure pathMeasure;
    private PathMeasure pathMeasureBorder;
    private int redAfter;
    private int redBefore;
    private int strokeWidth;
    private int textSize;
    private ValueAnimator valueAnimator;
    private ValueAnimator valueAnimator1;
    private ValueAnimator valueAnimator2;
    private ValueAnimator valueAnimator3;
    private ValueAnimator valueAnimator4;
    private ValueAnimator valueAnimatorBorder;
    private int wSize;
    private int wStart;

    public interface OnCheckedChangeListener {
        void onChange(boolean z);
    }

    public CheckBox(Context context) {
        super(context);
        this.hSize = dp2px(15.0f);
        this.wSize = dp2px(15.0f);
        this.textSize = dp2px(15.0f);
        this.wStart = dp2px(1.0f);
        this.hStart = dp2px(1.0f);
        this.Duration = 300;
        this.strokeWidth = dp2px(2.0f);
        this.boxText = "CheckBox";
        this.AnimationValue = 255;
        this.cAnimationValue = 0.0f;
        this.cAnimationValue1 = 0.0f;
        this.cAnimationValue2 = 255;
        this.checked = false;
        this.isHook = true;
        this.isShowBorder = false;
    }

    public CheckBox(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.hSize = dp2px(15.0f);
        this.wSize = dp2px(15.0f);
        this.textSize = dp2px(15.0f);
        this.wStart = dp2px(1.0f);
        this.hStart = dp2px(1.0f);
        this.Duration = 300;
        this.strokeWidth = dp2px(2.0f);
        this.boxText = "CheckBox";
        this.AnimationValue = 255;
        this.cAnimationValue = 0.0f;
        this.cAnimationValue1 = 0.0f;
        this.cAnimationValue2 = 255;
        this.checked = false;
        this.isHook = true;
        this.isShowBorder = false;
        this.mPaint = new Paint();
        if (attrs != null) {
            TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.BBox);
            int colorAfter = array.getColor(R.styleable.BBox_color_after, -7829368);
            int colorBefore = array.getColor(R.styleable.BBox_color_before, -7829368);
            this.redAfter = (colorAfter & Winspool.PRINTER_ENUM_ICONMASK) >> 16;
            this.greenAfter = (colorAfter & MotionEventCompat.ACTION_POINTER_INDEX_MASK) >> 8;
            this.blueAfter = colorAfter & 255;
            this.redBefore = (16711680 & colorBefore) >> 16;
            this.greenBefore = (colorBefore & MotionEventCompat.ACTION_POINTER_INDEX_MASK) >> 8;
            this.blueBefore = colorBefore & 255;
            this.boxText = array.getString(R.styleable.BBox_check_text);
            this.isHook = array.getInt(R.styleable.BBox_check_style, 1) == 1;
            this.isShowBorder = array.getBoolean(R.styleable.BBox_show_border, false);
            this.isCircle = array.getBoolean(R.styleable.BBox_is_circle_border, true);
            if (this.boxText == null) {
                this.boxText = "CheckBox";
            }
            array.recycle();
        }
        this.mPaint.setStrokeWidth(this.strokeWidth);
        this.mPaint.setStyle(Paint.Style.STROKE);
        this.mPaint.setAntiAlias(true);
        this.mPaint.setStrokeCap(Paint.Cap.SQUARE);
        this.mPaint.setDither(true);
        this.mPaintText = new Paint();
        this.mPaintText.setColor(ViewCompat.MEASURED_STATE_MASK);
        this.mPaintText.setAntiAlias(true);
        this.mPaintText.setStyle(Paint.Style.STROKE);
        this.mPaintText.setTextSize(this.textSize);
        this.mPaintText.setAntiAlias(true);
        this.mPath = new Path();
        this.mPathBorder = new Path();
        this.pathMeasure = new PathMeasure();
        this.pathMeasureBorder = new PathMeasure();
        this.mPath.addRect(this.wStart, this.hStart, this.wSize + wStart, this.hSize + hStart, Path.Direction.CW);
        this.pathMeasure.setPath(this.mPath, true);
        this.pathLenth = this.pathMeasure.getLength();
        Path path = this.mPathBorder;
        int i = this.wStart;
        int i2 = this.wSize;
        int i3 = this.hStart;
        int i4 = this.hSize;
        path.addCircle(i + (i2 / 2), i3 + (i4 / 2), (float) Math.sqrt(((i2 / 2) * (i2 / 2)) + ((i4 / 2) * (i4 / 2))), Path.Direction.CCW);
        this.pathMeasureBorder.setPath(this.mPathBorder, true);
        this.pathLenthBorder = this.pathMeasureBorder.getLength();
        this.mDst = new Path();
        this.mDstBorder = new Path();
        this.valueAnimator = ValueAnimator.ofInt(255, 0);
        this.valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.bigmercu.cBox.CheckBox.1
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator animation) {
                CheckBox.this.AnimationValue = ((Integer) animation.getAnimatedValue()).intValue();
                CheckBox.this.postInvalidate();
            }
        });
        this.valueAnimatorBorder = ValueAnimator.ofInt(0, (int) this.pathLenthBorder);
        this.valueAnimatorBorder.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.bigmercu.cBox.CheckBox.2
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator animation) {
                CheckBox.this.AnimationValueBorder = ((Integer) animation.getAnimatedValue()).intValue();
            }
        });
        if (this.isHook) {
            this.valueAnimator1 = ValueAnimator.ofInt(WinError.ERROR_INVALID_SEGMENT_NUMBER, WinError.ERROR_VIRUS_INFECTED);
            this.valueAnimator1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.bigmercu.cBox.CheckBox.3
                @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                public void onAnimationUpdate(ValueAnimator animation) {
                    CheckBox.this.AnimationValue1 = ((Integer) animation.getAnimatedValue()).intValue();
                }
            });
            int i5 = this.hSize;
            int i6 = this.wSize;
            this.valueAnimator2 = ValueAnimator.ofFloat(i5 + i6, (i6 * 2) / 5);
            this.valueAnimator2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.bigmercu.cBox.CheckBox.4
                @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                public void onAnimationUpdate(ValueAnimator animation) {
                    CheckBox.this.AnimationValue2 = ((Float) animation.getAnimatedValue()).floatValue();
                }
            });
            int i7 = this.hSize;
            int i8 = this.wSize;
            this.valueAnimator3 = ValueAnimator.ofFloat(i7 + i8, i7 + (i8 * 2));
            this.valueAnimator3.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.bigmercu.cBox.CheckBox.5
                @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                public void onAnimationUpdate(ValueAnimator animation) {
                    CheckBox.this.AnimationValue3 = ((Float) animation.getAnimatedValue()).floatValue();
                }
            });
            this.valueAnimator4 = ValueAnimator.ofFloat(0.0f, 0.207555f);
            this.valueAnimator4.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.bigmercu.cBox.CheckBox.6
                @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                public void onAnimationUpdate(ValueAnimator animation) {
                    CheckBox.this.AnimationValue4 = ((Float) animation.getAnimatedValue()).floatValue();
                }
            });
        } else {
            Double.isNaN(this.hSize);
            this.cValueAnimator = ValueAnimator.ofFloat(0.0f, (int) (hSize * 0.4d));
            this.cValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.bigmercu.cBox.CheckBox.7
                @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                public void onAnimationUpdate(ValueAnimator animation) {
                    CheckBox.this.cAnimationValue = ((Float) animation.getAnimatedValue()).floatValue();
                }
            });
            this.cValueAnimator1 = ValueAnimator.ofFloat(0.0f, this.wSize / 2);
            this.cValueAnimator1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.bigmercu.cBox.CheckBox.8
                @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                public void onAnimationUpdate(ValueAnimator animation) {
                    CheckBox.this.cAnimationValue1 = ((Float) animation.getAnimatedValue()).floatValue();
                }
            });
        }
        setOnClickListener(new OnClickListener() { // from class: com.bigmercu.cBox.CheckBox.9
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                CheckBox.this.setChecked(!checked);
            }
        });
    }

    public CheckBox(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.hSize = dp2px(15.0f);
        this.wSize = dp2px(15.0f);
        this.textSize = dp2px(15.0f);
        this.wStart = dp2px(1.0f);
        this.hStart = dp2px(1.0f);
        this.Duration = 300;
        this.strokeWidth = dp2px(2.0f);
        this.boxText = "CheckBox";
        this.AnimationValue = 255;
        this.cAnimationValue = 0.0f;
        this.cAnimationValue1 = 0.0f;
        this.cAnimationValue2 = 255;
        this.checked = false;
        this.isHook = true;
        this.isShowBorder = false;
    }

    @Override // android.view.View
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);
        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        int paddingLeft = getPaddingLeft() + 10;
        int paddingRight = getPaddingRight() + 10;
        int paddingTop = getPaddingTop() + 10;
        int paddingBottom = getPaddingBottom() + 10;
        if (widthSpecMode == Integer.MIN_VALUE && heightSpecMode == Integer.MIN_VALUE) {
            setMeasuredDimension((this.strokeWidth * 2) + this.wStart + this.wSize + 20 + paddingLeft + (this.textSize * this.boxText.length()), this.hStart + this.hSize + paddingTop + paddingBottom + (this.strokeWidth * 2));
            Log.d(TAG, this.wStart + " " + this.wSize + " " + (this.textSize * this.boxText.length()) + "   " + this.boxText.length());
            return;
        }
        if (widthSpecMode == Integer.MIN_VALUE) {
            setMeasuredDimension((this.strokeWidth * 2) + this.wStart + this.wSize + paddingLeft + 20 + (this.textSize * this.boxText.length()), widthSpecSize);
        } else if (heightSpecMode == Integer.MIN_VALUE) {
            setMeasuredDimension(heightSpecSize, this.hStart + this.hSize + paddingTop + paddingBottom + (this.strokeWidth * 2));
        }
    }

    @Override // android.view.View
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override // android.widget.Checkable
    public void setChecked(boolean set) {
        boolean z = this.checked;
        if (z == set) {
            return;
        }
        if (z) {
            animationReverse();
        } else {
            animationStart();
        }
        this.checked = set;
        OnCheckedChangeListener onCheckedChangeListener = this.mOnCheckedChangeListener;
        if (onCheckedChangeListener != null) {
            onCheckedChangeListener.onChange(this.checked);
        }
        postInvalidate();
    }

    @Override // android.widget.Checkable
    public boolean isChecked() {
        return this.checked;
    }

    @Override // android.widget.Checkable
    public void toggle() {
        setChecked(!this.checked);
    }

    public void setText(String text) {
        this.boxText = text;
        postInvalidate();
    }

    @Override // android.view.View
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.mScreenWidth = w;
        this.mSreenHeight = h;
    }

    @Override // android.view.View
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        this.mDst.reset();
        this.mDstBorder.reset();
        this.mDstBorder.lineTo(0.0f, 0.0f);
        this.mDst.lineTo(0.0f, 0.0f);
        int paddingLeft = getPaddingLeft() + 10;
        int paddingRight = getPaddingRight() + 10;
        int paddingTop = getPaddingTop() + 10;
        int paddingBottom = getPaddingBottom() + 10;
        int i = this.AnimationValue;
        int r = (int) (((1.0f - (i / 255.0f)) * this.redBefore) + ((i / 255.0f) * this.redAfter));
        int g = (int) (((1.0f - (i / 255.0f)) * this.greenBefore) + ((i / 255.0f) * this.greenAfter));
        int b = (int) (((1.0f - (i / 255.0f)) * this.blueBefore) + ((i / 255.0f) * this.blueAfter));
        this.mPaintColor = Color.rgb(r, g, b);
        this.mPaint.setColor(this.mPaintColor);
        if (this.isHook) {
            canvas.translate(this.wStart + paddingLeft, this.hStart + paddingTop);
            if (this.isShowBorder) {
                canvas.save();
            }
            canvas.drawText(this.boxText, this.wStart + this.wSize + 18, (this.hSize + this.textSize) / 2, this.mPaintText);
            float f = this.AnimationValue4;
            double d = (-f) * this.hSize;
            Double.isNaN(d);
            canvas.translate((-f) * this.wSize, (float) (d * 1.5d));
            this.pathMeasure.getSegment(this.AnimationValue3, this.pathLenth, this.mDst, true);
            this.pathMeasure.getSegment(0.0f, (this.wSize * 2) / 5, this.mDst, true);
            this.pathMeasure.getSegment((this.wSize * 2) / 5, this.AnimationValue2, this.mDst, true);
            canvas.rotate(this.AnimationValue1, this.wStart + (this.wSize / 2), this.hStart + (this.hSize / 2));
            canvas.drawPath(this.mDst, this.mPaint);
            if (this.isShowBorder) {
                canvas.restore();
                if (this.isCircle) {
                    this.pathMeasureBorder.getSegment(0.0f, this.AnimationValueBorder, this.mDstBorder, true);
                    canvas.drawPath(this.mDstBorder, this.mPaintText);
                    return;
                } else {
                    canvas.drawRect(this.wStart, this.hStart, this.wStart + this.wSize, this.hSize + this.hStart, this.mPaintText);
                    return;
                }
            }
            return;
        }
        canvas.translate(this.wStart + paddingLeft, this.hStart + paddingTop);
        if (this.isShowBorder) {
            canvas.save();
        }
        canvas.drawText(this.boxText, this.wStart + this.wSize + 18, (this.hSize + this.textSize) / 2, this.mPaintText);
        Path path = this.mDst;
        double d2 = this.wStart;
        float f2 = this.cAnimationValue;
        double d3 = f2;
        Double.isNaN(d3);
        Double.isNaN(d2);
        double d4 = this.hStart;
        double d5 = f2;
        Double.isNaN(d5);
        Double.isNaN(d4);
        path.moveTo((float) (d2 + (d3 * 0.3d)), (float) (d4 + (d5 * 0.3d)));
        Path path2 = this.mDst;
        float f3 = this.wStart + (this.wSize / 2);
        float f4 = this.hStart;
        float f5 = this.cAnimationValue;
        double d6 = f4 + f5;
        double d7 = f5;
        Double.isNaN(d7);
        Double.isNaN(d6);
        path2.lineTo(f3, (float) (d6 + (d7 * 0.2d)));
        Path path3 = this.mDst;
        double d8 = this.wStart + this.wSize;
        float f6 = this.cAnimationValue;
        double d9 = f6;
        Double.isNaN(d9);
        Double.isNaN(d8);
        float f7 = (float) (d8 - (d9 * 0.2d));
        double d10 = this.hStart;
        double d11 = f6;
        Double.isNaN(d11);
        Double.isNaN(d10);
        path3.lineTo(f7, (float) (d10 + (d11 * 0.2d)));
        Path path4 = this.mDst;
        float f8 = this.wStart + this.wSize;
        float f9 = this.cAnimationValue;
        double d12 = f8 - f9;
        double d13 = f9;
        Double.isNaN(d13);
        Double.isNaN(d12);
        path4.lineTo((float) (d12 - (d13 * 0.2d)), (this.hSize / 2) + this.hStart);
        Path path5 = this.mDst;
        double d14 = this.wStart + this.wSize;
        float f10 = this.cAnimationValue;
        double d15 = f10;
        Double.isNaN(d15);
        Double.isNaN(d14);
        float f11 = (float) (d14 - (d15 * 0.2d));
        double d16 = this.hStart + this.hSize;
        double d17 = f10;
        Double.isNaN(d17);
        Double.isNaN(d16);
        path5.lineTo(f11, (float) (d16 - (d17 * 0.2d)));
        Path path6 = this.mDst;
        float f12 = this.wStart + (this.wSize / 2);
        float f13 = this.hStart + this.hSize;
        float f14 = this.cAnimationValue;
        double d18 = f13 - f14;
        double d19 = f14;
        Double.isNaN(d19);
        Double.isNaN(d18);
        path6.lineTo(f12, (float) (d18 - (d19 * 0.2d)));
        Path path7 = this.mDst;
        double d20 = this.wStart;
        float f15 = this.cAnimationValue;
        double d21 = f15;
        Double.isNaN(d21);
        Double.isNaN(d20);
        float f16 = (float) (d20 + (d21 * 0.2d));
        double d22 = this.hStart + this.hSize;
        double d23 = f15;
        Double.isNaN(d23);
        Double.isNaN(d22);
        path7.lineTo(f16, (float) (d22 - (d23 * 0.2d)));
        Path path8 = this.mDst;
        float f17 = this.wStart;
        float f18 = this.cAnimationValue;
        double d24 = f17 + f18;
        double d25 = f18;
        Double.isNaN(d25);
        Double.isNaN(d24);
        path8.lineTo((float) (d24 + (d25 * 0.2d)), this.hStart + (this.hSize / 2));
        Path path9 = this.mDst;
        double d26 = this.wStart;
        float f19 = this.cAnimationValue;
        double d27 = f19;
        Double.isNaN(d27);
        Double.isNaN(d26);
        float f20 = (float) (d26 + (d27 * 0.3d));
        double d28 = this.hStart;
        double d29 = f19;
        Double.isNaN(d29);
        Double.isNaN(d28);
        path9.lineTo(f20, (float) (d28 + (d29 * 0.3d)));
        canvas.drawPath(this.mDst, this.mPaint);
        if (this.isShowBorder) {
            canvas.restore();
            if (this.isCircle) {
                this.pathMeasureBorder.getSegment(0.0f, this.AnimationValueBorder, this.mDstBorder, true);
                canvas.drawPath(this.mDstBorder, this.mPaintText);
            } else {
                canvas.drawRect(this.wStart, this.hStart, this.wStart + this.wSize, this.hSize + this.hStart, this.mPaintText);
            }
        }
    }

    private void animationStart() {
        if (this.isHook) {
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(this.valueAnimator1, this.valueAnimator2, this.valueAnimator3, this.valueAnimator4, this.valueAnimatorBorder, this.valueAnimator);
            animatorSet.setInterpolator(new DecelerateInterpolator());
            this.valueAnimatorBorder.setInterpolator(new AccelerateInterpolator());
            animatorSet.setDuration(this.Duration);
            animatorSet.start();
            return;
        }
        AnimatorSet animatorSet2 = new AnimatorSet();
        animatorSet2.playTogether(this.cValueAnimator, this.cValueAnimator1, this.valueAnimatorBorder, this.valueAnimator);
        animatorSet2.setInterpolator(new DecelerateInterpolator());
        this.valueAnimatorBorder.setInterpolator(new AccelerateInterpolator());
        animatorSet2.setDuration(this.Duration);
        animatorSet2.start();
    }

    private void animationReverse() {
        if (this.isHook) {
            this.valueAnimator1.reverse();
            this.valueAnimator2.reverse();
            this.valueAnimator3.reverse();
            this.valueAnimator4.reverse();
        } else {
            this.cValueAnimator.reverse();
            this.cValueAnimator1.reverse();
        }
        this.valueAnimatorBorder.reverse();
        this.valueAnimator.reverse();
    }

    public int dp2px(float value) {
        float density = getResources().getDisplayMetrics().density;
        return (int) ((value * density) + 0.5f);
    }

    public void setOnCheckedChangeListener(OnCheckedChangeListener listener) {
        this.mOnCheckedChangeListener = listener;
    }
}