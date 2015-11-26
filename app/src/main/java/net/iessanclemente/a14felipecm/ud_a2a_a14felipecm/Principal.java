package net.iessanclemente.a14felipecm.ud_a2a_a14felipecm;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Principal extends Activity {

    Spinner spCanciones;
    Button btnGravar;
    ImageView mImageView;
    ArrayList<String> canciones = new ArrayList<String>();
    String rutaCanciones = Environment.getExternalStorageDirectory().getAbsolutePath() + "/UD_A2A/MUSICA/";
    String rutaFotos = Environment.getExternalStorageDirectory().getAbsolutePath() + "/UD_A2A/FOTO/";
    private String arquivoGravar;
    private MediaPlayer mp = new MediaPlayer();
    private MediaRecorder mr = new MediaRecorder();
    private boolean pause;
    private boolean grabando=false;
    int pSelecc;
    SimpleDateFormat sdf;
    String currentDateandTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);
        btnGravar = (Button) findViewById(R.id.btnGrb);
        spCanciones = (Spinner) findViewById(R.id.spin_audio);
        mImageView = (ImageView) findViewById(R.id.imgv);

        sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        currentDateandTime = sdf.format(new Date());

        try{
            cargarSpinner();
            spCanciones.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    pSelecc = position;
                }
                @Override
                public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub
                }
            });
        }catch(IOException ioe){
            ioe.printStackTrace();
            Log.e("IO-ERROR","No existe la carpeta");
        }
    }

    //Reproducir cancion
    public void start(View v){
        if(!pause){
            try{
                mp.reset();
                mp.setDataSource(rutaCanciones + canciones.get(pSelecc).toString());
                mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mp.prepare();
                mp.start();
                Log.i("REPRODUCTOR","Play");
            }catch(IOException ioe){
                ioe.printStackTrace();
                Log.e("ERROR-REPRODUCTOR", "Play");
            }
        }else{
            mp.start();
            Log.i("REPRODUCTOR", "Resume");
        }

    }

    //Detener reproduccion
    public void stop(View v){
        mp.stop();
        pause=false;
        Log.i("REPRODUCTOR", "Stop");
    }

    //Pausar reproduccion
    public void pausar(View v){
        mp.pause();
        pause=true;
        Log.i("REPRODUCTOR", "Pausa");
    }

    public void accionGrabar(View v){
        if(!grabando){
            btnGravar.setCompoundDrawablesWithIntrinsicBounds(null,null,null, getResources().getDrawable(android.R.drawable.ic_delete));
            Snackbar.make(v,"Grabando",Snackbar.LENGTH_INDEFINITE).show();
            grabando=true;
            grabar();
        }else{
            btnGravar.setCompoundDrawablesWithIntrinsicBounds(null,null,null, getResources().getDrawable(android.R.drawable.ic_btn_speak_now));
            Snackbar.make(v,"Parado",Snackbar.LENGTH_SHORT).show();
            grabando=false;
            pararDeGravar();
            try {
                cargarSpinner();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void grabar(){
        mr = new MediaRecorder();
        arquivoGravar = rutaCanciones + "record-"+ currentDateandTime +".3gp";
        mr.setAudioSource(MediaRecorder.AudioSource.MIC);
        mr.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mr.setMaxDuration(10000);
        mr.setAudioEncodingBitRate(32768);
        mr.setAudioSamplingRate(8000); // No emulador só 8000 coma
        mr.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mr.setOutputFile(arquivoGravar);
        try {
            mr.prepare();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            Log.e("ERROR-GRAVAR", "Error gravando"+e.toString());
            mr.reset();
        }
        mr.start();
        Log.i("GRAVAR", "Play");
    }

    public void pararDeGravar(){
        Log.i("GRAVAR", "sTOP");
        mr.stop();
        mr.release();
        mr = null;
    }



    static final int REQUEST_IMAGE_CAPTURE = 1;

    public void sacarFoto(View view) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }


    public void cargarSpinner() throws IOException{
        canciones.clear();
        ArrayAdapter<String> adaptador = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, canciones);
        File fDirect = new File(rutaCanciones);
        for(File f : fDirect.listFiles()){
            if(f.isFile()){
                adaptador.add(f.getName());
            }
        }
        spCanciones.setAdapter(adaptador);
    }




    @Override
    protected void onPause() {
        super.onPause();

        if (mp.isPlaying()){
            mp.pause();
            pause = true;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (pause) {
            mp.start();
            pause = false;
        }
    }


    @Override
    protected void onSaveInstanceState(Bundle estado) {
        estado.putBoolean("MEDIAPLAYER_PAUSE", pause);
        super.onSaveInstanceState(estado);

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        savedInstanceState.putBoolean("MEDIAPLAYER_PAUSE", false);
        pause = savedInstanceState.getBoolean("MEDIAPLAYER_PAUSE");

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mp.isPlaying()) mp.stop();

        if (mp != null) mp.release();
        mp = null;

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            mImageView.setImageBitmap(imageBitmap);
            File fileDir = new File(rutaFotos);
            if(!fileDir.exists()) fileDir.mkdirs();
            File fileFoto = new File(fileDir, "Foto-"+currentDateandTime+".jpeg");
            try {
                FileOutputStream fos = new FileOutputStream(fileFoto);
                imageBitmap.compress(Bitmap.CompressFormat.JPEG, 85, fos);
                fos.flush();
                fos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


}
