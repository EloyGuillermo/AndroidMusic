package com.example.androidmusic;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.CancionesViewHolder> {
    static int codigoUsuario;
    ArrayList<Song> listaCanciones;
    Context c;
    Boolean longClickPulsado = false;
    int contadorCheckBox = 0;
    ArrayList<CancionesViewHolder> arrayCanciones = new ArrayList<>();


    public RecyclerAdapter(ArrayList<Song> listaCanciones, Context c) {
        this.listaCanciones = listaCanciones;
        this.c = c;
    }

    @NonNull
    @Override
    public CancionesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.listacanciones_row, null, false);
        //view.setOnClickListener(this);
        codigoUsuario = Reproductor.codigoUsuario;
        return new CancionesViewHolder(view);
    }

    @SuppressLint("ResourceType")
    @Override
    public void onBindViewHolder(@NonNull CancionesViewHolder holder, @SuppressLint("RecyclerView") int position) {
        arrayCanciones.add(holder);
        if (listaCanciones.get(position).getTitle() == null)
            holder.nombreTV.setText("Desconocido");
        else
            holder.nombreTV.setText(listaCanciones.get(position).getTitle().toString());
        if (listaCanciones.get(position).getArtist() == null)
            holder.artistaTV.setText("Desconocido");
        else
            holder.artistaTV.setText(listaCanciones.get(position).getArtist().toString());
        holder.portada.setBackgroundResource(0);
        holder.portada.setImageBitmap(listaCanciones.get(position).getPortada());
        if (c == ListaCanciones.listaCancionesRV.getContext()) {
            holder.itemCheckBox.setVisibility(View.INVISIBLE);
            holder.imageQuitar.setImageResource(R.drawable.quitar3);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openActivity(v);
                }
            });
            holder.imageQuitar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(c, "administracion", null, 1); // Poner c o context de canciones totales
                    SQLiteDatabase BaseDeDatos = admin.getWritableDatabase();
                    BaseDeDatos.delete("usuariocancion", "codigoUsuario = " + Reproductor.codigoUsuario + " AND codigoCancion = " + listaCanciones.get(position).getID(), null);
                    BaseDeDatos.close();
                    rellenarCanciones();
                    notifyDataSetChanged();
                    if (listaCanciones.size() == 0) {
                        if (Reproductor.mp.isPlaying()) {
                            Reproductor.mp.stop();
                        }
                        Intent intent = new Intent(c, Reproductor.class);
                        c.startActivity(intent);
                    }
                }
            });

        } else {
            holder.imageQuitar.setImageResource(0);
            //if (longClickPulsado)
            //    holder.itemCheckBox.setVisibility(View.VISIBLE);
            //else
            holder.itemCheckBox.setVisibility(View.INVISIBLE);

            holder.itemCheckBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectOrDeselect(holder, position, true);
                }
            });

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (longClickPulsado && contadorCheckBox > 0) {
                        selectOrDeselect(holder, position, false);
                    } else {
                        añadirAPlayList(v);
                    }
                }
            });

            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    selectOrDeselect(holder, position, false);
                    return true;
                }
            });
        }
    }

    public void rellenarCanciones() {
        listaCanciones.clear();
        try {
            AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(c, "administracion", null, 1);
            SQLiteDatabase BaseDeDatos = admin.getWritableDatabase();

            Cursor fila = BaseDeDatos.rawQuery("select c.uri, c.codigo from cancion c JOIN usuariocancion uc ON c.codigo = uc.codigoCancion WHERE uc.codigoUsuario = " + codigoUsuario + "", null);
            fila.moveToFirst();
            do {
                String uri = fila.getString(0);
                int codigo = fila.getInt(1);
                MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                mmr.setDataSource(c, Uri.parse(uri));
                String nombre = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
                String artista = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
                Bitmap bm;
                try {
                    byte[] artBytes = mmr.getEmbeddedPicture();
                    bm = BitmapFactory.decodeByteArray(artBytes, 0, artBytes.length);
                } catch (Exception e) {
                    BitmapDrawable bitmapDrawable = (BitmapDrawable) c.getDrawable(R.drawable.portada2);
                    bm = bitmapDrawable.getBitmap();
                }
                Song song = new Song(String.valueOf(uri), nombre, artista, bm, codigo);
                listaCanciones.add(song);
            } while (fila.moveToNext());
            BaseDeDatos.close();

        } catch (Exception e) {
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void selectOrDeselect(CancionesViewHolder holder, int position, boolean forma) {
        longClickPulsado = true;
        if (!forma) {
            holder.itemCheckBox.setChecked(!holder.itemCheckBox.isChecked());
            for (int i = 0; i < arrayCanciones.size(); i++) {
                arrayCanciones.get(i).itemCheckBox.setVisibility(View.VISIBLE);
            }
            CancionesTotales.botonAñado.setVisibility(View.VISIBLE);
        }
        if (holder.itemCheckBox.isChecked()) {
            AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(c, "administracion", null, 1);
            SQLiteDatabase BaseDeDatos = admin.getWritableDatabase();
            ContentValues registro = new ContentValues();
            registro.put("codigoUsuario", codigoUsuario);
            registro.put("codigoCancion", listaCanciones.get(position).getID());

            BaseDeDatos.insert("usuariocancion", null, registro);
            BaseDeDatos.close();
            contadorCheckBox++;
        } else {

            AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(c, "administracion", null, 1);
            SQLiteDatabase BaseDeDatos = admin.getWritableDatabase();
            BaseDeDatos.delete("usuariocancion", "codigoUsuario = " + Reproductor.codigoUsuario + " AND codigoCancion = " + listaCanciones.get(position).getID(), null);
            BaseDeDatos.close();
            contadorCheckBox--;
            if (contadorCheckBox == 0) {
                longClickPulsado = false;
                notifyDataSetChanged();
                CancionesTotales.botonAñado.setVisibility(View.INVISIBLE);
            }
        }
    }

    public void añadirAPlayList(View v) {
        Intent intent = new Intent(c, ListaCanciones.class);
        String uri = listaCanciones.get(CancionesTotales.listaCancionesRV.getChildAdapterPosition(v)).getUri();

        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(c, "administracion", null, 1); // Poner c o context de canciones totales
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

        c.startActivity(intent);
    }

    public void openActivity(View v) {
        Reproductor.detectoListaCanciones = true;
        if (Reproductor.mp.isPlaying()) {
            Reproductor.mp.stop();
        }
        Intent intent = new Intent(c, Reproductor.class);
        intent.putExtra("posRecojo", String.valueOf(ListaCanciones.listaCancionesRV.getChildAdapterPosition(v)));
        c.startActivity(intent);
    }

    @Override
    public int getItemCount() {
        return listaCanciones.size();
    }

    class CancionesViewHolder extends RecyclerView.ViewHolder {

        TextView nombreTV;
        TextView artistaTV;
        ImageView portada;
        ImageButton imageQuitar;
        CheckBox itemCheckBox;


        public CancionesViewHolder(View itemView) {
            super(itemView);
            nombreTV = itemView.findViewById(R.id.nombreTV);
            artistaTV = itemView.findViewById(R.id.artistaTV);
            portada = itemView.findViewById(R.id.portada);
            imageQuitar = itemView.findViewById(R.id.imagenQuitar);
            itemCheckBox = itemView.findViewById(R.id.itemCheckBox);
        }

        void bind(int listaIndex) {
            nombreTV.setText(String.valueOf(listaIndex));
            artistaTV.setText(String.valueOf(listaIndex));
        }
    }
}

