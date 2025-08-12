package com.opensourcebible.app.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DbHelper(context: Context) : SQLiteOpenHelper(
    context,
    DATABASE_NAME,
    null,
    DATABASE_VERSION
) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE books (
              id INTEGER PRIMARY KEY,
              osis TEXT UNIQUE NOT NULL,
              name_local TEXT NOT NULL,
              order_index INTEGER NOT NULL,
              chapters INTEGER NOT NULL
            );
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE verses (
              id INTEGER PRIMARY KEY,
              book_id INTEGER NOT NULL,
              chapter INTEGER NOT NULL,
              verse INTEGER NOT NULL,
              verse_ordinal INTEGER NOT NULL UNIQUE,
              text TEXT NOT NULL,
              FOREIGN KEY(book_id) REFERENCES books(id)
            );
            """.trimIndent()
        )

        db.execSQL("CREATE UNIQUE INDEX idx_ref ON verses(book_id, chapter, verse);")
        db.execSQL("CREATE INDEX idx_ordinal ON verses(verse_ordinal);")

        // FTS4 for broad device support (API21+)
        db.execSQL(
            """
            CREATE VIRTUAL TABLE verses_fts USING fts4(text);
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE meta (
              key TEXT PRIMARY KEY,
              value TEXT NOT NULL
            );
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE bookmarks (
              id INTEGER PRIMARY KEY,
              verse_ordinal INTEGER NOT NULL UNIQUE,
              created_at INTEGER NOT NULL
            );
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // For now, drop and recreate on version change
        db.execSQL("DROP TABLE IF EXISTS verses_fts")
        db.execSQL("DROP TABLE IF EXISTS verses")
        db.execSQL("DROP TABLE IF EXISTS books")
        db.execSQL("DROP TABLE IF EXISTS meta")
        db.execSQL("DROP TABLE IF EXISTS bookmarks")
        onCreate(db)
    }

    companion object {
        const val DATABASE_NAME = "bible_kor1910.db"
        const val DATABASE_VERSION = 2
    }
}


