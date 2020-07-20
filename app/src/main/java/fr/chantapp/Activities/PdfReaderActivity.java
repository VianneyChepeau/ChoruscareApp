package fr.chantapp.Activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;

import java.io.File;

import fr.chantapp.R;

public class PdfReaderActivity extends AppCompatActivity {

    private PDFView pdfViewer;
    private Long idChant;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_reader);

        pdfViewer = findViewById(R.id.pdfViewer);

        Intent intent = getIntent();
        idChant = intent.getLongExtra("idChant", 0L);

        pdfViewer.fromUri(Uri.fromFile(new File(intent.getStringExtra("fullPath"))))
                .defaultPage(1)
                .enableSwipe(true)
                .onLoad(new OnLoadCompleteListener() {
                    @Override
                    public void loadComplete(int nbPages) {

                    }
                })
                .onPageChange(new OnPageChangeListener() {
                    @Override
                    public void onPageChanged(int page, int pageCount) {

                    }
                })
                .load();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("Partition");
        MenuItem searchItem = menu.findItem(R.id.action_search);
        searchItem.setVisible(false);
        MenuItem addCoupletItem = menu.findItem(R.id.action_add_couplet);
        addCoupletItem.setVisible(false);
        MenuItem deleteChant = menu.findItem(R.id.action_delete_chant);
        deleteChant.setVisible(false);
        MenuItem editChant = menu.findItem(R.id.action_edit_chant);
        editChant.setVisible(false);
        MenuItem editOrdreParoleChant = menu.findItem(R.id.action_edit_ordre_paroles);
        editOrdreParoleChant.setVisible(false);
        MenuItem partagerBDD = menu.findItem(R.id.action_share);
        partagerBDD.setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent homeIntent = new Intent(getApplicationContext(), DisplayParolesActivity.class);
                homeIntent.putExtra("idChant", idChant);
                startActivity(homeIntent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
