package fr.chantapp.RecyclerViewResources;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import fr.chantapp.R;
import fr.chantapp.db.Chant;

public class ChantAdapter extends RecyclerView.Adapter<MyViewHolder> implements Filterable {

    private List<Chant> chantList;
    private List<Chant> chantListFull;

    public ChantAdapter(List<Chant> chantList) {
        this.chantList = chantList;
        chantListFull = new ArrayList<>(chantList);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.chant_item_cell, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.display(chantList.get(position));
    }

    @Override
    public int getItemCount() {
        return chantList.size();
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    private Filter filter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Chant> filteredList = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(chantListFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();

                for (Chant chant : chantListFull) {
                    if (chant.getLibelleChant().toLowerCase().contains(filterPattern) ||
                            chant.getAuteurChant().toLowerCase().contains(filterPattern) ||
                            chant.getCompositeurChant().toLowerCase().contains(filterPattern)) {
                        filteredList.add(chant);
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = filteredList;

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            chantList.clear();
            chantList.addAll((List)results.values);
            notifyDataSetChanged();
        }
    };
}
