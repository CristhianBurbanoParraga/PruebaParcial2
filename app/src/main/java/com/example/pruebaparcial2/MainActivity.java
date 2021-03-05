package com.example.pruebaparcial2;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.google.api.services.vision.v1.model.FaceAnnotation;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;
import com.google.api.services.vision.v1.model.TextAnnotation;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import WebServices.Asynchtask;
import WebServices.WebService;

public class MainActivity extends AppCompatActivity implements Asynchtask {

    public Vision vision;

    private static final int PICK_IMAGE = 100;

    Button btncargarimagen;
    Button btnprocesar;
    ImageView imagen;
    TextView nombreimagen;
    Uri imageUri;

    Map<String, String> datos;
    String message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imagen = (ImageView) findViewById(R.id.idimagen);
        btncargarimagen = (Button) findViewById(R.id.idabrirgaleria);
        btnprocesar = (Button) findViewById(R.id.idprocesar);
        nombreimagen = (TextView) findViewById(R.id.idnombreimagen);

        Vision.Builder visionBuilder = new Vision.Builder(new NetHttpTransport(),
                new AndroidJsonFactory(), null);
        visionBuilder.setVisionRequestInitializer(new VisionRequestInitializer("AIzaSyB5MkIB5lNnQH1kC1tZ3ATeEsv7z66moKs"));
        vision = visionBuilder.build();

        datos = new HashMap<String, String>();
        WebService ws= new WebService("http://www.geognos.com/api/en/countries/info/all.json", datos, this, this);
        ws.execute("");

        btncargarimagen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });
    }

    private void openGallery() {
        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(gallery, PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == PICK_IMAGE) {
            imageUri = data.getData();
            imagen.setImageURI(imageUri);
            Cursor returnCursor = getContentResolver().query(imageUri, null, null, null, null);
            int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            returnCursor.moveToFirst();
            nombreimagen.setText(returnCursor.getString(nameIndex));
        }
    }


    public void sendImage (View view) {
        //Intent intent = new Intent(MainActivity.this, InfoActivity.class);
        //b.putString("nombrepais", message);
        //intent.putExtras(b);
        //startActivity(intent);
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {

                BitmapDrawable drawable = (BitmapDrawable) imagen.getDrawable();
                Bitmap bitmap = drawable.getBitmap();
                bitmap = scaleBitmapDown(bitmap, 1200);
                ByteArrayOutputStream stream = new ByteArrayOutputStream(); //2da de la api
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream);
                byte[] imageInByte = stream.toByteArray();
                Image inputImage = new Image(); //googlevision
                inputImage.encodeContent(imageInByte);

                Feature desiredFeature = new Feature();
                desiredFeature.setType("TEXT_DETECTION");

                //Armamos la solicitud o las solicitudes .- FaceDetection solo o facedeteccion,textdetection,etc..
                AnnotateImageRequest request = new AnnotateImageRequest();
                request.setImage(inputImage);
                request.setFeatures(Arrays.asList(desiredFeature));
                BatchAnnotateImagesRequest batchRequest = new
                        BatchAnnotateImagesRequest();
                batchRequest.setRequests(Arrays.asList(request));

                //Asignamos al control VisionBuilder la solicitud
                BatchAnnotateImagesResponse batchResponse = null;
                try {
                    Vision.Images.Annotate annotateRequest =
                            vision.images().annotate(batchRequest);
                    //Enviamos la solicitud
                    annotateRequest.setDisableGZipContent(true);
                    batchResponse = annotateRequest.execute();
                } catch (IOException ex) {
                    message+=ex.getMessage()+"\n";
                    //Toast.makeText(MainActivity.this, ex.getMessage().toString(), Toast.LENGTH_SHORT).show();
                }
                if(batchResponse!=null) {
                    message="";
                    TextAnnotation text = batchResponse.getResponses().get(0).getFullTextAnnotation();
                    if (text != null) {
                        message = text.getText();
                        message = message.replaceAll("\n", "");
                        //nombreimagen.setText(message);
                    } else {
                        message += "This picture doesn't contain text" + "\n";
                    }
                } else {

                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        nombreimagen.setText(message);
                        /*
                        Bundle bundle = new Bundle();
                        bundle.putString("nombrepais", message);
                        intent.putExtras(bundle);
                        startActivity(intent);
                        */
                    }
                });
            }
        });
        //cargarImagen();
    }

    private Bitmap scaleBitmapDown(Bitmap bitmap, int maxDimension) {
        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();
        int resizedWidth = maxDimension;
        int resizedHeight = maxDimension;
        if (originalHeight > originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = (int) (resizedHeight * (float) originalWidth / (float) originalHeight);
        } else if (originalWidth > originalHeight) {
            resizedWidth = maxDimension;
            resizedHeight = (int) (resizedWidth * (float) originalHeight / (float) originalWidth);
        } else if (originalHeight == originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = maxDimension;
        }
        return Bitmap.createScaledBitmap(bitmap, resizedWidth, resizedHeight, false);
    }

    ArrayList<Paises> listaPaises;
    private double []  coordenadasPais;

    @Override
    public void processFinish(String result) throws JSONException {
        JSONObject pais=  new JSONObject(result);
        listaPaises = Paises.JsonObjectsBuild(pais);
    }

    public void cargarImagen (View view) {
        for (Paises p : listaPaises) {
            if (p.getNombrePais().toUpperCase().equals(message.toUpperCase())) {
                //System.out.println("ok");
                Intent intent = new Intent (this.getApplicationContext(), InfoActivity.class);

                intent.putExtra("listaCooordenadas",p.getCoordenadasPais());
                intent.putExtra("url_imagen",p.getUrl_Pais());
                intent.putExtra("nombre_Pais",p.getNombrePais());
                intent.putExtra("capital_Pais",p.getCapital());

                startActivityForResult(intent, 0);
                //return p
            } else {
                //System.out.println(p.getNombrePais().toUpperCase() + "     " + message.toUpperCase());
            }
        }
    }






}