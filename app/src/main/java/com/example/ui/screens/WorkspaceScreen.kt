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
import com.example.data.RelationalEdge
import com.example.data.RelationalNode
import com.example.viewmodel.MedicineWheelViewModel
import kotlin.math.cos
import kotlin.math.sin

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
    var showLinkNodesDialog by remember { mutableStateOf(false) }

    val selectedNode = remember(selectedNodeId, nodes) {
        nodes.find { it.id == selectedNodeId }
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
                            IconButton(
                                onClick = { showAddNodeDialog = true },
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

                // Interactive Wheel item
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(340.dp)
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
                            val radiusPx = (widthPx.coerceAtMost(heightPx) / 2f) * 0.65f
                            
                            val nodePositions = remember(nodes, widthPx, heightPx, radiusPx) {
                                calculateNodeLayout(nodes, widthPx / 2f, heightPx / 2f, radiusPx)
                            }

                            // Edges Canvas
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                edges.forEach { edge ->
                                    val startPos = nodePositions[edge.fromId]
                                    val endPos = nodePositions[edge.toId]
                                    if (startPos != null && endPos != null) {
                                        val lineColor = if (edge.ceremonyHonored) ColorEast else Color(0x3DFFFFFF)
                                        val strokeWidth = if (edge.ceremonyHonored) 4e-1f * density * 8f else 1.5f * density
                                        drawLine(
                                            color = lineColor,
                                            start = startPos,
                                            end = endPos,
                                            strokeWidth = strokeWidth
                                        )
                                    }
                                }
                            }

                            // Nodes
                            nodePositions.forEach { (nodeId, offset) ->
                                val node = nodes.find { it.id == nodeId }
                                if (node != null) {
                                    val isSelected = node.id == selectedNodeId
                                    val nodeColor = getDirectionColor(node.direction)
                                    val dpOffset = with(LocalDensity.current) {
                                        IntOffset(
                                            x = (offset.x - 22.dp.toPx()).toInt(),
                                            y = (offset.y - 22.dp.toPx()).toInt()
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .absoluteOffset(
                                                x = with(LocalDensity.current) { dpOffset.x.toDp() },
                                                y = with(LocalDensity.current) { dpOffset.y.toDp() }
                                            )
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(if (isSelected) Color.White else Color(0xFF1F1F2F))
                                            .border(
                                                width = if (isSelected) 3.dp else 2.dp,
                                                color = nodeColor,
                                                shape = CircleShape
                                            )
                                            .clickable { viewModel.selectNode(node.id) },
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
                    }
                }

                // Inspector card item (flows downward scrollably)
                item {
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
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left Column or primary block: the actual interactive wheel representation
                Box(
                    modifier = Modifier
                        .weight(1.2f)
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

                            Row {
                                IconButton(
                                    onClick = { showAddNodeDialog = true },
                                    modifier = Modifier.background(Color(0xFF1E1E2A), CircleShape).size(38.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Add Node", tint = Color.Green)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(
                                    onClick = { showLinkNodesDialog = true },
                                    modifier = Modifier.background(Color(0xFF1E1E2A), CircleShape).size(38.dp)
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = "Link Nodes", tint = ColorEast)
                                }
                            }
                        }

                        // Interactive Circle Box
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(24.dp))
                                .background(Color(0xFF13131E))
                                .border(1.dp, Color(0xFF23233E), RoundedCornerShape(24.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            // 1. Draw quadrants, axes and legend details on Canvas
                            MedicineWheelBackgroundCanvas()

                            // 2. Render Graph: We'll compute layouts reactive to box scale.
                            BoxWithConstraints(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                val widthPx = with(LocalDensity.current) { maxWidth.toPx() }
                                val heightPx = with(LocalDensity.current) { maxHeight.toPx() }
                                val radiusPx = (widthPx.coerceAtMost(heightPx) / 2f) * 0.65f
                                
                                // Compute positions for active nodes
                                val nodePositions = remember(nodes, widthPx, heightPx, radiusPx) {
                                    calculateNodeLayout(nodes, widthPx / 2f, heightPx / 2f, radiusPx)
                                }

                                // Draw lines for edges first (behind nodes)
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    edges.forEach { edge ->
                                        val startPos = nodePositions[edge.fromId]
                                        val endPos = nodePositions[edge.toId]
                                        if (startPos != null && endPos != null) {
                                            val lineColor = if (edge.ceremonyHonored) ColorEast else Color(0x3DFFFFFF)
                                            val strokeWidth = if (edge.ceremonyHonored) 4e-1f * density * 8f else 1.5f * density
                                            
                                            // Draw connection curve or line
                                            drawLine(
                                                color = lineColor,
                                                start = startPos,
                                                end = endPos,
                                                strokeWidth = strokeWidth
                                            )
                                        }
                                    }
                                }

                                // Render clickable nodes on top of links
                                nodePositions.forEach { (nodeId, offset) ->
                                    val node = nodes.find { it.id == nodeId }
                                    if (node != null) {
                                        val isSelected = node.id == selectedNodeId
                                        val nodeColor = getDirectionColor(node.direction)
                                        val dpOffset = with(LocalDensity.current) {
                                            IntOffset(
                                                x = (offset.x - 22.dp.toPx()).toInt(),
                                                y = (offset.y - 22.dp.toPx()).toInt()
                                            )
                                        }

                                        Box(
                                            modifier = Modifier
                                                .absoluteOffset(
                                                    x = with(LocalDensity.current) { dpOffset.x.toDp() },
                                                    y = with(LocalDensity.current) { dpOffset.y.toDp() }
                                                )
                                                .size(44.dp)
                                                .clip(CircleShape)
                                                .background(if (isSelected) Color.White else Color(0xFF1F1F2F))
                                                .border(
                                                    width = if (isSelected) 3.dp else 2.dp,
                                                    color = nodeColor,
                                                    shape = CircleShape
                                                )
                                                .clickable { viewModel.selectNode(node.id) },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = getNodeIcon(node.type),
                                                contentDescription = node.name,
                                                tint = if (isSelected) Color(0xFF13131E) else Color.White,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }

                                        // Text label positioned slightly below the node
                                        val labelOffset = with(LocalDensity.current) {
                                            IntOffset(
                                                x = (offset.x - 50.dp.toPx()).toInt(),
                                                y = (offset.y + 26.dp.toPx()).toInt()
                                            )
                                        }
                                        Box(
                                            modifier = Modifier
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
                                                fontSize = 10.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                textAlign = TextAlign.Center,
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Right Column: Node Inspector & Obligation audits
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

    // Dialogs
    if (showAddNodeDialog) {
        AddNodeDialog(
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
            onDismiss = { showLinkNodesDialog = false },
            onLink = { fromId, toId, type, strength ->
                viewModel.linkNodes(fromId, toId, type, strength)
                showLinkNodesDialog = false
            }
        )
    }
}

@Composable
fun MedicineWheelBackgroundCanvas() {
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
    onClose: () -> Unit
) {
    val nodeEdges = remember(node.id, edges) {
        edges.filter { it.fromId == node.id || it.toId == node.id }
    }

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
            border = BorderStroke(1.dp, getDirectionColor(node.direction).copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
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
                                    onClick = { viewModel.honorEdgeWithCeremony(edge.id, "ceremony_custom") },
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
    onDismiss: () -> Unit,
    onSave: (name: String, type: String, direction: String?, description: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("human") }
    var direction by remember { mutableStateOf<String?>("east") }

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

    var fromIndex by remember { mutableIntStateOf(0) }
    var toIndex by remember { mutableIntStateOf(1) }
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
