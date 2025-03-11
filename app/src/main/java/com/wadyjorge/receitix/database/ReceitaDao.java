package com.wadyjorge.receitix.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.wadyjorge.receitix.model.Receita;

import java.util.List;

@Dao
public interface ReceitaDao {

    @Insert
    long insert(Receita receita);

    @Update
    int update(Receita receita);

    @Delete
    int delete(Receita receita);

    @Query("SELECT * FROM receitas WHERE id = :id")
    Receita queryForId(long id);

    @Query("SELECT * FROM receitas ORDER BY nome ASC")
    List<Receita> queryAllAscending();

    @Query("SELECT * FROM receitas ORDER BY nome DESC")
    List<Receita> queryAllDownward();
}
