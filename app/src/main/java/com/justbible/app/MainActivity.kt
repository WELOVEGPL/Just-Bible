package com.justbible.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.justbible.app.data.BibleRepository
import com.justbible.app.ui.theme.JustBibleTheme
import com.justbible.app.util.ZipHelper
import com.justbible.app.ui.BookListWithBackScreen
import com.justbible.app.ui.ChapterListWithBackScreen
import com.justbible.app.ui.VerseListWithBackScreen
import com.justbible.app.ui.BookmarksScreen
import java.io.File
import java.io.InputStream

sealed class ViewState {
    data object Home : ViewState()
    data object Loading : ViewState()
    data class Viewer(val text: String) : ViewState()
    data class Error(val message: String) : ViewState()
    data object BibleBooks : ViewState()
    data class BibleChapters(val bookId: Int, val title: String) : ViewState()
    data class BibleVerses(val bookId: Int, val chapter: Int) : ViewState()
    data object Bookmarks : ViewState()
    data class JumpToFromBookmarks(val bookId: Int, val chapter: Int) : ViewState()
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            JustBibleTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    val viewState: MutableState<ViewState> = remember { mutableStateOf(ViewState.Home) }

                    val pickZipLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.OpenDocument()
                    ) { uri: Uri? ->
                        if (uri == null) return@rememberLauncherForActivityResult
                        viewState.value = ViewState.Loading
                        runCatching {
                            contentResolver.openInputStream(uri)?.use { inputStream ->
                                val targetDir = File(filesDir, "imports/${System.currentTimeMillis()}")
                                targetDir.mkdirs()
                                ZipHelper.unzipFromInputStream(inputStream, targetDir)
                                val firstTxt = targetDir.walkTopDown().firstOrNull { it.isFile && it.extension.equals("txt", ignoreCase = true) }
                                val text = firstTxt?.readText(Charsets.UTF_8)
                                    ?: throw IllegalStateException("ZIP 내에 .txt 파일이 없습니다")
                                viewState.value = ViewState.Viewer(text)
                            } ?: run {
                                throw IllegalStateException("파일을 열 수 없습니다")
                            }
                        }.onFailure { e ->
                            viewState.value = ViewState.Error(e.message ?: "알 수 없는 오류")
                        }
                    }

                    HomeOrViewer(
                        state = viewState.value,
                        onShowEmbedded = {
                            viewState.value = ViewState.Loading
                            runCatching {
                                assets.open("sample.txt").use(InputStream::readBytes).toString(Charsets.UTF_8)
                            }.onSuccess { text ->
                                viewState.value = ViewState.Viewer(text)
                            }.onFailure { e ->
                                viewState.value = ViewState.Error(e.message ?: "내장 파일 로드 실패")
                            }
                        },
                        onPickZip = {
                            // persistable permission은 단발성 임포트라 생략
                            pickZipLauncher.launch(arrayOf("application/zip", "application/x-zip-compressed"))
                        },
                        onOpenBookmarks = { viewState.value = ViewState.Bookmarks },
                        onJumpToFromBookmarks = { bid, ch -> viewState.value = ViewState.JumpToFromBookmarks(bid, ch) },
                        onOpenBible = { viewState.value = ViewState.BibleBooks },
                        onBack = { viewState.value = ViewState.Home }
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeOrViewer(
    state: ViewState,
    onShowEmbedded: () -> Unit,
    onPickZip: () -> Unit,
    onOpenBookmarks: () -> Unit,
    onJumpToFromBookmarks: (Int, Int) -> Unit,
    onOpenBible: () -> Unit,
    onBack: () -> Unit
) {
    when (state) {
        is ViewState.Home -> HomeScreen(onShowEmbedded = onShowEmbedded, onPickZip = onPickZip, onOpenBible = onOpenBible)
        is ViewState.Loading -> CenterText(text = "불러오는 중...")
        is ViewState.Viewer -> ViewerScreen(text = state.text, onBack = onBack)
        is ViewState.Error -> ErrorScreen(message = state.message, onBack = onBack)
        is ViewState.BibleBooks -> BibleNavRoot(onBack = onBack, openBookmarks = onOpenBookmarks)
        is ViewState.BibleChapters -> BibleNavRoot(onBack = onBack, openBookmarks = onOpenBookmarks)
        is ViewState.BibleVerses -> BibleNavRoot(onBack = onBack, openBookmarks = onOpenBookmarks)
        is ViewState.Bookmarks -> {
            val ctx = LocalContext.current
            val repo = remember(ctx) { BibleRepository(ctx) }
            BookmarksScreen(repo, onBack = onBack) { bookId, chapter, _ -> onJumpToFromBookmarks(bookId, chapter) }
        }
        is ViewState.JumpToFromBookmarks -> {
            val ctx = LocalContext.current
            val repo = remember(ctx) { BibleRepository(ctx) }
            BackHandler { onOpenBookmarks() }
            VerseListWithBackScreen(
                repo = repo,
                bookId = state.bookId,
                chapter = state.chapter,
                onBack = onOpenBookmarks,
                onOpenBookmarks = onOpenBookmarks
            )
        }
    }
}

@Composable
private fun HomeScreen(onShowEmbedded: () -> Unit, onPickZip: () -> Unit, onOpenBible: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "JustBible", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "메뉴", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(12.dp))
        Button(onClick = onOpenBible) { Text("성경 보기 (KOR1910)") }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onShowEmbedded) { Text("내장 텍스트 보기") }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onPickZip) { Text("ZIP 불러오기 (TXT)") }
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Tip: 절을 길게 누르면 북마크를 추가/해제합니다. 북마크된 절은 노란색 별로 표시되며, 상단의 북마크 메뉴에서 모아볼 수 있습니다.",
            style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun ViewerScreen(text: String, onBack: () -> Unit) {
    BackHandler { onBack() }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = onBack) { Text("뒤로") }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace)
        )
    }
}

// Legacy stub removed; navigation is handled by BibleNavRoot

@Composable
private fun BibleNavRoot(onBack: () -> Unit, openBookmarks: () -> Unit) {
    val context = LocalContext.current
    val repo = remember(context) { BibleRepository(context) }
    val bookAndTitle = remember { mutableStateOf<Pair<Int, String>?>(null) }
    val chapter = remember { mutableStateOf<Int?>(null) }

    BackHandler {
        when {
            chapter.value != null -> chapter.value = null
            bookAndTitle.value != null -> bookAndTitle.value = null
            else -> onBack()
        }
    }
    when {
        bookAndTitle.value == null -> BookListWithBackScreen(repo, onBack, onPick = { bid, title ->
            bookAndTitle.value = bid to title
        }, onOpenBookmarks = openBookmarks)
        chapter.value == null -> {
            val (bid, title) = bookAndTitle.value!!
            ChapterListWithBackScreen(repo, bid, title, onBack, onPick = { ch -> chapter.value = ch }, onOpenBookmarks = openBookmarks)
        }
        else -> {
            val (bid, _) = bookAndTitle.value!!
            VerseListWithBackScreen(repo, bid, chapter.value!!, onBack, onOpenBookmarks = openBookmarks)
        }
    }
}

// Legacy stubs removed; handled by BibleNavRoot

@Composable
private fun ErrorScreen(message: String, onBack: () -> Unit) {
    BackHandler { onBack() }
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "오류: $message")
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onBack) { Text("확인") }
    }
}

@Composable
private fun CenterText(text: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = text)
    }
}

@Preview(showBackground = true)
@Composable
private fun HomePreview() {
    JustBibleTheme { HomeScreen(onShowEmbedded = {}, onPickZip = {}, onOpenBible = {}) }
}


