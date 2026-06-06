package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.AppScreen
import com.example.viewmodel.MedicineWheelViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AppScaffold(viewModel: MedicineWheelViewModel) {
    val activeScreen by viewModel.activeScreen.collectAsState()
    val isSyndicated by viewModel.isSyndicated.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    
    // Core team guide index
    var selectedPersonaIdx by remember { mutableIntStateOf(0) }
    var showCouncilGuidance by remember { mutableStateOf(true) }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF131320),
                tonalElevation = 8.dp,
                windowInsets = WindowInsets.navigationBars
            ) {
                NavigationBarItem(
                    selected = activeScreen == AppScreen.Workspace,
                    onClick = { viewModel.setScreen(AppScreen.Workspace) },
                    icon = { Icon(Icons.Default.Search, contentDescription = "Workspace") },
                    label = { Text("Workspace", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = ColorEast,
                        indicatorColor = ColorEast,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    )
                )

                NavigationBarItem(
                    selected = activeScreen == AppScreen.Store,
                    onClick = { viewModel.setScreen(AppScreen.Store) },
                    icon = { Icon(Icons.Default.Refresh, contentDescription = "Storage") },
                    label = { Text("Edge Store", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = ColorEast,
                        indicatorColor = ColorEast,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    )
                )

                NavigationBarItem(
                    selected = activeScreen == AppScreen.Documentation,
                    onClick = { viewModel.setScreen(AppScreen.Documentation) },
                    icon = { Icon(Icons.Default.Info, contentDescription = "Specs") },
                    label = { Text("Specs Explorer", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = ColorEast,
                        indicatorColor = ColorEast,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    )
                )
            }
        },
        containerColor = Color(0xFF0F0F15)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // TOP HEADER: Banner with active synchronization status and council feedback triggers
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF131320))
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (isSyncing) ColorEast else if (isSyndicated) Color(0xFF33B3A6) else ColorSouth)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "MEDICINE WHEEL DEV SUITE",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Sync Quick Tracker
                    Icon(
                        imageVector = if (isSyndicated) Icons.Default.CheckCircle else Icons.Default.Warning,
                        contentDescription = "Sync State",
                        tint = if (isSyndicated) Color(0xFF33B3A6) else Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isSyncing) "Syncing..." else if (isSyndicated) "Syndicated" else "Woods Cache",
                        fontSize = 11.sp,
                        color = if (isSyndicated) Color(0xFF33B3A6) else Color.Gray,
                        fontFamily = FontFamily.Monospace
                    )
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    // Show guidance toggle
                    IconButton(
                        onClick = { showCouncilGuidance = !showCouncilGuidance },
                        modifier = Modifier.size(28.dp).background(Color(0xFF23233E), CircleShape)
                    ) {
                        Icon(
                            imageVector = if (showCouncilGuidance) Icons.Default.Star else Icons.Default.Warning,
                            contentDescription = "Toggle Guidance",
                            tint = ColorEast,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            
            HorizontalDivider(color = Color(0xFF1E1E34), thickness = 1.dp)

            // Expanded Persona Dialogue council box representing our core team guides!
            AnimatedVisibility(
                visible = showCouncilGuidance,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E2E)),
                    shape = RoundedCornerShape(0.dp),
                    border = BorderStroke(1.dp, ColorEast.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(viewModel = viewModel, selectedPersonaIdx = selectedPersonaIdx, onSelect = { selectedPersonaIdx = it })

                        Spacer(modifier = Modifier.height(10.dp))

                        val currentPersonaQuote = when (selectedPersonaIdx) {
                            0 -> "💕 Ava (Source): \"Let us settle ourselves here. Creative orientation is a flow, not a checklist. What wants to emerge in this relational space today?\""
                            1 -> "🧠 Mia (Architect): \"Edge persistence is fully responsive in SQLite. We treat relationships as first-class schemas. Every node is nested safely on this device.\""
                            2 -> "🌸 Miette (Illuminator): \"Every node you create is a seed of traditional teaching. Notice how the connections glow as we honor them through ceremony.\""
                            3 -> "🌊 Tushell (Keeper): \"Our local voice reflection logs metadata with strict spatial alignment. We are gathering precise digital medicine bundle files on edge.\""
                            else -> "🦉 Wise Owl (Reflector): \"Observe our directional balance of nodes. Ensure your intention in the East is validating properly in the West before North action.\""
                        }

                        Text(
                            text = currentPersonaQuote,
                            fontSize = 12.sp,
                            color = Color.LightGray,
                            lineHeight = 16.sp,
                            fontFamily = FontFamily.Serif
                        )
                    }
                }
            }

            // Screen switcher
            Box(modifier = Modifier.weight(1f)) {
                when (activeScreen) {
                    AppScreen.Workspace -> WorkspaceScreen(viewModel = viewModel, modifier = Modifier.fillMaxSize())
                    AppScreen.Store -> LocalStoreScreen(viewModel = viewModel, modifier = Modifier.fillMaxSize())
                    AppScreen.Documentation -> DocumentationScreen(viewModel = viewModel, modifier = Modifier.fillMaxSize())
                }
            }
        }
    }
}

@Composable
fun Row(viewModel: MedicineWheelViewModel, selectedPersonaIdx: Int, onSelect: (Int) -> Unit) {
    val personas = listOf("Ava 💕", "Mia 🧠", "Miette 🌸", "Tushell 🌊", "Owl 🦉")
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "CEREMONIAL PROTOCOL COUNCIL",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = ColorEast,
            fontFamily = FontFamily.Monospace
        )

        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            personas.forEachIndexed { idx, name ->
                val active = selectedPersonaIdx == idx
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (active) ColorEast else Color(0xFF23233E))
                        .clickable { onSelect(idx) }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = name,
                        fontSize = 10.sp,
                        color = if (active) Color.Black else Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
