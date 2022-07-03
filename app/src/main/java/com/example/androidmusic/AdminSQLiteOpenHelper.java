package com.example.androidmusic;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class AdminSQLiteOpenHelper extends SQLiteOpenHelper {

    public AdminSQLiteOpenHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase BaseDeDatos) {
        BaseDeDatos.execSQL("create table usuario(codigo integer primary key autoincrement, nombre text, username text, password text, avatar BLOB, genero text, fechanac text, rutaVideo text, nombreVideo text)");
        BaseDeDatos.execSQL("create table cancion(codigo integer primary key autoincrement, uri text, nombre text)");
        BaseDeDatos.execSQL("create table usuariocancion(codigoUsuario integer, codigoCancion integer)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
