package com.wadyjorge.receitix.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Comparator;
import java.util.Objects;

@Entity(tableName = "receitas")
public class Receita implements Cloneable {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @NonNull
    @ColumnInfo(name = "nome", index = true)
    private String nome;

    @ColumnInfo(name = "ingredientes")
    private String ingredientes;

    @ColumnInfo(name = "modo_preparo")
    private String modoPreparo;

    @ColumnInfo(name = "tempo_preparo")
    private String tempoPreparo;

    @ColumnInfo(name = "categoria")
    private int categoria;

    @ColumnInfo(name = "favorita")
    private boolean favorita;

    public static Comparator<Receita> ordenacaoCrescente = (receita1, receita2) ->
            receita1.getNome().compareToIgnoreCase(receita2.getNome());

    public static Comparator<Receita> ordenacaoDecrescente = (receita1, receita2) ->
            receita2.getNome().compareToIgnoreCase(receita1.getNome());
    
    public Receita(String nome, String ingredientes, String modoPreparo, String tempoPreparo, int categoria, boolean favorita) {
        this.nome = nome;
        this.ingredientes = ingredientes;
        this.modoPreparo = modoPreparo;
        this.tempoPreparo = tempoPreparo;
        this.categoria = categoria;
        this.favorita = favorita;
    }

    // Getters e Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getIngredientes() {
        return ingredientes;
    }

    public void setIngredientes(String ingredientes) {
        this.ingredientes = ingredientes;
    }

    public String getModoPreparo() {
        return modoPreparo;
    }

    public void setModoPreparo(String modoPreparo) {
        this.modoPreparo = modoPreparo;
    }

    public String getTempoPreparo() {
        return tempoPreparo;
    }

    public void setTempoPreparo(String tempoPreparo) {
        this.tempoPreparo = tempoPreparo;
    }

    public int getCategoria() {
        return categoria;
    }

    public void setCategoria(int categoria) {
        this.categoria = categoria;
    }

    public boolean isFavorita() {
        return favorita;
    }

    public void setFavorita(boolean favorita) {
        this.favorita = favorita;
    }

    // Sobrescrevendo o método equals para comparar objetos Receita
    @Override
    public boolean equals(Object obj) {

        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Receita receita = (Receita) obj;

        return nome.equalsIgnoreCase(receita.nome) &&
                ingredientes.equals(receita.ingredientes) &&
                modoPreparo.equals(receita.modoPreparo) &&
                tempoPreparo.equals(receita.tempoPreparo) &&
                categoria == receita.categoria &&
                favorita == receita.favorita;
    }

    // Sobrescrevendo o método hashCode para uso em coleções
    @Override
    public int hashCode() {
        return Objects.hash(
                nome.toLowerCase(),
                ingredientes,
                modoPreparo,
                tempoPreparo,
                categoria,
                favorita
        );
    }
}
