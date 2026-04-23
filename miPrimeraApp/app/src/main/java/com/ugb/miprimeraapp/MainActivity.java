package com.ugb.miprimeraapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends Activity {
    DB db;
    Button btn;
    TextView tempVal;
    String accion="nuevo", idAmigo="", urlFoto="", id="", rev="";
    FloatingActionButton fab;
    ImageButton img;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        img = findViewById(R.id.imgFotoAmigo);
        img.setOnClickListener(v->mostrarOpcionesImagen());

        db = new DB(this);

        btn = findViewById(R.id.btnGuardarAmigo);
        btn.setOnClickListener(v->guardarAmigo());

        fab = findViewById(R.id.fabListaAmigo);
        fab.setOnClickListener(v->regresarListaAmigos());

        mostrarDatosAmigos();
    }

    private void mostrarOpcionesImagen() {
        String[] opciones = {"Tomar Foto", "Elegir de Galería"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Seleccionar Imagen");
        builder.setItems(opciones, (dialog, which) -> {
            if (which == 0) {
                tomarFoto();
            } else {
                abrirGaleria();
            }
        });
        builder.show();
    }

    private void abrirGaleria() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, 2);
    }

    private void mostrarDatosAmigos(){
        try{
            Bundle parametros = getIntent().getExtras();
            if (parametros == null) return;
            
            accion = parametros.getString("accion");
            if(accion.equals("modificar")){
                JSONObject datos = new JSONObject(parametros.getString("amigos"));
                id = datos.getString("_id");
                rev = datos.getString("_rev");
                idAmigo = datos.getString("idAmigo");

                tempVal = findViewById(R.id.txtNombreAmigos);
                tempVal.setText(datos.getString("nombre"));

                tempVal = findViewById(R.id.txtDireccionAmigos);
                tempVal.setText(datos.getString("direccion"));

                tempVal = findViewById(R.id.txtTelefonoAmigos);
                tempVal.setText(datos.getString("telefono"));

                tempVal = findViewById(R.id.txtEmailAmigos);
                tempVal.setText(datos.getString("email"));

                tempVal = findViewById(R.id.txtDuiAmigos);
                tempVal.setText(datos.getString("dui"));

                urlFoto = datos.getString("foto");
                if (urlFoto != null && !urlFoto.isEmpty()) {
                    img.setImageURI(Uri.parse(urlFoto));
                }
            }
        }catch (Exception e){
            mostrarMsg("Error al mostrar los datos: "+ e.getMessage());
        }
    }

    private void tomarFoto(){
        try{
            File fotoAmigo = crearImgAmigo();
            if(fotoAmigo!=null){
                Uri uriFoto = FileProvider.getUriForFile(MainActivity.this, "com.ugb.miprimeraapp.fileprovider", fotoAmigo);
                Intent tomarFotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                tomarFotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, uriFoto);
                startActivityForResult(tomarFotoIntent, 1);
            }
        } catch (Exception e) {
            mostrarMsg("Error al abrir cámara: "+ e.getMessage());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK) {
            try {
                if(requestCode == 1) { // Cámara
                    img.setImageURI(null);
                    img.setImageURI(Uri.fromFile(new File(urlFoto)));
                } else if (requestCode == 2 && data != null) { // Galería
                    Uri selectedImage = data.getData();
                    // Copiamos la imagen de la galería a nuestra carpeta privada para tener una ruta permanente
                    urlFoto = copiarImagenAGaleriaPrivada(selectedImage);
                    img.setImageURI(null);
                    img.setImageURI(Uri.parse(urlFoto));
                }
            } catch (Exception e) {
                mostrarMsg("Error al procesar imagen: " + e.getMessage());
            }
        }
    }

    private String copiarImagenAGaleriaPrivada(Uri uri) throws Exception {
        InputStream is = getContentResolver().openInputStream(uri);
        File destFile = crearImgAmigo();
        FileOutputStream fos = new FileOutputStream(destFile);
        byte[] buffer = new byte[1024];
        int read;
        while ((read = is.read(buffer)) != -1) {
            fos.write(buffer, 0, read);
        }
        fos.close();
        is.close();
        return destFile.getAbsolutePath();
    }

    private File crearImgAmigo() throws Exception{
        String fechaHoraMs = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()),
                fileMane = "foto_"+ fechaHoraMs;
        File dirAlmacenamiento = getExternalFilesDir(Environment.DIRECTORY_DCIM);
        if(!dirAlmacenamiento.exists()){
            dirAlmacenamiento.mkdirs();
        }
        File image = File.createTempFile(fileMane, ".jpg", dirAlmacenamiento);
        urlFoto = image.getAbsolutePath();
        return image;
    }

    private void guardarAmigo(){
        try {
            tempVal = findViewById(R.id.txtNombreAmigos);
            String nombre = tempVal.getText().toString();

            tempVal = findViewById(R.id.txtDireccionAmigos);
            String direccion = tempVal.getText().toString();

            tempVal = findViewById(R.id.txtTelefonoAmigos);
            String tel = tempVal.getText().toString();

            tempVal = findViewById(R.id.txtEmailAmigos);
            String email = tempVal.getText().toString();

            tempVal = findViewById(R.id.txtDuiAmigos);
            String dui = tempVal.getText().toString();

            if (idAmigo.isEmpty()) idAmigo = new utilidades().generarUnicoId();

            String[] datos = {idAmigo, nombre, direccion, tel, email, dui, urlFoto};
            db.administrar_amigos(accion, datos);
            
            JSONObject datosAmigos = new JSONObject();
            if(accion.equals("modificar")){
                datosAmigos.put("_id", id);
                datosAmigos.put("_rev", rev);
            }
            datosAmigos.put("idAmigo", idAmigo);
            datosAmigos.put("nombre", nombre);
            datosAmigos.put("direccion", direccion);
            datosAmigos.put("telefono", tel);
            datosAmigos.put("email", email);
            datosAmigos.put("dui", dui);
            datosAmigos.put("foto", urlFoto);

            enviarDatosServidor objEnviarDatosServidor = new enviarDatosServidor(this);
            String respuesta = objEnviarDatosServidor.execute(datosAmigos.toString(), "POST", utilidades.url_mto).get();

            mostrarMsg("Registro guardado con éxito.");
            regresarListaAmigos();
            
        } catch (Exception e) {
            mostrarMsg("Error al guardar: " + e.getMessage());
        }
    }

    private void mostrarMsg(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }
    private void regresarListaAmigos(){
        Intent intent = new Intent(this, lista_amigos.class);
        startActivity(intent);
    }
}
