package com.lumoralabs.share

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lumoralabs.share.ui.screens.DeviceFileBrowser
import com.lumoralabs.share.ui.screens.TransferQueueScreen
import com.lumoralabs.share.model.ShareItem
import com.lumoralabs.share.ui.theme.LumoraGradientButton
import com.lumoralabs.share.ui.theme.LumoraGradientPanel
import com.lumoralabs.share.ui.theme.LumoraPalette
import com.lumoralabs.share.ui.theme.LumoraTheme

private enum class MainTab(val label: String) {
    TRANSFER("Transfer"),
    HISTORY("History"),
    STORAGE("Storage"),
    SETTINGS("Settings")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { LumoraShareApp() }
    }
}

@Composable
fun LumoraShareApp() {
    var selectedTab by remember { mutableStateOf(MainTab.TRANSFER) }
    var showBrowser by remember { mutableStateOf(false) }
    var showQueue by remember { mutableStateOf(false) }
    var showReceiver by remember { mutableStateOf(false) }
    var showConnect by remember { mutableStateOf(false) }
    var selectedItems by remember { mutableStateOf<List<ShareItem>>(emptyList()) }

    LumoraTheme {
        Scaffold(
            containerColor = LumoraPalette.Obsidian,
            bottomBar = {
                NavigationBar(
                    containerColor = LumoraPalette.Midnight,
                    contentColor = LumoraPalette.Platinum
                ) {
                    MainTab.values().forEach { tab ->
                        val icon = when (tab) {
                            MainTab.TRANSFER -> Icons.Default.Share
                            MainTab.HISTORY -> Icons.Default.History
                            MainTab.STORAGE -> Icons.Default.Storage
                            MainTab.SETTINGS -> Icons.Default.Settings
                        }
                        NavigationBarItem(
                            selected = selectedTab == tab,
                            onClick = {
                                selectedTab = tab
                                showBrowser = false
                                showQueue = false
                                showReceiver = false
                                showConnect = false
                            },
                            icon = {
                                Icon(icon, contentDescription = tab.label)
                            },
                            label = {
                                Text(tab.label, fontSize = 10.sp)
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.White,
                                selectedTextColor = LumoraPalette.Platinum,
                                indicatorColor = LumoraPalette.Blue.copy(alpha = 0.22f),
                                unselectedIconColor = LumoraPalette.Silver,
                                unselectedTextColor = LumoraPalette.Silver
                            )
                        )
                    }
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(LumoraPalette.Obsidian)
                    .padding(padding)
            ) {
                AppHeader()
                when (selectedTab) {
                    MainTab.TRANSFER -> {
                        when {
                            showBrowser -> {
                                RouteHeader(
                                    title = "Choose files",
                                    onBack = { showBrowser = false }
                                )
                                DeviceFileBrowser(
                                    selectedItems = selectedItems,
                                    onSelectionChanged = { selectedItems = it },
                                    onReviewQueue = {
                                        showBrowser = false
                                        showQueue = true
                                    }
                                )
                            }
                            showQueue -> {
                                RouteHeader(
                                    title = "Review batch",
                                    onBack = { showQueue = false; showBrowser = true }
                                )
                                TransferQueueScreen(
                                    items = selectedItems,
                                    onItemsChanged = { selectedItems = it },
                                    onBack = { showQueue = false; showBrowser = true },
                                    onContinue = {
                                        showQueue = false
                                        showConnect = true
                                    }
                                )
                            }
                            showConnect -> {
                                RouteHeader(
                                    title = "Connect a device",
                                    onBack = { showConnect = false; showQueue = true }
                                )
                                ConnectScreen()
                            }
                            showReceiver -> {
                                RouteHeader(
                                    title = "Receive files",
                                    onBack = { showReceiver = false }
                                )
                                ReceiveScreen()
                            }
                            else -> TransferHome(
                                onSend = { showBrowser = true },
                                onReceive = { showReceiver = true }
                            )
                        }
                    }
                    MainTab.HISTORY -> HistoryScreen()
                    MainTab.STORAGE -> StorageScreen()
                    MainTab.SETTINGS -> SettingsScreen()
                }
            }
        }
    }
}

@Composable
private fun AppHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp, vertical = 17.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(43.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(LumoraPalette.DeepBlue),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = "Lumora Share",
                tint = LumoraPalette.Platinum,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(
                "LUMORA SHARE",
                color = LumoraPalette.Platinum,
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.5.sp
            )
            Text(
                "Move without limits.",
                color = LumoraPalette.Silver,
                fontSize = 11.sp
            )
        }
        Spacer(Modifier.weight(1f))
        Surface(
            color = LumoraPalette.Surface,
            shape = CircleShape
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(LumoraPalette.Success)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    "Offline",
                    color = LumoraPalette.Silver,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun RouteHeader(title: String, onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextButton(onClick = onBack) {
            Icon(
                Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = LumoraPalette.Platinum
            )
            Spacer(Modifier.width(5.dp))
            Text("Back", color = LumoraPalette.Platinum)
        }
        Spacer(Modifier.width(8.dp))
        Text(
            title,
            color = LumoraPalette.Platinum,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp
        )
    }
}

@Composable
private fun TransferHome(
    onSend: () -> Unit,
    onReceive: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = 22.dp)
    ) {
        Spacer(Modifier.height(8.dp))
        Text(
            "YOUR FILES,",
            color = LumoraPalette.Silver,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp
        )
        Text(
            "in motion.",
            color = LumoraPalette.Platinum,
            fontSize = 31.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.7).sp
        )
        Spacer(Modifier.height(7.dp))
        Text(
            "A more effortless way to move what matters.",
            color = LumoraPalette.Silver,
            fontSize = 13.sp
        )
        Spacer(Modifier.height(22.dp))

        LumoraGradientPanel(
            modifier = Modifier
                .fillMaxWidth()
                .height(205.dp),
            shape = RoundedCornerShape(28.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(21.dp)
            ) {
                Column(
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Surface(
                        color = Color.White.copy(alpha = 0.16f),
                        shape = RoundedCornerShape(50.dp)
                    ) {
                        Text(
                            "LUMORA CONNECT",
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.4.sp,
                            modifier = Modifier.padding(
                                horizontal = 10.dp,
                                vertical = 6.dp
                            )
                        )
                    }
                    Spacer(Modifier.height(13.dp))
                    Text(
                        "Your devices.\nOne seamless flow.",
                        color = Color.White,
                        fontSize = 22.sp,
                        lineHeight = 27.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Send and receive with confidence.",
                        color = Color.White.copy(alpha = 0.88f),
                        fontSize = 11.sp
                    )
                }
                Icon(
                    imageVector = Icons.Default.Wifi,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.20f),
                    modifier = Modifier
                        .size(92.dp)
                        .align(Alignment.BottomEnd)
                )
            }
        }

        Spacer(Modifier.height(20.dp))
        Text(
            "GET STARTED",
            color = LumoraPalette.Silver,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.8.sp
        )
        Spacer(Modifier.height(11.dp))

        LumoraGradientButton(
            text = "  Select files to send",
            onClick = onSend,
            modifier = Modifier.height(54.dp)
        )
        Spacer(Modifier.height(11.dp))
        SecondaryAction(
            title = "Receive files",
            subtitle = "Prepare this device to receive a transfer",
            icon = Icons.Default.Wifi,
            onClick = onReceive
        )

        Spacer(Modifier.height(23.dp))
        Text(
            "BROWSE BY TYPE",
            color = LumoraPalette.Silver,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.8.sp
        )
        Spacer(Modifier.height(11.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            CategoryCard(
                title = "Apps",
                subtitle = "Installed apps",
                icon = Icons.Default.Apps,
                modifier = Modifier.weight(1f)
            )
            CategoryCard(
                title = "Photos",
                subtitle = "Your gallery",
                icon = Icons.Default.Image,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            CategoryCard(
                title = "Videos",
                subtitle = "Clips and movies",
                icon = Icons.Default.VideoLibrary,
                modifier = Modifier.weight(1f)
            )
            CategoryCard(
                title = "Music",
                subtitle = "Audio library",
                icon = Icons.Default.MusicNote,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            CategoryCard(
                title = "Documents",
                subtitle = "PDFs and more",
                icon = Icons.Default.Description,
                modifier = Modifier.weight(1f)
            )
            CategoryCard(
                title = "Other files",
                subtitle = "Browse folders",
                icon = Icons.Default.Folder,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(23.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.Shield,
                contentDescription = null,
                tint = LumoraPalette.Success,
                modifier = Modifier.size(17.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                "Transfers are designed to happen directly between nearby devices.",
                color = LumoraPalette.Silver,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
private fun SecondaryAction(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(19.dp),
        colors = CardDefaults.cardColors(
            containerColor = LumoraPalette.Surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(43.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(LumoraPalette.DeepBlue),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = LumoraPalette.Platinum)
            }
            Spacer(Modifier.width(13.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    color = LumoraPalette.Platinum,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    subtitle,
                    color = LumoraPalette.Silver,
                    fontSize = 10.sp
                )
            }
            Icon(
                Icons.Default.ArrowForward,
                contentDescription = null,
                tint = LumoraPalette.Pink
            )
        }
    }
}

@Composable
private fun CategoryCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = LumoraPalette.Surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(LumoraPalette.DeepBlue),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = LumoraPalette.Pink,
                    modifier = Modifier.size(19.dp)
                )
            }
            Spacer(Modifier.height(11.dp))
            Text(
                title,
                color = LumoraPalette.Platinum,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp
            )
            Text(
                subtitle,
                color = LumoraPalette.Silver,
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ReceiveScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 22.dp)
            .padding(top = 22.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(35.dp))
        Box(
            modifier = Modifier
                .size(112.dp)
                .clip(CircleShape)
                .background(LumoraPalette.DeepBlue),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Wifi,
                contentDescription = null,
                tint = LumoraPalette.Pink,
                modifier = Modifier.size(48.dp)
            )
        }
        Spacer(Modifier.height(22.dp))
        Text(
            "Ready when you are.",
            color = LumoraPalette.Platinum,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(9.dp))
        Text(
            "Nearby discovery and secure pairing are being built as part of the transfer engine milestone.",
            color = LumoraPalette.Silver,
            fontSize = 13.sp,
            lineHeight = 20.sp
        )
    }
}

