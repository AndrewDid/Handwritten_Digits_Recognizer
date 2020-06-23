package com.example.digitsrecognition;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;

public class PaintView extends View
{

    public static final int DEFAULT_BG_COLOR = Color.WHITE;
    public static int BRUSH_SIZE = 70;

    private int DEFAULT_COLOR = Color.BLACK;
    private int _backgroundColor = DEFAULT_BG_COLOR;
    private static final float TOUCH_TOLERANCE = 4;
    private float _X, _Y;
    private Path _path;
    private Paint _paint;
    private ArrayList<ArrayList<Path>> _paths = new ArrayList<>();
    private Bitmap _bitmap;
    private Canvas _canvas;
    private Paint _bitmapPaint = new Paint(Paint.DITHER_FLAG);
    private IRecognition _recognitionListiner;


    public PaintView(Context context)
    {
        this(context, null);
    }

    public PaintView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        _paint = new Paint();
        _paint.setAntiAlias(true);
        _paint.setDither(true);
        _paint.setColor(DEFAULT_COLOR);
        _paint.setStyle(Paint.Style.STROKE);
        _paint.setStrokeJoin(Paint.Join.ROUND);
        _paint.setStrokeCap(Paint.Cap.ROUND);
        _paint.setXfermode(null);
        _paint.setAlpha(0xff);
    }

    public void init(IRecognition recog, DisplayMetrics metrics)
    {
        int height = metrics.heightPixels;
        int width = metrics.widthPixels;

        _bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        _canvas = new Canvas(_bitmap);
        _canvas.drawColor(_backgroundColor);
        _recognitionListiner = recog;
    }

    public void clear()
    {
        _backgroundColor = DEFAULT_BG_COLOR;
        _canvas.drawColor(_backgroundColor);
        _paths.clear();
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        canvas.save();

        if(_paths.size() > 0)
            for (Path fp : _paths.get(_paths.size() - 1))
            {
                _paint.setColor(DEFAULT_COLOR);
                _paint.setStrokeWidth(BRUSH_SIZE);
                _paint.setMaskFilter(null);
                _canvas.drawPath(fp, _paint);
            }

        canvas.drawBitmap(_bitmap, 0, 0, _bitmapPaint);
        canvas.restore();
    }

    private void touchStart(float x, float y)
    {
        _path = new Path();
        _paths.add(new ArrayList<Path>());
        _paths.get(_paths.size() - 1).add(_path);

        _path.reset();
        _path.moveTo(x, y);
        _X = x;
        _Y = y;
        _path.lineTo(_X, _Y);
    }

    private void touchMove(float x, float y)
    {
        float dx = Math.abs(x - _X);
        float dy = Math.abs(y - _Y);

        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE)
        {
            _path.quadTo(_X, _Y, (x + _X) / 2, (y + _Y) / 2);
            _X = x;
            _Y = y;
        }
    }

    private void touchUp()
    {
        _path.lineTo(_X, _Y);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        float x = event.getX();
        float y = event.getY();

        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN :
                touchStart(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE :
                touchMove(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP :
                touchUp();
                invalidate();
                _recognitionListiner.startRecognition();
                break;
        }
        return true;
    }

    public boolean isEmpty()
    {
        return _paths.isEmpty();
    }

    public Bitmap getBitmap()
    {
        return _bitmap;
    }

    int getBrushColor()
    {
        return DEFAULT_COLOR;
    }

    void setBrushColor(int color)
    {
        DEFAULT_COLOR = color;
    }
}