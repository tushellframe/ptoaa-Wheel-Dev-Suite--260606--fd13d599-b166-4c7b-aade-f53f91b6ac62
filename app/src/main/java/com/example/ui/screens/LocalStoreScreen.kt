package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.ResearchCycle
import com.example.data.VoiceRecording
import com.example.viewmodel.MedicineWheelViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LocalStoreScreen(viewModel: MedicineWheelViewModel, modifier: Modifier = Modifier) {
    val cycles by viewModel.cycles.collectAsState()
    val recordings by viewModel.recordings.collectAsState()
    val isSyndicated by viewModel.isSyndicated.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()

    var showNewCycleDialog by remember { mutableStateOf(false) }
    var showNewRecordingDialog by remember { mutableStateOf(false) }

    val activeCycle = remember(cycles) { cycles.find { !it.archived } }
    val archivedCycles = remember(cycles) { cycles.filter { it.archived } }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F0F15))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // TOP SECTION: Sync status simulating Edge Storage / Woods mode
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF131320)),
                border = BorderStroke(1.dp, Color(0xFF23233E)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "EDGE PERSISTENCE ENGINE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = ColorEast,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = if (isSyndicated) "Sovereign Web Connected" else "Isolated Edge Woods Mode",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Icon(
                            imageVector = if (isSyndicated) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (isSyndicated) Color(0xFF33B3A6) else ColorSouth,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Your local changes are stored in SQLite on this physical device. You are fully capable of collecting data in the woods without any network connection. When back in range, push to sync with the server.",
                        color = Color.LightGray,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (isSyncing) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(color = ColorEast, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Uploading cached edge records and tape metadata...", color = ColorEast, fontSize = 13.sp)
                        }
                    } else {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(
                                onClick = { viewModel.pushLocalDataToServer() },
                                colors = ButtonDefaults.buttonColors(containerColor = ColorEast, contentColor = Color.Black),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Sync Cached Edge Data", fontSize = 12.sp)
                            }

                            if (isSyndicated) {
                                OutlinedButton(
                                    onClick = { viewModel.disconnectServer() },
                                    border = BorderStroke(1.dp, ColorSouth),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ColorSouth)
                                ) {
                                    Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Unlink", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // MIDDLE SECTION: Research Cycles (the macro turn of the wheel)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ACTIVE RESEARCH CYCLE",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    fontFamily = FontFamily.Monospace
                )

                if (activeCycle == null) {
                    Button(
                        onClick = { showNewCycleDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF23233E)),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp), tint = ColorEast)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Initiate New Cycle", fontSize = 12.sp, color = ColorEast)
                    }
                }
            }
        }

        if (activeCycle != null) {
            item {
                ActiveCycleCard(
                    cycle = activeCycle,
                    onAdvance = { cycleId, nextDir -> viewModel.advanceCycleDirection(cycleId, nextDir) },
                    onArchive = { cycleId -> viewModel.archiveCycle(cycleId) }
                )
            }
        } else {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF13131F)),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.DarkGray, modifier = Modifier.size(40.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No Active Cycle Running",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "A research cycle encapsulates a full turn of the Medicine Wheel. Launch a new cycle to record ceremonies and map connections under a central inquiry.",
                            color = Color.Gray,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }

        // BOTTOM SECTION: Local Audio/Voice reflections (record ourselves metadata)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "VOICE REFLECTIONS METADATA",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    fontFamily = FontFamily.Monospace
                )

                Button(
                    onClick = { showNewRecordingDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF23233E)),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Icon(Icons.Default.Create, contentDescription = null, modifier = Modifier.size(14.dp), tint = ColorEast)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reflect Spoken Text", fontSize = 12.sp, color = ColorEast)
                }
            }
        }

        if (recordings.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF13131F)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(16.dp), contentAlignment = Alignment.Center) {
                        Text(text = "No audio reflections spoken on edge. Click top-right to register tape notes.", color = Color.Gray, fontSize = 12.sp)
                    }
                }
            }
        } else {
            items(recordings) { rec ->
                RecordingItemRow(recording = rec)
            }
        }

        // ARCHIVED/COMPLETED CYCLES SECTION
        if (archivedCycles.isNotEmpty()) {
            item {
                Text(
                    text = "ARCHIVED SEVEN-GENERATIONS STORIES (${archivedCycles.size})",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            items(archivedCycles) { cycle ->
                ArchivedCycleCard(cycle = cycle)
            }
        }
    }

    // Dialog: Launch New Research Cycle
    if (showNewCycleDialog) {
        NewCycleDialog(
            onDismiss = { showNewCycleDialog = false },
            onInitiate = { question, initialDir ->
                viewModel.startCycle(question, initialDir)
                showNewCycleDialog = false
            }
        )
    }

    // Dialog: Spoken voice recording details
    if (showNewRecordingDialog) {
        NewRecordingDialog(
            onDismiss = { showNewRecordingDialog = false },
            onRecord = { title, desc, dir, duration ->
                viewModel.addLocalRecording(title, desc, dir, duration)
                showNewRecordingDialog = false
            }
        )
    }
}

