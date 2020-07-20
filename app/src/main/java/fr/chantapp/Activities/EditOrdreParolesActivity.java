package fr.chantapp.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.chantapp.R;
import fr.chantapp.db.Chant;
import fr.chantapp.db.DaoSession;
import fr.chantapp.db.DbSession;
import fr.chantapp.db.Parole;

public class EditOrdreParolesActivity extends AppCompatActivity {

    private DaoSession database;
    private Chant monChant;
    private LinearLayout layout;
    private Button saveOrdreBtn;
    private List<Spinner> spinnerList;
    private String error;
    private TextView errorOrdreParolesTV;
    private List<Parole> parolesToUpdate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_ordre_paroles);

        initialize();
        loadParoles();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.main_menu, menu);
        setTitle("Ordre des paroles");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        searchItem.setVisible(false);
        MenuItem editChant = menu.findItem(R.id.action_edit_chant);
        editChant.setVisible(false);
        MenuItem editOrdreParolesChant = menu.findItem(R.id.action_edit_ordre_paroles);
        editOrdreParolesChant.setVisible(false);
        MenuItem deleteChant = menu.findItem(R.id.action_delete_chant);
        deleteChant.setVisible(false);
        MenuItem addCouplet = menu.findItem(R.id.action_add_couplet);
        addCouplet.setVisible(false);
        MenuItem partagerBDD = menu.findItem(R.id.action_share);
        partagerBDD.setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Etes-vous sûr de vouloir quitter sans enregistrer ?");
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

    private void initialize(){
        database = DbSession.getDaoSession(this);
        layout = findViewById(R.id.layout_edit_ordre_paroles);
        saveOrdreBtn = findViewById(R.id.saveOrdreBtn);
        saveOrdreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Enregistrer();
            }
        });
        spinnerList = new ArrayList<>();
        errorOrdreParolesTV = findViewById(R.id.errorOrdreParolesTV);
        parolesToUpdate = new ArrayList<>();
        clearError();
    }

    private void loadParoles(){
        Intent intent = getIntent();
        monChant = database.getChantDao().load(intent.getLongExtra("idChant", 0L));

        Integer nombreCouplet = 0;
        List<String> listForComboBox = new ArrayList<>();
        for (Parole parole : monChant.getParoles()) {
            if(parole.getTypeParoleIdParole() == 1) { // refrain
                listForComboBox.add("Refrain");
            }
            if(parole.getTypeParoleIdParole() == 3){ // coda
                listForComboBox.add("Coda");
            }
            if(parole.getTypeParoleIdParole() == 2){ // couplet
                nombreCouplet++;
                listForComboBox.add("Couplet " + nombreCouplet.toString());
            }
        }

        addParolesInComboBox(listForComboBox);
        updateSpinnerWithData();
    }

    private void addParolesInComboBox(List<String> list){
        for (String item : list) {
            addComboBox(list);
        }
    }

    private void addComboBox(List<String> list){
        Spinner spinner = new Spinner(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0,0,0,10);
        spinner.setLayoutParams(params);
        ArrayAdapter<String> dropdownAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, list);
        dropdownAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dropdownAdapter);
        spinner.setBackgroundResource(R.drawable.spinner_background);
        spinnerList.add(spinner);
        layout.addView(spinner);
    }

    private void updateSpinnerWithData(){
        int nombreCouplet=0;
        int i = 0;
        for (Parole parole : monChant.getParoles()) {
            if(parole.getTypeParoleIdParole() == 1) { // refrain
                spinnerList.get(i).setSelection((int)parole.getOrdreAffichageParole());
            }
            if(parole.getTypeParoleIdParole() == 3){ // coda
                spinnerList.get(i).setSelection((int)parole.getOrdreAffichageParole());
            }
            if(parole.getTypeParoleIdParole() == 2){ // couplet
                nombreCouplet++;
                spinnerList.get(i).setSelection((int)parole.getOrdreAffichageParole());
            }
            i++;
        }
    }

    private List<String> getSpinnersSelectedElements(){
        List<String> selectedElements = new ArrayList<>();
        for (Spinner spinner : spinnerList) {
            selectedElements.add(spinner.getSelectedItem().toString());
        }
        return selectedElements;
    }

    private void clearError(){
        errorOrdreParolesTV.setText("");
    }
    private void addError(String error){
        errorOrdreParolesTV.setText(error);
    }

    private boolean isFormulaireValid(){
        clearError();
        parolesToUpdate.clear();
        int nombreCouplet = 0;
        boolean isformValid = true;
        for (Parole parole : monChant.getParoles()) {
            if(parole.getTypeParoleIdParole() == 1) { // refrain
                if(!getSpinnersSelectedElements().contains("Refrain")){
                    isformValid = false;
                    error = "Le refrain n'a pas été sélectionné";
                } else {
                    parole.setOrdreAffichageParole(getSpinnersSelectedElements().indexOf("Refrain"));
                    parolesToUpdate.add(parole);
                }
            }
            if(parole.getTypeParoleIdParole() == 3){ // coda
                if(!getSpinnersSelectedElements().contains("Coda")){
                    isformValid = false;
                    error = "La coda n'a pas été sélectionné";
                } else {
                    parole.setOrdreAffichageParole(getSpinnersSelectedElements().indexOf("Coda"));
                    parolesToUpdate.add(parole);
                }
            }
            if(parole.getTypeParoleIdParole() == 2){ // couplet
                nombreCouplet++;
                if(!getSpinnersSelectedElements().contains("Couplet " + Integer.toString(nombreCouplet))){
                    isformValid = false;
                    error = "Le couplet n°" + Integer.toString(nombreCouplet) + " n'a pas été sélectionné";
                } else {
                    parole.setOrdreAffichageParole(getSpinnersSelectedElements().indexOf("Couplet " + Integer.toString(nombreCouplet)));
                    parolesToUpdate.add(parole);
                }
            }
        }

        addError(error);
        return isformValid;
    }

    private void Enregistrer(){
        if(isFormulaireValid()){
            database.getParoleDao().updateInTx(parolesToUpdate);
            Intent intent = new Intent(getApplicationContext(), DisplayParolesActivity.class);
            intent.putExtra("idChant", monChant.getId());
            startActivity(intent);
        }
    }
}
