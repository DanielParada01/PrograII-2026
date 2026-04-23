package com.ugb.miprimeraapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class lista_amigos extends Activity {
    Bundle parametros = new Bundle();
    DB db;
    FloatingActionButton fab;
    ListView ltsAmigos;
    JSONArray jsonArray;
    JSONObject jsonObject;
    int posicion = 0;
    final ArrayList<amigos> alAmigos = new ArrayList<amigos>();
    final ArrayList<amigos> alAmigosCopia = new ArrayList<amigos>();
    detectarinternet di;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_amigos);

        db = new DB(this);
        di = new detectarinternet(this);
        
        fab = findViewById(R.id.fabAgregarAmigos);
        fab.setOnClickListener(v -> {
            parametros.clear();
            parametros.putString("accion", "nuevo");
            abrirActivity();
        });

        obtenerAmigos();
        buscarAmigos();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mimenu, menu);
        try {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
            posicion = info.position;
            menu.setHeaderTitle(jsonArray.getJSONObject(posicion).getJSONObject("value").getString("nombre"));
        } catch (Exception e) {
            mostrarMsg("Error al desplegar menu: " + e.getMessage());
        }
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        try {
            if (item.getItemId() == R.id.mnxAgregar) {
                parametros.clear();
                parametros.putString("accion", "nuevo");
                abrirActivity();
            } else if (item.getItemId() == R.id.mnxModificar) {
                parametros.clear();
                parametros.putString("accion", "modificar");
                parametros.putString("amigos", jsonArray.getJSONObject(posicion).getJSONObject("value").toString());
                abrirActivity();
            } else if (item.getItemId() == R.id.mnxEliminar) {
                borrarAmigo();
            }
            return true;
        } catch (Exception e) {
            mostrarMsg("Error al seleccionar un item del menu: " + e.getMessage());
            return super.onContextItemSelected(item);
        }
    }

    private void borrarAmigo() {
        try {
            JSONObject amigoSeleccionado = jsonArray.getJSONObject(posicion).getJSONObject("value");
            String nombre = amigoSeleccionado.getString("nombre");
            
            AlertDialog.Builder confirmacion = new AlertDialog.Builder(this);
            confirmacion.setTitle("¿Está seguro de borrar a?");
            confirmacion.setMessage(nombre);
            confirmacion.setPositiveButton("SI", (dialog, which) -> {
                try {
                    String idAmigoLocal = amigoSeleccionado.getString("idAmigo");
                    String respuestaLocal = db.administrar_amigos("eliminar", new String[]{idAmigoLocal});

                    if (respuestaLocal.equals("ok")) {
                        if (di.hayConexionInternet() && amigoSeleccionado.has("_id")) {
                            String _id = amigoSeleccionado.getString("_id");
                            String _rev = amigoSeleccionado.getString("_rev");
                            String url = utilidades.url_mto + "/" + _id + "?rev=" + _rev;

                            enviarDatosServidor objEnviar = new enviarDatosServidor(this);
                            String rServidor = objEnviar.execute("", "DELETE", url).get();
                            
                            if (rServidor.startsWith("{")) {
                                JSONObject resJSON = new JSONObject(rServidor);
                                if (resJSON.getBoolean("ok")) {
                                    mostrarMsg("Eliminado de local y servidor.");
                                } else {
                                    mostrarMsg("Error en servidor: " + rServidor);
                                }
                            } else {
                                mostrarMsg("Error de red: " + rServidor);
                            }
                        } else {
                            mostrarMsg("Amigo borrado con éxito (Local).");
                        }
                        obtenerAmigos();
                    }
                } catch (Exception e) {
                    mostrarMsg("Error al eliminar: " + e.getMessage());
                }
            });
            confirmacion.setNegativeButton("NO", null);
            confirmacion.show();
        } catch (Exception e) {
            mostrarMsg("Error: " + e.getMessage());
        }
    }

    private void obtenerAmigos() {
        try {
            if (di.hayConexionInternet()) {
                String respuesta = new obtenerDatosServidor().execute().get();
                
                if (respuesta == null || respuesta.trim().isEmpty()) {
                    mostrarMsg("Error: El servidor devolvió una respuesta vacía");
                    leerDeLocal();
                    return;
                }

                // Si la respuesta NO parece un JSON, mostramos el error de red directamente
                if (!respuesta.trim().startsWith("{")) {
                    mostrarMsg("Error de conexión: " + respuesta);
                    leerDeLocal(); // Cargamos los datos locales mientras tanto
                    return;
                }

                jsonObject = new JSONObject(respuesta);
                jsonArray = jsonObject.getJSONArray("rows");
                mostrarAmigos();
            } else {
                leerDeLocal();
            }
        } catch (Exception e) {
            mostrarMsg("Error al obtener datos: " + e.getMessage());
            leerDeLocal();
        }
    }

    private void leerDeLocal() {
        try {
            Cursor cAmigos = db.lista_amigos();
            jsonArray = new JSONArray();
            if (cAmigos.moveToFirst()) {
                do {
                    JSONObject fila = new JSONObject();
                    JSONObject value = new JSONObject();
                    value.put("idAmigo", cAmigos.getString(0));
                    value.put("nombre", cAmigos.getString(1));
                    value.put("direccion", cAmigos.getString(2));
                    value.put("telefono", cAmigos.getString(3));
                    value.put("email", cAmigos.getString(4));
                    value.put("dui", cAmigos.getString(5));
                    value.put("foto", cAmigos.getString(6));
                    
                    fila.put("value", value);
                    jsonArray.put(fila);
                } while (cAmigos.moveToNext());
            }
            mostrarAmigos();
        } catch (Exception e) {
            mostrarMsg("Error al leer local: " + e.getMessage());
        }
    }

    private void mostrarAmigos() {
        try {
            ltsAmigos = findViewById(R.id.ltsAmigos);
            alAmigos.clear();
            alAmigosCopia.clear();

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i).getJSONObject("value");
                alAmigos.add(new amigos(
                        obj.getString("idAmigo"),
                        obj.getString("nombre"),
                        obj.getString("direccion"),
                        obj.getString("telefono"),
                        obj.getString("email"),
                        obj.getString("dui"),
                        obj.getString("foto")
                ));
            }
            alAmigosCopia.addAll(alAmigos);
            ltsAmigos.setAdapter(new AdaptadorAmigos(this, alAmigos));
            registerForContextMenu(ltsAmigos);
            
            if (alAmigos.isEmpty()) {
                mostrarMsg("No hay amigos para mostrar");
            }
        } catch (Exception e) {
            mostrarMsg("Error al mostrar lista: " + e.getMessage());
        }
    }

    private void buscarAmigos() {
        TextView tempVal = findViewById(R.id.txtBuscarAmigos);
        tempVal.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                alAmigos.clear();
                String buscar = s.toString().trim().toLowerCase();
                if (buscar.isEmpty()) {
                    alAmigos.addAll(alAmigosCopia);
                } else {
                    for (amigos item : alAmigosCopia) {
                        if (item.getNombre().toLowerCase().contains(buscar) ||
                            item.getDui().contains(buscar) ||
                            item.getEmail().toLowerCase().contains(buscar)) {
                            alAmigos.add(item);
                        }
                    }
                }
                ltsAmigos.setAdapter(new AdaptadorAmigos(getApplicationContext(), alAmigos));
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void abrirActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtras(parametros);
        startActivity(intent);
    }

    private void mostrarMsg(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }
}
