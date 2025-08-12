package com.opensourcebible.app.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import android.util.Xml
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.zip.ZipInputStream
import java.io.File

object VplImporter {
    private val osisOrder = listOf(
        "GEN","EXO","LEV","NUM","DEU","JOS","JDG","RUT","1SA","2SA","1KI","2KI","1CH","2CH","EZR","NEH","EST","JOB","PSA","PRO","ECC","SNG","ISA","JER","LAM","EZK","DAN","HOS","JOL","AMO","OBA","JON","MIC","NAM","HAB","ZEP","HAG","ZEC","MAL",
        "MAT","MRK","LUK","JHN","ACT","ROM","1CO","2CO","GAL","EPH","PHP","COL","1TH","2TH","1TI","2TI","TIT","PHM","HEB","JAS","1PE","2PE","1JN","2JN","3JN","JUD","REV"
    )

    fun importFromAssets(context: Context, assetDir: String = "kor_vpl", db: SQLiteDatabase) {
        db.beginTransaction()
        try {
            // 기본 books 채우기(이름은 OSIS와 동일로 우선)
            osisOrder.forEachIndexed { index, osis ->
                db.execSQL(
                    "INSERT OR IGNORE INTO books(id, osis, name_local, order_index, chapters) VALUES(?,?,?,?,?)",
                    arrayOf(index + 1, osis, osis, index + 1, 150) // chapters는 추후 실제 카운트로 업데이트
                )
            }

            var verseOrdinal = 0
            val assetManager = context.assets
            var processedAny = false

            fun parseBuffered(reader: BufferedReader) {
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    val row = line!!.trim()
                    if (row.isEmpty()) continue
                    // 기대 형태: "GEN 1:1\t본문" 또는 탭 대신 공백 여러 개
                    val splitIdx = row.indexOf('\t').let { if (it >= 0) it else row.indexOf("  ") }
                    if (splitIdx <= 0) continue
                    val ref = row.substring(0, splitIdx).trim()
                    val text = row.substring(splitIdx + 1).trim()

                    val space = ref.indexOf(' ')
                    if (space <= 0) continue
                    val osis = ref.substring(0, space)
                    val cv = ref.substring(space + 1)
                    val colon = cv.indexOf(':')
                    if (colon <= 0) continue
                    val chapter = cv.substring(0, colon).toIntOrNull() ?: continue
                    val verse = cv.substring(colon + 1).toIntOrNull() ?: continue

                    val bookId = osisOrder.indexOf(osis) + 1
                    if (bookId <= 0) continue

                    verseOrdinal += 1
                    db.execSQL(
                        "INSERT INTO verses(book_id, chapter, verse, verse_ordinal, text) VALUES(?,?,?,?,?)",
                        arrayOf(bookId, chapter, verse, verseOrdinal, text)
                    )
                    db.execSQL(
                        "INSERT INTO verses_fts(rowid, text) VALUES(last_insert_rowid(), ?)",
                        arrayOf(text)
                    )
                }
            }

            fun parseXml(input: java.io.InputStream) {
                val parser = Xml.newPullParser()
                parser.setInput(input, "UTF-8")
                var event = parser.eventType
                while (event != org.xmlpull.v1.XmlPullParser.END_DOCUMENT) {
                    if (event == org.xmlpull.v1.XmlPullParser.START_TAG && parser.name == "v") {
                        val osis = parser.getAttributeValue(null, "b") ?: ""
                        val chapter = parser.getAttributeValue(null, "c")?.toIntOrNull()
                        val verse = parser.getAttributeValue(null, "v")?.toIntOrNull()
                        val text = parser.nextText() ?: ""

                        if (osis.isNotEmpty() && chapter != null && verse != null) {
                            val bookId = osisOrder.indexOf(osis) + 1
                            if (bookId > 0) {
                                verseOrdinal += 1
                                db.execSQL(
                                    "INSERT INTO verses(book_id, chapter, verse, verse_ordinal, text) VALUES(?,?,?,?,?)",
                                    arrayOf(bookId, chapter, verse, verseOrdinal, text)
                                )
                                db.execSQL(
                                    "INSERT INTO verses_fts(rowid, text) VALUES(last_insert_rowid(), ?)",
                                    arrayOf(text)
                                )
                            }
                        }
                    }
                    event = parser.next()
                }
            }

            // 1) assetDir 내 파일(.txt/.vpl) 처리 + .zip 있으면 풀어서 처리
            val files = assetManager.list(assetDir)?.sorted() ?: emptyList()
            if (files.isNotEmpty()) {
                for (file in files) {
                    val path = "$assetDir/$file"
                    when {
                        file.endsWith(".zip", ignoreCase = true) -> {
                            val tempDir = File(context.cacheDir, "vpl_zip_${System.currentTimeMillis()}")
                            tempDir.mkdirs()
                            assetManager.open(path).use { zipIn ->
                                ZipInputStream(zipIn).use { zis ->
                                    var entry = zis.nextEntry
                                    while (entry != null) {
                                        if (!entry.isDirectory) {
                                            when {
                                                entry.name.endsWith(".txt", true) || entry.name.endsWith(".vpl", true) -> {
                                                    BufferedReader(InputStreamReader(zis, Charsets.UTF_8)).use { reader ->
                                                        parseBuffered(reader)
                                                    }
                                                }
                                                entry.name.endsWith(".xml", true) -> {
                                                    // Do not close zis here; parser reads the entry stream
                                                    parseXml(zis)
                                                }
                                            }
                                        }
                                        entry = zis.nextEntry
                                    }
                                }
                            }
                            processedAny = true
                        }
                        file.endsWith(".txt", true) || file.endsWith(".vpl", true) -> {
                            BufferedReader(InputStreamReader(assetManager.open(path), Charsets.UTF_8)).use { reader ->
                                parseBuffered(reader)
                            }
                            processedAny = true
                        }
                        file.endsWith(".xml", true) -> {
                            assetManager.open(path).use { input ->
                                parseXml(input)
                            }
                            processedAny = true
                        }
                    }
                }
            }

            // 2) 루트에 kor_vpl.zip 이 있는 경우 처리 (보수적 호환)
            if (!processedAny) {
                runCatching {
                    assetManager.open("kor_vpl.zip").use { zipIn ->
                        ZipInputStream(zipIn).use { zis ->
                            var entry = zis.nextEntry
                            while (entry != null) {
                                if (!entry.isDirectory) {
                                    when {
                                        entry.name.endsWith(".txt", true) || entry.name.endsWith(".vpl", true) -> {
                                            BufferedReader(InputStreamReader(zis, Charsets.UTF_8)).use { reader ->
                                                parseBuffered(reader)
                                            }
                                        }
                                        entry.name.endsWith(".xml", true) -> {
                                            parseXml(zis)
                                        }
                                    }
                                }
                                entry = zis.nextEntry
                            }
                        }
                    }
                    processedAny = true
                }.onFailure { /* ignore */ }
            }

            // 실제 chapters 값 갱신
            for (i in osisOrder.indices) {
                val bookId = i + 1
                val c = db.rawQuery("SELECT MAX(chapter) FROM verses WHERE book_id=?", arrayOf(bookId.toString()))
                val maxChap = if (c.moveToFirst()) c.getInt(0) else 0
                c.close()
                if (maxChap > 0) {
                    db.execSQL("UPDATE books SET chapters=? WHERE id=?", arrayOf(maxChap, bookId))
                }
            }

            db.execSQL("INSERT OR REPLACE INTO meta(key,value) VALUES('source','eBible kor_vpl'), ('lang','kor'), ('v11n','KJV-ish'), ('license','Public Domain')")
            db.setTransactionSuccessful()
        } catch (e: Exception) {
            Log.e("VplImporter", "Import failed", e)
            throw e
        } finally {
            db.endTransaction()
        }
    }
}


