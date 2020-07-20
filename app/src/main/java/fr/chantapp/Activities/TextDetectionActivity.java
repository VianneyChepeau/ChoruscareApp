package fr.chantapp.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import fr.chantapp.R;

public class TextDetectionActivity extends AppCompatActivity {

    private ImageView myImageView;
    private TextView textDisplayTV;
    private Button captureImageButton, saveDetectedTextButton;
    private Bitmap imageBitmap;
    private int typeParole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_detection);

        Intent intent = getIntent();
        typeParole = intent.getIntExtra("typeParole", 0);

        myImageView = findViewById(R.id.myImageView);
        textDisplayTV = findViewById(R.id.textDisplayTV);
        captureImageButton = findViewById(R.id.captureImageButton);
        saveDetectedTextButton = findViewById(R.id.saveDetectedTextButton);

        captureImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });

        saveDetectedTextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), EditGeneralChantActivity.class);
                if(typeParole == 1)
                    intent.putExtra( "refrain", textDisplayTV.getText());
                else if(typeParole == 2)
                    intent.putExtra( "couplet", textDisplayTV.getText());
                else if(typeParole == 3)
                    intent.putExtra( "coda", textDisplayTV.getText());

                startActivity(intent);
            }
        });
    }

    private void detectTextFromImage(){
        TextRecognizer textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();
        if(!textRecognizer.isOperational()){
            Toast.makeText(this, "Le service 'TextRecognizer' n'est pas disponible !", Toast.LENGTH_SHORT).show();
        } else {
            textDisplayTV.setText("");
            Frame frame = new Frame.Builder().setBitmap(imageBitmap).build();
            SparseArray<TextBlock> textDetected = textRecognizer.detect(frame);
            String text = "";
            for (int i=0; i < textDetected.size()+1; i++){
                text += textDetected.get(i);
            }
            textDisplayTV.setText(text);
        }
    }

    static final int REQUEST_IMAGE_CAPTURE = 1;
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            imageBitmap = (Bitmap) extras.get("data");
            myImageView.setImageBitmap(imageBitmap);

            detectTextFromImage();
        }
    }
}
