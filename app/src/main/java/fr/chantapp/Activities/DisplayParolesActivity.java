package fr.chantapp.Activities;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.File;

import fr.chantapp.R;
import fr.chantapp.db.Chant;
import fr.chantapp.db.DaoSession;
import fr.chantapp.db.DbSession;
import fr.chantapp.db.Parole;

public class DisplayParolesActivity extends AppCompatActivity {

    private DaoSession database;
    private Chant monChant;
    private LinearLayout displayParolesLayout;
    private Button button_open_partition;
    public static final int STORAGE_PERMISSION = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_paroles);

        initialize();
        loadAndDisplayParoles();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        searchItem.setVisible(false);
        MenuItem addCoupletItem = menu.findItem(R.id.action_add_couplet);
        addCoupletItem.setVisible(false);
        MenuItem deleteChant = menu.findItem(R.id.action_delete_chant);
        deleteChant.setVisible(false);
        MenuItem partagerBDD = menu.findItem(R.id.action_share);
        partagerBDD.setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent homeIntent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(homeIntent);
                return true;
            case R.id.action_edit_chant:
                Intent editIntent = new Intent(getApplicationContext(), EditGeneralChantActivity.class);
                editIntent.putExtra("idChant", monChant.getId());
                startActivity(editIntent);
                return true;
            case R.id.action_edit_ordre_paroles:
                Intent editOrdreIntent = new Intent(getApplicationContext(), EditOrdreParolesActivity.class);
                editOrdreIntent.putExtra("idChant", monChant.getId());
                startActivity(editOrdreIntent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void initialize() {
        // initialise database
        database = DbSession.getDaoSession(this);
        // bind layout with variable
        displayParolesLayout = findViewById(R.id.displayParolesLayout);
        button_open_partition = findViewById(R.id.button_open_partition);
        button_open_partition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkStoragePermissions();
            }
        });
    }

    private void checkStoragePermissions() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION);
        } else {
            Toast.makeText(this, "Permission accordée", Toast.LENGTH_SHORT).show();
            openPartition();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case STORAGE_PERMISSION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permission accordée", Toast.LENGTH_SHORT).show();
                    openPartition();
                } else {
                    Toast.makeText(this, "Permission non accordée", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }

    private void openPartition() {
        Intent intent = new Intent(getApplicationContext(), PdfReaderActivity.class);
        intent.putExtra("fullPath", Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + monChant.getCheminPartitionChant().split(":")[1]);
        intent.putExtra("idChant", monChant.getId());
        startActivity(intent);
    }

    private void loadAndDisplayParoles() {
        Intent intent = getIntent();
        monChant = database.getChantDao().load(intent.getLongExtra("idChant", 0L));
        setTitle(monChant.getLibelleChant());
        if (TextUtils.isEmpty(monChant.getCheminPartitionChant()))
            button_open_partition.setVisibility(View.INVISIBLE);

        Integer i = 0;
        for (Parole parole : monChant.getParoles()) {
            if (parole.getTypeParoleIdParole() == 2) {
                i++;
            }
            addTextView(parole.getContenuParole(), parole.getTypeParoleIdParole(), i);
        }
    }

    private void addTextView(String content, Long typeParole, @Nullable Integer numeroCouplet) {
        TextView textView = new TextView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, AbsListView.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, 10);
        textView.setLayoutParams(params);
        textView.setTextSize(16);
        if (typeParole == 1) { // refrain
            textView.setTypeface(null, Typeface.BOLD_ITALIC);
            textView.setText("Refrain :\r\n" + content);
        } else if (typeParole == 2) { // couplet
            textView.setText(numeroCouplet + ". " + content);
        } else if (typeParole == 3) { // coda
            textView.setText("Coda :\r\n" + content);
            textView.setTypeface(null, Typeface.ITALIC);
        }

        displayParolesLayout.addView(textView);
    }
}
