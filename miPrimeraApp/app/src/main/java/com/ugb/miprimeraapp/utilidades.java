package com.ugb.miprimeraapp;

import android.util.Base64;

public class utilidades {
    // Cambia esta IP por la de tu Mac
    static String ip = "10.0.2.2";
    
    static String url_consulta = "http://" + ip + ":5984/dbamigos/_design/dbamigos/_view/dbamigos";
    static String url_mto = "http://" + ip + ":5984/dbamigos"; 
    static String user = "admin";
    static String passwd = "admin";
    
    // Usamos android.util.Base64 para evitar las líneas rojas y errores de compatibilidad
    static String credencialesCodificadas = Base64.encodeToString((user + ":" + passwd).getBytes(), Base64.NO_WRAP);

    public String generarUnicoId(){
        return java.util.UUID.randomUUID().toString();
    }
}