
package com.lumoralabs.share

import android.os.Bundle
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Background = Color(0xFF090814)
private val CardColor = Color(0xFF171428)
private val Purple = Color(0xFF8B5CF6)
private val LightPurple = Color(0xFFC4B5FD)
private val Muted = Color(0xFFAAA4C0)

private enum class ShareTab(
    val label: String
) {
    HOME("Home"),
    SEND("Send"),
    RECEIVE("Receive"),
    HISTORY("History"),
    SETTINGS("Settings")
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            LumoraShareApp()
        }
    }
}

@Composable
fun LumoraShareApp() {

    var selectedTab by remember {
        mutableStateOf(ShareTab.HOME)
    }

    var selectedFiles by remember {
        mutableStateOf<List<Uri>>(emptyList())
    }

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        selectedFiles = uris
    }

    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Purple,
            secondary = LightPurple,
            background = Background,
            surface = CardColor
        )
    ) {

        Scaffold(
            containerColor = Background,
            bottomBar = {
                NavigationBar(
                    containerColor = CardColor
                ) {

                    ShareTab.entries.forEach { tab ->

                        val icon = when (tab) {
                            ShareTab.HOME -> Icons.Default.Home
                            ShareTab.SEND -> Icons.Default.Send
                            ShareTab.RECEIVE -> Icons.Default.Download
                            ShareTab.HISTORY -> Icons.Default.History
                            ShareTab.SETTINGS -> Icons.Default.Settings
                        }

                        NavigationBarItem(
                            selected = selectedTab == tab,
                            onClick = {
                                selectedTab = tab
                            },
                            icon = {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = tab.label
                                )
                            },
                            label = {
                                Text(
                                    tab.label,
                                    fontSize = 10.sp
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = LightPurple,
                                selectedTextColor = LightPurple,
                                indicatorColor = Purple.copy(alpha = 0.2f),
                                unselectedIconColor = Muted,
                                unselectedTextColor = Muted
                            )
                        )
                    }
                }
            }
        ) { padding ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Background)
                    .padding(padding)
                    .padding(horizontal = 20.dp)
            ) {

                Header()

                when (selectedTab) {

                    ShareTab.HOME -> HomeScreen(
                        onSend = {
                            selectedTab = ShareTab.SEND
                        },
                        onReceive = {
                            selectedTab = ShareTab.RECEIVE
                        }
                    )

                    ShareTab.SEND -> SendScreen(
                        files = selectedFiles,
                        onSelectFiles = {
                            filePicker.launch(arrayOf("*/*"))
                        }
                    )

                    ShareTab.RECEIVE -> ReceiveScreen()

                    ShareTab.HISTORY -> HistoryScreen()

                    ShareTab.SETTINGS -> SettingsScreen()
                }
            }
        }
    }
}

@Composable
private fun Header() {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 22.dp, bottom = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier
                .size(44.dp)
                .background(Purple, RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Share,
                contentDescription = null,
                tint = Color.White
            )
        }

        Spacer(Modifier.width(12.dp))

        Column {
            Text(
                "LUMORA SHARE",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp
            )

            Text(
                "Share anything. Anywhere.",
                color = Muted,
                fontSize = 11.sp
            )
        }

        Spacer(Modifier.weight(1f))

        Icon(
            Icons.Default.Wifi,
            contentDescription = "Connection",
            tint = Muted
        )
    }
}

