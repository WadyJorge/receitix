package com.wadyjorge.receitix.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.wadyjorge.receitix.R;
import com.wadyjorge.receitix.model.Receita;

import java.util.List;

public class ReceitaAdapter extends BaseAdapter {

    private final Context context;
    private final List<Receita> listaReceitas;
    private final String[] tipos;
    private final String favoritaSim;
    private final String favoritaNao;

    private static class ReceitaHolder {
        TextView txtValorNomeReceita;
        TextView txtValorTempoPreparo;
        TextView txtValorCategoria;
        TextView txtValorFavorita;
    }

    public ReceitaAdapter(Context context, List<Receita> listaReceitas) {
        this.context = context;
        this.listaReceitas = listaReceitas;
        this.tipos = context.getResources().getStringArray(R.array.categorias);
        this.favoritaSim = context.getString(R.string.favorita_sim);
        this.favoritaNao = context.getString(R.string.favorita_nao);
    }

    @Override
    public int getCount() {
        return listaReceitas.size();
    }

    @Override
    public Object getItem(int position) {
        return listaReceitas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ReceitaHolder holder;

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item_lista_receitas, parent, false);

            holder = new ReceitaHolder();
            holder.txtValorNomeReceita = convertView.findViewById(R.id.textViewValorNomeReceita);
            holder.txtValorTempoPreparo = convertView.findViewById(R.id.textViewValorTempoPreparo);
            holder.txtValorCategoria = convertView.findViewById(R.id.textViewValorCategoria);
            holder.txtValorFavorita = convertView.findViewById(R.id.textViewValorFavorita);  // Referência ao novo campo

            convertView.setTag(holder);
        } else {
            holder = (ReceitaHolder) convertView.getTag();
        }

        Receita receita = listaReceitas.get(position);

        if (receita != null) {
            holder.txtValorNomeReceita.setText(receita.getNome());
            holder.txtValorTempoPreparo.setText(receita.getTempoPreparo());
            holder.txtValorCategoria.setText(tipos[receita.getCategoria()]);
            holder.txtValorFavorita.setText(receita.isFavorita() ? favoritaSim : favoritaNao);  // Atualização do valor de "Favorita"
        }

        return convertView;
    }
}
