package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.IntOffset
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.LinearEasing
import com.example.data.RelationalEdge
import com.example.data.RelationalNode
import com.example.viewmodel.MedicineWheelViewModel
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.atan2
import kotlin.math.abs

// Direction Colors Matching standard specs
val ColorEast = Color(0xFFFFD700)   // Gold / Yellow
val ColorSouth = Color(0xFFDC143C)  // Crimson / Red
val ColorWest = Color(0xFF4A4A8A)   // Indigo / Blue
val ColorNorth = Color(0xFFE5E5E5)  // White / Light Gray

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WorkspaceScreen(viewModel: MedicineWheelViewModel, modifier: Modifier = Modifier) {
    val nodes by viewModel.nodes.collectAsState()
    val edges by viewModel.edges.collectAsState()
    val selectedNodeId by viewModel.selectedNodeId.collectAsState()
    
    var showAddNodeDialog by remember { mutableStateOf(false) }
    var preselectedAddDirection by remember { mutableStateOf<String?>("east") }
    var showLinkNodesDialog by remember { mutableStateOf(false) }
    var activeEdgeForCeremony by remember { mutableStateOf<RelationalEdge?>(null) }
    var linkingFromNodeId by remember { mutableStateOf<String?>(null) }
    var prefilledLinkToNodeId by remember { mutableStateOf<String?>(null) }

    // Focus & Layout configuration states
    var graphHeightDp by remember { mutableStateOf(340.dp) }
    var radiusMultiplier by remember { mutableFloatStateOf(0.65f) }
    var isFullScreenFocus by remember { mutableStateOf(false) }
    var showLayoutSettings by remember { mutableStateOf(false) }

    val selectedNode = remember(selectedNodeId, nodes) {
        nodes.find { it.id == selectedNodeId }
    }

    val dragOffsets = remember { mutableStateMapOf<String, Offset>() }

    LaunchedEffect(nodes) {
        nodes.forEach { node ->
            if (node.xOffset == 0f && node.yOffset == 0f) {
                dragOffsets.remove(node.id)
            }
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F0F15))
    ) {
        val isCompact = maxWidth < 750.dp

        if (isCompact) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header item
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "RELATIONAL MAP",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = ColorEast,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "Circular Spatial Semantics",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Layout & Focus Settings Toggle button
                            IconButton(
                                onClick = { showLayoutSettings = !showLayoutSettings },
                                modifier = Modifier.background(Color(0xFF1E1E2A), CircleShape).size(38.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Layout Settings",
                                    tint = if (showLayoutSettings) ColorEast else Color.Gray
                                )
                            }
                            IconButton(
                                onClick = { viewModel.resetAllNodePositions() },
                                modifier = Modifier.background(Color(0xFF1E1E2A), CircleShape).size(38.dp)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = "Align Layout", tint = Color.Cyan)
                            }
                            IconButton(
                                onClick = { 
                                    preselectedAddDirection = "east"
                                    showAddNodeDialog = true 
                                },
                                modifier = Modifier.background(Color(0xFF1E1E2A), CircleShape).size(38.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Add Node", tint = Color.Green)
                            }
                            IconButton(
                                onClick = { showLinkNodesDialog = true },
                                modifier = Modifier.background(Color(0xFF1E1E2A), CircleShape).size(38.dp)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = "Link Nodes", tint = ColorEast)
                            }
                        }
                    }
                }

                // Collapsible Focus & Layout Settings Controls Card
                item {
                    AnimatedVisibility(
                        visible = showLayoutSettings,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E2E)),
                            border = BorderStroke(1.dp, ColorEast.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "LAYOUT & SIZES HUD",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ColorEast,
                                    fontFamily = FontFamily.Monospace
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                
                                // Height Controller
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Height: ${graphHeightDp.value.toInt()}dp",
                                        color = Color.White,
                                        fontSize = 11.sp
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        listOf(240, 340, 460, 580).forEach { h ->
                                            TextButton(
                                                onClick = { graphHeightDp = h.dp },
                                                colors = ButtonDefaults.textButtonColors(
                                                    contentColor = if (graphHeightDp == h.dp) ColorEast else Color.Gray
                                                ),
                                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                                modifier = Modifier.height(24.dp)
                                            ) {
                                                Text("${h}d", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                                Slider(
                                    value = graphHeightDp.value,
                                    onValueChange = { graphHeightDp = it.coerceIn(200f, 650f).dp },
                                    valueRange = 200f..650f,
                                    colors = SliderDefaults.colors(
                                        thumbColor = ColorEast,
                                        activeTrackColor = ColorEast,
                                        inactiveTrackColor = Color.Gray.copy(alpha = 0.3f)
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                // Node Spread (Radius) Controller
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Node Spread: ${(radiusMultiplier * 100).toInt()}%",
                                        color = Color.White,
                                        fontSize = 11.sp
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        listOf(0.45f, 0.65f, 0.85f, 1.05f).forEach { r ->
                                            TextButton(
                                                onClick = { radiusMultiplier = r },
                                                colors = ButtonDefaults.textButtonColors(
                                                    contentColor = if (radiusMultiplier == r) ColorEast else Color.Gray
                                                ),
                                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                                modifier = Modifier.height(24.dp)
                                            ) {
                                                Text("${(r * 100).toInt()}%", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                                Slider(
                                    value = radiusMultiplier,
                                    onValueChange = { radiusMultiplier = it },
                                    valueRange = 0.3f..1.2f,
                                    colors = SliderDefaults.colors(
                                        thumbColor = ColorEast,
                                        activeTrackColor = ColorEast,
                                        inactiveTrackColor = Color.Gray.copy(alpha = 0.3f)
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                // Full Screen Focus Toggle Button
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Full Screen Focus Mode",
                                            color = Color.White,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = "Collapses inspector to maximize active focus space",
                                            color = Color.Gray,
                                            fontSize = 10.sp
                                        )
                                    }
                                    Switch(
                                        checked = isFullScreenFocus,
                                        onCheckedChange = { isFullScreenFocus = it },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Color.Black,
                                            checkedTrackColor = ColorEast,
                                            uncheckedThumbColor = Color.Gray,
                                            uncheckedTrackColor = Color(0xFF23233E)
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                // Interactive Wheel item
                item {
                    MedicineWheelGraph(
                        nodes = nodes,
                        edges = edges,
                        selectedNodeId = selectedNodeId,
                        viewModel = viewModel,
                        dragOffsets = dragOffsets,
                        linkingFromNodeId = linkingFromNodeId,
                        onStartLink = { linkingFromNodeId = it },
                        onCancelLink = { linkingFromNodeId = null },
                        onSelectToLink = { toId ->
                            prefilledLinkToNodeId = toId
                            showLinkNodesDialog = true
                        },
                        onDoubleTap = { dir ->
                            preselectedAddDirection = dir
                            showAddNodeDialog = true
                        },
                        radiusMultiplier = radiusMultiplier,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(graphHeightDp)
                    )
                }

                // Inspector card item (flows downward scrollably)
                item {
                    if (!isFullScreenFocus) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF131320)),
                            border = BorderStroke(1.dp, Color(0xFF1E1E34)),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                if (selectedNode != null) {
                                    NodeInspectorPanel(
                                        node = selectedNode,
                                        edges = edges,
                                        allNodes = nodes,
                                        viewModel = viewModel,
                                        modifier = Modifier.fillMaxWidth(),
                                        onConductCeremony = { activeEdgeForCeremony = it },
                                        onClose = { viewModel.selectNode(null) }
                                    )
                                } else {
                                    EmptyInspectorPanel(
                                        nodeCount = nodes.size,
                                        edgeCount = edges.size,
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                                    )
                                }
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "✨ ZEN FOCUS ACTIVE - CLICK HEADER GEAR TO ADJUST",
                                color = Color.Gray,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left Column or primary block: the actual interactive wheel representation
                val leftWeight = if (isFullScreenFocus) 2.2f else 1.2f
                Box(
                    modifier = Modifier
                        .weight(leftWeight)
                        .fillMaxHeight()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Header indicating visual ontology mode
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "RELATIONAL MAP",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ColorEast,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "Circular Spatial Semantics",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                // Settings button to show/hide controls shelf
                                IconButton(
                                    onClick = { showLayoutSettings = !showLayoutSettings },
                                    modifier = Modifier.background(Color(0xFF1E1E2A), CircleShape).size(38.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = "Layout Settings",
                                        tint = if (showLayoutSettings) ColorEast else Color.Gray
                                    )
                                }
                                IconButton(
                                    onClick = { viewModel.resetAllNodePositions() },
                                    modifier = Modifier.background(Color(0xFF1E1E2A), CircleShape).size(38.dp)
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = "Align Layout", tint = Color.Cyan)
                                }
                                IconButton(
                                    onClick = { 
                                        preselectedAddDirection = "east"
                                        showAddNodeDialog = true 
                                    },
                                    modifier = Modifier.background(Color(0xFF1E1E2A), CircleShape).size(38.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Add Node", tint = Color.Green)
                                }
                                IconButton(
                                    onClick = { showLinkNodesDialog = true },
                                    modifier = Modifier.background(Color(0xFF1E1E2A), CircleShape).size(38.dp)
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = "Link Nodes", tint = ColorEast)
                                }
                            }
                        }

                        // Collapsible Focus & Layout Settings Card inside tablet side
                        AnimatedVisibility(
                            visible = showLayoutSettings,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E2E)),
                                border = BorderStroke(1.dp, ColorEast.copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "FOCUS & LAYOUT CONTROLLERS",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ColorEast,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    
                                    // Node Spread (Radius) Controller
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Node Spread: ${(radiusMultiplier * 100).toInt()}%",
                                            color = Color.White,
                                            fontSize = 11.sp
                                        )
                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            listOf(0.45f, 0.65f, 0.85f, 1.05f).forEach { r ->
                                                TextButton(
                                                    onClick = { radiusMultiplier = r },
                                                    colors = ButtonDefaults.textButtonColors(
                                                        contentColor = if (radiusMultiplier == r) ColorEast else Color.Gray
                                                    ),
                                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                                    modifier = Modifier.height(24.dp)
                                                ) {
                                                    Text("${(r * 100).toInt()}%", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                    Slider(
                                        value = radiusMultiplier,
                                        onValueChange = { radiusMultiplier = it },
                                        valueRange = 0.3f..1.2f,
                                        colors = SliderDefaults.colors(
                                            thumbColor = ColorEast,
                                            activeTrackColor = ColorEast,
                                            inactiveTrackColor = Color.Gray.copy(alpha = 0.3f)
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Full Screen Focus Mode
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "Full Screen Focus Mode",
                                                color = Color.White,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                            Text(
                                                text = "Hides right inspector panel to widen relational workspace",
                                                color = Color.Gray,
                                                fontSize = 10.sp
                                            )
                                        }
                                        Switch(
                                            checked = isFullScreenFocus,
                                            onCheckedChange = { isFullScreenFocus = it },
                                            colors = SwitchDefaults.colors(
                                                checkedThumbColor = Color.Black,
                                                checkedTrackColor = ColorEast,
                                                uncheckedThumbColor = Color.Gray,
                                                uncheckedTrackColor = Color(0xFF23233E)
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        // Interactive Circle Box
                        MedicineWheelGraph(
                            nodes = nodes,
                            edges = edges,
                            selectedNodeId = selectedNodeId,
                            viewModel = viewModel,
                            dragOffsets = dragOffsets,
                            linkingFromNodeId = linkingFromNodeId,
                            onStartLink = { linkingFromNodeId = it },
                            onCancelLink = { linkingFromNodeId = null },
                            onSelectToLink = { toId ->
                                prefilledLinkToNodeId = toId
                                showLinkNodesDialog = true
                            },
                            onDoubleTap = { dir ->
                                preselectedAddDirection = dir
                                showAddNodeDialog = true
                            },
                            radiusMultiplier = radiusMultiplier,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        )
                    }
                }

                // Right Column: Node Inspector & Obligation audits
                if (!isFullScreenFocus) {
                    Box(
                        modifier = Modifier
                            .weight(0.8f)
                            .fillMaxHeight()
                            .background(Color(0xFF131320))
                            .border(1.dp, Color(0xFF1E1E34))
                            .padding(16.dp)
                    ) {
                        if (selectedNode != null) {
                            NodeInspectorPanel(
                                node = selectedNode,
                                edges = edges,
                                allNodes = nodes,
                                viewModel = viewModel,
                                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                                onConductCeremony = { activeEdgeForCeremony = it },
                                onClose = { viewModel.selectNode(null) }
                            )
                        } else {
                            EmptyInspectorPanel(
                                nodeCount = nodes.size,
                                edgeCount = edges.size,
                                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                            )
                        }
                    }
                }
            }
        }
    }

    // Dialogs
    if (showAddNodeDialog) {
        AddNodeDialog(
            initialDirection = preselectedAddDirection,
            onDismiss = { showAddNodeDialog = false },
            onSave = { name, type, direction, desc ->
                viewModel.addNode(name, type, direction, desc)
                showAddNodeDialog = false
            }
        )
    }

    if (showLinkNodesDialog) {
        LinkNodesDialog(
            nodes = nodes,
            initialFromId = linkingFromNodeId,
            initialToId = prefilledLinkToNodeId,
            onDismiss = { 
                showLinkNodesDialog = false
                linkingFromNodeId = null
                prefilledLinkToNodeId = null
            },
            onLink = { fromId, toId, type, strength ->
                viewModel.linkNodes(fromId, toId, type, strength)
                showLinkNodesDialog = false
                linkingFromNodeId = null
                prefilledLinkToNodeId = null
            }
        )
    }

    activeEdgeForCeremony?.let { edge ->
        ConductCeremonyDialog(
            edge = edge,
            allNodes = nodes,
            onDismiss = { activeEdgeForCeremony = null },
            onConduct = { type, direction, participants, medicines, intentions, context, ceremonyId ->
                viewModel.logCeremony(
                    type = type,
                    direction = direction,
                    participants = participants,
                    medicines = medicines,
                    intentions = intentions,
                    context = context,
                    id = ceremonyId
                )
                viewModel.honorEdgeWithCeremony(edge.id, ceremonyId)
                activeEdgeForCeremony = null
            }
        )
    }
}

@Composable
fun MedicineWheelGraph(
    nodes: List<RelationalNode>,
    edges: List<RelationalEdge>,
    selectedNodeId: String?,
    viewModel: MedicineWheelViewModel,
    dragOffsets: SnapshotStateMap<String, Offset>,
    linkingFromNodeId: String?,
    onStartLink: (String) -> Unit,
    onCancelLink: () -> Unit,
    onSelectToLink: (String) -> Unit,
    onDoubleTap: (String?) -> Unit,
    radiusMultiplier: Float = 0.65f,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFF13131E))
            .border(1.dp, Color(0xFF23233E), RoundedCornerShape(24.dp)),
        contentAlignment = Alignment.Center
    ) {
        // Background Canvas
        MedicineWheelBackgroundCanvas()

        BoxWithConstraints(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            val widthPx = with(LocalDensity.current) { maxWidth.toPx() }
            val heightPx = with(LocalDensity.current) { maxHeight.toPx() }
            val radiusPx = (widthPx.coerceAtMost(heightPx) / 2f) * radiusMultiplier
            
            val nodePositions = remember(nodes, widthPx, heightPx, radiusPx) {
                calculateNodeLayout(nodes, widthPx / 2f, heightPx / 2f, radiusPx)
            }

            val finalNodePositions = remember(nodes, nodePositions, dragOffsets) {
                nodes.associate { node ->
                    val basePos = nodePositions[node.id] ?: Offset(widthPx / 2f, heightPx / 2f)
                    val activeOffset = dragOffsets[node.id] ?: Offset(node.xOffset, node.yOffset)
                    node.id to Offset(basePos.x + activeOffset.x, basePos.y + activeOffset.y)
                }
            }

            // Invisible gesture background to handle double taps on empty space to Quick-Add
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(widthPx, heightPx) {
                        detectTapGestures(
                            onDoubleTap = { offset ->
                                val cx = widthPx / 2f
                                val cy = heightPx / 2f
                                val dx = offset.x - cx
                                val dy = offset.y - cy
                                val angle = atan2(dy, dx)
                                val dir = when {
                                    abs(angle) <= Math.PI.toFloat() / 4f -> "east"
                                    angle > Math.PI.toFloat() / 4f && angle <= 3f * Math.PI.toFloat() / 4f -> "south"
                                    angle < -Math.PI.toFloat() / 4f && angle >= -3f * Math.PI.toFloat() / 4f -> "north"
                                    else -> "west"
                                }
                                onDoubleTap(dir)
                            },
                            onTap = {
                                viewModel.selectNode("")
                            }
                        )
                    }
            )

            // Edges Canvas
            Canvas(modifier = Modifier.fillMaxSize()) {
                edges.forEach { edge ->
                    val startPos = finalNodePositions[edge.fromId]
                    val endPos = finalNodePositions[edge.toId]
                    if (startPos != null && endPos != null) {
                        val isHighlighted = edge.fromId == linkingFromNodeId || edge.toId == linkingFromNodeId
                        val lineColor = if (edge.ceremonyHonored) {
                            ColorEast
                        } else if (isHighlighted) {
                            Color(0xFF00FFCC)
                        } else {
                            Color(0x55FFFFFF)
                        }
                        
                        val strokeWidth = if (edge.ceremonyHonored) {
                            4e-1f * density * 8f 
                        } else if (isHighlighted) {
                            2.5f * density
                        } else {
                            1.5f * density
                        }
                        
                        drawLine(
                            color = lineColor,
                            start = startPos,
                            end = endPos,
                            strokeWidth = strokeWidth
                        )

                        // Draw ceremonial beads on relations to celebrate connections
                        val midX = (startPos.x + endPos.x) / 2f
                        val midY = (startPos.y + endPos.y) / 2f
                        val midPoint = Offset(midX, midY)
                        
                        val outerBeadColor = if (edge.ceremonyHonored) ColorEast else getDirectionColor(nodes.find { it.id == edge.fromId }?.direction)
                        drawCircle(
                            color = outerBeadColor,
                            radius = 5.5f * density,
                            center = midPoint
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 2.5f * density,
                            center = midPoint
                        )
                    }
                }
            }

            // Nodes
            finalNodePositions.forEach { (nodeId, offset) ->
                val node = nodes.find { it.id == nodeId }
                if (node != null) {
                    val isSelected = node.id == selectedNodeId
                    val isLinkingSrc = node.id == linkingFromNodeId
                    val nodeColor = getDirectionColor(node.direction)
                    val dpOffset = with(LocalDensity.current) {
                        IntOffset(
                            x = (offset.x - 22.dp.toPx()).toInt(),
                            y = (offset.y - 22.dp.toPx()).toInt()
                        )
                    }

                    if (isLinkingSrc) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .absoluteOffset(
                                    x = with(LocalDensity.current) { dpOffset.x.toDp() - 6.dp },
                                    y = with(LocalDensity.current) { dpOffset.y.toDp() - 6.dp }
                                )
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(ColorEast.copy(alpha = 0.25f * (2f - pulseScale)))
                                .border(1.dp, ColorEast.copy(alpha = 0.4f), CircleShape)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .absoluteOffset(
                                x = with(LocalDensity.current) { dpOffset.x.toDp() },
                                y = with(LocalDensity.current) { dpOffset.y.toDp() }
                            )
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) Color.White else Color(0xFF1F1F2F))
                            .border(
                                width = if (isSelected) 3.dp else 2.dp,
                                color = if (isLinkingSrc) ColorEast else nodeColor,
                                shape = CircleShape
                            )
                            .pointerInput(node.id) {
                                detectDragGestures(
                                    onDragStart = {
                                        if (linkingFromNodeId == null) {
                                            viewModel.selectNode(node.id)
                                        }
                                    },
                                    onDragEnd = {
                                        val finalOffsetVal = dragOffsets[node.id] ?: Offset(node.xOffset, node.yOffset)
                                        viewModel.updateNodePosition(node.id, finalOffsetVal.x, finalOffsetVal.y)
                                    },
                                    onDragCancel = {
                                        val finalOffsetVal = dragOffsets[node.id] ?: Offset(node.xOffset, node.yOffset)
                                        viewModel.updateNodePosition(node.id, finalOffsetVal.x, finalOffsetVal.y)
                                    },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        val oldOffset = dragOffsets[node.id] ?: Offset(node.xOffset, node.yOffset)
                                        dragOffsets[node.id] = oldOffset + dragAmount
                                    }
                                )
                            }
                            .clickable {
                                val currentLinkingFrom = linkingFromNodeId
                                if (currentLinkingFrom != null) {
                                    if (currentLinkingFrom != node.id) {
                                        onSelectToLink(node.id)
                                    } else {
                                        viewModel.selectNode(node.id)
                                    }
                                } else {
                                    viewModel.selectNode(node.id)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getNodeIcon(node.type),
                            contentDescription = node.name,
                            tint = if (isSelected) Color(0xFF13131E) else Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    val labelOffset = with(LocalDensity.current) {
                        IntOffset(
                            x = (offset.x - 50.dp.toPx()).toInt(),
                            y = (offset.y + 26.dp.toPx()).toInt()
                        )
                    }
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .absoluteOffset(
                                x = with(LocalDensity.current) { labelOffset.x.toDp() },
                                y = with(LocalDensity.current) { labelOffset.y.toDp() }
                            )
                            .width(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = node.name,
                            color = if (isSelected) Color.White else Color(0xCCFFFFFF),
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        // Weaving kinship banner overlay
        if (linkingFromNodeId != null) {
            val sourceNode = nodes.find { it.id == linkingFromNodeId }
            if (sourceNode != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(12.dp)
                        .background(Color(0xE60F0F1A), RoundedCornerShape(12.dp))
                        .border(1.dp, ColorEast.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .fillMaxWidth(0.9f)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "WEAVING KINSHIP THREAD",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = ColorEast,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Select any other entity to link from ${sourceNode.name}",
                                color = Color.White,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        IconButton(
                            onClick = onCancelLink,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cancel linking",
                                tint = Color.Gray,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        // Float selection card HUD
        if (linkingFromNodeId == null && selectedNodeId != null) {
            val selected = nodes.find { it.id == selectedNodeId }
            if (selected != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp)
                        .background(Color(0xEE0F0F15), RoundedCornerShape(12.dp))
                        .border(1.dp, getDirectionColor(selected.direction).copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .padding(10.dp)
                        .widthIn(max = 220.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(getDirectionColor(selected.direction).copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = getNodeIcon(selected.type),
                                    contentDescription = null,
                                    tint = getDirectionColor(selected.direction),
                                    modifier = Modifier.size(10.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = selected.name,
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            text = "Direction: ${selected.direction?.uppercase() ?: "CENTER"}",
                            color = getDirectionColor(selected.direction),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = { onStartLink(selected.id) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ColorEast.copy(alpha = 0.22f),
                                    contentColor = ColorEast
                                ),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(24.dp).weight(1.1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share, 
                                    contentDescription = "Weave link", 
                                    modifier = Modifier.size(11.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("WEAVE", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                            
                            Spacer(modifier = Modifier.width(6.dp))
                            
                            IconButton(
                                onClick = { viewModel.resetNodePosition(selected.id) },
                                modifier = Modifier.size(24.dp).background(Color(0xFF1E1E2A), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Reset offset",
                                    tint = Color.Cyan,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MedicineWheelBackgroundCanvas() {
    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val halfMinDim = size.width.coerceAtMost(size.height) / 2f
            val outerRadius = halfMinDim * 0.85f
            val innerCircleRadius = halfMinDim * 0.2f

            // Draw structural circle indicators
            drawCircle(
                color = Color(0x1AFFFFFF),
                radius = outerRadius,
                center = Offset(cx, cy),
                style = Stroke(width = 1.5f)
            )
            drawCircle(
                color = Color(0x0DFFFFFF),
                radius = halfMinDim * 0.5f,
                center = Offset(cx, cy),
                style = Stroke(width = 1.5f)
            )
            drawCircle(
                color = Color(0x33FFFFFF),
                radius = innerCircleRadius,
                center = Offset(cx, cy),
                style = Stroke(width = 2f)
            )

            // Draw horizontal/vertical lines representing axes (dividing the quadrants)
            drawLine(
                color = Color(0x1F23233E),
                start = Offset(cx - outerRadius, cy),
                end = Offset(cx + outerRadius, cy),
                strokeWidth = 2f
            )
            drawLine(
                color = Color(0x1F23233E),
                start = Offset(cx, cy - outerRadius),
                end = Offset(cx, cy + outerRadius),
                strokeWidth = 2f
            )
            
            // Draw directional quadrant indicator slices (subtle glow)
            // East Slice: 315 to 45
            drawArc(
                color = ColorEast,
                startAngle = -45f,
                sweepAngle = 90f,
                useCenter = true,
                alpha = 0.04f,
                size = androidx.compose.ui.geometry.Size(outerRadius * 2, outerRadius * 2),
                topLeft = Offset(cx - outerRadius, cy - outerRadius)
            )
            // South Slice: 45 to 135
            drawArc(
                color = ColorSouth,
                startAngle = 45f,
                sweepAngle = 90f,
                useCenter = true,
                alpha = 0.04f,
                size = androidx.compose.ui.geometry.Size(outerRadius * 2, outerRadius * 2),
                topLeft = Offset(cx - outerRadius, cy - outerRadius)
            )
            // West Slice: 135 to 225
            drawArc(
                color = ColorWest,
                startAngle = 135f,
                sweepAngle = 90f,
                useCenter = true,
                alpha = 0.04f,
                size = androidx.compose.ui.geometry.Size(outerRadius * 2, outerRadius * 2),
                topLeft = Offset(cx - outerRadius, cy - outerRadius)
            )
            // North Slice: 225 to 315
            drawArc(
                color = ColorNorth,
                startAngle = 225f,
                sweepAngle = 90f,
                useCenter = true,
                alpha = 0.04f,
                size = androidx.compose.ui.geometry.Size(outerRadius * 2, outerRadius * 2),
                topLeft = Offset(cx - outerRadius, cy - outerRadius)
            )
        }

        // North Label (Top)
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 10.dp)
                .background(Color(0xEE0F0F15), RoundedCornerShape(6.dp))
                .border(BorderStroke(1.dp, ColorNorth.copy(alpha = 0.4f)), RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
            Text(
                text = "❄️ KIIWEDINONG (NORTH)",
                color = ColorNorth,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }

        // East Label (Right)
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 10.dp)
                .background(Color(0xEE0F0F15), RoundedCornerShape(6.dp))
                .border(BorderStroke(1.dp, ColorEast.copy(alpha = 0.4f)), RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
            Text(
                text = "WAABINONG (EAST) 🌸",
                color = ColorEast,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }

        // South Label (Bottom)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 10.dp)
                .background(Color(0xEE0F0F15), RoundedCornerShape(6.dp))
                .border(BorderStroke(1.dp, ColorSouth.copy(alpha = 0.4f)), RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
            Text(
                text = "ZHAAWANONG (SOUTH) 🔥",
                color = ColorSouth,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }

        // West Label (Left)
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 10.dp)
                .background(Color(0xEE0F0F15), RoundedCornerShape(6.dp))
                .border(BorderStroke(1.dp, ColorWest.copy(alpha = 0.4f)), RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
            Text(
                text = "🌊 EPANGISHMOK (WEST)",
                color = ColorWest,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

fun calculateNodeLayout(
    nodes: List<RelationalNode>,
    centerX: Float,
    centerY: Float,
    radius: Float
): Map<String, Offset> {
    val layout = mutableMapOf<String, Offset>()
    
    // Group nodes by direction to distribute them properly
    val groups = nodes.groupBy { it.direction }
    
    groups.forEach { (dir, list) ->
        val baseAngle = when (dir) {
            "east" -> 0.0f
            "south" -> Math.PI.toFloat() / 2f
            "west" -> Math.PI.toFloat()
            "north" -> 3f * Math.PI.toFloat() / 2f
            else -> null
        }

        if (baseAngle != null) {
            // Distribute list nodes symmetrically around the quadrant's center angle
            val spread = 0.45f // angle span offset in radians
            list.forEachIndexed { idx, node ->
                val fraction = if (list.size <= 1) 0f else (idx.toFloat() / (list.size - 1) - 0.5f)
                val angle = baseAngle + (fraction * spread)
                // Vary slightly in radius for spatial depth
                val customRadius = radius * (0.8f + (idx % 2 * 0.15f))
                val x = centerX + customRadius * cos(angle)
                val y = centerY + customRadius * sin(angle)
                layout[node.id] = Offset(x, y)
            }
        } else {
            // Center cluster (no direction or spiritual hub)
            list.forEachIndexed { idx, node ->
                val angle = (idx.toFloat() / list.size.coerceAtLeast(1)) * 2f * Math.PI.toFloat()
                val customRadius = 30.dp.value * (0.6f + idx * 0.1f)
                val x = centerX + customRadius * cos(angle)
                val y = centerY + customRadius * sin(angle)
                layout[node.id] = Offset(x, y)
            }
        }
    }
    
    return layout
}

fun getDirectionColor(direction: String?): Color {
    return when (direction) {
        "east" -> ColorEast
        "south" -> ColorSouth
        "west" -> ColorWest
        "north" -> ColorNorth
        else -> Color.DarkGray
    }
}

fun getNodeIcon(type: String): ImageVector {
    return when (type) {
        "human" -> Icons.Default.Person
        "land" -> Icons.Default.Favorite // M3 Core placeholder for earth
        "spirit" -> Icons.Default.Home
        "ancestor" -> Icons.Default.Create // M3 Core placeholder for teachings
        "future" -> Icons.Default.ArrowForward
        "knowledge" -> Icons.Default.Info
        else -> Icons.Default.Star
    }
}

@Composable
fun NodeInspectorPanel(
    node: RelationalNode,
    edges: List<RelationalEdge>,
    allNodes: List<RelationalNode>,
    viewModel: MedicineWheelViewModel,
    modifier: Modifier = Modifier,
    onConductCeremony: (RelationalEdge) -> Unit,
    onClose: () -> Unit
) {
    val nodeEdges = remember(node.id, edges) {
        edges.filter { it.fromId == node.id || it.toId == node.id }
    }

    var isEditing by remember(node.id) { mutableStateOf(false) }
    var editName by remember(node.id) { mutableStateOf(node.name) }
    var editDescription by remember(node.id) { mutableStateOf(node.description) }
    var editType by remember(node.id) { mutableStateOf(node.type) }
    var editDirection by remember(node.id) { mutableStateOf(node.direction) }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "INSPECTOR",
                fontSize = 11.sp,
                color = ColorEast,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onClose, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A2E)),
            border = BorderStroke(1.dp, getDirectionColor(editDirection).copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (isEditing) {
                    Text(
                        text = "EDIT NODE RELATION",
                        fontSize = 11.sp,
                        color = ColorEast,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Name", color = Color.Gray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ColorEast,
                            unfocusedBorderColor = Color(0xFF23233E),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = editDescription,
                        onValueChange = { editDescription = it },
                        label = { Text("Teachings / Description", color = Color.Gray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ColorEast,
                            unfocusedBorderColor = Color(0xFF23233E),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 4
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(text = "CLASSIFICATION TYPE", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    val typeOptions = listOf("human", "land", "spirit", "ancestor", "future", "knowledge")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(typeOptions) { typeOpt ->
                                val selected = editType == typeOpt
                                FilterChip(
                                    selected = selected,
                                    onClick = { editType = typeOpt },
                                    label = { Text(typeOpt, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = ColorEast.copy(alpha = 0.2f),
                                        selectedLabelColor = ColorEast,
                                        containerColor = Color(0xFF131320),
                                        labelColor = Color.Gray
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(text = "QUADRANT DIRECTION", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    Spacer(modifier = Modifier.height(4.dp))

                    val directions = listOf(
                        "east" to "East 🌸",
                        "south" to "South 🔥",
                        "west" to "West 🌊",
                        "north" to "North ❄️",
                        null to "Center 🌀"
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(directions) { (dirVal, title) ->
                            val selected = editDirection == dirVal
                            FilterChip(
                                selected = selected,
                                onClick = { editDirection = dirVal },
                                label = { Text(title, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = getDirectionColor(dirVal).copy(alpha = 0.2f),
                                    selectedLabelColor = getDirectionColor(dirVal),
                                    containerColor = Color(0xFF131320),
                                    labelColor = Color.Gray
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.updateNodeDetails(node.id, editName, editType, editDirection, editDescription)
                                isEditing = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ColorEast, contentColor = Color(0xFF13131E)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = "Save")
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("SAVE", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                editName = node.name
                                editDescription = node.description
                                editType = node.type
                                editDirection = node.direction
                                isEditing = false
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            border = BorderStroke(1.dp, Color(0xFF23233E)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("CANCEL", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(getDirectionColor(node.direction).copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = getNodeIcon(node.type),
                                contentDescription = "Node type icon",
                                tint = getDirectionColor(node.direction)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = node.name,
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Type: ${node.type.replaceFirstChar { it.uppercase() }}",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = Color(0x33FFFFFF))
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(text = "DESCRIPTION & TEACHINGS", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Text(
                        text = node.description.ifEmpty { "No teachings compiled for this entity yet." },
                        color = Color.White,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(vertical = 4.dp),
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = Color(0x33FFFFFF))
                    Spacer(modifier = Modifier.height(12.dp))

                    // Traditional Alignment Meta Info
                    if (node.direction != null) {
                        Text(text = "DIRECTION SYMBOLS", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                        Row(modifier = Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            val info = when(node.direction) {
                                "east" -> "Waabinong 🌸 Spring (Tobacco)"
                                "south" -> "Zhaawanong 🔥 Summer (Sage/Cedar)"
                                "west" -> "Epangishmok 🌊 Autumn (Cedar)"
                                "north" -> "Kiiwedinong ❄️ Winter (Sweetgrass)"
                                else -> ""
                            }
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = getDirectionColor(node.direction), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = info, color = getDirectionColor(node.direction), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = Color(0x33FFFFFF))
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = { isEditing = true },
                            colors = ButtonDefaults.textButtonColors(contentColor = ColorEast),
                            modifier = Modifier.weight(1.1f)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit details", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("EDIT", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        if (node.xOffset != 0f || node.yOffset != 0f) {
                            TextButton(
                                onClick = { viewModel.resetNodePosition(node.id) },
                                colors = ButtonDefaults.textButtonColors(contentColor = Color.Cyan),
                                modifier = Modifier.weight(1.3f)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = "Align node", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("REALIGN", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        TextButton(
                            onClick = { viewModel.removeNode(node.id) },
                            colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFDC143C)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete node", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("DELETE", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // OCAP Badge
        OcapBadge(nodeType = node.type)

        Spacer(modifier = Modifier.height(16.dp))

        // List of connected relationships (edges)
        Text(
            text = "RELATIONAL EDGES (${nodeEdges.size})",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Gray,
            modifier = Modifier.padding(vertical = 4.dp)
        )

        if (nodeEdges.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF131320))
            ) {
                Box(modifier = Modifier.padding(16.dp), contentAlignment = Alignment.Center) {
                    Text(text = "No relationships linked to other circular nodes yet.", color = Color.DarkGray, fontSize = 12.sp)
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                nodeEdges.forEach { edge ->
                    val currentTargetId = if (edge.fromId == node.id) edge.toId else edge.fromId
                    val targetNode = allNodes.find { it.id == currentTargetId }
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A2F)),
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (edge.ceremonyHonored) ColorEast.copy(alpha = 0.5f) else Color.Transparent
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = edge.relationshipType,
                                    fontWeight = FontWeight.Bold,
                                    color = if (edge.ceremonyHonored) ColorEast else Color.White,
                                    fontSize = 13.sp,
                                    fontFamily = FontFamily.Monospace
                                )

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (edge.ceremonyHonored) Icons.Default.CheckCircle else Icons.Default.Info,
                                        contentDescription = null,
                                        tint = if (edge.ceremonyHonored) ColorEast else Color.Gray,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (edge.ceremonyHonored) "Honored" else "Unceremonied",
                                        fontSize = 11.sp,
                                        color = if (edge.ceremonyHonored) ColorEast else Color.Gray
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Connected to: ${targetNode?.name ?: "Unknown Node"}",
                                fontSize = 12.sp,
                                color = Color.LightGray
                            )
                            Text(
                                text = "Relational strength: ${(edge.strength * 100).toInt()}%",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Button(
                                    onClick = { onConductCeremony(edge) },
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (edge.ceremonyHonored) Color(0x33FFD700) else Color(0x1AFFD700)
                                    ),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text(
                                        text = if (edge.ceremonyHonored) "Honor Again" else "Conduct Ceremony",
                                        fontSize = 10.sp,
                                        color = ColorEast
                                    )
                                }

                                Button(
                                    onClick = { viewModel.removeEdge(edge.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                                    contentPadding = PaddingValues(horizontal = 8.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete Link", tint = Color.Red, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Sever", color = Color.Red, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Big delete node action
        Button(
            onClick = { viewModel.removeNode(node.id) },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0x22DC143C)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ColorSouth)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Dissolve Node from Wheel", color = ColorSouth, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun OcapBadge(nodeType: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF13132B)),
        border = BorderStroke(1.dp, Color(0xFF232353))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = ColorEast,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "OCAP® SOVEREIGN DATA",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontFamily = FontFamily.Monospace
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            val detailText = when (nodeType) {
                "spirit", "ancestor" -> "Sacred restriction: Controlled locally on edge. Fully secure from standard cloud scanning."
                "human" -> "Individual Consent parameters verified. Subject retains strict Possession sovereignty."
                else -> "Edge cache compliant. Under direct local community Ownership and Control permissions."
            }
            Text(
                text = detailText,
                fontSize = 11.sp,
                color = Color.LightGray,
                lineHeight = 15.sp
            )
        }
    }
}

@Composable
fun EmptyInspectorPanel(
    nodeCount: Int,
    edgeCount: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = null,
            tint = Color(0xFF23233E),
            modifier = Modifier.size(56.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "SOVEREIGN WORKSPACE",
            fontSize = 11.sp,
            color = ColorEast,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Select an entity on the circular wheel to inspect or manage its ceremonies, connections, and OCAP® conditions.",
            color = Color.Gray,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp),
            lineHeight = 18.sp
        )

        Spacer(modifier = Modifier.height(32.dp))
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF161625)),
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Local Register Stats",
                    fontSize = 14.sp,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total Wheel Nodes", color = Color.Gray, fontSize = 12.sp)
                    Text("$nodeCount active", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Tended Kin Links", color = Color.Gray, fontSize = 12.sp)
                    Text("$edgeCount mapped", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNodeDialog(
    initialDirection: String? = "east",
    onDismiss: () -> Unit,
    onSave: (name: String, type: String, direction: String?, description: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("human") }
    var direction by remember { mutableStateOf<String?>(initialDirection) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Emanate Local Node", color = Color.White) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Entity Name") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Teaches / Description") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Select Type
                Text("Entity Type:", color = Color.Gray, fontSize = 12.sp)
                val types = listOf("human", "land", "spirit", "ancestor", "future", "knowledge")
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    types.take(3).forEach { t ->
                        val selected = type == t
                        SuggestionChip(
                            onClick = { type = t },
                            label = { Text(t, fontSize = 11.sp, color = if (selected) Color.Black else Color.White) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = if (selected) ColorEast else Color(0xFF23233E)
                            )
                        )
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    types.drop(3).forEach { t ->
                        val selected = type == t
                        SuggestionChip(
                            onClick = { type = t },
                            label = { Text(t, fontSize = 11.sp, color = if (selected) Color.Black else Color.White) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = if (selected) ColorEast else Color(0xFF23233E)
                            )
                        )
                    }
                }

                // Select Direction
                Text("Traditional Quadrant:", color = Color.Gray, fontSize = 12.sp)
                val dirs = listOf("east", "south", "west", "north", null)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    dirs.forEach { d ->
                        val label = d ?: "center"
                        val selected = direction == d
                        val dirColor = d?.let { getDirectionColor(it) } ?: Color.Gray
                        SuggestionChip(
                            onClick = { direction = d },
                            label = { Text(label, fontSize = 11.sp, color = if (selected) Color.Black else Color.White) },
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
                onClick = { if (name.isNotEmpty()) onSave(name, type, direction, description) },
                colors = ButtonDefaults.buttonColors(containerColor = ColorEast, contentColor = Color.Black)
            ) {
                Text("Emanate Entity")
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
fun LinkNodesDialog(
    nodes: List<RelationalNode>,
    initialFromId: String? = null,
    initialToId: String? = null,
    onDismiss: () -> Unit,
    onLink: (fromId: String, toId: String, type: String, strength: Float) -> Unit
) {
    if (nodes.size < 2) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Insufficient Nodes", color = Color.White) },
            text = { Text("You must create at least two entities on the map before linking them together.", color = Color.LightGray) },
            confirmButton = { Button(onClick = onDismiss) { Text("OK") } },
            containerColor = Color(0xFF1E1E2E)
        )
        return
    }

    var fromIndex by remember { 
        val findIdx = nodes.indexOfFirst { it.id == initialFromId }
        mutableIntStateOf(if (findIdx != -1) findIdx else 0) 
    }
    var toIndex by remember { 
        val findIdx = nodes.indexOfFirst { it.id == initialToId }
        mutableIntStateOf(if (findIdx != -1) findIdx else if (fromIndex == 0) 1 else 0) 
    }
    var relationshipType by remember { mutableStateOf("SERVES") }
    var strength by remember { mutableFloatStateOf(0.85f) }

    val relTypes = listOf("STEWARDS", "BORN_FROM", "SERVES", "GIVES_BACK_TO", "ALIGNED_WITH", "KINSHIP_OF")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tend Relational Link", color = Color.White) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // From select
                Text("Source Entity:", color = Color.Gray, fontSize = 12.sp)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(nodes.size) { idx ->
                        val selected = fromIndex == idx
                        SuggestionChip(
                            onClick = { fromIndex = idx },
                            label = { Text(nodes[idx].name, color = if (selected) Color.Black else Color.White) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = if (selected) ColorEast else Color(0xFF23233E)
                            )
                        )
                    }
                }

                // To select
                Text("Target Entity:", color = Color.Gray, fontSize = 12.sp)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(nodes.size) { idx ->
                        val selected = toIndex == idx
                        SuggestionChip(
                            onClick = { toIndex = idx },
                            label = { Text(nodes[idx].name, color = if (selected) Color.Black else Color.White) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = if (selected) Color(0xFF6B4C9A) else Color(0xFF23233E)
                            )
                        )
                    }
                }

                // Relationship Type select
                Text("Relationship Type:", color = Color.Gray, fontSize = 12.sp)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(relTypes) { rel ->
                        val selected = relationshipType == rel
                        SuggestionChip(
                            onClick = { relationshipType = rel },
                            label = { Text(rel, color = if (selected) Color.Black else Color.White, fontFamily = FontFamily.Monospace) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = if (selected) ColorEast else Color(0xFF23233E)
                            )
                        )
                    }
                }

                // Target Strength Slider
                Text("Connection Strength: ${(strength * 100).toInt()}%", color = Color.Gray, fontSize = 12.sp)
                Slider(
                    value = strength,
                    onValueChange = { strength = it },
                    valueRange = 0.1f..1.0f,
                    colors = SliderDefaults.colors(thumbColor = ColorEast, activeTrackColor = ColorEast)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    if (fromIndex != toIndex) {
                        onLink(nodes[fromIndex].id, nodes[toIndex].id, relationshipType, strength)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ColorEast, contentColor = Color.Black)
            ) {
                Text("Weave Edge")
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

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ConductCeremonyDialog(
    edge: RelationalEdge,
    allNodes: List<RelationalNode>,
    onDismiss: () -> Unit,
    onConduct: (
        type: String,
        direction: String,
        participants: String,
        medicines: String,
        intentions: String,
        context: String,
        ceremonyId: String
    ) -> Unit
) {
    val sourceNode = remember(edge.fromId, allNodes) { allNodes.find { it.id == edge.fromId } }
    val targetNode = remember(edge.toId, allNodes) { allNodes.find { it.id == edge.toId } }

    var ceremonyType by remember { mutableStateOf("Talking Circle") }
    var participants by remember { mutableStateOf(listOfNotNull(sourceNode?.name, targetNode?.name).joinToString(", ")) }
    var intentions by remember { 
        mutableStateOf(
            "To honor and kindle reciprocity between ${sourceNode?.name ?: "relative"} and ${targetNode?.name ?: "relative"} under standard OCAP guidelines."
        ) 
    }
    
    val availableMedicines = listOf("tobacco", "sage", "sweetgrass", "cedar", "strawberry")
    var selectedMedicines by remember { mutableStateOf(setOf("tobacco")) }
    
    var researchContext by remember { mutableStateOf("Kinship Alignment Check") }
    val targetDir = targetNode?.direction ?: "east"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Star, "Ceremony icon", tint = ColorEast, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Conduct Reciprocity Ceremony",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "By honoring the relationship thread \"${edge.relationshipType}\", you validate sovereign accountability (Wilson Three R's) and elevate the general alignment index.",
                    color = Color.LightGray,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )

                HorizontalDivider(color = Color(0x33FFFFFF))

                Text("Ceremony Type:", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                val ceremonyTypes = listOf("Talking Circle", "Smudging", "Spirit Feeding", "Sunrise Offering")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        items(ceremonyTypes) { type ->
                            val selected = ceremonyType == type
                            SuggestionChip(
                                onClick = { ceremonyType = type },
                                label = { Text(type, fontSize = 10.sp, color = if (selected) Color.Black else Color.White) },
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = if (selected) ColorEast else Color(0xFF23233E)
                                )
                            )
                        }
                    }
                }

                Text("Select Medicine Offerings:", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(availableMedicines) { med ->
                        val selected = selectedMedicines.contains(med)
                        val chipColor = when(med) {
                            "tobacco" -> ColorEast
                            "sage" -> ColorSouth
                            "cedar" -> ColorWest
                            "sweetgrass" -> ColorNorth
                            else -> Color(0xFFE2583E)
                        }
                        FilterChip(
                            selected = selected,
                            onClick = {
                                if (selected) {
                                    selectedMedicines = selectedMedicines - med
                                } else {
                                    selectedMedicines = selectedMedicines + med
                                }
                            },
                            label = { Text(med.replaceFirstChar { it.uppercase() }, fontSize = 10.sp, color = if (selected) Color.Black else Color.White) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = chipColor,
                                containerColor = Color(0xFF23233E)
                            )
                        )
                    }
                }

                OutlinedTextField(
                    value = participants,
                    onValueChange = { participants = it },
                    label = { Text("Ceremonial Participants", fontSize = 12.sp) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = intentions,
                    onValueChange = { intentions = it },
                    label = { Text("Traditional Intentions", fontSize = 12.sp) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                OutlinedTextField(
                    value = researchContext,
                    onValueChange = { researchContext = it },
                    label = { Text("Research Context Study", fontSize = 12.sp) },
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
                onClick = {
                    val ceremonyId = "ceremony_${System.currentTimeMillis()}"
                    onConduct(
                        ceremonyType.lowercase().replace(" ", "_"),
                        targetDir,
                        participants,
                        selectedMedicines.joinToString(", "),
                        intentions,
                        researchContext,
                        ceremonyId
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = ColorEast, contentColor = Color.Black)
            ) {
                Text("Consecrate Kinship Bond", fontWeight = FontWeight.Bold)
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
