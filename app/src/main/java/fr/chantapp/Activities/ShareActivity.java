package fr.chantapp.Activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import fr.chantapp.R;
import fr.chantapp.db.Chant;
import fr.chantapp.db.ChantJson;
import fr.chantapp.db.DaoSession;
import fr.chantapp.db.DbSession;
import fr.chantapp.db.Parole;

public class ShareActivity extends AppCompatActivity {

    private DaoSession database;
    private File exportJsonFile;
    public static final int STORAGE_PERMISSION = 200;
    private Button exportDataButton;
    private Button importDataButton;
    private boolean isExport;
    private ProgressBar progressBar;
    private TextView infoTV;
    private static final int PICK_FILE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);

        initialize();
    }

    private void initialize(){
        database = DbSession.getDaoSession(this);
        BindElements();
    }

    private void BindElements(){
        exportDataButton = findViewById(R.id.exportDataButton);
        exportDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isExport = true;
                checkStoragePermissions();
            }
        });
        importDataButton = findViewById(R.id.importDataButton);
        importDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isExport = false;
                checkStoragePermissions();
            }
        });
        progressBar = findViewById(R.id.progressBar);
        progressBar.setMax(100);
        progressBar.setProgress(0);
        infoTV = findViewById(R.id.infoTV);
        infoTV.setText("");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)   {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        setTitle("Sauvegarde & Partage");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        searchItem.setVisible(false);
        MenuItem addCoupletItem = menu.findItem(R.id.action_add_couplet);
        addCoupletItem.setVisible(false);
        MenuItem editChant = menu.findItem(R.id.action_edit_chant);
        editChant.setVisible(false);
        MenuItem editOrdreParolesChant = menu.findItem(R.id.action_edit_ordre_paroles);
        editOrdreParolesChant.setVisible(false);
        MenuItem deleteChant = menu.findItem(R.id.action_delete_chant);
        deleteChant.setVisible(false);
        MenuItem share = menu.findItem(R.id.action_share);
        share.setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent homeIntent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(homeIntent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void importData(){
        infoTV.setText("Importation des données");

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/octet-stream");
        intent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        try {
            startActivityForResult(Intent.createChooser(intent, "Choisir un fichier de sauvegarde"), PICK_FILE_REQUEST);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "Aucun gestionnaire de fichier trouvé", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_FILE_REQUEST && resultCode == RESULT_OK &&
                data != null && data.getData() != null) {
            Uri uri = data.getData();

            try{
                progressBar.setProgress(0);
                StringBuffer output = new StringBuffer();
                FileReader fileReader = new FileReader(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + uri.getPath().split(":")[1]);
                BufferedReader bufferedReader = new BufferedReader(fileReader);
                String line = "";

                progressBar.setProgress(10);
                while((line = bufferedReader.readLine()) != null){
                    output.append(line + "\n");
                }
                progressBar.setProgress(30);

                Gson gson = new Gson();

                Type chantJsonType = new TypeToken<ArrayList<ChantJson>>(){}.getType();
                Type paroleType = new TypeToken<ArrayList<Parole>>(){}.getType();

                ArrayList<ChantJson> chantList = gson.fromJson(output.toString(), chantJsonType);
                ArrayList<Parole> paroleList = gson.fromJson(output.toString(), paroleType);
                progressBar.setProgress(60);


                for (ChantJson chantJson : chantList) {
                    Chant monChant = new Chant(database.getChantDao().getNewId(this), chantJson.libelleChant, chantJson.auteurChant, chantJson.compositeurChant, null);
                    Log.e("idChant ---", ((Long)monChant.getId()).toString());
                    monChant.__setDaoSession(database);
                    int i = 0;
                    for (Parole parole : chantJson.paroles) {
                        parole.__setDaoSession(database);
                        parole.setId(database.getParoleDao().getNewId(this) + i);
                        parole.setChantIdParole(monChant.getId());
                        Log.e("id", ((Long)parole.getId()).toString());
                        Log.e("contenu parole", parole.getContenuParole());
                        Log.e("idChant", ((Long)parole.getChantIdParole()).toString());
                        Log.e("ordre Affichage", ((Long)parole.getOrdreAffichageParole()).toString());
                        Log.e("type parole", ((Long)parole.getTypeParoleIdParole()).toString());
                        i++;
                    }
                    progressBar.setProgress(80);

                    database.getParoleDao().insertInTx(chantJson.paroles);
                    database.getChantDao().insert(monChant);
                }

                progressBar.setProgress(100);

                Toast.makeText(this, "Importation terminée", Toast.LENGTH_SHORT).show();
            } catch(Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void exportData(){
        infoTV.setText("Exportation des données");
        progressBar.setProgress(0);
        progressBar.setProgress(10);
        exportJsonFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "documents/backup.json");
        if(exportJsonFile.exists()){
            exportJsonFile.delete();
        }

        progressBar.setProgress(20);
        try{
            if(exportJsonFile.createNewFile()){
                FileWriter fileWriter = new FileWriter(exportJsonFile.getAbsoluteFile());
                BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

                progressBar.setProgress(50);
                List<ChantJson> chantJsonList = new ArrayList<>();
                for (Chant chant : database.getChantDao().loadAll()) {
                    chantJsonList.add(new ChantJson(chant.getId(), chant.getLibelleChant(), chant.getAuteurChant(), chant.getCompositeurChant(), chant.getParoles()));
                }
                progressBar.setProgress(80);

                Gson gson = new Gson();
                bufferedWriter.write(gson.toJson(chantJsonList));
                bufferedWriter.close();

                progressBar.setProgress(100);
            }

            Toast.makeText(this, "Exportation terminée", Toast.LENGTH_SHORT).show();

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Le fichier de sauvegarde se trouve dans vos documents: \r\n" + exportJsonFile.getAbsolutePath());
            builder.setCancelable(true);
            builder.setPositiveButton(
                    "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }

    private void checkStoragePermissions() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION);
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION);
        } else {
            Toast.makeText(this, "Permission accordée", Toast.LENGTH_SHORT).show();
            if(isExport)
                exportData();
            else
                importData();
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
                    if(isExport)
                        exportData();
                    else
                        importData();
                } else {
                    Toast.makeText(this, "Permission non accordée", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }
}
