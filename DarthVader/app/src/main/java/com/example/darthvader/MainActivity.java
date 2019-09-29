package com.example.darthvader;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionPoint;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceContour;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceLandmark;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int CAMERA_REQUEST = 1888;
    private ImageView imageView;
    private static final int MY_CAMERA_PERMISSION_CODE = 100;
    private static final int MY_STORAGE_PERMISSION_CODE = 1;
    private Uri FileUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(Build.VERSION.SDK_INT>=24){
            try{
                Method m = StrictMode.class.getMethod("disableDeathOnFileUriExposure");
                m.invoke(null);
            }catch(Exception e){
                e.printStackTrace();
            }
        }

        this.imageView = (ImageView) this.findViewById(R.id.imageView);
        Button capture = (Button) this.findViewById(R.id.capture);

        capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_CAMERA_PERMISSION_CODE);
                if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                {
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_PERMISSION_CODE);

                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_STORAGE_PERMISSION_CODE);
                }
                else
                {
                    Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                   // String outPath = "/users/"+"1.jpg";
                    //File outFile = new File(outPath);
                    //Uri outuri = Uri.fromFile(outFile);
                    System.out.println("HELLLLOOOOOOOO1221");

                    //Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    File file = new File(Environment.getExternalStorageDirectory(), "img1.jpg");

                    FileUri = Uri.fromFile(file);
                    System.out.println(file);
                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, FileUri);

                    startActivityForResult(cameraIntent, CAMERA_REQUEST);
                }
            }
        });


        //FirebaseVisionImage image = FirebaseVisionImage.fromBitmap();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_PERMISSION_CODE)
        {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();


                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, FileUri);


                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            }
            else
            {
                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
            }
        }



    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK)
        {
            System.out.println("HELLLLOOOOOOOO");

            Bitmap photo = null;

            //Bitmap photo = (Bitmap) data.getExtras().get("data");

            try {
                photo = MediaStore.Images.Media.getBitmap(this.getContentResolver(), FileUri);
                System.out.println("SUCCESS");
            } catch (IOException e) {
                e.printStackTrace();
            }


           imageView.setImageBitmap(photo);

            detect_face(photo);

        }
    }

    private void detect_face(Bitmap photo) {


        // Real-time contour detection of multiple faces
        FirebaseVisionFaceDetectorOptions options =
                new FirebaseVisionFaceDetectorOptions.Builder()
                        .setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
                        .enableTracking()
                        .build();

        FirebaseVisionFaceDetector detector = FirebaseVision.getInstance().getVisionFaceDetector(options);


        FirebaseVisionImage firebaseImage = FirebaseVisionImage.fromBitmap(photo);
        Task<List<FirebaseVisionFace>> result = detector
                .detectInImage(firebaseImage)
                .addOnSuccessListener(new
                                              OnSuccessListener<List<FirebaseVisionFace>>() {
                                                  @Override
                                                  public void onSuccess(List<FirebaseVisionFace> faces) {
                                                      for (FirebaseVisionFace face : faces) {
                                                          System.out.println("Smiling Prob ["+face.getSmilingProbability()+"]");

                                                          if(face.getSmilingProbability()>.90)
                                                          {
                                                              System.out.println("Your Smiling");
                                                          }
                                                          else
                                                          {
                                                              System.out.println("Please Smile");
                                                          }

                                                      }
                                                  }
                                              });



    }


}
