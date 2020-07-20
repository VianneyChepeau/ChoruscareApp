package fr.chantapp.Activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

import fr.chantapp.R;
import fr.chantapp.db.Chant;
import fr.chantapp.db.DaoSession;
import fr.chantapp.db.DbSession;
import fr.chantapp.db.Parole;

public class EditGeneralChantActivity extends AppCompatActivity {

    private Chant monChant;
    private DaoSession database;

    private LinearLayout coupletContainer;
    private EditText titreChantET;
    private EditText auteurChantET;
    private EditText compositeurChantET;
    private EditText refrainChantET;
    private EditText codaChantET;
    private List<EditText> listCoupletsET;
    private TextView errorTV;
    private Button enregistrerChantBtn;
    private boolean modificationMode;
    private Button button_choose_file;
    private EditText edit_text_file_name;
    private static final int PICK_FILE_REQUEST = 1;
    public static final int STORAGE_PERMISSION = 200;
    private Uri fileUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_general_chant);

        bindElements();
        clearError();

        database = DbSession.getDaoSession(this);
        monChant = new Chant();
        monChant.__setDaoSession(database);
        Intent intent = getIntent();
        Log.e("_id ", ((Long)intent.getLongExtra("idChant", 0L)).toString());
        if (intent.getLongExtra("idChant", 0L) != 0L) {
            modificationMode = true;
            getAndFillChant(intent.getLongExtra("idChant", 0L));
        } else {
            monChant.setId(database.getChantDao().getNewId(this));
            modificationMode = false;
        }
        setClickListenerBtns();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu, menu);
        setTitle(modificationMode ? "Modifier" : "Nouveau chant");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        MenuItem searchButton = menu.findItem(R.id.action_search);
        searchButton.setVisible(false);
        MenuItem editChant = menu.findItem(R.id.action_edit_chant);
        editChant.setVisible(false);
        MenuItem editOrdreParolesChant = menu.findItem(R.id.action_edit_ordre_paroles);
        editOrdreParolesChant.setVisible(false);
        MenuItem deleteButton = menu.findItem(R.id.action_delete_chant);
        deleteButton.setVisible(modificationMode);
        MenuItem partagerBDD = menu.findItem(R.id.action_share);
        partagerBDD.setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_couplet:
                addCouplet(null);
                return true;
            case R.id.action_delete_chant:
                AlertDialog.Builder deleteBuilder = new AlertDialog.Builder(this);
                deleteBuilder.setMessage("Etes-vous sûr de vouloir supprimer ce chant ?");
                deleteBuilder.setCancelable(true);
                deleteBuilder.setNegativeButton(
                        "Non",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                deleteBuilder.setPositiveButton(
                        "Oui",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                database.getParoleDao().deleteInTx(monChant.getParoles());
                                database.getChantDao().delete(monChant);
                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                startActivity(intent);
                            }
                        });

                AlertDialog deleteAlert = deleteBuilder.create();
                deleteAlert.show();
                return true;
            case android.R.id.home:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Etes-vous sûr de vouloir quitter sans sauvegarder ?");
                builder.setCancelable(true);
                builder.setNegativeButton(
                        "Non",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                builder.setPositiveButton(
                        "Oui",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                onBackPressed();
                            }
                        });

                AlertDialog alert = builder.create();
                alert.show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void getAndFillChant(Long idChant) {
        monChant = database.getChantDao().load(idChant);
        titreChantET.setText(monChant.getLibelleChant());
        auteurChantET.setText(monChant.getAuteurChant());
        compositeurChantET.setText(monChant.getCompositeurChant());
        edit_text_file_name.setText(monChant.getCheminPartitionChant());
        for (Parole parole : monChant.getParoles()) {
            if (parole.getTypeParoleIdParole() == 1) // refrain
                refrainChantET.setText(parole.getContenuParole());
            if (parole.getTypeParoleIdParole() == 3) // coda
                codaChantET.setText(parole.getContenuParole());
            if (parole.getTypeParoleIdParole() == 2) // couplet
                addCouplet(parole.getContenuParole());
        }
    }

    private void bindElements() {
        titreChantET = findViewById(R.id.titreChantET);
        auteurChantET = findViewById(R.id.auteurChantET);
        compositeurChantET = findViewById(R.id.compositeurChantET);
        refrainChantET = findViewById(R.id.refrainChantET);
        codaChantET = findViewById(R.id.codaChantET);
        listCoupletsET = new ArrayList<>();
        errorTV = findViewById(R.id.errorTV);
        enregistrerChantBtn = findViewById(R.id.enregistrerChantBtn);
        coupletContainer = findViewById(R.id.coupletContainer);
        button_choose_file = findViewById(R.id.button_choose_file);
        edit_text_file_name = findViewById(R.id.edit_text_file_name);
        edit_text_file_name.setEnabled(false);
    }

    //region gestion des erreurs liées au formulaire
    private void clearError() {
        errorTV.setText("");
    }

    private void addError(String error) {
        if (!TextUtils.isEmpty(error)) {
            clearError();
            errorTV.setText(error);
        }
    }
    //endregion

    private void setClickListenerBtns() {
        enregistrerChantBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EnregistrerChant();
            }
        });
        button_choose_file.setOnClickListener(new View.OnClickListener() {
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
            openFileChooser();
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
                    openFileChooser();
                } else {
                    Toast.makeText(this, "Permission non accordée", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/pdf");
        intent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        try {
            startActivityForResult(Intent.createChooser(intent, "Choisir un fichier PDF"), PICK_FILE_REQUEST);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "Aucun gestionnaire de fichier trouvé", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_FILE_REQUEST && resultCode == RESULT_OK &&
                data != null && data.getData() != null) {
            fileUri = data.getData();
            edit_text_file_name.setEnabled(false);
            edit_text_file_name.setText(fileUri.getPath());
            monChant.setCheminPartitionChant(fileUri.getPath());
            Log.e("nom document ", fileUri.getPath());
        }
    }

    private void addCouplet(String contenuCouplet) {
        EditText editText = new EditText(this);
        editText.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, AbsListView.LayoutParams.WRAP_CONTENT));
        editText.setHint("Couplet " + ((Integer) (listCoupletsET.size() + 1)).toString());

        if (!TextUtils.isEmpty(contenuCouplet))
            editText.setText(contenuCouplet);

        listCoupletsET.add(editText);
        coupletContainer.addView(editText);
    }

    private boolean isFormulaireValid() {
        try {
            clearError();
            String error = "";

            if (TextUtils.isEmpty(titreChantET.getText().toString()))
                error = "Le titre du chant doit être renseigné";
            if (TextUtils.isEmpty(auteurChantET.getText().toString()))
                error = "L'auteur du chant doit être renseigné";
            if (TextUtils.isEmpty(compositeurChantET.getText().toString()))
                error = "Le compositeur du chant doit être renseigné";

            boolean isCoupletsEmpty = false;
            if (listCoupletsET.size() > 0) {
                for (EditText edt : listCoupletsET) {
                    if (TextUtils.isEmpty(edt.getText().toString()))
                        isCoupletsEmpty = true;
                }
            } else {
                isCoupletsEmpty = true;
            }

            if (TextUtils.isEmpty(refrainChantET.getText().toString()) &&
                    TextUtils.isEmpty(codaChantET.getText().toString()) &&
                    isCoupletsEmpty)
                error = "Le chant ne contient aucune parole";
            addError(error);

            return TextUtils.isEmpty(error);
        } catch (Exception ex) {
            addError("Une erreur s'est produite lors de l'enregistrement du chant. Tous les champs n'ont pas été rempli correctement: \r\n" + ex);
            return false;
        }
    }

    private void EnregistrerChant() {
        try {
            if (isFormulaireValid()) {
                if (!modificationMode)
                    monChant.setId(database.getChantDao().getNewId(this));

                Log.e("id ", ((Long)monChant.getId()).toString());

                monChant.setLibelleChant(titreChantET.getText().toString());
                monChant.setAuteurChant(auteurChantET.getText().toString());
                monChant.setCompositeurChant(compositeurChantET.getText().toString());

                List<Parole> paroles = new ArrayList<>();

                int i = 0;

                if (!TextUtils.isEmpty(refrainChantET.getText().toString())) {
                    Parole refrain = new Parole();
                    refrain.__setDaoSession(database);
                    refrain.setId(database.getParoleDao().getNewId(this) + i);
                    refrain.setTypeParoleIdParole(1);
                    refrain.setChantIdParole(monChant.getId());
                    refrain.setOrdreAffichageParole(1);
                    refrain.setContenuParole(refrainChantET.getText().toString());
                    paroles.add(refrain);
                    i++;
                }

                if (!TextUtils.isEmpty(codaChantET.getText().toString())) {
                    Parole coda = new Parole();
                    coda.__setDaoSession(database);
                    coda.setId(database.getParoleDao().getNewId(this) + i);
                    coda.setTypeParoleIdParole(3);
                    coda.setChantIdParole(monChant.getId());
                    coda.setOrdreAffichageParole(1);
                    coda.setContenuParole(codaChantET.getText().toString());
                    paroles.add(coda);
                    i++;
                }


                for (EditText edt : listCoupletsET) {
                    if (!TextUtils.isEmpty(edt.getText().toString())) {
                        Parole couplet = new Parole();
                        couplet.__setDaoSession(database);
                        couplet.setId(database.getParoleDao().getNewId(this) + i);
                        couplet.setTypeParoleIdParole(2);
                        couplet.setChantIdParole(monChant.getId());
                        couplet.setOrdreAffichageParole(1);
                        couplet.setContenuParole(edt.getText().toString());
                        paroles.add(couplet);
                        i++;
                    }
                }

                if (modificationMode) {
                    database.getParoleDao().deleteInTx(monChant.getParoles());
                    database.getChantDao().delete(monChant);
                }

                database.getParoleDao().insertInTx(paroles);
                database.getChantDao().insert(monChant);

                Intent intent = new Intent(getApplicationContext(), DisplayParolesActivity.class);
                intent.putExtra("idChant", monChant.getId());
                startActivity(intent);
            }
        } catch (Exception ex) {
            addError("Impossible d'enregistrer suite à une erreur : " + ex.getMessage());
        }
    }


}
