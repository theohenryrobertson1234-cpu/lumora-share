package com.lumoralabs.share.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lumoralabs.share.data.DeviceFileRepository
import com.lumoralabs.share.model.FileCategory
import com.lumoralabs.share.model.ShareItem
import com.lumoralabs.share.ui.theme.LumoraPalette
import com.lumoralabs.share.ui.theme.LumoraGradientButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun DeviceFileBrowser(
    selectedItems: List<ShareItem>,
    onSelectionChanged: (List<ShareItem>) -> Unit,
    onReviewQueue: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember(context) { DeviceFileRepository(context) }
    var category by remember { mutableStateOf(FileCategory.APPS) }
    var displayedItems by remember { mutableStateOf<List<ShareItem>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var hasAccess by remember { mutableStateOf(category == FileCategory.APPS) }
    var accessRefresh by remember { mutableStateOf(0) }

    fun mediaPermissions(target: FileCategory): Array<String> {
        if (Build.VERSION.SDK_INT >= 34) {
            return when (target) {
                FileCategory.PHOTOS -> arrayOf(
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
                )
                FileCategory.VIDEOS -> arrayOf(
                    Manifest.permission.READ_MEDIA_VIDEO,
                    Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
                )
                FileCategory.MUSIC -> arrayOf(Manifest.permission.READ_MEDIA_AUDIO)
                else -> emptyArray()
            }
        }
        if (Build.VERSION.SDK_INT >= 33) {
            return when (target) {
                FileCategory.PHOTOS -> arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
                FileCategory.VIDEOS -> arrayOf(Manifest.permission.READ_MEDIA_VIDEO)
                FileCategory.MUSIC -> arrayOf(Manifest.permission.READ_MEDIA_AUDIO)
                else -> emptyArray()
            }
        }
        return arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    fun accessGranted(target: FileCategory): Boolean {
        if (target == FileCategory.APPS ||
            target == FileCategory.DOCUMENTS ||
            target == FileCategory.OTHER
        ) return true
        return mediaPermissions(target).any {
            context.checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        hasAccess = accessGranted(category)
        accessRefresh++
    }

    val documentPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (uris.isNotEmpty()) {
            val picked = uris.mapNotNull { uri ->
                try {
                    val cursor = context.contentResolver.query(
                        uri,
                        arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE),
                        null,
                        null,
                        null
                    )
                    var name = uri.lastPathSegment ?: "Shared file"
                    var size = 0L
                    cursor?.use {
                        if (it.moveToFirst()) {
                            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                            val sizeIndex = it.getColumnIndex(OpenableColumns.SIZE)
                            if (nameIndex >= 0) name = it.getString(nameIndex) ?: name
                            if (sizeIndex >= 0 && !it.isNull(sizeIndex)) size = it.getLong(sizeIndex)
                        }
                    }
                    val mime = context.contentResolver.getType(uri)
                    ShareItem(
                        id = "PICKED:$uri",
                        name = name,
                        sizeBytes = size,
                        category = category,
                        uri = uri,
                        mimeType = mime
                    )
                } catch (_: Exception) {
                    null
                }
            }
            onSelectionChanged((selectedItems + picked).distinctBy { it.id })
        }
    }

    LaunchedEffect(category, accessRefresh) {
        hasAccess = accessGranted(category)
        if (category == FileCategory.DOCUMENTS || category == FileCategory.OTHER) {
            displayedItems = emptyList()
            loading = false
        } else if (!hasAccess) {
            displayedItems = emptyList()
            loading = false
        } else {
            loading = true
            displayedItems = withContext(Dispatchers.IO) {
                when (category) {
                    FileCategory.APPS -> repository.getInstalledApps()
                    FileCategory.PHOTOS -> repository.getPhotos()
                    FileCategory.VIDEOS -> repository.getVideos()
                    FileCategory.MUSIC -> repository.getMusic()
                    else -> emptyList()
                }
            }
            loading = false
        }
    }

    val selectedIds = selectedItems.map { it.id }.toSet()
    val categories = FileCategory.values()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LumoraPalette.Obsidian)
            .padding(horizontal = 18.dp)
    ) {
        Text(
            "Choose what moves with you.",
            color = LumoraPalette.Platinum,
            fontSize = 23.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(5.dp))
        Text(
            "Your selections stay with you as you browse other categories.",
            color = LumoraPalette.Silver,
            fontSize = 12.sp
        )
        Spacer(Modifier.height(16.dp))

        androidx.compose.foundation.lazy.LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 2.dp)
        ) {
            items(categories) { item ->
                val active = category == item
                Surface(
                    color = if (active) LumoraPalette.Blue.copy(alpha = 0.22f)
                    else LumoraPalette.Surface,
                    shape = RoundedCornerShape(13.dp),
                    modifier = Modifier.clickable {
                        category = item
                    }
                ) {
                    Text(
                        item.title,
                        color = if (active) LumoraPalette.Platinum else LumoraPalette.Silver,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                    )
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        when {
            category == FileCategory.DOCUMENTS || category == FileCategory.OTHER -> {
                CategoryPickerPrompt(
                    category = category,
                    onBrowse = { documentPicker.launch(arrayOf("*/*")) }
                )
            }
            !hasAccess -> {
                CategoryPermissionPrompt(
                    category = category,
                    onGrant = {
                        val permissions = mediaPermissions(category)
                        if (permissions.isNotEmpty()) permissionLauncher.launch(permissions)
                    }
                )
            }
            loading -> {
                Box(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = LumoraPalette.Blue)
                }
            }
            displayedItems.isEmpty() -> {
                EmptyCategory(category)
            }
            else -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            category.title,
                            color = LumoraPalette.Platinum,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "${displayedItems.size} items found",
                            color = LumoraPalette.Silver,
                            fontSize = 11.sp
                        )
                    }
                    TextButton(
                        onClick = {
                            val allSelected = displayedItems.all { it.id in selectedIds }
                            val next = if (allSelected) {
                                selectedItems.filterNot { old -> displayedItems.any { it.id == old.id } }
                            } else {
                                (selectedItems + displayedItems).distinctBy { it.id }
                            }
                            onSelectionChanged(next)
                        }
                    ) {
                        Text(
                            if (displayedItems.all { it.id in selectedIds }) "Clear category"
                            else "Select all",
                            color = LumoraPalette.Pink
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(displayedItems, key = { it.id }) { item ->
                        SelectableFileRow(
                            item = item,
                            selected = item.id in selectedIds,
                            onClick = {
                                val next = if (item.id in selectedIds) {
                                    selectedItems.filterNot { it.id == item.id }
                                } else {
                                    selectedItems + item
                                }
                                onSelectionChanged(next)
                            }
                        )
                    }
                }
            }
        }

        if (selectedItems.isNotEmpty()) {
            Surface(
                color = LumoraPalette.SurfaceElevated,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
            ) {
                Row(
                    modifier = Modifier.padding(start = 15.dp, end = 12.dp, top = 12.dp, bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "${selectedItems.size} selected",
                            color = LumoraPalette.Platinum,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            "Combined selection across categories",
                            color = LumoraPalette.Silver,
                            fontSize = 10.sp
                        )
                    }
                    TextButton(onClick = { onSelectionChanged(emptyList()) }) {
                        Text("Clear", color = LumoraPalette.Pink)
                    }
                    Button(
                        onClick = onReviewQueue,
                        colors = ButtonDefaults.buttonColors(containerColor = LumoraPalette.Blue),
                        shape = RoundedCornerShape(13.dp)
                    ) {
                        Text("Review")
                    }
                }
            }
        }
    }
}

