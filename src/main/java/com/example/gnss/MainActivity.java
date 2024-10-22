package com.example.gnss;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    // Declaração do botão chamado linkGNSS
    private Button linkGNSS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Define o layout a partir do arquivo XML activity_main
        setContentView(R.layout.activity_main);

        // Associa o botão linkGNSS ao botão com o ID gnss_link no layout
        linkGNSS = findViewById(R.id.gnss_link);

        // Define um listener de clique para o botão linkGNSS
        linkGNSS.setOnClickListener(this); // Registra o listener para o clique
    }

    // Método onClick é chamado quando um botão é clicado
    @Override
    public void onClick(View view) {
        // Verifica se o botão clicado é o gnss_link
        if (view.getId() == R.id.gnss_link) {
            // Cria uma nova Intent para iniciar a GNSSActivity
            Intent intent = new Intent(MainActivity.this, GNSSActivity.class);
            // Inicia a nova atividade GNSSActivity
            startActivity(intent);
        }
    }
}
