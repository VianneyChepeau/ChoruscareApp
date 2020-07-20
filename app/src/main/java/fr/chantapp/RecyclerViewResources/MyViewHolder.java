package fr.chantapp.RecyclerViewResources;

import android.annotation.SuppressLint;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import fr.chantapp.R;
import fr.chantapp.db.Chant;

public class MyViewHolder extends RecyclerView.ViewHolder {

    private TextView titreChantTV;
    private TextView auteurChantTV;
    private TextView compositeurChantTV;
    private TextView idChantTV;

    public MyViewHolder(@NonNull View itemView) {
        super(itemView);

        titreChantTV = itemView.findViewById(R.id.titreChantTV);
        auteurChantTV = itemView.findViewById(R.id.auteurChantTV);
        compositeurChantTV = itemView.findViewById(R.id.compositeurChantTV);
        idChantTV = itemView.findViewById(R.id.displayParolesLayout);
    }

    @SuppressLint("SetTextI18n")
    public void display(Chant chant){
        titreChantTV.setText(chant.getLibelleChant());
        auteurChantTV.setText(chant.getAuteurChant());
        compositeurChantTV.setText(chant.getCompositeurChant());
        idChantTV.setText(Long.toString(chant.getId()));
    }
}
