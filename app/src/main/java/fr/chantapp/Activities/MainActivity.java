package fr.chantapp.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import fr.chantapp.R;
import fr.chantapp.RecyclerViewResources.ChantAdapter;
import fr.chantapp.RecyclerViewResources.RecyclerItemClickListener;
import fr.chantapp.db.Chant;
import fr.chantapp.db.ChantJson;
import fr.chantapp.db.DaoSession;
import fr.chantapp.db.DbSession;
import fr.chantapp.db.Parole;
import fr.chantapp.db.TypeParole;

public class MainActivity extends AppCompatActivity {

    private DaoSession database;
    private RecyclerView myRecyclerView;
    private ChantAdapter myAdapter;
    private FloatingActionButton addChantButton;
    private Menu menu;
    private boolean deleteMode;
    private List<LinearLayout> listChantsSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initialize();
    }

    private void initialize(){
        initDatabase();
        initListChantInRecyclerView();
        initButtonAddChant();
        recyclerViewItemOnClickListener();
        listChantsSelected = new ArrayList<>();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)   {
        this.menu = menu;
        this.deleteMode = false;
        getMenuInflater().inflate(R.menu.main_menu, menu);
        setTitle("Choruscare");
        getSupportActionBar().setDisplayShowHomeEnabled(false);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView)searchItem.getActionView();
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                myAdapter.getFilter().filter(newText);
                return false;
            }
        });

        SearchManager searchManager = (SearchManager)getSystemService(this.SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        MenuItem addCoupletItem = menu.findItem(R.id.action_add_couplet);
        addCoupletItem.setVisible(false);
        MenuItem editChant = menu.findItem(R.id.action_edit_chant);
        editChant.setVisible(false);
        MenuItem editOrdreParolesChant = menu.findItem(R.id.action_edit_ordre_paroles);
        editOrdreParolesChant.setVisible(false);
        MenuItem deleteChant = menu.findItem(R.id.action_delete_chant);
        deleteChant.setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete_chant:
                deleteSelectedChants();
                return true;
            case android.R.id.home:
                setMenuButtonsInDeleteMode(false);
                initListChantInRecyclerView();
                listChantsSelected.clear();
                return true;
            case R.id.action_share:
                Intent intent = new Intent(getApplicationContext(), ShareActivity.class);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void deleteSelectedChants(){
        setMenuButtonsInDeleteMode(false);
        if(listChantsSelected != null && listChantsSelected.size() > 0){
            List<Chant> listChantToDelete = new ArrayList<>();
            for (LinearLayout layout : listChantsSelected) {
                TextView idChantTV = (TextView)layout.findViewById(R.id.displayParolesLayout);
                listChantToDelete.add(database.getChantDao().load(Long.parseLong(idChantTV.getText().toString())));
            }
            for (Chant chant : listChantToDelete) {
                database.getParoleDao().deleteInTx(chant.getParoles());
                database.getChantDao().delete(chant);
            }
            initListChantInRecyclerView();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Aucun chant n'a été sélectionné. Veuillez sélectionner au moins un chant pour pouvoir supprimer.");
            builder.setCancelable(true);
            /*builder.setNegativeButton(
                    "Non",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });*/
            builder.setPositiveButton(
                    "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    private void setMenuButtonsInDeleteMode(boolean deleteMode){
        this.deleteMode = deleteMode;
        getSupportActionBar().setDisplayShowHomeEnabled(deleteMode);
        getSupportActionBar().setDisplayHomeAsUpEnabled(deleteMode);
        MenuItem deleteChant = menu.findItem(R.id.action_delete_chant);
        deleteChant.setVisible(deleteMode);
        MenuItem search = menu.findItem(R.id.action_search);
        search.setVisible(!deleteMode);
    }

    private void recyclerViewItemOnClickListener(){
        myRecyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(this, myRecyclerView, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override public void onItemClick(View view, int position) {
                        if(!deleteMode){
                            final LinearLayout layout = (LinearLayout)view;
                            new CountDownTimer(1000, 1000) {
                                public void onTick(long millisUntilFinished) {
                                    layout.setBackground(new ColorDrawable(ContextCompat.getColor(getApplicationContext(), android.R.color.darker_gray)));
                                }
                                public void onFinish() {
                                    layout.setBackground(new ColorDrawable(ContextCompat.getColor(getApplicationContext(), android.R.color.transparent)));
                                }
                            }.start();
                            TextView idChantTV = layout.findViewById(R.id.displayParolesLayout);
                            Intent intent = new Intent(getApplicationContext(), DisplayParolesActivity.class);
                            intent.putExtra("idChant", Long.parseLong(idChantTV.getText().toString()));
                            startActivity(intent);
                        } else {
                            onClickForDelete((LinearLayout)view);
                        }
                    }

                    @Override public void onLongItemClick(View view, int position) {
                        onClickForDelete((LinearLayout)view);
                    }
                })
        );
    }

    private void onClickForDelete(LinearLayout layout){
        if(!listChantsSelected.contains(layout)){
            layout.setBackground(new ColorDrawable(ContextCompat.getColor(getApplicationContext(), android.R.color.darker_gray)));
            setMenuButtonsInDeleteMode(true);
            listChantsSelected.add(layout);
        } else {
            layout.setBackground(new ColorDrawable(ContextCompat.getColor(getApplicationContext(), android.R.color.transparent)));
            listChantsSelected.remove(layout);
        }
    }

    private void initDatabase(){
        database = DbSession.getDaoSession(this);
        if(database.getTypeParoleDao().loadAll().size() == 0){
            database.getTypeParoleDao().insert(new TypeParole(1, "Refrain"));
            database.getTypeParoleDao().insert(new TypeParole(2, "Couplet"));
            database.getTypeParoleDao().insert(new TypeParole(3, "Coda"));
        }
        if(database.getChantDao().loadAll().size() == 0){
            String db = "[\n" +
                    "  {\n" +
                    "    \"auteurChant\": \"Bienheureux P.Brottier\",\n" +
                    "    \"compositeurChant\": \"Frère Jean-Baptiste du Jonchay\",\n" +
                    "    \"id\": 1,\n" +
                    "    \"libelleChant\": \"Vivre comme le Christ\",\n" +
                    "    \"paroles\": [\n" +
                    "      {\n" +
                    "        \"chantIdParole\": 1,\n" +
                    "        \"contenuParole\": \"Vivre comme le Christ,\\nToujours livré à l\\u0027amour,\\nPour aller son chemin de vie dans la confiance, la force et la louange.\",\n" +
                    "        \"id\": 103,\n" +
                    "        \"ordreAffichageParole\": 0,\n" +
                    "        \"typeParoleIdParole\": 1\n" +
                    "      },\n" +
                    "      {\n" +
                    "        \"chantIdParole\": 1,\n" +
                    "        \"contenuParole\": \"Ne soyez pas ces ombres d\\u0027hommes\\nQui vont devant eux au hasard.\\nMais faites fructifier en vous, \\nLes dons que Dieu vous a donnés pour vivre.\",\n" +
                    "        \"id\": 104,\n" +
                    "        \"ordreAffichageParole\": 1,\n" +
                    "        \"typeParoleIdParole\": 2\n" +
                    "      },\n" +
                    "      {\n" +
                    "        \"chantIdParole\": 1,\n" +
                    "        \"contenuParole\": \"Pour préparer votre avenir \\nDemandez simplement à Dieu\\nLa force de bien accomplir\\nTout ce qu\\u0027il attendra de vous.\",\n" +
                    "        \"id\": 105,\n" +
                    "        \"ordreAffichageParole\": 2,\n" +
                    "        \"typeParoleIdParole\": 2\n" +
                    "      },\n" +
                    "      {\n" +
                    "        \"chantIdParole\": 1,\n" +
                    "        \"contenuParole\": \"Tant que le souffle nous tient vie,\\nIl nous faut bénir notre Dieu.\\nNous chanterons sans nous lasser\\nSon infinie miséricorde.\",\n" +
                    "        \"id\": 106,\n" +
                    "        \"ordreAffichageParole\": 3,\n" +
                    "        \"typeParoleIdParole\": 2\n" +
                    "      },\n" +
                    "      {\n" +
                    "        \"chantIdParole\": 1,\n" +
                    "        \"contenuParole\": \"Soyez  compatissants et bons\\nPour tous ceux qui souffrent et qui pleurent \\nVous savez que votre bonheur\\nEst de semer la joie de dieu pour vivre.\",\n" +
                    "        \"id\": 107,\n" +
                    "        \"ordreAffichageParole\": 4,\n" +
                    "        \"typeParoleIdParole\": 2\n" +
                    "      },\n" +
                    "      {\n" +
                    "        \"chantIdParole\": 1,\n" +
                    "        \"contenuParole\": \"Avec un coeur plein de confiance\\nRemettez à Dieu votre vie\\nAyez foi en sa providence\\nC\\u0027est son amour qui nous conduit pour vivre.\",\n" +
                    "        \"id\": 108,\n" +
                    "        \"ordreAffichageParole\": 5,\n" +
                    "        \"typeParoleIdParole\": 2\n" +
                    "      }\n" +
                    "    ]\n" +
                    "  }\n" +
                    "]";
            Gson gson = new Gson();
            Type chantJsonType = new TypeToken<ArrayList<ChantJson>>(){}.getType();
            ArrayList<ChantJson> chantList = gson.fromJson(db, chantJsonType);
            for (ChantJson chantJson : chantList) {
                Chant monChant = new Chant(database.getChantDao().getNewId(this), chantJson.libelleChant, chantJson.auteurChant, chantJson.compositeurChant, null);
                monChant.__setDaoSession(database);
                int i = 0;
                for (Parole parole : chantJson.paroles) {
                    parole.__setDaoSession(database);
                    parole.setId(database.getParoleDao().getNewId(this) + i);
                    parole.setChantIdParole(monChant.getId());
                    i++;
                }
                database.getParoleDao().insertInTx(chantJson.paroles);
                database.getChantDao().insert(monChant);
            }
        }
    }

    private void initListChantInRecyclerView(){
        myRecyclerView = findViewById(R.id.myRecyclerView);
        myRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false));
        try{
            myAdapter = new ChantAdapter(database.getChantDao().loadAll());
        } catch (Exception ex){
            Log.e("SQL ERROR ", ex.getMessage());
        }
        myRecyclerView.setAdapter(myAdapter);
    }

    private void initButtonAddChant(){
        addChantButton = findViewById(R.id.addChantButton);
        addChantButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), EditGeneralChantActivity.class);
                intent.putExtra("idChant", 0L);
                startActivity(intent);
            }
        });
    }
}
