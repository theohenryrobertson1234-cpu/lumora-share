package com.lumoralabs.share.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lumoralabs.share.model.FileCategory
import com.lumoralabs.share.model.ShareItem
import com.lumoralabs.share.ui.theme.LumoraPalette

@Composable
fun TransferQueueScreen(
    items: List<ShareItem>,
    onItemsChanged: (List<ShareItem>) -> Unit,
    onBack: () -> Unit,
    onContinue: () -> Unit
) {
    val totalBytes = items.sumOf { it.sizeBytes.coerceAtLeast(0L) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LumoraPalette.Obsidian)
            .padding(horizontal = 18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Your transfer queue",
                    color = LumoraPalette.Platinum,
                    fontSize = 23.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "One batch. Your chosen order.",
                    color = LumoraPalette.Silver,
                    fontSize = 12.sp
                )
            }
            TextButton(onClick = onBack) {
                Text("Back", color = LumoraPalette.Pink)
            }
        }

        Spacer(Modifier.height(14.dp))
        Card(
            shape = RoundedCornerShape(19.dp),
            colors = CardDefaults.cardColors(containerColor = LumoraPalette.Surface)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("BATCH SUMMARY", color = LumoraPalette.Silver, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.4.sp)
                    Spacer(Modifier.height(6.dp))
                    Text("${items.size} files selected", color = LumoraPalette.Platinum, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                    Text(formatBytes(totalBytes), color = LumoraPalette.Silver, fontSize = 11.sp)
                }
                Surface(color = LumoraPalette.DeepBlue, shape = RoundedCornerShape(14.dp)) {
                    Text(
                        "SEQUENTIAL",
                        color = LumoraPalette.Pink,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                    )
                }
            }
        }

        Spacer(Modifier.height(15.dp))
        Text("TRANSFER ORDER", color = LumoraPalette.Silver, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
        Spacer(Modifier.height(8.dp))

        if (items.isEmpty()) {
            Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                Text("Your queue is empty. Add files to get started.", color = LumoraPalette.Silver)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(items, key = { _, item -> item.id }) { index, item ->
                    QueueItemRow(
                        item = item,
                        index = index,
                        canMoveUp = index > 0,
                        canMoveDown = index < items.lastIndex,
                        onMoveUp = {
                            val next = items.toMutableList()
                            val moved = next.removeAt(index)
                            next.add(index - 1, moved)
                            onItemsChanged(next)
                        },
                        onMoveDown = {
                            val next = items.toMutableList()
                            val moved = next.removeAt(index)
                            next.add(index + 1, moved)
                            onItemsChanged(next)
                        },
                        onRemove = {
                            onItemsChanged(items.filterNot { it.id == item.id })
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(10.dp))
        Button(
            onClick = onContinue,
            enabled = items.isNotEmpty(),
            modifier = Modifier.fillMaxWidth().height(53.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = LumoraPalette.Blue,
                disabledContainerColor = LumoraPalette.SurfaceElevated
            )
        ) {
            Text("Continue to device connection", fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(10.dp))
    }
}

@Composable
private fun QueueItemRow(
    item: ShareItem,
    index: Int,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onRemove: () -> Unit
) {
    val icon = when (item.category) {
        FileCategory.APPS -> Icons.Default.Android
        FileCategory.PHOTOS -> Icons.Default.Image
        FileCategory.VIDEOS -> Icons.Default.VideoLibrary
        FileCategory.MUSIC -> Icons.Default.MusicNote
        else -> Icons.Default.Description
    }
    Surface(
        color = LumoraPalette.Surface,
        shape = RoundedCornerShape(15.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 11.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(color = LumoraPalette.DeepBlue, shape = RoundedCornerShape(10.dp)) {
                Text(
                    (index + 1).toString().padStart(2, '0'),
                    color = LumoraPalette.Pink,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 7.dp)
                )
            }
            Spacer(Modifier.width(10.dp))
            Icon(icon, contentDescription = null, tint = LumoraPalette.Pink, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(9.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.name, color = LumoraPalette.Platinum, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("${item.category.title} • ${item.sizeLabel}", color = LumoraPalette.Silver, fontSize = 9.sp)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButtonCompact(Icons.Default.ArrowUpward, "Move up", canMoveUp, onMoveUp)
                IconButtonCompact(Icons.Default.ArrowDownward, "Move down", canMoveDown, onMoveDown)
            }
            IconButtonCompact(Icons.Default.DeleteOutline, "Remove", true, onRemove, danger = true)
        }
    }
}

@Composable
private fun IconButtonCompact(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    description: String,
    enabled: Boolean,
    onClick: () -> Unit,
    danger: Boolean = false
) {
    androidx.compose.material3.IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.size(29.dp)
    ) {
        Icon(
            icon,
            contentDescription = description,
            tint = when {
                !enabled -> LumoraPalette.Silver.copy(alpha = 0.3f)
                danger -> LumoraPalette.Danger
                else -> LumoraPalette.Silver
            },
            modifier = Modifier.size(16.dp)
        )
    }
}

private fun formatBytes(bytes: Long): String {
    if (bytes <= 0L) return "Total size unavailable for some items"
    val kb = bytes / 1024.0
    val mb = kb / 1024.0
    val gb = mb / 1024.0
    return when {
        gb >= 1.0 -> String.format("%.2f GB", gb)
        mb >= 1.0 -> String.format("%.2f MB", mb)
        else -> String.format("%.1f KB", kb)
    }
}
