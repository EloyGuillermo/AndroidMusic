package com.example.androidmusic;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.gauravk.audiovisualizer.visualizer.BarVisualizer;
import com.gauravk.audiovisualizer.visualizer.BlobVisualizer;
import com.gauravk.audiovisualizer.visualizer.WaveVisualizer;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Reproductor extends AppCompatActivity implements GestureDetector.OnGestureListener{

    TextView nombreCancion, duracionCancion, duracionSegundos, nombreArtista;
    ImageButton botonPlayPause, botonRepetir, botonAleatorio;
    MenuItem itemNombreUsuario;
    ImageView iv;
    int repetir = 2, aleatorio = 2, posicion = 0;
    SeekBar barraProgreso;
    Runnable runnable;
    Handler handler;
    static ArrayList<Song> listaCanciones = new ArrayList<>();
    static MediaPlayer mp = new MediaPlayer();
    Boolean detectoPause = true;
    public static boolean detectoListaCanciones = false;
    Bundle posRecojo = null;
    static String nombreUsuario;
    static int codigoUsuario;
    BlobVisualizer mVisualizer;
    private GestureDetector gestureDetector;

    @SuppressLint({"CutPasteId", "ResourceType"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reproductor);

        this.gestureDetector = new GestureDetector(this, this);
        nombreUsuario = MainActivity.nombreUsuario;
        codigoUsuario = MainActivity.codigoUsuario;
        duracionCancion = (TextView) findViewById(R.id.duracionCancion);
        duracionSegundos = (TextView) findViewById(R.id.duracionSegundos);
        nombreCancion = (TextView) findViewById(R.id.nombreCancion);
        nombreArtista = (TextView) findViewById(R.id.nombreArtista);
        botonPlayPause = (ImageButton) findViewById(R.id.botonPlay);
        botonRepetir = (ImageButton) findViewById(R.id.botonBucle);
        botonAleatorio = (ImageButton) findViewById(R.id.botonAleatorio);
        iv = (ImageView) findViewById(R.id.imagenMusica);
        barraProgreso = findViewById(R.id.barraProgreso);
        itemNombreUsuario = findViewById(R.id.itemNombreUsuario);

        rellenarCanciones();
        handler = new Handler();
        barraProgreso.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    if (listaCanciones.size() != 0) {
                        if (mp.isPlaying() || (!mp.isPlaying() && detectoPause)) {
                            mp.seekTo(progress);
                            barraProgreso.setProgress(progress);
                            actualizoDatos();
                        } else if (!mp.isPlaying() && !detectoPause) {
                            mp.seekTo(progress);
                            barraProgreso.setProgress(progress);
                            mp.start();
                            actualizoDatos();
                        }
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        mVisualizer = findViewById(R.id.blast);

        if (listaCanciones.size() != 0) {
            if (detectoListaCanciones) {
                ponerDeListaCanciones();
            } else {
                if (!mp.isPlaying()) {
                    mp = MediaPlayer.create(this, Uri.parse(listaCanciones.get(posicion).getUri()));
                }
            }
            actualizoDatos();
            updateSeekBar();
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        Bitmap bm = getBitmapFromBytes(MainActivity.blob);
        MenuItem nombre = menu.findItem(R.id.itemNombreUsuario);
        MenuItem avatar = menu.findItem(R.id.avatar);
        if (bm == null && MainActivity.genero.equals("Hombre")) {
            avatar.setIcon(R.drawable.hombre);
        } else if (bm == null && MainActivity.genero.equals("Mujer")) {
            avatar.setIcon(R.drawable.mujer);
        } else {
            Drawable d = new BitmapDrawable(getResources(), bm);
            avatar.setIcon(d);
        }
        nombre.setTitle(nombreUsuario);
        return super.onPrepareOptionsMenu(menu);
    }

    public static Bitmap getBitmapFromBytes(byte[] bytes) {
        if (bytes != null) {
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        }
        return null;
    }

    public void actualizoDatos() {
        try {
            nombreCancion.setText(listaCanciones.get(posicion).getTitle());
            nombreArtista.setText(listaCanciones.get(posicion).getArtist());
            iv.setBackgroundResource(0);
            iv.setImageBitmap(listaCanciones.get(posicion).getPortada());
            duracionCancion.setText(devuelvoMinSeg(mp.getDuration()));
            duracionSegundos.setText(devuelvoMinSeg(mp.getCurrentPosition()));
        } catch (IndexOutOfBoundsException ioobe) {
        }
    }

    String currentPhotoPath;

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    //static final int REQUEST_TAKE_PHOTO = 1;

    public void tomarFoto(View view) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(takePictureIntent, 1);
        /*
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }*/
    }

    static final int REQUEST_IMAGE_CAPTURE = 1;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            listaCanciones.get(posicion).setPortada(imageBitmap);
        }
    }

    public void ponerDeListaCanciones() {
        try {
            posRecojo = getIntent().getExtras();
            String posRecojoString = posRecojo.getString("posRecojo");
            posicion = Integer.parseInt(posRecojoString);
            mp = MediaPlayer.create(this, Uri.parse(listaCanciones.get(posicion).getUri()));

            //barraProgreso.setMax(mp.getDuration());
            //mVisualizer.setAudioSessionId(mp.getAudioSessionId());
            //mp.start();
            //botonPlayPause.setImageResource(R.drawable.pausa);
            detectoListaCanciones = false;
            //detectoPause = false;
            View view = findViewById(R.id.botonPlay);
            PlayPause(view);
            mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    metodoEjecutar();
                }
            });

        } catch (NullPointerException npe) {
        }
    }

    public String devuelvoMinSeg(long miliSegundos) {
        Date date = new Date(miliSegundos);
        @SuppressLint("SimpleDateFormat") SimpleDateFormat formatter = new SimpleDateFormat("m:ss");
        return formatter.format(date);
    }

    private void comprobarFinalCancion() {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                metodoEjecutar();
                handler.postDelayed(this, 100);
            }
        }, 500);
    }

    public void posAleatoriaNoRepetida() {
        int posActual = posicion;
        while (posicion == posActual) {
            posicion = (int) (Math.random() * listaCanciones.size());
        }
    }

    public void mostrarEnToast(String algo) {
        Toast.makeText(this, algo, Toast.LENGTH_SHORT).show();
    }

    private void metodoEjecutar() {
        View view = findViewById(R.id.botonSiguiente);
        Siguiente(view);
        actualizoDatos();
        /*if (posicion < listaCanciones.size() - 1) {
            if (!mp.isPlaying() && !detectoPause) {
                mp.stop();
                if (aleatorio == 1) {
                    posAleatoriaNoRepetida();
                } else {
                    posicion++;
                }
                mp = MediaPlayer.create(this, Uri.parse(listaCanciones.get(posicion).getUri()));
                barraProgreso.setMax(mp.getDuration());
                mp.start();
                // nombreCancion.setText(listaStringsCanciones.get(posicion));
            }
        }*/
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

/*

    public void rellenarCancioness() {
        listaCanciones.clear();
        listaCanciones.clear();
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
            Song cancion = new Song(idAudio, titulo, artista, bm);
            listaCanciones.add(cancion);
        }
    }*/

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.overflow, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.listaCancionesItem || id == R.id.cancionesItem) {
            try {
                irDeItemAActivity("ListaCanciones");
                detectoListaCanciones = true;
            } catch (ClassNotFoundException e) {
                Toast.makeText(this, "Ha ocurrido un error inesperado", Toast.LENGTH_SHORT).show();
            }
        } else if (id == R.id.avatar) {
            mensajeCerrarSesion();
        } else if (id == R.id.itemCerrarSesion) {
            mensajeCerrarSesion();
        } else if (id == R.id.itemSalir) {
            mensajeSalir();
        }
        return super.onOptionsItemSelected(item);
    }

    public void mensajeSalir() {
        AlertDialog.Builder alerta = new AlertDialog.Builder(Reproductor.this);
        alerta.setMessage("¿Desea salir?")
                .setCancelable(false)
                .setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                            finishAffinity();
                            System.exit(0);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        AlertDialog titulo = alerta.create();
        titulo.setTitle("Salir de la Aplicación");
        titulo.show();
    }

    public void mensajeCerrarSesion() {
        AlertDialog.Builder alerta = new AlertDialog.Builder(Reproductor.this);
        alerta.setMessage("¿Desea cerrar sesión?")
                .setCancelable(false)
                .setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            irDeItemAActivity("MainActivity");
                            Reproductor.mp.stop();
                            finish();
                        } catch (ClassNotFoundException e) {
                        }
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        AlertDialog titulo = alerta.create();
        titulo.setTitle("Cerrar Sesión");
        titulo.show();
    }

    public void irDeItemAActivity(String strNombreActivity) throws ClassNotFoundException {
        Intent intent = new Intent(this, Class.forName(("com.example.androidmusic." + strNombreActivity)));
        startActivity(intent);
    }

    public void PlayPause(View view) {
        try {
            if (listaCanciones.size() != 0) {
                if (mp.isPlaying()) {
                    detectoPause = true;
                    mp.pause();
                    mVisualizer.setVisibility(View.INVISIBLE);
                    botonPlayPause.setImageResource(R.drawable.reproducir);
                    //  nombreCancion.setText(listaStringsCanciones.get(posicion));
                } else {
                    detectoPause = false;
                    barraProgreso.setMax(mp.getDuration());
                    mVisualizer.setVisibility(View.VISIBLE);
                    mVisualizer.setAudioSessionId(mp.getAudioSessionId());
                    mp.start();
                    //updateSeekBar();
                    botonPlayPause.setImageResource(R.drawable.pausa);
                    //   nombreCancion.setText(listaStringsCanciones.get(posicion));
                }
            }
            mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    metodoEjecutar();
                }
            });
        } catch (IllegalStateException ise) {
        }
    }

    public void primeraCancion(View view) {
        if (listaCanciones.size() != 0) {
            detectoPause = false;
            if (mp.isPlaying()) {
                mp.stop();
                posicion = 0;
                mp = MediaPlayer.create(this, Uri.parse(listaCanciones.get(posicion).getUri()));
                mVisualizer.setAudioSessionId(mp.getAudioSessionId());
                mp.start();
            } else {
                posicion = 0;
                mp = MediaPlayer.create(this, Uri.parse(listaCanciones.get(posicion).getUri()));
                mVisualizer.setAudioSessionId(mp.getAudioSessionId());
                mp.start();
                botonPlayPause.setImageResource(R.drawable.pausa);
            }
            barraProgreso.setMax(mp.getDuration());
            actualizoDatos();
        }
        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                metodoEjecutar();
            }
        });
    }

    public void ultimaCancion(View view) {
        if (listaCanciones.size() != 0) {
            detectoPause = false;
            if (mp.isPlaying()) {
                mp.stop();
                posicion = listaCanciones.size() - 1;
                mp = MediaPlayer.create(this, Uri.parse(listaCanciones.get(posicion).getUri()));
                mVisualizer.setAudioSessionId(mp.getAudioSessionId());
                mp.start();
            } else {
                posicion = listaCanciones.size() - 1;
                mp = MediaPlayer.create(this, Uri.parse(listaCanciones.get(posicion).getUri()));
                mVisualizer.setAudioSessionId(mp.getAudioSessionId());
                mp.start();
                botonPlayPause.setImageResource(R.drawable.pausa);
            }
            barraProgreso.setMax(mp.getDuration());
            actualizoDatos();
        }
        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                metodoEjecutar();
            }
        });
    }

    public void updateSeekBar() {
        int currPos = mp.getCurrentPosition();
        barraProgreso.setProgress(currPos);

        runnable = () -> {
            updateSeekBar();
            actualizoDatos();
        };
        handler.postDelayed(runnable, 1000);
    }

    public void Repetir(View view) {
        if (repetir == 1) {
            botonRepetir.setImageResource(R.drawable.no_bucle);
            mp.setLooping(false);
            repetir = 2;
            Toast.makeText(this, "Modo Bucle Desactivado", Toast.LENGTH_SHORT).show();
        } else {
            botonRepetir.setImageResource(R.drawable.bucle);
            botonAleatorio.setImageResource(R.drawable.no_aleatorio);
            mp.setLooping(true);
            repetir = 1;
            aleatorio = 2;
            Toast.makeText(this, "Modo Bucle Activado", Toast.LENGTH_SHORT).show();
        }
    }

    public void Aleatorio(View view) {
        if (aleatorio == 1) {
            botonAleatorio.setImageResource(R.drawable.no_aleatorio);
            aleatorio = 2;
            Toast.makeText(this, "Modo Aleatorio Desactivado", Toast.LENGTH_SHORT).show();
        } else {
            botonAleatorio.setImageResource(R.drawable.aleatorio);
            botonRepetir.setImageResource(R.drawable.no_bucle);
            aleatorio = 1;
            repetir = 2;
            Toast.makeText(this, "Modo Aleatorio Activado", Toast.LENGTH_SHORT).show();
        }
    }

    public void Siguiente(View view) {
        if (listaCanciones.size() != 0) {
            if (repetir == 1) {
                mp.seekTo(0);
                barraProgreso.setProgress(0);
            } else if (aleatorio == 1) {
                if (mp.isPlaying()) {
                    mp.stop();
                    posAleatoriaNoRepetida();
                    mp = MediaPlayer.create(this, Uri.parse(listaCanciones.get(posicion).getUri()));
                    mVisualizer.setAudioSessionId(mp.getAudioSessionId());
                    mp.start();
                    //nombreCancion.setText(listaStringsCanciones.get(posicion));
                } else {
                    posAleatoriaNoRepetida();
                    mp = MediaPlayer.create(this, Uri.parse(listaCanciones.get(posicion).getUri()));
                    mVisualizer.setAudioSessionId(mp.getAudioSessionId());
                    mp.start();
                    botonPlayPause.setImageResource(R.drawable.pausa);
                    //nombreCancion.setText(listaStringsCanciones.get(posicion));
                }
                barraProgreso.setMax(mp.getDuration());
            } else {
                detectoPause = false;
                if (posicion < listaCanciones.size() - 1) {
                    if (mp.isPlaying()) {
                        mp.stop();
                        posicion++;
                        mp = MediaPlayer.create(this, Uri.parse(listaCanciones.get(posicion).getUri()));
                        mVisualizer.setAudioSessionId(mp.getAudioSessionId());
                        mp.start();
                        //nombreCancion.setText(listaStringsCanciones.get(posicion));
                    } else {
                        posicion++;
                        mp = MediaPlayer.create(this, Uri.parse(listaCanciones.get(posicion).getUri()));
                        mVisualizer.setAudioSessionId(mp.getAudioSessionId());
                        mp.start();
                        botonPlayPause.setImageResource(R.drawable.pausa);
                        // nombreCancion.setText(listaStringsCanciones.get(posicion));
                    }
                } else {
                    if (mp.isPlaying()) {
                        mp.stop();
                        posicion = 0;
                        mp = MediaPlayer.create(this, Uri.parse(listaCanciones.get(posicion).getUri()));
                        mVisualizer.setAudioSessionId(mp.getAudioSessionId());
                        mp.start();
                        //nombreCancion.setText(listaStringsCanciones.get(posicion));
                    } else {
                        posicion = 0;
                        mp = MediaPlayer.create(this, Uri.parse(listaCanciones.get(posicion).getUri()));
                        mVisualizer.setAudioSessionId(mp.getAudioSessionId());
                        mp.start();
                        botonPlayPause.setImageResource(R.drawable.pausa);
                        // nombreCancion.setText(listaStringsCanciones.get(posicion));
                    }
                }
                barraProgreso.setMax(mp.getDuration());
            }
        }
        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                metodoEjecutar();
            }
        });
    }

    public void Anterior(View view) {
        if (listaCanciones.size() != 0) {
            if (repetir == 1) {
                mp.seekTo(0);
                barraProgreso.setProgress(0);
            } else if (aleatorio == 1) {
                detectoPause = false;
                if (mp.isPlaying()) {
                    mp.stop();
                    posAleatoriaNoRepetida();
                    mp = MediaPlayer.create(this, Uri.parse(listaCanciones.get(posicion).getUri()));
                    mVisualizer.setAudioSessionId(mp.getAudioSessionId());
                    mp.start();
                    //nombreCancion.setText(listaStringsCanciones.get(posicion));
                } else {
                    posAleatoriaNoRepetida();
                    mp = MediaPlayer.create(this, Uri.parse(listaCanciones.get(posicion).getUri()));
                    botonPlayPause.setImageResource(R.drawable.pausa);
                    //nombreCancion.setText(listaStringsCanciones.get(posicion));
                }
                barraProgreso.setMax(mp.getDuration());
            } else {
                detectoPause = false;
                if (posicion >= 1) {
                    if (mp.isPlaying()) {
                        mp.stop();
                        posicion--;
                        mp = MediaPlayer.create(this, Uri.parse(listaCanciones.get(posicion).getUri()));
                        mVisualizer.setAudioSessionId(mp.getAudioSessionId());
                        mp.start();
                        //nombreCancion.setText(listaStringsCanciones.get(posicion));
                    } else {
                        posicion--;
                        mp = MediaPlayer.create(this, Uri.parse(listaCanciones.get(posicion).getUri()));
                        mVisualizer.setAudioSessionId(mp.getAudioSessionId());
                        mp.start();
                        botonPlayPause.setImageResource(R.drawable.pausa);
                        // nombreCancion.setText(listaStringsCanciones.get(posicion));
                    }
                } else {
                    if (mp.isPlaying()) {
                        mp.stop();
                        posicion = listaCanciones.size() - 1;
                        mp = MediaPlayer.create(this, Uri.parse(listaCanciones.get(posicion).getUri()));
                        mVisualizer.setAudioSessionId(mp.getAudioSessionId());
                        mp.start();
                        //nombreCancion.setText(listaStringsCanciones.get(posicion));
                    } else {
                        posicion = listaCanciones.size() - 1;
                        mp = MediaPlayer.create(this, Uri.parse(listaCanciones.get(posicion).getUri()));
                        mVisualizer.setAudioSessionId(mp.getAudioSessionId());
                        mp.start();
                        botonPlayPause.setImageResource(R.drawable.pausa);
                        // nombreCancion.setText(listaStringsCanciones.get(posicion));
                    }
                }
                barraProgreso.setMax(mp.getDuration());
            }
        }
        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                metodoEjecutar();
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        this.gestureDetector.onTouchEvent(e);
        return super.onTouchEvent(e);
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        {
            boolean result = false;
            try {
                if (Math.abs(velocityX) > Math.abs(velocityY)) {
                    if (velocityX < 0) {
                        View view = findViewById(R.id.botonSiguiente);
                        Siguiente(view);
                        actualizoDatos();
                        result = true;
                    } else if (velocityX > 0) {
                        View view = findViewById(R.id.botonAnterior);
                        Anterior(view);
                        actualizoDatos();
                        result = true;
                    }
                }
            } catch (Exception exception) {
            }
            return result;
        }
    }
}