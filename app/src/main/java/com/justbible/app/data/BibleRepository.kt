package com.justbible.app.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase

class BibleRepository(private val context: Context) {
    private val dbHelper: DbHelper = DbHelper(context)

    fun ensureDatabase(): SQLiteDatabase {
        val db = dbHelper.writableDatabase
        val hasVerses = db.rawQuery("SELECT count(1) FROM verses", null).use { c ->
            c.moveToFirst() && c.getLong(0) > 0L
        }
        if (!hasVerses) {
            VplImporter.importFromAssets(context, "kor_vpl", db)
        }
        return db
    }

    fun getBooks(): List<Book> {
        val db = ensureDatabase()
        val list = mutableListOf<Book>()
        db.rawQuery("SELECT id, osis, name_local, order_index, chapters FROM books ORDER BY order_index", null).use { c ->
            while (c.moveToNext()) {
                list.add(
                    Book(
                        id = c.getInt(0),
                        osis = c.getString(1),
                        nameLocal = c.getString(2),
                        orderIndex = c.getInt(3),
                        chapters = c.getInt(4)
                    )
                )
            }
        }
        return list
    }

    fun getChapters(bookId: Int): Int {
        val db = ensureDatabase()
        db.rawQuery("SELECT chapters FROM books WHERE id=?", arrayOf(bookId.toString())).use { c ->
            return if (c.moveToFirst()) c.getInt(0) else 0
        }
    }

    fun getVerses(bookId: Int, chapter: Int): List<Verse> {
        val db = ensureDatabase()
        val list = mutableListOf<Verse>()
        db.rawQuery(
            "SELECT id, verse, verse_ordinal, text FROM verses WHERE book_id=? AND chapter=? ORDER BY verse",
            arrayOf(bookId.toString(), chapter.toString())
        ).use { c ->
            while (c.moveToNext()) {
                list.add(
                    Verse(
                        id = c.getInt(0),
                        verse = c.getInt(1),
                        verseOrdinal = c.getInt(2),
                        text = c.getString(3)
                    )
                )
            }
        }
        return list
    }

    fun toggleBookmark(verseOrdinal: Int) {
        val db = ensureDatabase()
        val exists = db.rawQuery("SELECT 1 FROM bookmarks WHERE verse_ordinal=?", arrayOf(verseOrdinal.toString())).use { it.moveToFirst() }
        if (exists) {
            db.execSQL("DELETE FROM bookmarks WHERE verse_ordinal=?", arrayOf(verseOrdinal))
        } else {
            val now = System.currentTimeMillis()
            db.execSQL("INSERT OR IGNORE INTO bookmarks(verse_ordinal, created_at) VALUES(?,?)", arrayOf(verseOrdinal, now))
        }
    }

    fun isBookmarked(verseOrdinal: Int): Boolean {
        val db = ensureDatabase()
        return db.rawQuery("SELECT 1 FROM bookmarks WHERE verse_ordinal=?", arrayOf(verseOrdinal.toString())).use { it.moveToFirst() }
    }

    fun listBookmarks(): List<BookmarkItem> {
        val db = ensureDatabase()
        val list = mutableListOf<BookmarkItem>()
        db.rawQuery(
            "SELECT b.verse_ordinal, v.book_id, v.chapter, v.verse, v.text FROM bookmarks b JOIN verses v ON v.verse_ordinal=b.verse_ordinal ORDER BY b.created_at DESC",
            null
        ).use { c ->
            while (c.moveToNext()) {
                list.add(
                    BookmarkItem(
                        verseOrdinal = c.getInt(0),
                        bookId = c.getInt(1),
                        chapter = c.getInt(2),
                        verse = c.getInt(3),
                        text = c.getString(4)
                    )
                )
            }
        }
        return list
    }

    data class Book(
        val id: Int,
        val osis: String,
        val nameLocal: String,
        val orderIndex: Int,
        val chapters: Int
    )

    data class Verse(
        val id: Int,
        val verse: Int,
        val verseOrdinal: Int,
        val text: String
    )

    data class BookmarkItem(
        val verseOrdinal: Int,
        val bookId: Int,
        val chapter: Int,
        val verse: Int,
        val text: String
    )
}


