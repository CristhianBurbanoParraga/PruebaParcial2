package com.example.pruebaparcial2;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import WebServices.Asynchtask;
import WebServices.WebService;

public class InfoActivity extends AppCompatActivity implements OnMapReadyCallback {


    private double []  coordenadasPais;

    TextView pais;
    TextView infotext;
    ImageView imagen;

    Paises paises;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        paises = new Paises();

        pais = (TextView) findViewById(R.id.txtpais);
        infotext = (TextView) findViewById(R.id.infotext);
        imagen = (ImageView) findViewById(R.id.imagen);

        Bundle bundle = this.getIntent().getExtras();
       // valorPasado=bundle.getString("nombrepais");
        pais.setText(bundle.getString("nombrepais"));

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);

    }

    GoogleMap mapa;

    @Override
    public void onMapReady(GoogleMap googleMap) {

        Glide.with( this.getApplicationContext()).load(getIntent().getStringExtra("url_imagen")).into(imagen);
        mapa = googleMap;
        mapa.getUiSettings().setZoomControlsEnabled(true);
        mapa.setMapType(GoogleMap.MAP_TYPE_NORMAL);


        pais.setText(getIntent().getStringExtra("nombre_pais"));

        double [] lstCoord = getIntent().getDoubleArrayExtra("listaCooordenadas");

        String info = "Pais: " + getIntent().getStringExtra("nombre_Pais") + "\n" +
                "Url bandera: " + getIntent().getStringExtra("url_imagen") + "\n" +
                "Capital: " + getIntent().getStringExtra("capital_Pais") + "\n" +
                "Coordenadas: " + "\n" +
                "WEST: " +lstCoord[0] + "\n" +
                "EAST: " + lstCoord[1] + "\n" +
                "NORTH: " + lstCoord[2] + "\n" +
                "SOUTH" + lstCoord[3];
        infotext.setText(info);

        CameraUpdate camUpd1 = CameraUpdateFactory.newLatLngZoom(new LatLng(lstCoord[4],lstCoord[5]), 5);
        mapa.moveCamera(camUpd1);

        PolylineOptions lineas = new PolylineOptions()
                .add(new LatLng(lstCoord[2], lstCoord[0])) //NOR OESTE
                .add(new LatLng(lstCoord[2], lstCoord[1])) //NOR ESTE
                .add(new LatLng(lstCoord[3], lstCoord[1]))  //SUR ESTE
                .add(new LatLng(lstCoord[3], lstCoord[0]))  // SUR OESTE
                .add(new LatLng(lstCoord[2], lstCoord[0]));  // NOR OESTE
        lineas.width(8);
        lineas.color(Color.RED);
        mapa.addPolyline(lineas);
    }
}