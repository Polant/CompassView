package com.example.compassview.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;

import com.example.compassview.R;

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

    private enum CompassDirection {
        N, NNE, NE, ENE,
        E, ESE, SE, SSE,
        S, SSW, SW, WSW,
        W, WNW, NW, NNW
    }

    private Paint markerPaint;
    private Paint textPaint;
    private Paint circlePaint;
    private String northString;
    private String southString;
    private String eastString;
    private String westString;
    private int textHeight;

    //свойство для хранения направляеня и его геттеры, сеттеры.
    private float bearing;

    //Продольный и поперечный наклон.
    private float pitch;
    private float roll;

    private int[] borderGradientColors;
    private float[] borderGradientPositions;
    private int[] glassGradientColors;
    private float[] glassGradientPositions;
    private int skyHorizonColorFrom;
    private int skyHorizonColorTo;
    private int groundHorizonColorFrom;
    private int groundHorizonColorTo;

    public void setBearing(float _bearing) {
        bearing = _bearing;
        //проверка доступности.
        sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED);
    }

    public float getBearing() {
        return bearing;
    }

    public float getRoll() {
        return roll;
    }

    public void setRoll(float roll) {
        this.roll = roll;
    }

    public float getPitch() {
        return pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    protected void initCompassView() {
        setFocusable(true);

        //тут инициализируем поля из полученных ресурсов.
        Resources res = getResources();

        circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        circlePaint.setColor(res.getColor(R.color.background_color));
        circlePaint.setStrokeWidth(1);
        circlePaint.setStyle(Paint.Style.STROKE);

        northString = res.getString(R.string.cardinal_north);
        southString = res.getString(R.string.cardinal_south);
        eastString = res.getString(R.string.cardinal_east);
        westString = res.getString(R.string.cardinal_west);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(res.getColor(R.color.text_color));
        textPaint.setFakeBoldText(true);
        textPaint.setSubpixelText(true);
        textPaint.setTextAlign(Paint.Align.LEFT);

        textHeight = (int)textPaint.measureText("yY");

        markerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        markerPaint.setColor(res.getColor(R.color.marker_color));
        markerPaint.setAlpha(200);
        markerPaint.setStrokeWidth(1);
        markerPaint.setStyle(Paint.Style.STROKE);
        markerPaint.setShadowLayer(2, 1, 1, res.getColor(R.color.shadow_color));


        borderGradientColors = new int[4];
        borderGradientPositions = new float[4];

        borderGradientColors[3] = res.getColor(R.color.outer_border);
        borderGradientColors[2] = res.getColor(R.color.inner_border_one);
        borderGradientColors[1] = res.getColor(R.color.inner_border_two);
        borderGradientColors[0] = res.getColor(R.color.inner_border);

        borderGradientPositions[3] = 0.0f;
        borderGradientPositions[2] = 1 - 0.3f;
        borderGradientPositions[1] = 1 - 0.3f;
        borderGradientPositions[0] = 1.0f;

        glassGradientColors = new int[5];
        glassGradientPositions = new float[5];

        int glassColor = 245;
        glassGradientColors[4] = Color.argb(65, glassColor, glassColor, glassColor);
        glassGradientColors[3] = Color.argb(100, glassColor, glassColor, glassColor);
        glassGradientColors[2] = Color.argb(50, glassColor, glassColor, glassColor);
        glassGradientColors[1] = Color.argb(0, glassColor, glassColor, glassColor);
        glassGradientColors[0] = Color.argb(0, glassColor, glassColor, glassColor);

        glassGradientPositions[4] = 1 - 0.0f;
        glassGradientPositions[3] = 1 - 0.06f;
        glassGradientPositions[2] = 1 - 0.10f;
        glassGradientPositions[1] = 1 - 0.20f;
        glassGradientPositions[0] = 1 - 1.0f;

        skyHorizonColorFrom = res.getColor(R.color.horizon_sky_from);
        skyHorizonColorTo = res.getColor(R.color.horizon_sky_to);
        groundHorizonColorFrom = res.getColor(R.color.horizon_ground_from);
        groundHorizonColorTo= res.getColor(R.color.horizon_ground_to);
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
        float ringWidth = textHeight + 4;

        int height = getMeasuredHeight();
        int weight = getMeasuredWidth();

        int px = weight / 2;
        int py = height / 2;
        Point center = new Point(px, py);

        int radius = Math.min(px, py) - 2;

        RectF boundingBox = new RectF(center.x - radius, center.y - radius, center.x + radius, center.y + radius);
        RectF innerBoundingBox = new RectF(center.x - radius + ringWidth, center.y - radius + ringWidth,
                center.x + radius - ringWidth, center.y + radius - ringWidth);

        float innerRadius = innerBoundingBox.height() / 2;

        RadialGradient borderRadient = new RadialGradient(px, py, radius,
                borderGradientColors, borderGradientPositions, Shader.TileMode.CLAMP);

        Paint pgb = new Paint();
        pgb.setShader(borderRadient);

        Path outerRingPath = new Path();
        outerRingPath.addOval(boundingBox, Path.Direction.CW);

        canvas.drawPath(outerRingPath, pgb);

        LinearGradient skyShader = new LinearGradient(center.x, innerBoundingBox.top, center.x,
                innerBoundingBox.bottom, skyHorizonColorFrom, skyHorizonColorTo, Shader.TileMode.CLAMP);
        Paint skyPaint = new Paint();
        skyPaint.setShader(skyShader);

        LinearGradient groundShader = new LinearGradient(center.x, innerBoundingBox.top, center.x,
                innerBoundingBox.bottom, groundHorizonColorFrom, groundHorizonColorTo, Shader.TileMode.CLAMP);
        Paint groundPaint = new Paint();
        groundPaint.setShader(groundShader);

        float tiltDegree = pitch;
        while (tiltDegree > 90 || tiltDegree < -90){
            if (tiltDegree > 90)
                tiltDegree = -90 + (tiltDegree - 90);
            if (tiltDegree < -90)
                tiltDegree = 90 - (tiltDegree + 90);
        }

        float rollDegree = roll;
        while (rollDegree > 180 || rollDegree < -180){
            if (rollDegree > 180)
                rollDegree = -180 + (rollDegree - 180);
            if (rollDegree < -180)
                rollDegree = 180 - (rollDegree + 180);
        }

        Path skyPath = new Path();
        skyPath.addArc(innerBoundingBox, -tiltDegree, (180 + (2 * tiltDegree)));

        canvas.save();
        canvas.rotate(-rollDegree, px, py);
        canvas.drawOval(innerBoundingBox, groundPaint);
        canvas.drawPath(skyPath, skyPaint);
        canvas.drawPath(skyPath, markerPaint);

        int markWidth = radius / 2;
        int startX = markWidth - center.x;
        int endX = markWidth + center.x;

        double h = innerRadius * Math.cos(Math.toRadians(90 - tiltDegree));
        double justTiltY = center.y - h;

        float pxPerDegree = (innerBoundingBox.height() / 2) / 45f;

        for (int i = 90; i >= -90; i -= 10){
            double yPos = justTiltY + i * pxPerDegree;
            if ((yPos < innerBoundingBox.top + textHeight) ||
                    yPos > innerBoundingBox.bottom - textHeight){
                continue;
            }

            //Рисую линию и угол наклона для каждой метки шкалы.
            canvas.drawLine(startX, (float) yPos, endX, (float) yPos, markerPaint);
            int displayPos = (int) (tiltDegree - 1);
            String displayString = String.valueOf(displayPos);
            float stringSizeWidth = textPaint.measureText(displayString);
            canvas.drawText(displayString,
                    (int)(center.x - stringSizeWidth / 2),
                    (int)(yPos) + 1,
                    textPaint);
        }

        markerPaint.setStrokeWidth(2);
        canvas.drawLine(center.x - radius / 2,
                (float) justTiltY,
                center.x + radius / 2,
                (float) justTiltY,
                markerPaint);
        markerPaint.setStrokeWidth(1);

        //Рисую стрелку.
        Path rollArrow = new Path();
        rollArrow.moveTo(center.x - 3, (int) innerBoundingBox.top + 14);
        rollArrow.lineTo(center.x, (int) innerBoundingBox.top + 10);
        rollArrow.moveTo(center.x + 3, innerBoundingBox.top + 14);
        rollArrow.lineTo(center.x, innerBoundingBox.top + 10);
        canvas.drawPath(rollArrow, markerPaint);

        //Вывожу строку.
        String rollText = String.valueOf(rollDegree);
        double rollTextWidth = textPaint.measureText(rollText);
        canvas.drawText(rollText,
                (float) (center.x - rollTextWidth / 2),
                innerBoundingBox.top + textHeight + 2,
                textPaint);

        //Возвращаю канвас в вертикальное положение, чтобы нарисовать оставшиеся
        //метки для циферблата.
        canvas.restore();

        canvas.save();
        canvas.rotate(180, center.x, center.y);
        for (int i = -180; i < 180; i += 10) {
            //Вывожу цифровое значение каждые 30 градусов.
            if (i % 30 == 0) {
                String rollString = String.valueOf(i * -1);
                float rollStringWidth = textPaint.measureText(rollString);
                PointF rollStringCenter = new PointF(
                        center.x - rollStringWidth / 2,
                        innerBoundingBox.top + 1 + textHeight
                );
                canvas.drawText(rollString, rollStringCenter.x, rollStringCenter.y,
                        textPaint);
            }
            //В противном случае рисую метку.
            else {
                canvas.drawLine(center.x, (int) innerBoundingBox.top,
                        center.x, (int) innerBoundingBox.top + 5,
                        markerPaint);
            }
            canvas.rotate(10, center.x, center.y);
        }
        canvas.restore();

        canvas.save();
        canvas.rotate(-1 * (bearing), px, py);

        //Должна ли эта переменная иметь тип double?
        double increment = 22.5;

        for (double i = 0; i < 360; i += increment) {
            CompassDirection cd = CompassDirection.values()[(int) (i / 22.5)];

            String headString = cd.toString();
            float headStringWidth = textPaint.measureText(headString);
            PointF headStringCenter = new PointF(center.x - headStringWidth / 2,
                    boundingBox.top + 1 + textHeight);

            if (i % increment == 0){
                canvas.drawText(headString, headStringCenter.x, headStringCenter.y,
                        textPaint);
            }
            else {
                canvas.drawLine(center.x, (int) boundingBox.top,
                        center.x, (int) boundingBox.top + 3,
                        markerPaint);
            }
            canvas.rotate((int) increment, center.x, center.y);
        }
        canvas.restore();

        //Создание "стеклянного купола".
        RadialGradient glassShader = new RadialGradient(px, py, (int) innerRadius,
                glassGradientColors, glassGradientPositions,
                Shader.TileMode.CLAMP);
        Paint glassPaint = new Paint();
        glassPaint.setShader(glassShader);

        canvas.drawOval(innerBoundingBox, glassPaint);

        //Рисую внешнее кольцо.
        canvas.drawOval(boundingBox, circlePaint);
        //Рисую внутреннее кольцо.
        circlePaint.setStrokeWidth(2);
        canvas.drawOval(innerBoundingBox, circlePaint);
    }



    //--------------------------------------//

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