@Composable
private fun HistoryScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp)
            .padding(top = 20.dp)
    ) {
        ScreenHeading("Your activity", "Every transfer, kept in one place.")
        Spacer(Modifier.height(25.dp))
        EmptyState(
            icon = Icons.Default.History,
            title = "Your story starts here",
            description = "Completed and interrupted transfers will appear here once the transfer engine is connected."
        )
    }
}

@Composable
private fun StorageScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp)
            .padding(top = 20.dp)
    ) {
        ScreenHeading("Storage intelligence", "A clearer picture of the files on your device.")
        Spacer(Modifier.height(22.dp))
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = LumoraPalette.Surface)
        ) {
            Column(Modifier.padding(18.dp)) {
                Text(
                    "STORAGE OVERVIEW",
                    color = LumoraPalette.Silver,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    "Your storage insights are coming.",
                    color = LumoraPalette.Platinum,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(7.dp))
                Text(
                    "We'll add accurate storage usage, large-file discovery and duplicate review after the file-indexing foundation is ready.",
                    color = LumoraPalette.Silver,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
private fun SettingsScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp)
            .padding(top = 20.dp)
    ) {
        ScreenHeading("Make it yours", "The details behind your Lumora experience.")
        Spacer(Modifier.height(20.dp))
        SettingCard("Application", "Lumora Share", "Version 0.1.0")
        SettingCard("Experience", "Sound, haptics and motion", "Personalisation controls will be added with the interaction system.")
        SettingCard("Privacy", "Your files stay yours", "Incoming transfers will require your approval.")
        SettingCard("About", "Built by Lumora Labs", "Move without limits.")
    }
}

