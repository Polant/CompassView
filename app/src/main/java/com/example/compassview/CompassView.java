package com.example.compassview;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;

/**
 * Created by Антон on 14.06.2015.
 */
public class CompassView extends View {

    public CompassView(Context context, AttributeSet attrs, int defaultStyle) {
        super(context, attrs, defaultStyle);
        initCompassView();
    }

    public CompassView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initCompassView();
    }

    public CompassView(Context context) {
        super(context);
        initCompassView();
    }


    private Paint markerPaint;
    private Paint textPaint;
    private Paint circlePaint;
    private String northString;
    private String southString;
    private String eastString;
    private String westString;
    private int textHeight;


    protected void initCompassView() {
        setFocusable(true);

        //тут инициализируем поля из полученных ресурсов.
        Resources res = getResources();

        circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        circlePaint.setColor(res.getColor(R.color.background_color));
        circlePaint.setStrokeWidth(1);
        circlePaint.setStyle(Paint.Style.FILL_AND_STROKE);

        northString = res.getString(R.string.cardinal_north);
        southString = res.getString(R.string.cardinal_south);
        eastString = res.getString(R.string.cardinal_east);
        westString = res.getString(R.string.cardinal_west);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(res.getColor(R.color.text_color));

        textHeight = (int)textPaint.measureText("yY");

        markerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        markerPaint.setColor(res.getColor(R.color.marker_color));
    }

    //-----------------------------------------------//
    //нужно переопределить onMeasure().
    @Override
    protected void onMeasure(int widthMeasureSpec, int heigthMeasureSpec) {
        // Компас представляет собой окружность, занимающую все доступное пространство.
        // Установим размеры элемента, вычислив короткую грань (высоту или ширину).

        int measureWidth = this.measure(widthMeasureSpec);
        int measureHeight = this.measure(heigthMeasureSpec);

        int diametr = Math.min(measureWidth, measureHeight);

        setMeasuredDimension(diametr, diametr);
    }

    private int measure(int measureSpec) {
        int result = 0;

        // Декодируем параметр measureSpec.
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.UNSPECIFIED) {
            // Если границы не указаны, вернем размер по умолчанию (200).
            result = 200;
        } else {
            // Так как нужно заполнить все доступное пространство,
            // всегда возвращаем максимальный доступный размер.
            result = specSize;
        }
        return result;
    }
    //-----------------------------------------------//

    //также необходимо переопределить метод onDraw().
    @Override
    public void onDraw(Canvas canvas)
    {
        int mMeasureWidth = getMeasuredWidth();
        int mMeasureHeigth = getMeasuredHeight();

        int px = mMeasureWidth / 2;
        int py = mMeasureHeigth / 2;

        int radius = Math.min(px, py);

        //нарисуем фоновую окружность.
        canvas.drawCircle(px, py, radius, circlePaint);

        // Поворачиваем ракурс таким образом, чтобы
        // "верх" всегда указывал на текущее направление.
        canvas.save();
        canvas.rotate(-bearing, px, py);

        int textWidth = (int)textPaint.measureText("W");
        int cardinalX = px - textWidth/2;
        int cardinalY = py - radius + textHeight;

        for(int i = 0; i < 24; i++)
        {
            canvas.drawLine(px, py - radius, px, py - radius + 10, markerPaint);

            canvas.save();
            canvas.translate(0, textHeight);

            if(i % 6 == 0)
            {
                String dirString = "";
                switch (i)
                {
                    case(0) : {
                        dirString = northString;
                        int arrowY = 2 * textHeight;
                        canvas.drawLine(px, arrowY, px - 5, 3 * textHeight,
                                markerPaint);
                        canvas.drawLine(px, arrowY, px + 5, 3 * textHeight,
                                markerPaint);
                        break;
                    }
                    case(6):	dirString	=	eastString;	break;
                    case(12):	dirString	=	southString; break;
                    case(18):	dirString	=	westString;	break;
                }
                canvas.drawText(dirString, cardinalX, cardinalY, textPaint);
            }
            else if(i % 3 == 0)
            {
                // Отображаем текст каждые 45°.
                String angle = String.valueOf(i * 15);
                float angleTextWidth = textPaint.measureText(angle);
                int angleTextX = (int)(px-angleTextWidth / 2);
                int angleTextY = py - radius + textHeight;
                canvas.drawText(angle, angleTextX, angleTextY, textPaint);
            }

            canvas.restore();
            canvas.rotate(15, px, py);
        }

        canvas.restore();
    }

    //свойство для хранения направляеня и его геттеры, сеттеры.
    private float bearing;
    public void setBearing(float _bearing) {
        bearing = _bearing;
        //проверка доступности.
        sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED);
    }
    public float getBearing() {
        return bearing;
    }

    //далее обеспечим проверку доступности.
    @Override
    public boolean dispatchPopulateAccessibilityEvent(final AccessibilityEvent event)
    {
        super.dispatchPopulateAccessibilityEvent(event);

        if (isShown()){
            String bearingStr = String.valueOf(bearing);

            if(bearingStr.length() > AccessibilityEvent.MAX_TEXT_LENGTH) {
                bearingStr = bearingStr.substring(0, AccessibilityEvent.MAX_TEXT_LENGTH);
            }

            event.getText().add(bearingStr);
            return true;
        }
        else
            return false;
    }

}
