package com.example.androidmusic;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;

public class ListaCanciones extends AppCompatActivity {

    SwipeRefreshLayout swipeRefreshLayout;
    public static RecyclerView listaCancionesRV;
    ArrayList<Song> listaCanciones = new ArrayList<>();
    public int codigoUsuario;
    public static RecyclerAdapter adapter;
    Runnable runnable;
    Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_canciones);
        handler = new Handler();
        codigoUsuario = Reproductor.codigoUsuario;
        //updateDatos();
        rellenarCanciones();
        listaCancionesRV = findViewById(R.id.listaCancionesRV);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        listaCancionesRV.setLayoutManager(linearLayoutManager);
        adapter = new RecyclerAdapter(listaCanciones, this);
        listaCancionesRV.setAdapter(adapter);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                rellenarCanciones();
                adapter.notifyDataSetChanged();
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(getApplicationContext(), "administracion", null, 1); // Poner c o context de canciones totales
                SQLiteDatabase BaseDeDatos = admin.getWritableDatabase();
                BaseDeDatos.delete("usuariocancion", "codigoUsuario = " + Reproductor.codigoUsuario + " AND codigoCancion = " + listaCanciones.get(viewHolder.getAdapterPosition()).getID(), null);
                BaseDeDatos.close();
                listaCanciones.remove(viewHolder.getAdapterPosition());
                adapter.notifyDataSetChanged();
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(listaCancionesRV);
    }

    /*
    public void updateDatos() {
        runnable = () -> {
            rellenarCanciones();
            listaCancionesRV = findViewById(R.id.listaCancionesRV);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
            listaCancionesRV.setLayoutManager(linearLayoutManager);
            adapter = new RecyclerAdapter(listaCanciones, this);
            listaCancionesRV.setAdapter(adapter);
            adapter.notifyDataSetChanged();
            swipeRefreshLayout.setRefreshing(false);
            updateDatos();
        };
        handler.postDelayed(runnable, 500);
    }*/

    public void abrirCancionesTotales(View view) {
        Intent intent = new Intent(this, CancionesTotales.class);
        startActivity(intent);
    }

    public void rellenarCanciones() {
        listaCanciones.clear();
        try {
            AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "administracion", null, 1);
            SQLiteDatabase BaseDeDatos = admin.getWritableDatabase();

            Cursor fila = BaseDeDatos.rawQuery("select c.uri, c.codigo from cancion c JOIN usuariocancion uc ON c.codigo = uc.codigoCancion WHERE uc.codigoUsuario = " + codigoUsuario + "", null);
            fila.moveToFirst();
            do {
                String uri = fila.getString(0);
                int codigo = fila.getInt(1);
                MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                mmr.setDataSource(this, Uri.parse(uri));
                String nombre = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
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
