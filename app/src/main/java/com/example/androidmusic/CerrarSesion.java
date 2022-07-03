package com.example.androidmusic;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class CerrarSesion extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cerrar_sesion);
    }

    public void cerrarSesion(View view){
        Intent intent = new Intent(this, MainActivity.class);
        if (Reproductor.mp.isPlaying()) {
            Reproductor.mp.stop();
        }
        startActivity(intent);
    }
}