@Composable
private fun HomeScreen(
    onSend: () -> Unit,
    onReceive: () -> Unit
) {

    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        Text(
            "Welcome to",
            color = Muted,
            fontSize = 15.sp
        )

        Text(
            "Lumora Share.",
            color = Color.White,
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(10.dp))

        Text(
            "Move files between nearby devices simply and securely.",
            color = Muted,
            fontSize = 14.sp
        )

        Spacer(Modifier.height(28.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(170.dp)
                .background(
                    Color(0xFF21183E),
                    RoundedCornerShape(24.dp)
                ),
            contentAlignment = Alignment.Center
        ) {

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Icon(
                    Icons.Default.Devices,
                    contentDescription = null,
                    tint = LightPurple,
                    modifier = Modifier.size(55.dp)
                )

                Spacer(Modifier.height(12.dp))

                Text(
                    "Your devices. Connected.",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )

                Text(
                    "Fast, private, nearby sharing",
                    color = LightPurple,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        Text(
            "START SHARING",
            color = Muted,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        Spacer(Modifier.height(12.dp))

        ActionCard(
            title = "Send files",
            description = "Choose files to send to another device",
            icon = Icons.Default.Send,
            onClick = onSend
        )

        Spacer(Modifier.height(14.dp))

        ActionCard(
            title = "Receive files",
            description = "Get files from a nearby device",
            icon = Icons.Default.Download,
            onClick = onReceive
        )

        Spacer(Modifier.height(22.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Shield,
                contentDescription = null,
                tint = LightPurple,
                modifier = Modifier.size(18.dp)
            )

            Spacer(Modifier.width(8.dp))

            Text(
                "Your transfers are designed to stay private.",
                color = Muted,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun ActionCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardColor
        )
    ) {

        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        Purple.copy(alpha = 0.18f),
                        RoundedCornerShape(14.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = LightPurple
                )
            }

            Spacer(Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                Text(
                    description,
                    color = Muted,
                    fontSize = 11.sp
                )
            }

            Icon(
                Icons.Default.ArrowForward,
                contentDescription = null,
                tint = LightPurple
            )
        }
    }
}

@Composable
private fun SendScreen(
    files: List<Uri>,
    onSelectFiles: () -> Unit
) {

    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        Text(
            "Send files",
            color = Color.White,
            fontSize = 27.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(8.dp))

        Text(
            "Select the files you want to share.",
            color = Muted,
            fontSize = 14.sp
        )

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = onSelectFiles,
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp),
            shape = RoundedCornerShape(15.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Purple
            )
        ) {
            Icon(
                Icons.Default.FolderOpen,
                contentDescription = null
            )

            Spacer(Modifier.width(10.dp))

            Text("Choose files")
        }

        Spacer(Modifier.height(22.dp))

        Text(
            "SELECTED FILES (${files.size})",
            color = Muted,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(12.dp))

        if (files.isEmpty()) {

            EmptyState(
                icon = Icons.Default.Folder,
                title = "No files selected",
                description = "Your selected files will appear here."
            )

        } else {

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {

                items(files) { uri ->

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = CardColor
                        )
                    ) {

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Icon(
                                Icons.Default.InsertDriveFile,
                                contentDescription = null,
                                tint = LightPurple
                            )

                            Spacer(Modifier.width(12.dp))

                            Text(
                                uri.lastPathSegment
                                    ?.substringAfterLast('/')
                                    ?: "Selected file",
                                color = Color.White,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            Text(
                "File selection works. Device connection and transfer are coming in the next development stage.",
                color = Muted,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun ReceiveScreen() {

    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        Text(
            "Receive files",
            color = Color.White,
            fontSize = 27.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(8.dp))

        Text(
            "Get files from another nearby device.",
            color = Muted,
            fontSize = 14.sp
        )

        Spacer(Modifier.height(35.dp))

        EmptyState(
            icon = Icons.Default.Wifi,
            title = "Ready to receive",
            description = "Nearby device discovery will be activated in our next development stage."
        )
    }
}

@Composable
private fun HistoryScreen() {

    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        Text(
            "Transfer history",
            color = Color.White,
            fontSize = 27.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(25.dp))

        EmptyState(
            icon = Icons.Default.History,
            title = "Nothing here yet",
            description = "Your completed and failed transfers will appear here."
        )
    }
}

@Composable
private fun SettingsScreen() {

    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        Text(
            "Settings",
            color = Color.White,
            fontSize = 27.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(22.dp))

        SettingRow("App name", "Lumora Share")

        SettingRow("Developer", "Lumora Labs")

        SettingRow("Version", "0.1.0")

        SettingRow("Connection", "Offline-first")

        SettingRow("Privacy", "Approval required for incoming transfers")
    }
}

@Composable
private fun SettingRow(
    title: String,
    value: String
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardColor
        )
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Text(
                title,
                color = Muted,
                fontSize = 11.sp
            )

            Spacer(Modifier.height(4.dp))

            Text(
                value,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 35.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Box(
            modifier = Modifier
                .size(82.dp)
                .background(
                    Purple.copy(alpha = 0.16f),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {

            Icon(
                icon,
                contentDescription = null,
                tint = LightPurple,
                modifier = Modifier.size(38.dp)
            )
        }

        Spacer(Modifier.height(18.dp))

        Text(
            title,
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(8.dp))

        Text(
            description,
            color = Muted,
            fontSize = 12.sp
        )
    }
}
