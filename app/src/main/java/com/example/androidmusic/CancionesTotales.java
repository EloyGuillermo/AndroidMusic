package com.example.androidmusic;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomappbar.BottomAppBar;

import java.util.ArrayList;

public class CancionesTotales extends AppCompatActivity {

    public static RecyclerView listaCancionesRV;
    ArrayList<Song> listaCanciones = new ArrayList<>();
    static int codigoUsuario;
    //public static ArrayList<Song> cancionesNuevas = new ArrayList<>();
    public static Button botonAñado;
    EditText buscadorET;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_canciones_totales);
        buscadorET = (EditText) findViewById(R.id.buscadorET);
        botonAñado = (Button) findViewById(R.id.botonAñado);
        codigoUsuario = Reproductor.codigoUsuario;
        rellenarCanciones();
        listaCancionesRV = findViewById(R.id.listaCancionesRV);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        listaCancionesRV.setLayoutManager(linearLayoutManager);
        RecyclerAdapter adapter = new RecyclerAdapter(listaCanciones, this);
        listaCancionesRV.setAdapter(adapter);

        buscadorET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                actualizarCanciones();
                adapter.notifyDataSetChanged();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    public void añadirAPlayList(View v) {
        Intent intent = new Intent(this, ListaCanciones.class);
        String uri = listaCanciones.get(listaCancionesRV.getChildAdapterPosition(v)).getUri();

        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "administracion", null, 1);
        SQLiteDatabase BaseDeDatos = admin.getWritableDatabase();

        Cursor fila = BaseDeDatos.rawQuery("SELECT codigo FROM cancion WHERE uri LIKE '" + uri + "'", null);

        int codigoCancion = 0;
        if (fila.moveToFirst()) {
            codigoCancion = fila.getInt(0);
        }

        ContentValues registro = new ContentValues();
        registro.put("codigoUsuario", codigoUsuario);
        registro.put("codigoCancion", codigoCancion);

        BaseDeDatos.insert("usuariocancion", null, registro);
        BaseDeDatos.close();


        startActivity(intent);
    }

    public void volver(View view) {
        Intent intent = new Intent(this, ListaCanciones.class);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, ListaCanciones.class);
        startActivity(intent);
    }

    public void rellenarCanciones() {
        listaCanciones.clear();
        try {
            AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "administracion", null, 1);
            SQLiteDatabase BaseDeDatos = admin.getWritableDatabase();


            Cursor fila = BaseDeDatos.rawQuery("select uri, nombre, codigo from cancion WHERE codigo NOT IN (SELECT codigoCancion from usuariocancion WHERE codigoUsuario = " + codigoUsuario + ")", null);
            fila.moveToFirst();
            do {
                String uri = fila.getString(0);
                String nombre = fila.getString(1);
                int codigo = fila.getInt(2);

                MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                mmr.setDataSource(this, Uri.parse(uri));
                String artista = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
                Bitmap bm;
                try {
                    byte[] artBytes = mmr.getEmbeddedPicture();
                    bm = BitmapFactory.decodeByteArray(artBytes, 0, artBytes.length);
                } catch (Exception e) {
                    BitmapDrawable bitmapDrawable = (BitmapDrawable) getDrawable(R.drawable.portada2);
                    bm = bitmapDrawable.getBitmap();
                }
                Song song = new Song(String.valueOf(uri), nombre, artista, bm, codigo);
                listaCanciones.add(song);

            } while (fila.moveToNext());
            BaseDeDatos.close();

        } catch (Exception e) {
        }
    }

    public void actualizarCanciones() {
        if (buscadorET.getText().equals("")) {
            rellenarCanciones();
        } else {
            try {
                listaCanciones.clear();
                AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "administracion", null, 1);
                SQLiteDatabase BaseDeDatos = admin.getWritableDatabase();

                Cursor fila = BaseDeDatos.rawQuery("select uri, nombre, codigo from cancion WHERE codigo NOT IN (SELECT codigoCancion from usuariocancion WHERE codigoUsuario = " + codigoUsuario + ") AND nombre LIKE '" + buscadorET.getText() + "%'", null);
                fila.moveToFirst();
                do {
                    String uri = fila.getString(0);
                    String nombre = fila.getString(1);
                    int codigo = fila.getInt(2);

                    MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                    mmr.setDataSource(this, Uri.parse(uri));
                    String artista = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
                    Bitmap bm;
                    try {
                        byte[] artBytes = mmr.getEmbeddedPicture();
                        bm = BitmapFactory.decodeByteArray(artBytes, 0, artBytes.length);
                    } catch (Exception e) {
                        BitmapDrawable bitmapDrawable = (BitmapDrawable) getDrawable(R.drawable.portada2);
                        bm = bitmapDrawable.getBitmap();
                    }
                    Song song = new Song(String.valueOf(uri), nombre, artista, bm, codigo);
                    listaCanciones.add(song);

                } while (fila.moveToNext());
                BaseDeDatos.close();

            } catch (Exception e) {
            }
        }
    }
}