@Composable
private fun ScreenHeading(title: String, subtitle: String) {
    Text(
        title,
        color = LumoraPalette.Platinum,
        fontSize = 27.sp,
        fontWeight = FontWeight.Bold
    )
    Spacer(Modifier.height(7.dp))
    Text(
        subtitle,
        color = LumoraPalette.Silver,
        fontSize = 13.sp
    )
}

@Composable
private fun SettingCard(title: String, value: String, description: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 11.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = LumoraPalette.Surface)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                title.uppercase(),
                color = LumoraPalette.Silver,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.4.sp
            )
            Spacer(Modifier.height(7.dp))
            Text(
                value,
                color = LumoraPalette.Platinum,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                description,
                color = LumoraPalette.Silver,
                fontSize = 11.sp,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
private fun EmptyState(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(82.dp)
                .clip(CircleShape)
                .background(LumoraPalette.DeepBlue),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = LumoraPalette.Pink,
                modifier = Modifier.size(35.dp)
            )
        }
        Spacer(Modifier.height(17.dp))
        Text(
            title,
            color = LumoraPalette.Platinum,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(7.dp))
        Text(
            description,
            color = LumoraPalette.Silver,
            fontSize = 12.sp,
            lineHeight = 18.sp
        )
    }
}

@Composable
private fun SelectedFilesFooter(count: Int, onClear: () -> Unit) {
    Surface(
        color = LumoraPalette.SurfaceElevated,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = LumoraPalette.Success,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                "$count document selection(s) added",
                color = LumoraPalette.Platinum,
                fontSize = 11.sp,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = onClear) {
                Text("Clear", color = LumoraPalette.Pink, fontSize = 11.sp)
            }
        }
    }
}


@Composable
private fun ConnectScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .padding(top = 38.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(112.dp)
                .clip(CircleShape)
                .background(LumoraPalette.DeepBlue),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Wifi,
                contentDescription = null,
                tint = LumoraPalette.Pink,
                modifier = Modifier.size(48.dp)
            )
        }
        Spacer(Modifier.height(22.dp))
        Text(
            "Your queue is ready.",
            color = LumoraPalette.Platinum,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(9.dp))
        Text(
            "The next production milestone connects this reviewed batch to nearby-device discovery, pairing and the sequential transfer engine.",
            color = LumoraPalette.Silver,
            fontSize = 13.sp,
            lineHeight = 20.sp
        )
        Spacer(Modifier.height(18.dp))
        Surface(
            color = LumoraPalette.Surface,
            shape = RoundedCornerShape(15.dp)
        ) {
            Text(
                "No files have been sent yet.",
                color = LumoraPalette.Warning,
                fontSize = 11.sp,
                modifier = Modifier.padding(horizontal = 15.dp, vertical = 11.dp)
            )
        }
    }
}