@Composable
fun ActiveCycleCard(
    cycle: ResearchCycle,
    onAdvance: (cycleId: String, nextDir: String) -> Unit,
    onArchive: (cycleId: String) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E2E)),
        border = BorderStroke(1.dp, ColorEast.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Create,
                        contentDescription = null,
                        tint = ColorEast,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "RESEARCH CYCLE IN ACTION",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorEast,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Badge(containerColor = ColorEast, contentColor = Color.Black) {
                    Text(
                        text = "Active",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = cycle.researchQuestion,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = Color(0x1F23233E))
            Spacer(modifier = Modifier.height(14.dp))

            // Current directional alignment and next directions
            Row(modifier = Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Column {
                    Text("ACTIVE WHEEL LOCATION", fontSize = 10.sp, color = Color.Gray)
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                        val currentLabel = when(cycle.currentDirection) {
                            "east" -> "Waabinong (East) 🌸"
                            "south" -> "Zhaawanong (South) 🔥"
                            "west" -> "Epangishmok (West) 🌊"
                            "north" -> "Kiiwedinong (North) ❄️"
                            else -> "Spiritual Center"
                        }
                        Text(
                            text = currentLabel,
                            color = getDirectionColor(cycle.currentDirection),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }

                // Advancement Button in E->S->W->N flow
                val (nextDir, nextLabel) = when(cycle.currentDirection) {
                    "east" -> Pair("south", "Go South 🔥")
                    "south" -> Pair("west", "Go West 🌊")
                    "west" -> Pair("north", "Go North ❄️")
                    else -> Pair("east", "Go East 🌸")
                }

                Button(
                    onClick = { onAdvance(cycle.id, nextDir) },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF131320)),
                    border = BorderStroke(1.dp, getDirectionColor(nextDir).copy(alpha = 0.5f)),
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    Text(text = nextLabel, color = getDirectionColor(nextDir), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Wilson Alignment and OCAP Status
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF13131F))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Wilson Integrity Check", fontSize = 10.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${(cycle.wilsonAlignment * 100).toInt()}%",
                                color = ColorEast,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (cycle.wilsonAlignment > 0.7f) "Accountable" else "Deepen Links",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF13131F))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Sovereignty OCAP®", fontSize = 10.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lock, "verified icon", tint = Color(0xFF33B3A6), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Compliant",
                                color = Color(0xFF33B3A6),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            // Archiving for seven generations action
            if (cycle.currentDirection == "north") {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { onArchive(cycle.id) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF33B3A6), contentColor = Color.Black)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Archive for Seven Generations", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun RecordingItemRow(recording: VoiceRecording) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF13131F)),
        border = BorderStroke(1.dp, Color(0xFF1D1B28))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(getDirectionColor(recording.direction).copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = getDirectionColor(recording.direction)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = recording.title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text(text = recording.description, color = Color.Gray, fontSize = 11.sp, maxLines = 1)
                
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                    Text(
                        text = "Quadrant: ${recording.direction.uppercase()}",
                        color = getDirectionColor(recording.direction),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date(recording.timestamp)),
                        color = Color.DarkGray,
                        fontSize = 10.sp
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(text = recording.durationText, color = Color.White, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (recording.isUploaded) Icons.Default.CheckCircle else Icons.Default.Info,
                        contentDescription = null,
                        tint = if (recording.isUploaded) Color(0xFF33B3A6) else Color.Gray,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = if (recording.isUploaded) "Synced" else "Cached",
                        color = if (recording.isUploaded) Color(0xFF33B3A6) else Color.Gray,
                        fontSize = 9.sp
                    )
                }
            }
        }
    }
}

