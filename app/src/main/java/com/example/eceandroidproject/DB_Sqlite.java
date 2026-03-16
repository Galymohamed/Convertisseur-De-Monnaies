/**
 * Convertisseur De Monnaies — Projet Android ECE
 * ------------------------------------------------
 * Auteur  : Mohamed GALY
 * Cours   : Développement Mobile Android — ECE Paris
 * Licence : MIT
 *
 * Gestionnaire de la base de données SQLite locale.
 * Stocke les taux de change pour permettre un fonctionnement hors ligne.
 */
package com.example.eceandroidproject;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

public class DB_Sqlite extends SQLiteOpenHelper {

    /** Version du schéma de la base de données. */
    public static final int DB_VERSION = 1;
    /** Nom du fichier de base de données sur l'appareil. */
    public static final String DB_NAME = "CBdata.db";

    // Requête de création de la table des taux de change
    private static final String SQL_CREATE =
            "CREATE TABLE IF NOT EXISTS " + RateManager.RateEntry.TABLE_NAME + " (" +
                    RateManager.RateEntry._ID + " INTEGER PRIMARY KEY," +
                    RateManager.RateEntry.COLUMN_NAME + " TEXT," +
                    RateManager.RateEntry.COLUMN_RATE + " REAL)";

    // Requête de suppression de la table (utilisée lors des mises à jour de schéma)
    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + RateManager.RateEntry.TABLE_NAME;

    public DB_Sqlite(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    /** Appelé lors de la création initiale de la base de données. */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE);
    }

    /**
     * Appelé lors d'une mise à jour de version.
     * Supprime l'ancienne table et recrée le schéma.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    /** Gère un éventuel retour à une version antérieure du schéma. */
    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    /**
     * Classe utilitaire définissant le schéma de la table des taux de change.
     * Contenu : identifiant, code devise et valeur du taux.
     */
    public static final class RateManager {

        private RateManager() {}

        /** Définit les colonnes et le nom de la table. */
        public static class RateEntry implements BaseColumns {
            /** Nom de la table SQLite. */
            public static final String TABLE_NAME = "CBDate";
            /** Colonne contenant le code ISO de la devise (ex. "USD"). */
            public static final String COLUMN_NAME = "devise";
            /** Colonne contenant la valeur du taux par rapport à l'EUR. */
            public static final String COLUMN_RATE = "rate";
        }
    }
}
