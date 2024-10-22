package com.example.gnss;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.GnssStatus;
import android.util.AttributeSet;
import android.view.View;
import android.os.Handler;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class EsferaCelesteView extends View {
    private GnssStatus gnssStatus;
    private Paint paint;
    private int radius;
    private int viewWidth, viewHeight;
    private float currentRotation = 0; // Ângulo de rotação do celular
    private final Handler handler = new Handler(); // Para a animação de rotação

    // Filtros de satélites
    private boolean[] satelliteFilters;

    public EsferaCelesteView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint();
        paint.setAntiAlias(true);
        satelliteFilters = new boolean[4]; // Supondo que temos 4 constelações

        // Configura os filtros padrão (todos visíveis)
        for (int i = 0; i < satelliteFilters.length; i++) {
            satelliteFilters[i] = true;
        }

        // Configura o listener de clique para filtro de satélites
        setOnClickListener(v -> {
            if (detectClickInCenter()) {
                showSatelliteFilterDialog();
            }
        });
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        // Dimensões da view
        viewWidth = getMeasuredWidth();
        viewHeight = getMeasuredHeight();
        radius = Math.min(viewWidth, viewHeight) / 2;

        // Mover o canvas para o centro da view
        canvas.translate(viewWidth / 2, viewHeight / 2);

        // Rotacionar a esfera de acordo com a rotação do celular
        canvas.rotate((float) Math.toDegrees(currentRotation));

        // Desenhar círculos concêntricos para representar a Esfera Celeste
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2);
        paint.setColor(Color.BLUE);
        int circleCount = 4; // Quantidade de círculos concêntricos
        for (int i = 1; i <= circleCount; i++) {
            canvas.drawCircle(0, 0, (radius * i) / circleCount, paint);
        }

        // Desenhar linhas cruzadas (horizontal e vertical)
        canvas.drawLine(0, -radius, 0, radius, paint); // Linha vertical
        canvas.drawLine(-radius, 0, radius, 0, paint); // Linha horizontal

        // Desenhar os satélites
        if (gnssStatus != null) {
            Log.d("EsferaCeleste", "Satélites: " + gnssStatus.getSatelliteCount());
            paint.setStyle(Paint.Style.FILL);

            for (int i = 0; i < gnssStatus.getSatelliteCount(); i++) {
                float azimuth = gnssStatus.getAzimuthDegrees(i);
                float elevation = gnssStatus.getElevationDegrees(i);
                int constellationType = getConstellationType(gnssStatus, i);

                // Verifica se o satélite deve ser exibido com base no filtro
                if (constellationType != -1 && satelliteFilters[constellationType]) {
                    // Definir a cor baseada no tipo de constelação
                    paint.setColor(getColorForConstellation(constellationType));

                    // Converter azimute e elevação para coordenadas x, y no canvas
                    float adjustedRadius = (float) (radius * (90.0 - elevation) / 90.0); // Ajuste do raio com base na elevação
                    float x = (float) (adjustedRadius * Math.sin(Math.toRadians(azimuth)));
                    float y = (float) (adjustedRadius * Math.cos(Math.toRadians(azimuth)));

                    // Desenhar o satélite como um círculo colorido
                    canvas.drawCircle(x, -y, 10, paint); // O y é invertido para a orientação correta

                    // Numerar o satélite ao lado do ponto
                    paint.setColor(Color.WHITE);
                    paint.setTextSize(24);
                    canvas.drawText(String.valueOf(i + 1), x + 12, -y, paint);

                    Log.d("EsferaCeleste", "Satélite " + (i + 1) + ": Azimute = " + azimuth + ", Elevação = " + elevation);
                }
            }
        } else {
            // Desenhar mensagem se não houver dados GNSS
            paint.setColor(Color.WHITE);
            paint.setTextSize(48);
            canvas.drawText("Nenhum satélite encontrado", -radius, 0, paint);
            Log.d("EsferaCeleste", "Nenhum dado GNSS disponível");
        }
    }

    public void setGnssStatus(GnssStatus status) {
        this.gnssStatus = status;
        invalidate(); // Solicita a atualização da view quando novos dados chegam
    }

    public void setRotation(float rotation) {
        this.currentRotation = rotation;
        invalidate(); // Atualiza a rotação da view
    }

    private boolean detectClickInCenter() {
        // Aqui você pode usar coordenadas de clique, se necessário
        return true; // Por enquanto, retorna true para simplificar
    }

    private void showSatelliteFilterDialog() {
        String[] satelliteTypes = {"GPS", "GLONASS", "Galileo", "BeiDou"};

        new AlertDialog.Builder(getContext())
                .setTitle("Filtrar Satélites")
                .setMultiChoiceItems(satelliteTypes, satelliteFilters, (dialog, which, isChecked) -> {
                    // Atualiza o filtro de satélites com base nas opções selecionadas
                    satelliteFilters[which] = isChecked;
                })
                .setPositiveButton("OK", (dialog, which) -> invalidate()) // Atualiza a view quando os filtros são aplicados
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private int getConstellationType(GnssStatus status, int index) {
        switch (status.getConstellationType(index)) {
            case GnssStatus.CONSTELLATION_GPS:
                return 0;
            case GnssStatus.CONSTELLATION_GLONASS:
                return 1;
            case GnssStatus.CONSTELLATION_GALILEO:
                return 2;
            case GnssStatus.CONSTELLATION_BEIDOU:
                return 3;
            default:
                return -1; // Constelação desconhecida ou não suportada
        }
    }

    private int getColorForConstellation(int constellationType) {
        switch (constellationType) {
            case 0: // GPS
                return Color.GREEN;
            case 1: // GLONASS
                return Color.RED;
            case 2: // Galileo
                return Color.BLUE;
            case 3: // BeiDou
                return Color.YELLOW;
            default:
                return Color.GRAY; // Caso algum satélite não pertença a constelações conhecidas
        }
    }
}