@Composable
private fun SelectableFileRow(
    item: ShareItem,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        color = if (selected) LumoraPalette.DeepBlue.copy(alpha = 0.72f)
        else LumoraPalette.Surface,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 13.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(43.dp)
                    .background(LumoraPalette.DeepBlue, RoundedCornerShape(13.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (item.icon != null) {
                    Image(
                        bitmap = item.icon.asImageBitmap(),
                        contentDescription = item.name,
                        modifier = Modifier.size(34.dp)
                    )
                } else {
                    val icon = when (item.category) {
                        FileCategory.APPS -> Icons.Default.Android
                        FileCategory.PHOTOS -> Icons.Default.Image
                        FileCategory.VIDEOS -> Icons.Default.VideoLibrary
                        FileCategory.MUSIC -> Icons.Default.MusicNote
                        else -> Icons.Default.Description
                    }
                    Icon(icon, contentDescription = null, tint = LumoraPalette.Pink)
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    item.name,
                    color = LumoraPalette.Platinum,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    item.sizeLabel,
                    color = LumoraPalette.Silver,
                    fontSize = 10.sp
                )
            }
            if (selected) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = LumoraPalette.Pink,
                    modifier = Modifier.size(21.dp)
                )
            } else {
                Box(
                    Modifier
                        .size(20.dp)
                        .background(Color.Transparent, CircleShape)
                )
            }
        }
    }
}

@Composable
private fun CategoryPickerPrompt(
    category: FileCategory,
    onBrowse: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth().weight(1f),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.FolderOpen,
            contentDescription = null,
            tint = LumoraPalette.Pink,
            modifier = Modifier.size(52.dp)
        )
        Spacer(Modifier.height(14.dp))
        Text(category.title, color = LumoraPalette.Platinum, fontSize = 19.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(6.dp))
        Text(
            "Choose files using Android's secure file picker. Your choices will be added to the same transfer selection.",
            color = LumoraPalette.Silver,
            fontSize = 12.sp
        )
        Spacer(Modifier.height(18.dp))
        LumoraGradientButton("Browse ${category.title.lowercase()}", onBrowse)
    }
}

@Composable
private fun CategoryPermissionPrompt(
    category: FileCategory,
    onGrant: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth().weight(1f),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            when (category) {
                FileCategory.PHOTOS -> Icons.Default.Image
                FileCategory.VIDEOS -> Icons.Default.VideoLibrary
                else -> Icons.Default.MusicNote
            },
            contentDescription = null,
            tint = LumoraPalette.Pink,
            modifier = Modifier.size(48.dp)
        )
        Spacer(Modifier.height(14.dp))
        Text(
            "Allow access to ${category.title.lowercase()}",
            color = LumoraPalette.Platinum,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(7.dp))
        Text(
            "Lumora Share needs permission to display the media you choose to send. You remain in control of what is selected.",
            color = LumoraPalette.Silver,
            fontSize = 12.sp
        )
        Spacer(Modifier.height(18.dp))
        LumoraGradientButton("Grant access", onGrant)
    }
}

@Composable
private fun EmptyCategory(category: FileCategory) {
    Column(
        modifier = Modifier.fillMaxWidth().weight(1f),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            when (category) {
                FileCategory.PHOTOS -> Icons.Default.Image
                FileCategory.VIDEOS -> Icons.Default.VideoLibrary
                else -> Icons.Default.MusicNote
            },
            contentDescription = null,
            tint = LumoraPalette.Silver,
            modifier = Modifier.size(45.dp)
        )
        Spacer(Modifier.height(12.dp))
        Text("Nothing to show yet", color = LumoraPalette.Platinum, fontWeight = FontWeight.SemiBold)
        Text(
            "No accessible ${category.title.lowercase()} were found on this device.",
            color = LumoraPalette.Silver,
            fontSize = 11.sp
        )
    }
}
