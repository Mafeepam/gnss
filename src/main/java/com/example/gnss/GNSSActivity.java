package com.example.gnss;

import android.Manifest;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.GnssStatus;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.HashMap;
import java.util.Map;

public class GNSSActivity extends AppCompatActivity implements SensorEventListener {

    private EsferaCelesteView esferaCelesteView;
    private TextView locationTextView;
    private SignalQualityChartView signalQualityChartView;  // Componente para gráfico de qualidade do sinal

    private SensorManager sensorManager;
    private Sensor rotationSensor;
    private float[] rotationMatrix = new float[9];
    private float[] orientationAngles = new float[3];

    private FusedLocationProviderClient fusedLocationClient; // Adicionado para obter a localização
    private String selectedCoordinateFormat = "Graus [+/-DDD.DDDDD]"; // Formato padrão
    private static final int REQUEST_LOCATION_PERMISSION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gnss);

        // Inicializar a Esfera Celeste, coordenadas e gráfico de qualidade de sinal
        esferaCelesteView = findViewById(R.id.esferaCelesteView);
        locationTextView = findViewById(R.id.coordinatesText);
        signalQualityChartView = findViewById(R.id.signalQualityChartView);

        // Definir comportamento ao clicar no campo de coordenadas
        locationTextView.setOnClickListener(v -> showCoordinateFormatDialog());

        // Gerenciamento de sensores
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        sensorManager.registerListener(this, rotationSensor, SensorManager.SENSOR_DELAY_UI);

        // Inicializar FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Registrar GNSS Status Callback para obter dados de satélite
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Solicitar permissões, se necessário
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
            return;
        }
        locationManager.registerGnssStatusCallback(gnssStatusCallback);
    }

    // Callback para GNSS status, atualizando dados do gráfico de sinal e da esfera celeste
    private GnssStatus.Callback gnssStatusCallback = new GnssStatus.Callback() {
        @Override
        public void onSatelliteStatusChanged(GnssStatus status) {
            // Atualiza os dados na Esfera Celeste
            esferaCelesteView.setGnssStatus(status); // Passa o status dos satélites para a Esfera Celeste

            // Atualiza os dados de qualidade de sinal no gráfico
            Map<Integer, Float> signalData = new HashMap<>();
            for (int i = 0; i < status.getSatelliteCount(); i++) {
                int svid = status.getSvid(i);
                float snr = status.getCn0DbHz(i);
                if (snr > 0) {
                    signalData.put(svid, snr);
                }
            }
            signalQualityChartView.updateSignalData(signalData); // Passa os dados de sinal para o gráfico
        }
    };

    @Override
    public void onSensorChanged(SensorEvent event) {
        SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values);
        SensorManager.getOrientation(rotationMatrix, orientationAngles);
        esferaCelesteView.setRotation(orientationAngles[0]);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sensorManager.unregisterListener(this);
    }

    // Método para mostrar o diálogo de seleção de formato de coordenadas
    private void showCoordinateFormatDialog() {
        String[] formats = {"Graus [+/-DDD.DDDDD]", "Graus-Minutos [+/-DDD:MM.MMMMM]", "Graus-Minutos-Segundos [+/-DDD:MM:SS.SSSSS]"};
        new AlertDialog.Builder(this)
                .setTitle("Selecionar formato de coordenadas")
                .setItems(formats, (dialog, which) -> {
                    // Atualiza o formato de coordenadas com base na seleção
                    switch (which) {
                        case 0:
                            selectedCoordinateFormat = "Graus [+/-DDD.DDDDD]";
                            break;
                        case 1:
                            selectedCoordinateFormat = "Graus-Minutos [+/-DDD:MM.MMMMM]";
                            break;
                        case 2:
                            selectedCoordinateFormat = "Graus-Minutos-Segundos [+/-DDD:MM:SS.SSSSS]";
                            break;
                    }
                    // Atualiza a exibição das coordenadas
                    updateCoordinateDisplay();
                })
                .show();
    }

    // Método para atualizar a exibição das coordenadas
    private void updateCoordinateDisplay() {
        getLocation(); // Obter a localização atual e atualizar a exibição
    }

    // Método para obter a localização atual
    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null) {
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    // Formata as coordenadas
                    String formattedLatitude = formatCoordinate(latitude, selectedCoordinateFormat);
                    String formattedLongitude = formatCoordinate(longitude, selectedCoordinateFormat);
                    // Atualiza a UI com as coordenadas formatadas
                    locationTextView.setText("Lat: " + formattedLatitude + ", Lon: " + formattedLongitude);
                } else {
                    locationTextView.setText("Localização não disponível");
                }
            });
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
        }
    }

    // Método para formatar as coordenadas baseado no formato selecionado
    private String formatCoordinate(double coordinate, String format) {
        if (format.equals("Graus [+/-DDD.DDDDD]")) {
            return String.format("%.5f", coordinate);
        } else if (format.equals("Graus-Minutos [+/-DDD:MM.MMMMM]")) {
            return convertToDegreesMinutes(coordinate);
        } else if (format.equals("Graus-Minutos-Segundos [+/-DDD:MM:SS.SSSSS]")) {
            return convertToDegreesMinutesSeconds(coordinate);
        }
        return String.valueOf(coordinate); // Retorna o valor original se nada for correspondente
    }

    // Método para converter coordenadas em graus-minutos
    private String convertToDegreesMinutes(double coordinate) {
        int degrees = (int) coordinate;
        double minutes = (coordinate - degrees) * 60;
        return String.format("%d:%.5f", degrees, minutes);
    }

    // Método para converter coordenadas em graus-minutos-segundos
    private String convertToDegreesMinutesSeconds(double coordinate) {
        int degrees = (int) coordinate;
        double minutes = (coordinate - degrees) * 60;
        int intMinutes = (int) minutes;
        double seconds = (minutes - intMinutes) * 60;
        return String.format("%d:%02d:%.5f", degrees, intMinutes, seconds);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocation(); // Obter a localização após a permissão ser concedida
            } else {
                locationTextView.setText("Permissão de localização negada");
            }
        }
    }
}