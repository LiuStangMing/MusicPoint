package com.ccc.musicpoint.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import com.ccc.musicpoint.R;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by ccc on 2018/2/11.
 * 仿网易云的唱片UI
 */
public class MusicPoint extends View {

    private static final String TAG = "MusicPoint";

    private static final int PLAY_DEGREE = -15;
    private static final int PAUSE_DEGREE = -45;

    private ImagesDownloadTask mTask;
    /**
     * 唱针当前角度
     */
    private int currentNeedleDegree = PAUSE_DEGREE;
    /**
     * 唱片当前角度
     */
    private int currentRotate = 0;

    private Paint needlePaint;
    private Paint discPaint;
    /**
     * 唱针的两段长度
     */
    private int longArmLength = 200;
    private int shortArmLength = 120;
    /**
     * 唱针头的两段长度
     */
    private int longHeadLength = 100;
    private int shortHeadLength = 50;
    /**
     * 顶部两个大小圆的半径
     */
    private int bigCircleRadius = 50;
    private int smallCircleRadius = 30;
    /**
     * 外圆环的宽度
     */
    private int circleWidth;
    /**
     * 图片的宽度
     */
    private int pictureWidth;

    private Bitmap mBitmap;
    private Bitmap mNeedleBitmap;
    private int mNeedleDragee;

    private int imageResource;
    private int circleColor;

    private Path clipPath;
    /**
     * 唱片图片显示的区域
     */
    private RectF desRect;
    /**
     * 唱针图片显示区域
     */
    private RectF needleRect;
    /**
     * 屏幕宽度
     */
    private int mScreenWidth;
    /**
     * 播放标志位
     */
    private boolean isStart;
    /**
     * 唱片的旋转速度
     */
    private float diskRotateSpeed = 1;
    /**
     * 边距
     */
    private int mPadding;

    public MusicPoint(Context context) {
        this(context, null);
    }

    public MusicPoint(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MusicPoint(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.MusicPoint, defStyleAttr, 0);
        imageResource = ta.getResourceId(R.styleable.MusicPoint_src, 0);
        circleColor = ta.getColor(R.styleable.MusicPoint_circleColor, 0);
        ta.recycle();

        init();
    }

    private void init() {
        mTask = new ImagesDownloadTask();
        //上下左右padding取最小值
        mPadding = Math.min(Math.min(getPaddingLeft(), getPaddingRight()),
                Math.min(getPaddingTop(), getPaddingBottom()));
        needlePaint = new Paint();
        needlePaint.setAntiAlias(true);
        discPaint = new Paint();
        discPaint.setStyle(Paint.Style.STROKE);
        discPaint.setAntiAlias(true);

        clipPath = new Path();
        mScreenWidth = getResources().getDisplayMetrics().widthPixels;

        if (circleColor != 0) {
            discPaint.setColor(circleColor);
        }
        if (imageResource != 0) {
            mBitmap = BitmapFactory.decodeResource(getResources(), imageResource);
            initBitmap();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);

        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        int width, height;

        if (widthMode == MeasureSpec.AT_MOST) {
            width = mScreenWidth - (mPadding * 2);
        } else {
            width = widthSize - (mPadding * 2);
        }

        if (heightMode == MeasureSpec.AT_MOST) {
            height = mScreenWidth - (mPadding * 2);
        } else {
            height = heightSize - (mPadding * 2);
        }

        setMeasuredDimension(width, height);

    }

    /**
     * 获取bitmap参数
     */
    private void initBitmap() {

        if (mBitmap == null) {
            return;
        }

        int width = mBitmap.getWidth();
        int height = mBitmap.getHeight();
        pictureWidth = Math.min(width, height);
        if (pictureWidth >= mScreenWidth / 2) {
            pictureWidth = mScreenWidth / 2;
        }
        Log.e(TAG, "pictureWidth = " + pictureWidth);

        circleWidth = pictureWidth / 4 - (2 * mPadding);
        discPaint.setStrokeWidth(circleWidth);

        int left = -pictureWidth / 2 + -circleWidth - mPadding;
        int top = -pictureWidth / 2 + -circleWidth - mPadding;
        desRect = new RectF(left, top, pictureWidth / 2 + circleWidth - mPadding,
                pictureWidth / 2 + circleWidth - mPadding);
        clipPath.addCircle(0, 0, pictureWidth / 2 + circleWidth - mPadding, Path.Direction.CW);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (isStart) {
            currentRotate += diskRotateSpeed;
            if (currentRotate / 360 == 1) {
                currentRotate = 0;
            }
        }
        drawDisk(canvas, currentRotate);
        if (mNeedleBitmap != null) {
            drawNeedleforImage(canvas,mNeedleDragee);
        } else {
            drawNeedle(canvas, currentNeedleDegree);
        }
        if (currentNeedleDegree > PAUSE_DEGREE) {
            invalidate();
        }
    }

    private void drawDisk(Canvas canvas, int degree) {
        canvas.save();
        canvas.translate(getMeasuredWidth() / 2 + mPadding, longArmLength + circleWidth
                + pictureWidth / 2);
        canvas.rotate(degree);
        canvas.drawCircle(0, 0, pictureWidth / 2 + circleWidth - mPadding, discPaint);
        canvas.clipPath(clipPath);
        if (mBitmap != null) {
            canvas.drawBitmap(mBitmap, null, desRect, discPaint);
        }
        canvas.restore();
    }

