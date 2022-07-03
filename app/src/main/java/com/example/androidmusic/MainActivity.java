package com.example.androidmusic;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {

    private static final int SELECT_FILE = 1;
    private static final int REQUEST_PERMISSION = 45;
    private static int CAMERA_PERMISSION_CODE = 100;
    private static int VIDEO_RECORD_CODE = 101;
    ActivityResultLauncher<String> storagePermissionLauncher;
    private TextView avatarTV, videoTV, generoTV;
    private EditText nombreET, usernameET, passwordET, fechaET;
    private CheckBox hombreCB, mujerCB;
    private ImageButton avatar, imagenCalendario, imagenVolver;
    private Button botonGrabar;
    private VideoView video;
    boolean detectoCamara, detectoCamaraParaAvatar = false;
    Bitmap bmp;
    static byte[] blob;
    static String nombreUsuario;
    static int codigoUsuario;
    Uri videoPath;
    String fileName = "", selectedPath = "";
    int dia, mes, ano, anoValidar;
    public static String genero;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        usernameET = (EditText) findViewById((R.id.usernameET));
        nombreET = (EditText) findViewById((R.id.nombreET));
        passwordET = (EditText) findViewById((R.id.passwordET));
        hombreCB = (CheckBox) findViewById((R.id.hombreCB));
        mujerCB = (CheckBox) findViewById((R.id.mujerCB));
        fechaET = (EditText) findViewById((R.id.fechaNacimiento));
        avatar = (ImageButton) findViewById((R.id.imagenAvatar2));
        imagenVolver = (ImageButton) findViewById((R.id.imagenVolver));
        imagenCalendario = (ImageButton) findViewById((R.id.imagenCalendario));
        video = (VideoView) findViewById((R.id.videoView));
        avatarTV = (TextView) findViewById((R.id.avatarTV));
        videoTV = (TextView) findViewById((R.id.videoTV));
        generoTV = (TextView) findViewById((R.id.generoTV));
        botonGrabar = (Button) findViewById((R.id.botonGrabar));

        hombreCB.setChecked(true);

        mujerCB.setOnCheckedChangeListener((check, checkedId) -> {
            if (mujerCB.isChecked()) {
                hombreCB.setChecked(false);
            } else if (!hombreCB.isChecked()) {
                mujerCB.setChecked(true);
            }
        });

        hombreCB.setOnCheckedChangeListener((check, checkedId) -> {
            if (hombreCB.isChecked()) {
                mujerCB.setChecked(false);
            } else if (!mujerCB.isChecked()) {
                hombreCB.setChecked(true);
            }
        });

        pedirPermiso();
        /*
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_PERMISSION);
            return;
        }*/

        //getCameraPermission();
        //assigning storage permission launcher
        /*storagePermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), result -> {
            if (result) {
                fetchSongs();
            } else {
                respondOnUserPermissionActs();

            }
        });

        storagePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);*/
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    /*
    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull final String[] permissions, @NonNull final int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            } else {
            }
        }
    }*/

    /*
    private void respondOnUserPermissionActs() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            fetchSongs();
        } else if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            new AlertDialog.Builder(this)
                    .setTitle("Requesting Permission")
                    .setMessage("Allow us to fetch & show songs on your device")
                    .setPositiveButton("Allow ", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            storagePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                        }
                    })
                    .setNegativeButton("Don't Allow", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Toast.makeText(getApplicationContext(), " You denied to fetch songs", Toast.LENGTH_LONG).show();
                            dialogInterface.dismiss();
                        }
                    })
                    .show();
        } else {
        }
    }*/

    // FetchSongs Movil
    private void fetchSongs() {
        ArrayList<Song> songs = new ArrayList<>();
        Uri songLibraryUri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            songLibraryUri = MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);
        } else {
            songLibraryUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        }

        String[] projection = new String[]{
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.SIZE,
                MediaStore.Audio.Media.ALBUM_ID,
        };

        String sortOrder = MediaStore.Audio.Media.DATE_ADDED + " DESC";

        try (Cursor cursor = getContentResolver().query(songLibraryUri, projection, null, null, sortOrder)) {

            int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
            int nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME);
            int durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION);
            int sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE);
            int albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID);

            while (cursor.moveToNext()) {
                long id = cursor.getLong(idColumn);
                String name = cursor.getString(nameColumn);
                int duration = cursor.getInt(durationColumn);
                int size = cursor.getInt(sizeColumn);
                long albumId = cursor.getLong(albumIdColumn);

                Uri uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);

                Uri albumartUri = ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), albumId);

                String extension = name.substring(name.lastIndexOf(".") + 1);

                name = name.substring(0, name.lastIndexOf("."));
                MediaPlayer auxMP = MediaPlayer.create(this, uri);
                Song song = new Song(uri.toString(), name);
                if (extension.equals("mp3") && auxMP != null) {
                    songs.add(song);
                    System.out.println("\n\n Cancion Añadida");
                }
            }

            AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "administracion", null, 1);
            SQLiteDatabase BaseDatos = admin.getWritableDatabase();

            try {
                for (int i = 0; i < songs.size(); i++) {

                    Cursor fila = BaseDatos.rawQuery("SELECT uri FROM cancion WHERE uri LIKE '" + songs.get(i).getUri() + "'", null);

                    if (!fila.moveToFirst()) {
                        ContentValues registro = new ContentValues();
                        registro.put("uri", String.valueOf(songs.get(i).getUri()));
                        registro.put("nombre", songs.get(i).getTitle());
                        BaseDatos.insert("cancion", null, registro);
                    }
                }

            } catch (Exception e) {
            }
            BaseDatos.close();
        }
    }

    // FetchSongs Raw
    public void fetchSongss() {/*
        ArrayList<Song> songs = new ArrayList<>();
        Field[] fields = R.raw.class.getFields();

        for (int i = 0; i < fields.length; i++) {
            int idAudio = this.getResources().getIdentifier(fields[i].getName(), "raw", this.getPackageName());
            //listaCanciones.add(idAudio);
            Uri archivo = Uri.parse("android.resource://" + getPackageName() + "/" + idAudio);
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            mmr.setDataSource(this, archivo);
            String titulo = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
            String artista = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
            Bitmap bm;
            try {
                byte[] artBytes = mmr.getEmbeddedPicture();
                bm = BitmapFactory.decodeByteArray(artBytes, 0, artBytes.length);
            } catch (Exception e) {
                BitmapDrawable bitmapDrawable = (BitmapDrawable) getDrawable(R.drawable.portada2);
                bm = bitmapDrawable.getBitmap();
            }
            Song song = new Song(archivo.toString(), titulo);
            songs.add(song);
        }
        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "administracion", null, 1);
        SQLiteDatabase BaseDatos = admin.getWritableDatabase();
        try {
            for (int i = 0; i < songs.size(); i++) {

                Cursor fila = BaseDatos.rawQuery("SELECT uri FROM cancion WHERE uri LIKE '" + songs.get(i).getUri() + "'", null);

                if (!fila.moveToFirst()) {
                    ContentValues registro = new ContentValues();
                    registro.put("uri", String.valueOf(songs.get(i).getUri()));
                    registro.put("nombre", songs.get(i).getTitle());
                    BaseDatos.insert("cancion", null, registro);
                }
            }

        } catch (Exception e) {
        }
        BaseDatos.close();
        */
    }

    public void login(View view) {
        if (nombreET.getVisibility() == View.INVISIBLE) {
            try {
                AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "administracion", null, 1);
                SQLiteDatabase BaseDeDatos = admin.getWritableDatabase();

                String usuario = usernameET.getText().toString();
                String password = passwordET.getText().toString();

                if (!usuario.isEmpty() && !password.isEmpty()) {
                    Cursor fila = BaseDeDatos.rawQuery("select username, password, avatar, codigo, genero from usuario where username LIKE '" + usuario + "' AND password LIKE '" + password + "'", null);
                    if (fila.moveToFirst()) {
                        Intent reproductor = new Intent(this, Reproductor.class);
                        nombreUsuario = usuario;
                        blob = fila.getBlob(2);
                        codigoUsuario = fila.getInt(3);
                        genero = fila.getString(4);
                        startActivity(reproductor);
                    } else {
                        Toast.makeText(this, "Datos erróneos", Toast.LENGTH_SHORT).show();
                        BaseDeDatos.close();
                    }
                } else {
                    Toast.makeText(this, "Debes introducir el nombre y la contraseña", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
            }
        } else {
            Toast.makeText(this, "Registrate Primero", Toast.LENGTH_SHORT).show();
        }
    }

    private void getCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        }
    }


    public void recordVideo(View view) {
        detectoCamara = false;
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        startActivityForResult(intent, VIDEO_RECORD_CODE);
    }

    public void abrirGaleria(View v) {
        detectoCamara = true;
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(
                Intent.createChooser(intent, "Seleccione una imagen"),
                SELECT_FILE);
    }

    static final int REQUEST_VIDEO_CAPTURE = 1;


    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent imageReturnedIntent) {
        if (detectoCamara) {
            super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
            detectoCamaraParaAvatar = true;
            //Uri selectedImageUri = null;
            Uri selectedImage;
            String filePath = null;
            switch (requestCode) {
                case SELECT_FILE:
                    if (resultCode == Activity.RESULT_OK) {
                        selectedImage = imageReturnedIntent.getData();
                        selectedPath = selectedImage.getPath();
                        if (requestCode == SELECT_FILE) {
                            if (selectedPath != null) {
                                InputStream imageStream = null;
                                try {
                                    imageStream = getContentResolver().openInputStream(
                                            selectedImage);
                                } catch (FileNotFoundException e) {
                                }
                                bmp = BitmapFactory.decodeStream(imageStream);
                                avatar.setImageBitmap(bmp);
                            }
                        }
                    }
                    break;
            }
        } else {
            super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
            if (requestCode == VIDEO_RECORD_CODE) {
                if (resultCode == RESULT_OK) {
                    videoPath = imageReturnedIntent.getData();
                    Log.i("VIDEO_RECORD_TAG", "Video is recorded and available at path " + videoPath);
                    sacarUriReal(videoPath);
                } else if (resultCode == RESULT_CANCELED) {
                    Log.i("VIDEO_RECORD_TAG", "Recording video cancelled");
                } else {
                    Log.i("VIDEO_RECORD_TAG", "Video has got some error");
                }
            }
            /*
            if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK) {
                Uri videoUri = imageReturnedIntent.getData();
                video.setVideoURI(videoUri);
                video.start();
            }*/
        }
    }

    public void sacarUriReal(Uri falseUri) {
        String uriReal;
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Video.Media.DATA};
            cursor = this.getContentResolver().query(Uri.parse(String.valueOf(falseUri)), proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
            cursor.moveToFirst();
            uriReal = cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        videoPath = Uri.parse(uriReal);
        video.setVideoURI(Uri.parse(uriReal));
        fileName = videoPath.getLastPathSegment();
        video.start();
    }

    /*
    public void dispatchTakeVideoIntent(View view) {
        detectoCamara = false;
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE);
        }
    }*/

    public void establecerFecha(View view) {
        final Calendar c = Calendar.getInstance();
        dia = c.get(Calendar.DAY_OF_MONTH);
        mes = c.get(Calendar.MONTH);
        ano = c.get(Calendar.YEAR);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (datePicker, ano, mes, dia) -> fechaET.setText(dia + "/" + (mes + 1) + "/" + ano), dia, mes, ano);
        datePickerDialog.updateDate(2000, 01, 01);
        datePickerDialog.show();
    }

    public void volver(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void camposVisibility(int n) {
        if (n == 1) {
            imagenCalendario.setBackgroundResource(0);
            imagenCalendario.setImageResource(R.drawable.calendar);
            avatar.setBackgroundResource(0);
            avatar.setImageResource(R.drawable.avatar);
            imagenVolver.setImageResource(0);
            imagenVolver.setImageResource(R.drawable.volver);
            imagenVolver.setVisibility(View.VISIBLE);
            nombreET.setVisibility(View.VISIBLE);
            usernameET.setVisibility(View.VISIBLE);
            passwordET.setVisibility(View.VISIBLE);
            mujerCB.setVisibility(View.VISIBLE);
            hombreCB.setVisibility(View.VISIBLE);
            fechaET.setVisibility(View.VISIBLE);
            avatar.setVisibility(View.VISIBLE);
            imagenCalendario.setVisibility(View.VISIBLE);
            video.setVisibility(View.VISIBLE);
            generoTV.setVisibility(View.VISIBLE);
            avatarTV.setVisibility(View.VISIBLE);
            videoTV.setVisibility(View.VISIBLE);
            botonGrabar.setVisibility(View.VISIBLE);
        } else {
            imagenVolver.setImageResource(0);
            imagenVolver.setImageResource(R.drawable.volver);
            imagenCalendario.setBackgroundResource(0);
            imagenCalendario.setImageResource(R.drawable.calendar);
            avatar.setBackgroundResource(0);
            avatar.setImageResource(R.drawable.avatar);
            imagenVolver.setVisibility(View.INVISIBLE);
            nombreET.setVisibility(View.INVISIBLE);
            mujerCB.setVisibility(View.INVISIBLE);
            hombreCB.setVisibility(View.INVISIBLE);
            fechaET.setVisibility(View.INVISIBLE);
            avatar.setVisibility(View.INVISIBLE);
            imagenCalendario.setVisibility(View.INVISIBLE);
            video.setVisibility(View.INVISIBLE);
            generoTV.setVisibility(View.INVISIBLE);
            avatarTV.setVisibility(View.INVISIBLE);
            videoTV.setVisibility(View.INVISIBLE);
            botonGrabar.setVisibility(View.INVISIBLE);
            nombreET.setText("");
            usernameET.setText("");
            passwordET.setText("");
            mujerCB.setChecked(false);
            hombreCB.setChecked(true);
            fechaET.setText("");
        }
    }

    public boolean validamosDatos(String nombre, String username, String password) {
        boolean correcto = true;
        anoValidar = Integer.parseInt(fechaET.getText().toString().substring(fechaET.getText().toString().lastIndexOf("/") + 1));
        if (!nombre.matches("[A-zÀ-ÿ ]{10,}")) {
            correcto = false;
            Toast.makeText(this, "Nombre inválido", Toast.LENGTH_SHORT).show();
        }

        if (username.length() < 8) {
            correcto = false;
            Toast.makeText(this, "Username inválido", Toast.LENGTH_SHORT).show();
        }

        if (password.length() < 8) {
            correcto = false;
            Toast.makeText(this, "Contraseña inválida", Toast.LENGTH_SHORT).show();
        }

        if (anoValidar > 2005) {
            correcto = false;
            Toast.makeText(this, "Año de nacimiento inválido", Toast.LENGTH_SHORT).show();
        }
        return correcto;
    }

    public boolean existeUsername(String username) {
        boolean exists = false;

        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "administracion", null, 1);
        SQLiteDatabase BaseDeDatos = admin.getWritableDatabase();

        Cursor fila = BaseDeDatos.rawQuery("select username from usuario WHERE username = '" + username + "'", null);
        if (fila.moveToNext()) {
            exists = true;
        }
        BaseDeDatos.close();
        return exists;
    }

    public static byte[] getBytesFromBitmap(Bitmap bitmap) {
        if (bitmap != null) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream);
            return stream.toByteArray();
        }
        return null;
    }

    public void registrarse(View view) throws ParseException {
        camposVisibility(1);
        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "administracion", null, 1);
        SQLiteDatabase BaseDeDatos = admin.getWritableDatabase();
        nombreET.requestFocus();
        String nombre = nombreET.getText().toString();
        String username = usernameET.getText().toString();
        String password = passwordET.getText().toString();
        if (mujerCB.isChecked()) {
            genero = "Mujer";
        } else {
            genero = "Hombre";
        }
        String fechaNacimiento = fechaET.getText().toString();
        blob = getBytesFromBitmap(bmp);

        if (!nombre.isEmpty() && !username.isEmpty() && !password.isEmpty() && !genero.isEmpty() && !fechaNacimiento.isEmpty()) {
            if (validamosDatos(nombre, username, password)) {
                if (!existeUsername(username)) {
                    ContentValues registro = new ContentValues();
                    registro.put("nombre", nombre);
                    registro.put("username", username);
                    registro.put("password", password);
                    registro.put("avatar", blob);
                    registro.put("genero", genero);
                    registro.put("fechanac", fechaNacimiento);
                    try {
                        registro.put("rutaVideo", videoPath.toString());
                    } catch (NullPointerException npe) {

                    }
                    try {
                        registro.put("nombreVideo", fileName);
                    } catch (NullPointerException npe) {

                    }

                    BaseDeDatos.insert("usuario", null, registro);
                    BaseDeDatos.close();

                    camposVisibility(2);
                    usernameET.requestFocus();
                    Toast.makeText(this, "Usuario Guardado", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(this, MainActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "Ese username ya existe", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            Toast.makeText(this, "Debes rellenar todos los datos", Toast.LENGTH_SHORT).show();
        }
        detectoCamaraParaAvatar = false;
    }

    @AfterPermissionGranted(123)
    public void pedirPermiso() {
        String[] permisos = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO};
        if (EasyPermissions.hasPermissions(this, permisos)) {
            fetchSongs();
        } else {
            EasyPermissions.requestPermissions(this, "La aplicación necesita permisos para funcionar", 123, permisos);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {

    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this).build().show();
        }
    }
}