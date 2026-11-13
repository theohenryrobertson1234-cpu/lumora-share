
package com.lumoralabs.share.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val BrowserBackground = Color(0xFF090814)
private val BrowserCard = Color(0xFF171428)
private val BrowserPurple = Color(0xFF8B5CF6)
private val BrowserLightPurple = Color(0xFFC4B5FD)
private val BrowserMuted = Color(0xFFAAA4C0)

@Composable
fun DeviceFileBrowser(
    onSelectFiles: () -> Unit
) {
    val context = LocalContext.current

    var category by remember {
        mutableStateOf(FileCategory.APPS)
    }

    var apps by remember {
        mutableStateOf<List<ShareItem>>(emptyList())
    }

    var selectedIds by remember {
        mutableStateOf(setOf<String>())
    }

    var loading by remember {
        mutableStateOf(true)
    }

    LaunchedEffect(Unit) {
        apps = withContext(Dispatchers.IO) {
            DeviceFileRepository(context).getInstalledApps()
        }
        loading = false
    }

    val categories = FileCategory.entries

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BrowserBackground)
    ) {

        Text(
            "Share files",
            color = Color.White,
            fontSize = 27.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(6.dp))

        Text(
            "Choose what you want to share with another device.",
            color = BrowserMuted,
            fontSize = 13.sp
        )

        Spacer(Modifier.height(18.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEach { item ->

                val active = category == item

                Surface(
                    color = if (active) {
                        BrowserPurple
                    } else {
                        BrowserCard
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.clickable {
                        category = item
                    }
                ) {
                    Text(
                        text = item.title,
                        color = if (active) {
                            Color.White
                        } else {
                            BrowserMuted
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(
                            horizontal = 15.dp,
                            vertical = 11.dp
                        )
                    )
                }
            }
        }

        Spacer(Modifier.height(18.dp))

        if (category == FileCategory.APPS) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Installed applications",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )

                    Text(
                        "${apps.size} applications found",
                        color = BrowserMuted,
                        fontSize = 11.sp
                    )
                }

                TextButton(
                    onClick = {
                        selectedIds = if (
                            selectedIds.size == apps.size
                        ) {
                            emptySet()
                        } else {
                            apps.map { it.id }.toSet()
                        }
                    }
                ) {
                    Text(
                        if (
                            apps.isNotEmpty() &&
                            selectedIds.size == apps.size
                        ) "Clear all" else "Select all",
                        color = BrowserLightPurple
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            when {
                loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = BrowserPurple
                        )
                    }
                }

                apps.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No launchable applications found.",
                            color = BrowserMuted
                        )
                    }
                }

                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(
                            bottom = 12.dp
                        ),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(
                            items = apps,
                            key = { it.id }
                        ) { app ->

                            val selected = app.id in selectedIds

                            AppGridItem(
                                app = app,
                                selected = selected,
                                onClick = {
                                    selectedIds =
                                        if (selected) {
                                            selectedIds - app.id
                                        } else {
                                            selectedIds + app.id
                                        }
                                }
                            )
                        }
                    }
                }
            }

        } else {

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.FolderOpen,
                    contentDescription = null,
                    tint = BrowserLightPurple,
                    modifier = Modifier.size(48.dp)
                )

                Spacer(Modifier.height(14.dp))

                Text(
                    category.title,
                    color = Color.White,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(7.dp))

                Text(
                    "This category will be connected to the device file browser in the next stage.",
                    color = BrowserMuted,
                    fontSize = 12.sp
                )

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = onSelectFiles,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrowserPurple
                    )
                ) {
                    Text("Browse device files")
                }
            }
        }

        if (category == FileCategory.APPS) {
            Surface(
                color = BrowserCard,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "${selectedIds.size} selected",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )

                            Text(
                                "Applications ready for preparation",
                                color = BrowserMuted,
                                fontSize = 10.sp
                            )
                        }

                        Button(
                            enabled = selectedIds.isNotEmpty(),
                            onClick = {
                                Toast.makeText(
                                    context,
                                    "App package preparation is the next development stage.",
                                    Toast.LENGTH_LONG
                                ).show()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BrowserPurple,
                                disabledContainerColor = Color(0xFF39334B)
                            )
                        ) {
                            Text("Continue")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AppGridItem(
    app: ShareItem,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        color = if (selected) {
            Color(0xFF30234F)
        } else {
            BrowserCard
        },
        shape = RoundedCornerShape(15.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = 6.dp,
                vertical = 12.dp
            ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Box(
                modifier = Modifier.size(48.dp),
                contentAlignment = Alignment.Center
            ) {
                if (app.icon != null) {
                    Image(
                        bitmap = app.icon.asImageBitmap(),
                        contentDescription = app.name,
                        modifier = Modifier.size(42.dp)
                    )
                } else {
                    Icon(
                        Icons.Default.Android,
                        contentDescription = null,
                        tint = BrowserLightPurple,
                        modifier = Modifier.size(38.dp)
                    )
                }

                if (selected) {
                    Surface(
                        color = BrowserPurple,
                        shape = RoundedCornerShape(50.dp),
                        modifier = Modifier
                            .size(19.dp)
                            .align(Alignment.TopEnd)
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = Color.White,
                            modifier = Modifier.padding(3.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Text(
                app.name,
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(4.dp))

            Text(
                app.sizeLabel,
                color = BrowserMuted,
                fontSize = 9.sp
            )
        }
    }
}