    private void drawNeedleforImage(Canvas canvas, int degree) {
        canvas.save();
        canvas.translate(getMeasuredWidth() / 2 + mPadding,0);
        canvas.rotate(degree);
        canvas.drawBitmap(mNeedleBitmap,null,needleRect,null);
        canvas.restore();
    }

    public void drawNeedle(Canvas canvas, int degree) {
        //水平居中
        canvas.save();
        canvas.translate(getMeasuredWidth() / 2 + mPadding, 0);
        //准备绘制唱针手臂
        needlePaint.setStrokeWidth(20);
        needlePaint.setColor(Color.parseColor("#C0C0C0"));
        //绘制第一段
        canvas.rotate(degree);
        canvas.drawLine(0, 0, 0, longArmLength, needlePaint);
        //绘制第二段
        canvas.translate(0, longArmLength - 5);
        canvas.rotate(-30);
        canvas.drawLine(0, 0, 0, shortArmLength, needlePaint);
        //绘制唱针头
        //绘制第一段唱针
        canvas.translate(0, shortArmLength);
        needlePaint.setStrokeWidth(40);
        canvas.drawLine(0, 0, 0, longHeadLength, needlePaint);
        //绘制第二段
        canvas.translate(0, longHeadLength);
        needlePaint.setStrokeWidth(60);
        canvas.drawLine(0, 0, 0, shortHeadLength, needlePaint);
        canvas.restore();
        //两个重叠的圆形，唱针顶部的旋转点
        canvas.save();
        canvas.translate(getMeasuredWidth() / 2 + mPadding, 0);
        needlePaint.setStyle(Paint.Style.FILL);
        needlePaint.setColor(Color.parseColor("#C0C0C0"));
        canvas.drawCircle(0, 0, bigCircleRadius, needlePaint);
        needlePaint.setColor(Color.parseColor("#8A8A8A"));
        canvas.drawCircle(0, 0, smallCircleRadius, needlePaint);
        canvas.restore();
    }

    /**
     * 资源加载图片
     *
     * @param resource 图片id
     */
    public void setImageResource(int resource) {
        if (mBitmap != null) {
            mBitmap.recycle();
            mBitmap = null;
        }
        mBitmap = BitmapFactory.decodeResource(getResources(), resource);
        initBitmap();
    }

    /**
     * 网络加载图片
     *
     * @param url url地址
     */
    public void setImageUrl(String url) {
        if (mTask == null) {
            return;
        }
        if (url == null && "".equals(url)) {
            return;
        }
        mTask.execute(url);
    }

    /**
     * 开始
     */
    public void start() {
        currentNeedleDegree = PLAY_DEGREE;
        if (mNeedleDragee != 0){
            mNeedleDragee = PLAY_DEGREE;
        }
        isStart = true;
        invalidate();
    }

    /**
     * 暂停
     */
    public void pause() {
        currentNeedleDegree = PAUSE_DEGREE;
        if (mNeedleDragee != 0){
            mNeedleDragee = PAUSE_DEGREE;
        }
        isStart = false;
    }

    public boolean isStart() {
        return isStart;
    }

    /**
     * 设置旋转速度
     *
     * @param speed 旋转的速度
     */
    public void setSpeed(int speed) {
        if (speed == 0) {
            diskRotateSpeed = 1;
        } else {
            diskRotateSpeed = speed;
        }
    }

    /**
     * 设置外环的颜色
     */
    public void setCircleColor(int color) {
        discPaint.setColor(color);
    }

    /**
     * 设置唱针的图片
     */
    public void setNeedleImage(int imageResource,int dragee) {
        mNeedleBitmap = BitmapFactory.decodeResource(getResources(), imageResource);
        mNeedleDragee = dragee;
        if (mNeedleBitmap == null) {
            return;
        }
        computeNeedleBitmap();
    }

    private void computeNeedleBitmap() {
        int width = mNeedleBitmap.getWidth();
        int height = mNeedleBitmap.getHeight();

        if (width > mScreenWidth) {
            width = mScreenWidth / 8;
        }

        if (height > mScreenWidth) {
            height = mScreenWidth / 2;
        }

        Log.e(TAG, "needle bitmap width = " + width + " and height = " + height);
        needleRect = new RectF();
        int left = -(width / 2);
        int top = 0;
        needleRect.set(left, top, left + width, top + height);
    }

    /**
     * 简易图片下载器
     */
    private class ImagesDownloadTask extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... strings) {
            String imgUrl = strings[0];
            Log.e(TAG, "url = " + imgUrl);
            InputStream is;
            try {
                URL connUrl = new URL(imgUrl);
                HttpURLConnection conn = (HttpURLConnection) connUrl.openConnection();
                conn.setConnectTimeout(10000);
                conn.setRequestMethod("GET");
                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    is = conn.getInputStream();
                    //TODO 图片压缩
                    return BitmapFactory.decodeStream(is);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            Log.e(TAG, "bitmap = " + bitmap);
            if (bitmap != null) {
                if (mBitmap != null) {
                    mBitmap.recycle();
                    mBitmap = null;
                }
                mBitmap = bitmap;
                initBitmap();
                invalidate();
            }
        }
    }
}