@Composable
fun ArchivedCycleCard(cycle: ResearchCycle) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF13131F)),
        border = BorderStroke(1.dp, Color(0x33333333)),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Share, "archived icon", tint = Color.Gray, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("ARCHIVAL SOVEREIGN STORY", fontSize = 9.sp, color = Color.Gray, fontFamily = FontFamily.Monospace)
                }
                Text("Approved: Elder & Youth council", fontSize = 9.sp, color = Color(0xFF33B3A6), fontFamily = FontFamily.Monospace)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = cycle.researchQuestion, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Cermonies Tended: ${cycle.ceremoniesConducted}", fontSize = 11.sp, color = Color.Gray)
                Text("•", fontSize = 11.sp, color = Color.Gray)
                Text("Wilson Score: ${(cycle.wilsonAlignment * 100).toInt()}%", fontSize = 11.sp, color = Color.Gray)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewCycleDialog(
    onDismiss: () -> Unit,
    onInitiate: (question: String, initialDir: String) -> Unit
) {
    var question by remember { mutableStateOf("") }
    var direction by remember { mutableStateOf("east") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Initiate Sovereign Cycle", color = Color.White) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = question,
                    onValueChange = { question = it },
                    label = { Text("Inquiry Question") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Opening Direction (Recommended: East for Vision):", color = Color.Gray, fontSize = 12.sp)
                val dirs = listOf("east", "south", "west", "north")
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    dirs.forEach { d ->
                        val selected = direction == d
                        val dirColor = getDirectionColor(d)
                        SuggestionChip(
                            onClick = { direction = d },
                            label = { Text(d, color = if (selected) Color.Black else Color.White) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = if (selected) dirColor else Color(0xFF23233E)
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (question.isNotEmpty()) onInitiate(question, direction) },
                colors = ButtonDefaults.buttonColors(containerColor = ColorEast, contentColor = Color.Black)
            ) {
                Text("Kindle Fire")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Dismiss", color = Color.Gray)
            }
        },
        containerColor = Color(0xFF1E1E2E)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewRecordingDialog(
    onDismiss: () -> Unit,
    onRecord: (title: String, desc: String, dir: String, duration: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var direction by remember { mutableStateOf("east") }
    var mockDuration by remember { mutableStateOf("1:15") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Record Spoken Reflections", color = Color.White) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Reflection Title") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Core Teachings / Observations") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Spoken in Quadrant:", color = Color.Gray, fontSize = 12.sp)
                val dirs = listOf("east", "south", "west", "north")
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    dirs.forEach { d ->
                        val selected = direction == d
                        val dirColor = getDirectionColor(d)
                        SuggestionChip(
                            onClick = { direction = d },
                            label = { Text(d, color = if (selected) Color.Black else Color.White) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = if (selected) dirColor else Color(0xFF23233E)
                            )
                        )
                    }
                }

                OutlinedTextField(
                    value = mockDuration,
                    onValueChange = { mockDuration = it },
                    label = { Text("Simulated Tape Duration (min:sec)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (title.isNotEmpty()) onRecord(title, description, direction, mockDuration) },
                colors = ButtonDefaults.buttonColors(containerColor = ColorEast, contentColor = Color.Black)
            ) {
                Text("Register Voice Reflection")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Dismiss", color = Color.Gray)
            }
        },
        containerColor = Color(0xFF1E1E2E)
    )
}
