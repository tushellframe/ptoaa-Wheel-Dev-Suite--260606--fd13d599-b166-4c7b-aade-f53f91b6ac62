package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.MedicineWheelViewModel

interface SpecItem {
    val id: String
    val domain: String
    val title: String
    val icon: androidx.compose.ui.graphics.vector.ImageVector
    val ojibweName: String
    val status: String // "Implemented" , "Active Draft" , "Gap / Future"
    val content: String
    val diagramType: String // "wheel", "flow", "layers", "mcp", "sovereignty"
    val aspects: List<SpecAspect>
}

data class SpecAspect(
    val title: String,
    val ojibweName: String,
    val status: String, // "Implemented", "Active Draft", "Conceptual Gap"
    val description: String,
    val schemaCode: String = "" // Collapsible structured format
)

class StaticSpecItem(
    override val id: String,
    override val domain: String,
    override val title: String,
    override val icon: androidx.compose.ui.graphics.vector.ImageVector,
    override val ojibweName: String,
    override val status: String,
    override val content: String,
    override val diagramType: String,
    override val aspects: List<SpecAspect>
) : SpecItem

@Composable
private fun ComplianceAuditCard(
    isCompliant: Boolean,
    complianceScore: Int,
    scoreMessage: String,
    unalignedNodesCount: Int,
    unhonoredEdgesCount: Int
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131320)),
        border = BorderStroke(1.dp, if (isCompliant) Color(0xFF33B3A6).copy(alpha = 0.5f) else ColorSouth.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TRADITIONAL CO-COORDINATE AUDIT",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    fontFamily = FontFamily.Monospace
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isCompliant) Color(0xFF33B3A6).copy(alpha = 0.2f) else ColorSouth.copy(alpha = 0.2f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (isCompliant) "SECURE" else "GAP STACKED",
                        color = if (isCompliant) Color(0xFF33B3A6) else ColorSouth,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "$complianceScore%",
                    color = if (isCompliant) Color(0xFF33B3A6) else ColorEast,
                    fontWeight = FontWeight.Black,
                    fontSize = 24.sp,
                    fontFamily = FontFamily.Monospace
                )
                Column {
                    Text(
                        text = "Sovereignty Compliance Index",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp
                    )
                    Text(
                        text = scoreMessage,
                        color = Color.LightGray,
                        fontSize = 10.sp,
                        lineHeight = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF23233E))
            ) {
                val compPart = complianceScore / 100f
                Box(modifier = Modifier.fillMaxHeight().weight(compPart.coerceAtLeast(0.01f)).background(if (isCompliant) Color(0xFF33B3A6) else ColorEast))
                if (compPart < 1f) {
                    Box(modifier = Modifier.fillMaxHeight().weight((1f - compPart).coerceAtLeast(0.01f)).background(ColorSouth))
                }
            }

            if (!isCompliant) {
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(ColorSouth.copy(alpha = 0.1f))
                        .border(1.dp, ColorSouth.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                        .padding(8.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = ColorSouth, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("RELATIONAL OFFENSE RECTIFICATION REQUIRED", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = ColorSouth, fontFamily = FontFamily.Monospace)
                        }
                        if (unalignedNodesCount > 0) {
                            Text("• Align $unalignedNodesCount space-less nodes with their Traditional Direction in Workspace.", fontSize = 9.sp, color = Color.LightGray)
                        }
                        if (unhonoredEdgesCount > 0) {
                            Text("• Conduct Smudging/Talking Circle on $unhonoredEdgesCount relations to honor kinship.", fontSize = 9.sp, color = Color.LightGray)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DocumentationScreen(viewModel: MedicineWheelViewModel, modifier: Modifier = Modifier) {
    val specs = remember { getSpecData() }
    var selectedSpecIndex by remember { mutableStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    
    // Live db states for protocol auditing
    val allNodes by viewModel.nodes.collectAsState()
    val allEdges by viewModel.edges.collectAsState()
    val allCycles by viewModel.cycles.collectAsState()

    // Calculate dynamic traditional validation score based on live Room DB data
    val unalignedNodes = allNodes.filter { it.direction.isNullOrBlank() }
    val unhonoredEdges = allEdges.filter { !it.ceremonyHonored }
    val totalElements = allNodes.size + allEdges.size
    
    val (complianceScore, isCompliant, scoreMessage) = remember(allNodes, allEdges) {
        if (totalElements == 0) {
            Triple(100, true, "Empty cache - fully compliant with standard settings.")
        } else {
            val failedCount = unalignedNodes.size + unhonoredEdges.size
            val score = (((totalElements - failedCount).toFloat() / totalElements.toFloat()) * 100f).toInt().coerceIn(0, 100)
            val pass = unalignedNodes.isEmpty() && unhonoredEdges.isEmpty()
            val msg = if (pass) {
                "Sovereignty verified. 100% compliant with spatial and ceremonial directives."
            } else {
                "${unalignedNodes.size} nodes unaligned. ${unhonoredEdges.size} relations unhonored."
            }
            Triple(score, pass, msg)
        }
    }

    // Categories available
    val categories = listOf("All", "Core Ontology", "Spatial Graphics", "Ceremy Protocols", "Sovereignty", "MCP Interface")

    // Filter specs by query and category
    val filteredSpecs = specs.filter { spec ->
        val matchesQuery = spec.title.contains(searchQuery, ignoreCase = true) || 
                           spec.ojibweName.contains(searchQuery, ignoreCase = true) || 
                           spec.content.contains(searchQuery, ignoreCase = true) ||
                           spec.aspects.any { it.title.contains(searchQuery, ignoreCase = true) || it.description.contains(searchQuery, ignoreCase = true) }
        
        val matchesCat = selectedCategory == "All" || spec.domain == selectedCategory
        matchesQuery && matchesCat
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
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = "TRADITIONAL PROTOCOL SPECIFICATIONS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorEast,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }

                item {
                    ComplianceAuditCard(
                        isCompliant = isCompliant,
                        complianceScore = complianceScore,
                        scoreMessage = scoreMessage,
                        unalignedNodesCount = unalignedNodes.size,
                        unhonoredEdgesCount = unhonoredEdges.size
                    )
                }

                item {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search specifications / schemas...", fontSize = 12.sp, color = Color.Gray) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.LightGray,
                            focusedContainerColor = Color(0xFF131320),
                            unfocusedContainerColor = Color(0xFF131320),
                            focusedBorderColor = ColorEast,
                            unfocusedBorderColor = Color(0xFF23233E)
                        ),
                        textStyle = LocalTextStyle.current.copy(fontSize = 12.sp)
                    )
                }

                item {
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(categories) { cat ->
                            val active = selectedCategory == cat
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(30.dp))
                                    .background(if (active) ColorEast else Color(0xFF1E1E2E))
                                    .clickable { selectedCategory = cat }
                                    .border(1.dp, if (active) ColorEast else Color(0xFF23233E), RoundedCornerShape(30.dp))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = cat,
                                    fontSize = 10.sp,
                                    color = if (active) Color.Black else Color.LightGray,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                if (filteredSpecs.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = Color.DarkGray, modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No specs match search terms.", color = Color.Gray, fontSize = 12.sp)
                        }
                    }
                } else {
                    itemsIndexed(filteredSpecs) { idx, spec ->
                        val originalIndex = specs.indexOfFirst { it.id == spec.id }
                        val isExpanded = originalIndex == selectedSpecIndex

                        Card(
                            modifier = Modifier
                                .fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isExpanded) Color(0xFF1E1E2E) else Color(0xFF13131F)
                            ),
                            border = BorderStroke(
                                width = 1.dp,
                                color = if (isExpanded) ColorEast else Color(0xFF23233E)
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedSpecIndex = if (isExpanded) -1 else originalIndex },
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(CircleShape)
                                                .background(if (isExpanded) ColorEast.copy(alpha = 0.2f) else Color(0xFF23233E)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = spec.icon,
                                                contentDescription = null,
                                                tint = if (isExpanded) ColorEast else Color.Gray,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = spec.title,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isExpanded) Color.White else Color.LightGray
                                            )
                                            Text(
                                                text = "${spec.domain} • ${spec.ojibweName}",
                                                fontSize = 10.sp,
                                                color = Color.Gray
                                            )
                                        }
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        val badgeColor = when (spec.status) {
                                            "Implemented" -> Color(0xFF33B3A6)
                                            "Active Draft" -> ColorEast
                                            else -> ColorSouth
                                        }
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(badgeColor.copy(alpha = 0.15f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = spec.status,
                                                color = badgeColor,
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Icon(
                                            imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                            contentDescription = null,
                                            tint = Color.Gray,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                AnimatedVisibility(visible = isExpanded) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 12.dp)
                                    ) {
                                        HorizontalDivider(color = Color(0xFF1E1E34), modifier = Modifier.padding(bottom = 12.dp))
                                        
                                        if (spec.diagramType != "none") {
                                            Text(
                                                text = "COSMOLOGICAL CANVAS DIAGRAM",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.Gray,
                                                fontFamily = FontFamily.Monospace,
                                                modifier = Modifier.padding(bottom = 6.dp)
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(140.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(Color(0xFF0F0F15))
                                                    .border(1.dp, Color(0xFF23233E), RoundedCornerShape(8.dp)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                SpecDiagramRenderer(diagramType = spec.diagramType)
                                            }
                                            Spacer(modifier = Modifier.height(12.dp))
                                        }

                                        Text(
                                            text = "DESIGN PROSE & LOGICAL BLUEPRINT",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Gray,
                                            fontFamily = FontFamily.Monospace,
                                            modifier = Modifier.padding(bottom = 4.dp)
                                        )
                                        Text(
                                            text = spec.content,
                                            color = Color.LightGray,
                                            fontSize = 12.sp,
                                            lineHeight = 16.sp,
                                            modifier = Modifier.padding(bottom = 12.dp)
                                        )

                                        if (spec.aspects.isNotEmpty()) {
                                            HorizontalDivider(color = Color(0xFF1E1E34), modifier = Modifier.padding(bottom = 10.dp))
                                            Text(
                                                text = "SUB-DOMAINS & INTERFACE FUNCTIONAL SCHEMAS",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = ColorEast,
                                                fontFamily = FontFamily.Monospace,
                                                modifier = Modifier.padding(bottom = 6.dp)
                                            )

                                            Column(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                spec.aspects.forEachIndexed { aIdx, aspect ->
                                                    var aspectExpanded by remember { mutableStateOf(false) }
                                                    Card(
                                                        colors = CardDefaults.cardColors(containerColor = Color(0xFF191928)),
                                                        border = BorderStroke(1.dp, if (aspectExpanded) ColorEast.copy(alpha = 0.4f) else Color(0xFF23233E)),
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .clickable { aspectExpanded = !aspectExpanded }
                                                    ) {
                                                        Column(modifier = Modifier.padding(10.dp)) {
                                                            Row(
                                                                modifier = Modifier.fillMaxWidth(),
                                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                                verticalAlignment = Alignment.CenterVertically
                                                            ) {
                                                                Column {
                                                                    Text(
                                                                        text = "${originalIndex + 1}.${aIdx + 1} ${aspect.title}",
                                                                        fontSize = 12.sp,
                                                                        fontWeight = FontWeight.Bold,
                                                                        color = Color.White
                                                                    )
                                                                    Text(
                                                                        text = "Ojibwe: ${aspect.ojibweName}",
                                                                        fontSize = 10.sp,
                                                                        color = Color.Gray
                                                                    )
                                                                }

                                                                Row(
                                                                    verticalAlignment = Alignment.CenterVertically,
                                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                                ) {
                                                                    val aBadgeColor = when (aspect.status) {
                                                                        "Implemented" -> Color(0xFF33B3A6)
                                                                        "Active Draft" -> ColorEast
                                                                        else -> Color(0xFFFF3B30)
                                                                    }
                                                                    Text(
                                                                        text = aspect.status,
                                                                        color = aBadgeColor,
                                                                        fontSize = 8.sp,
                                                                        fontWeight = FontWeight.Bold,
                                                                        modifier = Modifier
                                                                            .clip(RoundedCornerShape(4.dp))
                                                                            .background(aBadgeColor.copy(alpha = 0.15f))
                                                                            .padding(horizontal = 4.dp, vertical = 1.dp)
                                                                    )
                                                                    Icon(
                                                                        imageVector = if (aspectExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                                                        contentDescription = null,
                                                                        tint = Color.Gray,
                                                                        modifier = Modifier.size(16.dp)
                                                                    )
                                                                }
                                                            }

                                                            AnimatedVisibility(visible = aspectExpanded) {
                                                                Column(modifier = Modifier.padding(top = 8.dp)) {
                                                                    Text(
                                                                        text = aspect.description,
                                                                        fontSize = 11.sp,
                                                                        color = Color.LightGray,
                                                                        lineHeight = 15.sp,
                                                                        modifier = Modifier.padding(bottom = 8.dp)
                                                                    )

                                                                    if (aspect.schemaCode.isNotBlank()) {
                                                                        Text(
                                                                            text = "STRUCTURED SCHEMA DEFINITION:",
                                                                            fontSize = 8.sp,
                                                                            fontWeight = FontWeight.Bold,
                                                                            color = Color.Gray,
                                                                            fontFamily = FontFamily.Monospace,
                                                                            modifier = Modifier.padding(bottom = 2.dp)
                                                                        )
                                                                        HighlightedCodeView(code = aspect.schemaCode)
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
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
                // LEFT COLUMN: Navigation list with compliance metrics, search filters
                Column(
                    modifier = Modifier
                        .weight(1.0f)
                        .fillMaxHeight()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "TRADITIONAL PROTOCOL SPECIFICATIONS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorEast,
                        fontFamily = FontFamily.Monospace
                    )

                    ComplianceAuditCard(
                        isCompliant = isCompliant,
                        complianceScore = complianceScore,
                        scoreMessage = scoreMessage,
                        unalignedNodesCount = unalignedNodes.size,
                        unhonoredEdgesCount = unhonoredEdges.size
                    )

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search specifications / schemas...", fontSize = 12.sp, color = Color.Gray) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.LightGray,
                            focusedContainerColor = Color(0xFF131320),
                            unfocusedContainerColor = Color(0xFF131320),
                            focusedBorderColor = ColorEast,
                            unfocusedBorderColor = Color(0xFF23233E)
                        ),
                        textStyle = LocalTextStyle.current.copy(fontSize = 12.sp)
                    )

                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(categories) { cat ->
                            val active = selectedCategory == cat
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(30.dp))
                                    .background(if (active) ColorEast else Color(0xFF1E1E2E))
                                    .clickable { selectedCategory = cat }
                                    .border(1.dp, if (active) ColorEast else Color(0xFF23233E), RoundedCornerShape(30.dp))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = cat,
                                    fontSize = 10.sp,
                                    color = if (active) Color.Black else Color.LightGray,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (filteredSpecs.isEmpty()) {
                            item {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 40.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color.DarkGray, modifier = Modifier.size(36.dp))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("No specs match search terms.", color = Color.Gray, fontSize = 12.sp)
                                }
                            }
                        } else {
                            itemsIndexed(specs) { idx, spec ->
                                val belongsFilter = selectedCategory == "All" || spec.domain == selectedCategory
                                if (belongsFilter) {
                                    val active = idx == selectedSpecIndex
                                    val matchesSearchHighlight = searchQuery.isNotBlank() && spec.title.contains(searchQuery, ignoreCase = true)
                                    
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { selectedSpecIndex = idx },
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (active) Color(0xFF1E1E2E) else Color(0xFF13131F)
                                        ),
                                        border = BorderStroke(
                                            width = 1.dp,
                                            color = if (active) ColorEast else if (matchesSearchHighlight) ColorEast.copy(alpha = 0.5f) else Color(0xFF23233E)
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(
                                                modifier = Modifier.weight(1f),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(28.dp)
                                                        .clip(CircleShape)
                                                        .background(if (active) ColorEast.copy(alpha = 0.2f) else Color(0xFF23233E)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = spec.icon,
                                                        contentDescription = null,
                                                        tint = if (active) ColorEast else Color.Gray,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Column {
                                                    Text(
                                                        text = spec.title,
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (active) Color.White else Color.LightGray
                                                    )
                                                    Text(
                                                        text = "${spec.domain} • ${spec.ojibweName}",
                                                        fontSize = 10.sp,
                                                        color = Color.Gray
                                                    )
                                                }
                                            }

                                            val badgeColor = when (spec.status) {
                                                "Implemented" -> Color(0xFF33B3A6)
                                                "Active Draft" -> ColorEast
                                                else -> ColorSouth
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(badgeColor.copy(alpha = 0.15f))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = spec.status,
                                                    color = badgeColor,
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // RIGHT COLUMN: Detailed Specification Reader & Aspect / Class Code Collapser
                Box(
                    modifier = Modifier
                        .weight(1.2f)
                        .fillMaxHeight()
                        .background(Color(0xFF131320))
                        .border(BorderStroke(1.dp, Color(0xFF1E1E34)))
                        .padding(16.dp)
                ) {
                    if (selectedSpecIndex in specs.indices) {
                        val spec = specs[selectedSpecIndex]
                        
                        Column(modifier = Modifier.fillMaxSize()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "SPECIFICATION EXPLORER ENGINE",
                                    fontSize = 10.sp,
                                    color = ColorEast,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFF33B3A6)))
                                    Text(text = "DYNAMIC", fontSize = 9.sp, color = Color.Gray, fontFamily = FontFamily.Monospace)
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = spec.title,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Traditional Dimension: ${spec.ojibweName}",
                                    fontSize = 12.sp,
                                    color = ColorEast,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "|  Domain: ${spec.domain}",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = Color(0xFF1E1E34))
                            Spacer(modifier = Modifier.height(10.dp))

                            if (spec.diagramType != "none") {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "COSMOLOGICAL VECTOR GRAPHICS ARCHITECTURE",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Gray,
                                        fontFamily = FontFamily.Monospace,
                                        modifier = Modifier.padding(bottom = 6.dp)
                                    )
                                    Text(
                                        text = "SVG RENDER",
                                        fontSize = 8.sp,
                                        color = ColorEast,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(130.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF0F0F15))
                                        .border(1.dp, Color(0xFF23233E), RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    SpecDiagramRenderer(diagramType = spec.diagramType)
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                            }

                            Text(
                                text = "DESIGN PROSE & LOGICAL BLUEPRINT",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )

                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                item {
                                    Text(
                                        text = spec.content,
                                        color = Color.LightGray,
                                        fontSize = 12.sp,
                                        lineHeight = 16.sp
                                    )
                                }

                                item {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    HorizontalDivider(color = Color(0xFF1E1E34))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "SUB-DOMAINS & INTERFACE FUNCTIONAL SCHEMAS",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ColorEast,
                                        fontFamily = FontFamily.Monospace,
                                        modifier = Modifier.padding(bottom = 6.dp)
                                    )
                                }

                                itemsIndexed(spec.aspects) { aIdx, aspect ->
                                    var expanded by remember { mutableStateOf(false) }
                                    
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFF191928)),
                                        border = BorderStroke(1.dp, if (expanded) ColorEast.copy(alpha = 0.4f) else Color(0xFF23233E)),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { expanded = !expanded }
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column {
                                                    Text(
                                                        text = "${selectedSpecIndex + 1}.${aIdx + 1} ${aspect.title}",
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color.White
                                                    )
                                                    Text(
                                                        text = "Ojibwe: ${aspect.ojibweName}",
                                                        fontSize = 10.sp,
                                                        color = Color.Gray
                                                    )
                                                }

                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    val aBadgeColor = when (aspect.status) {
                                                        "Implemented" -> Color(0xFF33B3A6)
                                                        "Active Draft" -> ColorEast
                                                        else -> Color(0xFFFF3B30)
                                                    }
                                                    Text(
                                                        text = aspect.status,
                                                        color = aBadgeColor,
                                                        fontSize = 8.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(4.dp))
                                                            .background(aBadgeColor.copy(alpha = 0.15f))
                                                            .padding(horizontal = 4.dp, vertical = 1.dp)
                                                    )
                                                    Icon(
                                                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                                        contentDescription = null,
                                                        tint = Color.Gray,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }

                                            AnimatedVisibility(visible = expanded) {
                                                Column(modifier = Modifier.padding(top = 8.dp)) {
                                                    Text(
                                                        text = aspect.description,
                                                        fontSize = 11.sp,
                                                        color = Color.LightGray,
                                                        lineHeight = 15.sp,
                                                        modifier = Modifier.padding(bottom = 8.dp)
                                                    )

                                                    if (aspect.schemaCode.isNotBlank()) {
                                                        Text(
                                                            text = "STRUCTURED SCHEMA DEFINITION:",
                                                            fontSize = 8.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = Color.Gray,
                                                            fontFamily = FontFamily.Monospace,
                                                            modifier = Modifier.padding(bottom = 2.dp)
                                                        )
                                                        HighlightedCodeView(code = aspect.schemaCode)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HighlightedCodeView(code: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F0F15)),
        border = BorderStroke(1.dp, Color(0xFF23233E)),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(8.dp)
                .horizontalScroll(rememberScrollState())
        ) {
            val lines = code.trimIndent().split("\n")
            lines.forEach { line ->
                Text(
                    text = line,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Color.LightGray,
                    lineHeight = 13.sp
                )
            }
        }
    }
}

@Composable
fun SpecDiagramRenderer(diagramType: String) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        
        when (diagramType) {
            "wheel" -> {
                val cxFloat: Float = cx
                val cyFloat: Float = cy
                val r45: Float = 40.dp.toPx()
                val d50: Float = 46.dp.toPx()
                val d35: Float = 32.dp.toPx()
                val r5: Float = 5.dp.toPx()

                // Drawing circular wheel quadrants
                drawCircle(color = Color(0xFF191928), radius = r45, center = Offset(cxFloat, cyFloat))
                drawCircle(color = ColorEast, radius = r45, center = Offset(cxFloat, cyFloat), style = Stroke(width = 2f))
                
                // Cross divisions as axis
                drawLine(Color(0x33FFFFFF), Offset(cxFloat - d50, cyFloat), Offset(cxFloat + d50, cyFloat), strokeWidth = 2f)
                drawLine(Color(0x33FFFFFF), Offset(cxFloat, cyFloat - d50), Offset(cxFloat, cyFloat + d50), strokeWidth = 2f)
                
                // Direction indicators dot
                drawCircle(ColorEast, radius = r5, center = Offset(cxFloat + d35, cyFloat)) // East
                drawCircle(ColorSouth, radius = r5, center = Offset(cxFloat, cyFloat + d35)) // South
                drawCircle(ColorWest, radius = r5, center = Offset(cxFloat - d35, cyFloat)) // West
                drawCircle(ColorNorth, radius = r5, center = Offset(cxFloat, cyFloat - d35)) // North
            }
            "layers" -> {
                val r80: Float = 60.dp.toPx()
                val sizeW: Float = 120.dp.toPx()
                val sizeH: Float = 22.dp.toPx()
                val offset1: Float = 24.dp.toPx()
                val offset2: Float = 4.dp.toPx()
                val offset3: Float = 32.dp.toPx()
                val cr = 4.dp.toPx()

                // Bottom Layer: Local Database
                drawRoundRect(
                    color = ColorWest,
                    topLeft = Offset(cx - r80, cy + offset1),
                    size = androidx.compose.ui.geometry.Size(sizeW, sizeH),
                    style = Stroke(width = 1.5f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(cr, cr)
                )
                // Mid Layer: Dev Suite UI
                drawRoundRect(
                    color = ColorEast,
                    topLeft = Offset(cx - r80, cy - offset2),
                    size = androidx.compose.ui.geometry.Size(sizeW, sizeH),
                    style = Stroke(width = 1.5f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(cr, cr)
                )
                // Top layer: Web Syndicate
                drawRoundRect(
                    color = Color(0xFF33B3A6),
                    topLeft = Offset(cx - r80, cy - offset3),
                    size = androidx.compose.ui.geometry.Size(sizeW, sizeH),
                    style = Stroke(width = 1.5f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(cr, cr)
                )
                
                // Flow vertical arrows (thin line connections)
                drawLine(Color.White, Offset(cx, cy + 22.dp.toPx()), Offset(cx, cy - 2.dp.toPx()), strokeWidth = 1.5f)
                drawLine(Color.White, Offset(cx, cy - 6.dp.toPx()), Offset(cx, cy - 30.dp.toPx()), strokeWidth = 1.5f)
            }
            "flow" -> {
                // Horizontal loop progression for research cycles
                val sizeVal = 18.dp.toPx()
                val stepX = 35.dp.toPx()
                // Nodes
                drawCircle(ColorEast, radius = sizeVal/2, center = Offset(cx - 1.5f * stepX, cy))
                drawCircle(ColorSouth, radius = sizeVal/2, center = Offset(cx - 0.5f * stepX, cy))
                drawCircle(ColorWest, radius = sizeVal/2, center = Offset(cx + 0.5f * stepX, cy))
                drawCircle(ColorNorth, radius = sizeVal/2, center = Offset(cx + 1.5f * stepX, cy))

                // Connective arrows
                drawLine(Color.Gray, Offset(cx - 1.25f * stepX, cy), Offset(cx - 0.75f * stepX, cy), strokeWidth = 2f)
                drawLine(Color.Gray, Offset(cx - 0.25f * stepX, cy), Offset(cx + 0.25f * stepX, cy), strokeWidth = 2f)
                drawLine(Color.Gray, Offset(cx + 0.75f * stepX, cy), Offset(cx + 1.25f * stepX, cy), strokeWidth = 2f)
            }
            "mcp" -> {
                // LLM and Local database bridge diagram
                val sizeVal = 20.dp.toPx()
                val gatewayW = 50.dp.toPx()
                val gatewayH = 18.dp.toPx()
                
                // Draw LLM Agent core (Gold circle)
                drawCircle(ColorEast, radius = sizeVal, center = Offset(cx - 50.dp.toPx(), cy))
                
                // Draw MCP Gateway Adapter (Crimson box)
                drawRoundRect(
                    color = ColorSouth,
                    topLeft = Offset(cx - gatewayW / 2f, cy - gatewayH / 2f),
                    size = androidx.compose.ui.geometry.Size(gatewayW, gatewayH),
                    style = Stroke(width = 1.5f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx(), 4.dp.toPx())
                )
                
                // Draw Local Database Circle (Indigo)
                drawCircle(ColorWest, radius = sizeVal, center = Offset(cx + 50.dp.toPx(), cy))
                
                // Draw bidirectional connections through the MCP adapter
                drawLine(Color.White, Offset(cx - 30.dp.toPx(), cy), Offset(cx - gatewayW / 2f, cy), strokeWidth = 1.5f)
                drawLine(Color.White, Offset(cx + gatewayW / 2f, cy), Offset(cx + 30.dp.toPx(), cy), strokeWidth = 1.5f)
            }
            "sovereignty" -> {
                // Shield and padlock visual
                val outerR1 = 45.dp.toPx()
                val outerR2 = 30.dp.toPx()
                val centerR = 12.dp.toPx()
                
                // Centred node
                drawCircle(ColorEast, radius = centerR, center = Offset(cx, cy))
                // Concentric shield circles
                drawCircle(ColorNorth, radius = outerR2, center = Offset(cx, cy), style = Stroke(width = 1.5f))
                drawCircle(ColorSouth, radius = outerR1, center = Offset(cx, cy), style = Stroke(width = 1.5f))
                
                // Lock visual indicators
                drawLine(ColorWest, Offset(cx - 8.dp.toPx(), cy - 16.dp.toPx()), Offset(cx + 8.dp.toPx(), cy - 16.dp.toPx()), strokeWidth = 3f)
                drawLine(ColorWest, Offset(cx - 8.dp.toPx(), cy - 16.dp.toPx()), Offset(cx - 8.dp.toPx(), cy), strokeWidth = 1.5f)
                drawLine(ColorWest, Offset(cx + 8.dp.toPx(), cy - 16.dp.toPx()), Offset(cx + 8.dp.toPx(), cy), strokeWidth = 1.5f)
            }
            else -> {}
        }
    }
}

fun getSpecData(): List<SpecItem> {
    return listOf(
        StaticSpecItem(
            id = "ontology_core",
            domain = "Core Ontology",
            title = "ontology-core",
            icon = Icons.Default.Share,
            ojibweName = "Mshkiki",
            status = "Implemented",
            content = "Unified foundational data schemas translating Indigenous relational ontology to modern system records. Models the primary entities (human, land, spirit, ancestor, future, knowledge) and treats relationships as first-class objects carrying specific OcapFlags and AccountabilityTracking.\n\nOur Suite fully implements this using Room Database tables 'nodes' and 'edges' to record first-class entities with proper directional assignments and timestamps. Custom details are structured in standard primitive matrices inside SQLite for ultra-fast, zero-infrastructure retrieval out in the woods.",
            diagramType = "layers",
            aspects = listOf(
                SpecAspect(
                    title = "Node Entity Scheme",
                    ojibweName = "Akiing",
                    status = "Implemented",
                    description = "Defines the core entity structural schema. Unlike standard Western DB structures, nodes are not mere rows in a table; they are living relational agents grouped into seven existential types: human, land, spirit, ancestor, future, knowledge, and custom.",
                    schemaCode = """
                        {
                          "@context": "https://indigenous.ontology/context.jsonld",
                          "@type": "RelationalNode",
                          "id": "node_aki_001",
                          "type": "land",
                          "nativeName": "Akiing",
                          "sacredRegistryId": "TR-983",
                          "direction": "south",
                          "generationalTier": "present"
                        }
                    """.trimIndent()
                ),
                SpecAspect(
                    title = "Kinship Accountability Edges",
                    ojibweName = "Inawendiwin",
                    status = "Implemented",
                    description = "Relationships are modeled as first-class directed graph edges carrying emotional/ceremonial weight, sacred reciprocity ratings, and accountability logs.",
                    schemaCode = """
                        {
                          "@context": "https://indigenous.ontology/context.jsonld",
                          "@type": "RelationalEdge",
                          "id": "node_001:node_002",
                          "fromId": "node_001",
                          "toId": "node_002",
                          "reciprocityRating": 0.95,
                          "ceremonyHonored": true,
                          "custodian": "Elder_Council_West"
                        }
                    """.trimIndent()
                ),
                SpecAspect(
                    title = "Directional Metadata Integration",
                    ojibweName = "Anishinaabe-Izhitwaawin",
                    status = "Implemented",
                    description = "Establishes four cardinal directions. East represents vision & spring, South represents growth & summer, West represents harvest & autumn, and North represents wisdom & winter. Every node registers a physical or cosmological orientation.",
                    schemaCode = """
                        enum class SpatialDirection {
                            EAST,   // Waabinong
                            SOUTH,  // Zhaawanong
                            WEST,   // Epangishmok
                            NORTH   // Kiiwedinong
                        }
                    """.trimIndent()
                )
            )
        ),
        StaticSpecItem(
            id = "graph_viz",
            domain = "Spatial Graphics",
            title = "graph-viz",
            icon = Icons.Default.Place,
            ojibweName = "Circle Semantics",
            status = "Implemented",
            content = "Translates standard force-directed messy graph layouts into traditional concentric quadrants. In this representation, spatial position carries spiritual, seasonal, and biological meaning. Nodes align along a radial coordinate system matching the path of the sun.\n\nOur suite includes a gorgeous, reactive circular representation using mathematical angular offsets (0f for East, PI/2 for South, PI for West, 3PI/2 for North) inside an automatic layout builder. Nodes with identical directions are arranged symmetrically around their quadrant's central angle.",
            diagramType = "wheel",
            aspects = listOf(
                SpecAspect(
                    title = "Solar Arc Quadrant Layouts",
                    ojibweName = "Keewatinong-Geedan",
                    status = "Implemented",
                    description = "Mathematical angular partitioning of nodes. East = 0f (0°), South = PI/2 (90°), West = PI (180°), North = 3PI/2 (270°). Multiple nodes sitting in the same quadrant are placed symmetrically within narrow radial limits.",
                    schemaCode = """
                        fun getQuadrantAngle(direction: String): Float {
                            return when(direction.lowercase()) {
                                "east" -> 0.0f
                                "south" -> Math.PI.toFloat() / 2f
                                "west" -> Math.PI.toFloat()
                                "north" -> Math.PI.toFloat() * 1.5f
                                else -> 0.0f
                            }
                        }
                    """.trimIndent()
                ),
                SpecAspect(
                    title = "Concentric Generational Rings",
                    ojibweName = "Nizhwaasway-Mino-Bimaadiziwin",
                    status = "Active Draft",
                    description = "Represents spatial distance from the center hub. Ring 1 is immediate local community, Ring 2 is regional tribal territory, and Ring 3 is cosmological/spiritual.",
                    schemaCode = """
                        {
                          "concentricTiers": [
                            {"tier": 1, "radiusDp": 80, "group": "Local Hearth"},
                            {"tier": 2, "radiusDp": 140, "group": "Tribal Alliance"},
                            {"tier": 3, "radiusDp": 200, "group": "Seven Generations"}
                          ]
                        }
                    """.trimIndent()
                ),
                SpecAspect(
                    title = "Glowing Ceremonial Vector Ribbon",
                    ojibweName = "Waasnoode",
                    status = "Implemented",
                    description = "Renders dynamic links between nodes. Completed ceremonies make these ribbons glow brightly in primary sacred gold, while unhonored edges remain faint white to show that they require spiritual attention.",
                    schemaCode = """
                        val ribbonBrush = Brush.sweepGradient(
                            listOf(ColorEast, ColorSouth, ColorWest, ColorNorth, ColorEast)
                        )
                    """.trimIndent()
                )
            )
        ),
        StaticSpecItem(
            id = "cycles_ceremonies",
            domain = "Ceremy Protocols",
            title = "cycles-ceremonies",
            icon = Icons.Default.Refresh,
            ojibweName = "Wiisinin",
            status = "Implemented",
            content = "Encapsulates research projects as cyclic journeys around a core inquiry instead of a linear extraction pipeline. Cycles track progression of direction and maintain complete stats on ceremonies logged (smudging, talking_circle, spirit_feeding, openings, closings).\n\nEntirely offline-capable. Users can initiate cycles, select current direction, conduct ceremonies with appropriate medicines, and archive the completed cycles using Elder/Youth approval checks.",
            diagramType = "flow",
            aspects = listOf(
                SpecAspect(
                    title = "Circular Inquiry Lifecycles",
                    ojibweName = "Mino-Bimaadiziwin",
                    status = "Implemented",
                    description = "Tracks research cycles. Each cycle begins with inquiry preparation in the East, community engagement in the South, synthesis in the West, and archiving in the North.",
                    schemaCode = """
                        {
                          "cycleId": "cycle_winter_2026",
                          "currentDirection": "North",
                          "ceremoniesCompletedCount": 14,
                          "elderApprovalStatus": "Approved",
                          "creationTimestamp": 1780765800000
                        }
                    """.trimIndent()
                ),
                SpecAspect(
                    title = "Active Smudging & Offering Triggers",
                    ojibweName = "Nookwezigan",
                    status = "Implemented",
                    description = "Allows logging the utilization of specific sacred medicines (Tobacco (Semaa), Cedar (Giizhik), Sage (Mashkodewashk), Sweetgrass (Wiingashk)) along with intentions.",
                    schemaCode = """
                        enum class SacredMedicine {
                            SEMAA_TOBACCO,
                            GIIZHIK_CEDAR,
                            MASHKODEWASHK_SAGE,
                            WIINGASHK_SWEETGRASS
                        }
                    """.trimIndent()
                ),
                SpecAspect(
                    title = "Voice-Chronicle Reflection Tape",
                    ojibweName = "Dibajimowin",
                    status = "Implemented",
                    description = "Stores audio recordings securely on-device with cryptographic hashes. Enables audio reflection notes that accompany ceremonies.",
                    schemaCode = """
                        data class VoiceRecording(
                            val id: String,
                            val timestamp: Long,
                            val metadataHash: String,
                            val durationSeconds: Int,
                            val isUploaded: Boolean
                        )
                    """.trimIndent()
                )
            )
        ),
        StaticSpecItem(
            id = "sovereign_ocap",
            domain = "Sovereignty",
            title = "sovereign-ocap",
            icon = Icons.Default.Lock,
            ojibweName = "Ogimaawiwin",
            status = "Gap / Future",
            content = "Future Specification Module. Tracks personal and community consent parameters as a living stateful relationship rather than a single boolean checkbox. Transition states flow from Pending -> Granted -> Active -> Renewal-Needed -> Withdrawn. Withdrawing consent triggers a cascading hold across all dependent relations.",
            diagramType = "sovereignty",
            aspects = listOf(
                SpecAspect(
                    title = "Extraction Lock Safeguard",
                    ojibweName = "Gii-naagidowenimaa",
                    status = "Conceptual Gap",
                    description = "Automatically detects and halts external sync operations if a node or edge has been designated as sacred/private material. Contains high-privacy masking filters for geographic coords.",
                    schemaCode = """
                        {
                          "policy": "OCAP-Msk-09",
                          "enforcement": "StrictLocalOnly",
                          "overrideAuthority": "ElderCircleConsensusOnly",
                          "geographicPrivacyRangeMeters": 5000.0
                        }
                    """.trimIndent()
                ),
                SpecAspect(
                    title = "Relational Consent State Machine",
                    ojibweName = "Gwayakwaadiziwin",
                    status = "Conceptual Gap",
                    description = "Dynamic consent lifecycles. Traditional consensus is a living relationship, not a single static checkmark. States flow from Pending -> Approved -> Active -> Under Review -> Revoked.",
                    schemaCode = """
                        sealed class ConsentState {
                            object Pending : ConsentState()
                            data class Active(val approvedByLineage: String) : ConsentState()
                            data class UnderReview(val concernLogged: String) : ConsentState()
                            object Withdrawn : ConsentState()
                        }
                    """.trimIndent()
                ),
                SpecAspect(
                    title = "Dual-Key Ledger Ownership",
                    ojibweName = "Odinawemaagan",
                    status = "Conceptual Gap",
                    description = "Decentralized cryptographic co-signing. Ensures data possession is divided between the researcher (local key) and the historical lineage caretakers of that territory (ancestor key).",
                    schemaCode = """
                        interface IDualKeyLedger {
                            suspend fun coSignRecord(localKeySig: String, elderKeySig: String): Boolean
                        }
                    """.trimIndent()
                )
            )
        ),
        StaticSpecItem(
            id = "mcp_connector",
            domain = "MCP Interface",
            title = "mcp-connector",
            icon = Icons.Default.Info,
            ojibweName = "Gidizhimowin",
            status = "Active Draft",
            content = "Exposes the Medicine Wheel relational database as an open Model Context Protocol server. This allows sovereign localized AI agents and LLMs to interact with digital medicine bundles, query relational structures safely, and coordinate tool actions.",
            diagramType = "mcp",
            aspects = listOf(
                SpecAspect(
                    title = "Context Resource Bundles",
                    ojibweName = "Mashkiki-Gigo",
                    status = "Active Draft",
                    description = "Exposes relational nodes as MCP resources under the medicinewheel://nodes/{nodeId} URI pattern. Injectable resource frames allow LLMs to read the full geographic and traditional metadata.",
                    schemaCode = """
                        {
                          "uri": "medicinewheel://nodes/node_001",
                          "name": "Akiing Land Offering Node",
                          "mimeType": "application/json",
                          "text": "{\n  \"id\": \"node_001\",\n  \"name\": \"Akiing\",\n  \"direction\": \"South\"\n}"
                        }
                    """.trimIndent()
                ),
                SpecAspect(
                    title = "Relational LLM Tool Bindings",
                    ojibweName = "Anokii-Anishinaabe",
                    status = "Active Draft",
                    description = "Standard tool schema bindings for query_relation_network, link_sacred_nodes_with_duty, and record_spoken_dibajimowin. This enables autonomous LLM agents to act as digital scribes during active consultation events.",
                    schemaCode = """
                        {
                          "name": "link_sacred_nodes_with_duty",
                          "description": "Establish a kinship accountability edge between two nodes on the Medicine Wheel map",
                          "inputSchema": {
                            "type": "object",
                            "properties": {
                              "fromId": {"type": "string"},
                              "toId": {"type": "string"},
                              "relationshipType": {"type": "string"}
                            },
                            "required": ["fromId", "toId", "relationshipType"]
                          }
                        }
                    """.trimIndent()
                ),
                SpecAspect(
                    title = "Sovereign Local Prompt Templates",
                    ojibweName = "Nandotamowin",
                    status = "Implemented",
                    description = "Secure context-curator prompts stored on the device. Prevents Western LLM engines from interpreting traditional metadata via generic utilitarian definitions.",
                    schemaCode = """
                        const val SOVEREIGN_PREFACE = "When reasoning about Aki (Earth) or Ancestor nodes, the agent must treat connections as living relatives under reciprocal accountability, not mere data vertices."
                    """.trimIndent()
                )
            )
        )
    )
}
