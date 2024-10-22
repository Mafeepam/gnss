package com.example.gnss;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import java.util.HashMap;
import java.util.Map;

public class SignalQualityChartView extends View {

    private Paint paint;
    private Map<Integer, Float> signalData; // SVID -> SNR

    public SignalQualityChartView(Context context) {
        super(context);
        init();
    }

    public SignalQualityChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SignalQualityChartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setColor(Color.BLUE);
        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(40);
        signalData = new HashMap<>();
    }

    // Método para atualizar os dados dos satélites
    public void updateSignalData(Map<Integer, Float> data) {
        this.signalData = data;
        invalidate(); // Redesenhar a view com os novos dados
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Desenhar o título
        canvas.drawText("Gráfico de Qualidade de Sinal", 50, 100, paint);

        if (signalData == null || signalData.isEmpty()) {
            return; // Nenhum dado disponível, não desenha o gráfico
        }

        // Desenhar o gráfico de barras para a qualidade de sinal (SNR)
        float barWidth = 100;
        float startX = 50;
        float startY = 200;
        float maxHeight = 300;

        for (Map.Entry<Integer, Float> entry : signalData.entrySet()) {
            int svid = entry.getKey();
            float snr = entry.getValue();

            // Normalizar a altura da barra baseado no valor do SNR (por exemplo, assumindo SNR máx de 50)
            float barHeight = (snr / 50) * maxHeight;

            // Desenhar a barra
            canvas.drawRect(startX, startY - barHeight, startX + barWidth, startY, paint);

            // Desenhar o SVID abaixo da barra
            canvas.drawText("SVID " + svid, startX, startY + 50, paint);

            // Avançar para a próxima barra
            startX += barWidth + 20;
        }
    }
}
