package com.opensourcebible.app.util

import java.io.File
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

object ZipHelper {
    fun unzipFromInputStream(input: InputStream, targetDir: File) {
        ZipInputStream(input).use { zis ->
            var entry: ZipEntry? = zis.nextEntry
            while (entry != null) {
                val safeFile = resolveZipEntry(targetDir, entry)
                if (entry.isDirectory) {
                    safeFile.mkdirs()
                } else {
                    safeFile.parentFile?.mkdirs()
                    safeFile.outputStream().use { out ->
                        zis.copyTo(out)
                    }
                }
                zis.closeEntry()
                entry = zis.nextEntry
            }
        }
    }

    private fun resolveZipEntry(targetDir: File, entry: ZipEntry): File {
        val dest = File(targetDir, entry.name)
        val canonicalTargetDir = targetDir.canonicalPath
        val canonicalDest = dest.canonicalPath
        require(canonicalDest.startsWith(canonicalTargetDir + File.separator)) { "Zip 경로 탈출 차단됨: ${entry.name}" }
        return dest
    }
}


