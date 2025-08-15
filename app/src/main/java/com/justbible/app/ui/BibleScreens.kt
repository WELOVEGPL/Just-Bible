package com.justbible.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.height
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TextButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Home
// swipe 제거: 롱프레스 사용으로 전환
// Use filled star only to avoid missing outlined set on older bundles
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.justbible.app.data.BibleRepository
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button

@Composable
fun BookListScreen(repo: BibleRepository, onPick: (bookId: Int, title: String) -> Unit) {
    val context = LocalContext.current
    val books = remember { repo.getBooks() }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(12.dp)) {
        items(books) { b ->
            val display = BookNameMapper.displayName(context, b.osis, null) ?: b.nameLocal
            Text(
                text = "${b.orderIndex}. ${display} (${b.osis})",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.clickable { onPick(b.id, display) }
            )
        }
    }
}

@Composable
fun ChapterListScreen(repo: BibleRepository, bookId: Int, bookTitle: String, onPick: (chapter: Int) -> Unit) {
    val chapters = remember { repo.getChapters(bookId) }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(12.dp)) {
        items((1..chapters).toList()) { ch ->
            Text(
                text = "$bookTitle ${ch}장",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.clickable { onPick(ch) }
            )
        }
    }
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
fun VerseListScreen(repo: BibleRepository, bookId: Int, chapter: Int) {
    val verses = remember { repo.getVerses(bookId, chapter) }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(12.dp)) {
        items(verses) { v ->
            val context = LocalContext.current
            val bookmarkedState = remember(v.verseOrdinal) { mutableStateOf(repo.isBookmarked(v.verseOrdinal)) }
            Column(
                modifier = Modifier.combinedClickable(
                    onClick = {},
                    onLongClick = {
                        // 토글 후 토스트 안내
                        repo.toggleBookmark(v.verseOrdinal)
                        val now = repo.isBookmarked(v.verseOrdinal)
                        bookmarkedState.value = now
                        Toast.makeText(context, if (now) "북마크 추가" else "북마크 해제", Toast.LENGTH_SHORT).show()
                    }
                )
            ) {
                RowHeader(
                    left = "$chapter:${v.verse}",
                    bookmarked = bookmarkedState.value,
                    onToggle = {}
                )
                Text(text = v.text, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

@Composable
fun ListRow(text: String, onClick: () -> Unit) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp)
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun BookListWithBackScreen(repo: BibleRepository, onBack: () -> Unit, onPick: (bookId: Int, title: String) -> Unit, onOpenBookmarks: () -> Unit = {}) {
    val context = LocalContext.current
    val books = remember { repo.getBooks() }
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("책 선택") }, navigationIcon = { TextButton(onClick = onBack) { androidx.compose.foundation.layout.Row { Icon(Icons.Filled.Home, contentDescription = null); androidx.compose.foundation.layout.Spacer(Modifier.width(4.dp)); Text("홈 화면으로") } } }, actions = {
                TextButton(onClick = onOpenBookmarks) { Text("북마크") }
            })
        }
    ) { pv ->
        LazyColumn(Modifier.fillMaxSize().padding(pv), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)) {
            items(books) { b ->
                val display = BookNameMapper.displayName(context, b.osis, null) ?: b.nameLocal
                ListRow(text = "${b.orderIndex}. ${display} (${b.osis})") { onPick(b.id, display) }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ChapterListWithBackScreen(repo: BibleRepository, bookId: Int, bookTitle: String, onBack: () -> Unit, onPick: (chapter: Int) -> Unit, onOpenBookmarks: () -> Unit = {}) {
    val chapters = remember { repo.getChapters(bookId) }
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("$bookTitle") }, navigationIcon = { TextButton(onClick = onBack) { androidx.compose.foundation.layout.Row { Icon(Icons.Filled.Home, contentDescription = null); androidx.compose.foundation.layout.Spacer(Modifier.width(4.dp)); Text("홈 화면으로") } } }, actions = {
                TextButton(onClick = onOpenBookmarks) { Text("북마크") }
            })
        }
    ) { pv ->
        LazyColumn(Modifier.fillMaxSize().padding(pv), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)) {
            items((1..chapters).toList()) { ch ->
                ListRow(text = "$bookTitle ${ch}장") { onPick(ch) }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
fun VerseListWithBackScreen(repo: BibleRepository, bookId: Int, chapter: Int, onBack: () -> Unit, onOpenBookmarks: () -> Unit = {}) {
    val verses = remember { repo.getVerses(bookId, chapter) }
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("${chapter}장") }, navigationIcon = { TextButton(onClick = onBack) { androidx.compose.foundation.layout.Row { Icon(Icons.Filled.Home, contentDescription = null); androidx.compose.foundation.layout.Spacer(Modifier.width(4.dp)); Text("홈 화면으로") } } }, actions = {
                TextButton(onClick = onOpenBookmarks) { Text("북마크") }
            })
        }
    ) { pv ->
        LazyColumn(Modifier.fillMaxSize().padding(pv), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)) {
            items(verses) { v ->
                val context = LocalContext.current
                val bookmarkedState = remember(v.verseOrdinal) { mutableStateOf(repo.isBookmarked(v.verseOrdinal)) }
                Column(
                    modifier = Modifier.combinedClickable(
                        onClick = {},
                        onLongClick = {
                            repo.toggleBookmark(v.verseOrdinal)
                            val now = repo.isBookmarked(v.verseOrdinal)
                            bookmarkedState.value = now
                            Toast.makeText(context, if (now) "북마크 추가" else "북마크 해제", Toast.LENGTH_SHORT).show()
                        }
                    )
                ) {
                    RowHeader(left = "$chapter:${v.verse}", bookmarked = bookmarkedState.value, onToggle = {})
                    Text(text = v.text, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun BookmarksScreen(repo: BibleRepository, onBack: () -> Unit, onPick: (bookId: Int, chapter: Int, title: String) -> Unit) {
    val items = remember { repo.listBookmarks() }
    // 책 이름 매핑
    val context = LocalContext.current
    val books = remember { repo.getBooks().associateBy { it.id } }
    Scaffold(
        topBar = { TopAppBar(title = { Text("북마크") }, navigationIcon = { TextButton(onClick = onBack) { androidx.compose.foundation.layout.Row { Icon(Icons.Filled.Home, contentDescription = null); androidx.compose.foundation.layout.Spacer(Modifier.width(4.dp)); Text("홈 화면으로") } } }) }
    ) { pv ->
        LazyColumn(Modifier.fillMaxSize().padding(pv), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)) {
            items(items) { it ->
                val osis = books[it.bookId]?.osis
                val fallback = books[it.bookId]?.nameLocal ?: osis ?: "책"
                val title = BookNameMapper.displayName(context, osis, null) ?: fallback
                var showConfirm by remember { mutableStateOf(false) }
                if (showConfirm) {
                    AlertDialog(
                        onDismissRequest = { showConfirm = false },
                        confirmButton = {
                            Button(onClick = {
                                repo.toggleBookmark(it.verseOrdinal)
                                showConfirm = false
                            }) { Text("예") }
                        },
                        dismissButton = {
                            Button(onClick = { showConfirm = false }) { Text("아니오") }
                        },
                        title = { Text("북마크 삭제") },
                        text = { Text("선택한 구절 북마크를 삭제할까요?") }
                    )
                }
                Text(
                    text = "$title ${it.chapter}장 ${it.verse}절 · ${it.text}",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .combinedClickable(
                            onClick = { onPick(it.bookId, it.chapter, title) },
                            onLongClick = { showConfirm = true }
                        )
                        .padding(vertical = 12.dp)
                )
            }
        }
    }
}

@Composable
private fun RowHeader(left: String, bookmarked: Boolean, onToggle: () -> Unit) {
    androidx.compose.foundation.layout.Row(Modifier.fillMaxWidth()) {
        Text(text = left, style = MaterialTheme.typography.labelMedium, modifier = Modifier.weight(1f))
        // 표시 전용: 북마크된 경우에만 아이콘 노출
        if (bookmarked) {
            Icon(Icons.Filled.Star, contentDescription = null, tint = Color(0xFFFFD54F))
        }
    }
}


