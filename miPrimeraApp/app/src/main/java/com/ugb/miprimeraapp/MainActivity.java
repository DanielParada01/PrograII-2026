package com.ugb.miprimeraapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


public class MainActivity extends AppCompatActivity {
  TabHost tbh;

    TextView tempVal;
    Spinner spn;
    Button btn;
    Double valores[] = new Double[] {1.0, 0.85, 7.67, 26.42, 36.80, 495.77};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        tbh=findViewById(R.id.tbhConversores);
        tbh.setup();

        tbh = findViewById(R.id.tbhConversores);
        tbh.setup();

        tbh.addTab(tbh.newTabSpec("Monedas").setContent(R.id.tabMonedas).setIndicator("MONEDAS", null));
        tbh.addTab(tbh.newTabSpec("Longitud").setContent(R.id.tabLongitud).setIndicator("LONGITUD", null));
        tbh.addTab(tbh.newTabSpec("Volumen").setContent(R.id.tabVolumen).setIndicator("VOLUMEN", null));
        tbh.addTab(tbh.newTabSpec("Masa").setContent(R.id.tabMasa).setIndicator("MASA", null));

        btn = findViewById(R.id.btnConvertir);
        btn.setOnClickListener(v->convertirMonedas());
    }
    private void convertirMonedas(){
        spn = findViewById(R.id.spnMonedasDe);
        int de = spn.getSelectedItemPosition();

        spn = findViewById(R.id.spnMonedasA);
        int a = spn.getSelectedItemPosition();

        tempVal = findViewById(R.id.txtMonedasAConvertir);
        double cantidad = Double.parseDouble(tempVal.getText().toString());
        double respuesta = conversor(de, a, cantidad);

        tempVal = findViewById(R.id.lblRespuestaMonedas);
        tempVal.setText("Respuesta: "+ respuesta);
    }
    double conversor(int de, int a, double cantidad){
        return valores[a]/valores[de] * cantidad;
    }
    }