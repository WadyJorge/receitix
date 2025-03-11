package com.wadyjorge.receitix.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.wadyjorge.receitix.R;
import com.wadyjorge.receitix.model.Receita;

@Database(entities = {Receita.class}, version = 1)
public abstract class ReceitasDatabase extends RoomDatabase {

    public abstract ReceitaDao receitaDao();

    private static volatile ReceitasDatabase INSTANCE;

    public static ReceitasDatabase getInstance(final Context context) {

        if (INSTANCE == null) {
            synchronized (ReceitasDatabase.class) {

                if (INSTANCE == null) {
                    try {
                        INSTANCE = Room.databaseBuilder(
                                        context.getApplicationContext(),
                                        ReceitasDatabase.class,
                                        "receitas.db"
                                )
                                .fallbackToDestructiveMigration()
                                .build();
                    } catch (Exception e) {
                        throw new RuntimeException(context.getString(R.string.erro_criar_banco_de_dados), e);
                    }
                }
            }
        }
        return INSTANCE;
    }
}
