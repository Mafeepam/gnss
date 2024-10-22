package com.example.gnss;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.Nullable;

public class SignalQualityView extends View {
    private Paint paint;
    private float[] signalStrengths;
    private int[] satelliteIds;

    public SignalQualityView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint();
        paint.setAntiAlias(true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (signalStrengths == null || satelliteIds == null) {
            return;
        }

        int barWidth = getWidth() / signalStrengths.length;
        int maxHeight = getHeight() - 50;

        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(24);
        for (int i = 0; i < signalStrengths.length; i++) {
            // Desenhar as barras de sinal
            float snr = signalStrengths[i];
            float barHeight = (snr / 100) * maxHeight;

            paint.setColor(Color.GREEN);
            canvas.drawRect(i * barWidth, getHeight() - barHeight, (i + 1) * barWidth - 10, getHeight(), paint);

            // Desenhar o ID do satélite abaixo da barra
            paint.setColor(Color.WHITE);
            canvas.drawText(String.valueOf(satelliteIds[i]), i * barWidth + 10, getHeight() - 10, paint);
        }
    }

    public void setSignalData(float[] signalStrengths, int[] satelliteIds) {
        this.signalStrengths = signalStrengths;
        this.satelliteIds = satelliteIds;
        invalidate(); // Atualiza a view
    }
}
