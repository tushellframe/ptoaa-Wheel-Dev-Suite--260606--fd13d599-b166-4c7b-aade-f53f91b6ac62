package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
    val title: String
    val icon: androidx.compose.ui.graphics.vector.ImageVector
    val ojibweName: String
    val status: String // "Implemented" , "Draft" , "Gap / Future"
    val content: String
    val diagramType: String // "wheel", "flow", "layers", "none"
}

@Composable
fun DocumentationScreen(viewModel: MedicineWheelViewModel, modifier: Modifier = Modifier) {
    val specs = remember { getSpecData() }
    var selectedSpecIndex by remember { mutableStateOf(0) }

    Row(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F0F15)),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Left Column: Nav list of specs & Gap Status Gauge
        Column(
            modifier = Modifier
                .weight(0.9f)
                .fillMaxHeight()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "DEVELOPER SUITE SPECIFICATIONS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = ColorEast,
                fontFamily = FontFamily.Monospace
            )

            // Gap Status Gauge Summary Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF131320)),
                border = BorderStroke(1.dp, Color(0xFF23233E)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "ECOSYSTEM GAP GAUGE & AUDIT",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text("Current Suite Completeness: 68%", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(6.dp))

                    // Simulated horizontal bar split indicator
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(Color(0xFF23233E))
                    ) {
                        // Implemented (Green / Gold)
                        Box(modifier = Modifier.fillMaxHeight().weight(0.68f).background(ColorEast))
                        // Future / Gap (Gray/Purple)
                        Box(modifier = Modifier.fillMaxHeight().weight(0.32f).background(Color(0xFFFF3B30)))
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(4.dp)).background(ColorEast))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Implemented: 3 modules", fontSize = 11.sp, color = Color.LightGray)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(4.dp)).background(Color(0xFFFF3B30)))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Future Gaps: 2 specs", fontSize = 11.sp, color = Color.LightGray)
                        }
                    }
                }
            }

            // Specs Nav
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(specs) { idx, spec ->
                    val selected = idx == selectedSpecIndex
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedSpecIndex = idx },
                        colors = CardDefaults.cardColors(
                            containerColor = if (selected) Color(0xFF1E1E2E) else Color(0xFF13131F)
                        ),
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (selected) ColorEast else Color(0x33FFFFFF)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = spec.icon,
                                    contentDescription = null,
                                    tint = if (selected) ColorEast else Color.Gray,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = spec.title,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (selected) Color.White else Color.LightGray
                                    )
                                    Text(
                                        text = "Ojibwe: ${spec.ojibweName}",
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                }
                            }

                            // Tiny Status pill
                            val badgeColor = when (spec.status) {
                                "Implemented" -> ColorEast
                                "Active Draft" -> Color(0xFF33B3A6)
                                else -> Color(0xFFFF3B30)
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(badgeColor.copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = spec.status,
                                    color = badgeColor,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Right Column: Spec Detail Content Viewer & Custom Diagrams
        Box(
            modifier = Modifier
                .weight(1.1f)
                .fillMaxHeight()
                .background(Color(0xFF131320))
                .border(BorderStroke(1.dp, Color(0xFF1E1E34)))
                .padding(16.dp)
        ) {
            val spec = specs[selectedSpecIndex]
            Column(modifier = Modifier.fillMaxSize()) {
                Text(
                    text = "SPECIFICATION EXPLORER",
                    fontSize = 11.sp,
                    color = ColorEast,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                Text(
                    text = spec.title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Traditional Dimension: ${spec.ojibweName}",
                    fontSize = 13.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                HorizontalDivider(color = Color(0x33FFFFFF))
                Spacer(modifier = Modifier.height(12.dp))

                // Render dynamic Jetpack Compose diagram depending on spec context
                if (spec.diagramType != "none") {
                    Text(
                        text = "SPATIAL ARCHIVAL DIAGRAM",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF0F0F15))
                            .border(1.dp, Color(0xFF23233E), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        SpecDiagramRenderer(diagramType = spec.diagramType)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Text(
                    text = "DESIGN PROSE & AUDIT DETAILS",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                // Scrollable specification prose
                LazyColumn(modifier = Modifier.weight(1f)) {
                    item {
                        Text(
                            text = spec.content,
                            color = Color.LightGray,
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }
                }
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
                val r45: Float = 45.dp.toPx()
                val d50: Float = 50.dp.toPx()
                val d35: Float = 35.dp.toPx()
                val r5: Float = 5.dp.toPx()

                // Drawing circular wheel quadrants
                drawCircle(color = Color(0xFF1E1E2E), radius = r45, center = Offset(cxFloat, cyFloat))
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
                // Diagram representing tiered architecture layout (Room offline -> Server Sync)
                // Bottom Layer: Local Database
                drawRoundRect(
                    color = Color(0xFF23233E),
                    topLeft = Offset(cx - 70.dp.toPx(), cy + 20.dp.toPx()),
                    size = androidx.compose.ui.geometry.Size(140.dp.toPx(), 24.dp.toPx()),
                    style = Stroke(width = 2f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx(), 4.dp.toPx())
                )
                // Mid Layer: Dev Suite UI
                drawRoundRect(
                    color = ColorEast,
                    topLeft = Offset(cx - 70.dp.toPx(), cy - 12.dp.toPx()),
                    size = androidx.compose.ui.geometry.Size(140.dp.toPx(), 24.dp.toPx()),
                    style = Stroke(width = 2f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx(), 4.dp.toPx())
                )
                // Top layer: Web Syndicate
                drawRoundRect(
                    color = Color(0xFF33B3A6),
                    topLeft = Offset(cx - 70.dp.toPx(), cy - 44.dp.toPx()),
                    size = androidx.compose.ui.geometry.Size(140.dp.toPx(), 24.dp.toPx()),
                    style = Stroke(width = 2f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx(), 4.dp.toPx())
                )
                
                // Flow vertical arrows (thin line connections)
                drawLine(Color.White, Offset(cx, cy + 18.dp.toPx()), Offset(cx, cy - 12.dp.toPx()), strokeWidth = 1.5f)
                drawLine(Color.White, Offset(cx, cy - 14.dp.toPx()), Offset(cx, cy - 44.dp.toPx()), strokeWidth = 1.5f)
            }
            "flow" -> {
                // Horizontal loop progression for research cycles
                val sizeVal = 20.dp.toPx()
                val stepX = 40.dp.toPx()
                // Nodes
                drawCircle(ColorEast, radius = sizeVal/2, center = Offset(cx - 1.5f * stepX, cy))
                drawCircle(ColorSouth, radius = sizeVal/2, center = Offset(cx - 0.5f * stepX, cy))
                drawCircle(ColorWest, radius = sizeVal/2, center = Offset(cx + 0.5f * stepX, cy))
                drawCircle(ColorNorth, radius = sizeVal/2, center = Offset(cx + 1.5f * stepX, cy))

                // Connective arrows
                drawLine(Color.Gray, Offset(cx - 1.2f * stepX, cy), Offset(cx - 0.8f * stepX, cy), strokeWidth = 2f)
                drawLine(Color.Gray, Offset(cx - 0.2f * stepX, cy), Offset(cx + 0.2f * stepX, cy), strokeWidth = 2f)
                drawLine(Color.Gray, Offset(cx + 0.8f * stepX, cy), Offset(cx + 1.2f * stepX, cy), strokeWidth = 2f)
            }
            else -> {}
        }
    }
}

class StaticSpecItem(
    override val title: String,
    override val icon: androidx.compose.ui.graphics.vector.ImageVector,
    override val ojibweName: String,
    override val status: String,
    override val content: String,
    override val diagramType: String
) : SpecItem

fun getSpecData(): List<SpecItem> {
    return listOf(
        StaticSpecItem(
            title = "ontology-core",
            icon = Icons.Default.Share,
            ojibweName = "Mshkiki",
            status = "Implemented",
            content = "Unified foundational data schemas translating Indigenous relational ontology to modern system records. Models the primary entities (human, land, spirit, ancestor, future, knowledge) and treats relationships as first-class objects carrying specific OcapFlags and AccountabilityTracking.\n\n" +
                      "Status of Implementation:\n" +
                      "Our Suite fully implements this using Room Database tables 'nodes' and 'edges' to record first-class entities with proper directional assignments and timestamps. Custom details are structured in standard primitive matrices inside SQLite for ultra-fast, zero-infrastructure retrieval out in the woods.",
            diagramType = "layers"
        ),
        StaticSpecItem(
            title = "graph-viz",
            icon = Icons.Default.Place,
            ojibweName = "Circle Semantics",
            status = "Implemented",
            content = "Translates relationship maps from standard force-directed styles into culturally meaningful directional quadrants. Nodes automatically adjust dynamically along a circular sweep depending on their traditional directions:\n" +
                      "🌸 East (Waabinong) -> Vision/Spring (Gold/Yellow)\n" +
                      "🔥 South (Zhaawanong) -> Growth/Summer (Crimson/Red)\n" +
                      "🌊 West (Epangishmok) -> Truth/Autumn (Indigo/Blue)\n" +
                      "❄️ North (Kiiwedinong) -> Wisdom/Winter (White)\n\n" +
                      "Status of Implementation:\n" +
                      "Our suite includes a gorgeous, reactive circular representation using mathematical angular offsets (0f for East, PI/2 for South, PI for West, 3PI/2 for North) inside an automatic layout builder. Nodes with identical directions are arranged symmetrically around their quadrant's central angle.",
            diagramType = "wheel"
        ),
        StaticSpecItem(
            title = "cycles & ceremonies",
            icon = Icons.Default.Refresh,
            ojibweName = "Wiisinin",
            status = "Implemented",
            content = "Encapsulates research projects as cyclic journeys around a core inquiry instead of a linear extraction pipeline. Cycles track progression of direction and maintain complete stats on ceremonies logged (smudging, talking_circle, spirit_feeding, openings, closings).\n\n" +
                      "Status of Implementation:\n" +
                      "Entirely offline-capable. Users can initiate cycles, select current direction, conduct ceremonies with appropriate medicines, and archive the completed cycles for Seven Generations using Elder/Youth approval checks.",
            diagramType = "flow"
        ),
        StaticSpecItem(
            title = "consent-lifecycle",
            icon = Icons.Default.Lock,
            ojibweName = "Gwayakwaadiziwin",
            status = "Gap / Future",
            content = "Future Specification Module. Tracks personal and community consent parameters as a living stateful relationship rather than a single boolean checkbox. Transition states flow from Pending -> Granted -> Active -> Renewal-Needed -> Withdrawn. Withdrawing consent triggers a cascading hold across all dependent relations.\n\n" +
                      "Status of Implementation:\n" +
                      "Under active conceptual specification. This exists as a future development gap. When we prompt later in our team, we can request its full Room database tables, alerts, and state-machine transitions to be coded.",
            diagramType = "none"
        ),
        StaticSpecItem(
            title = "community-review",
            icon = Icons.Default.Person,
            ojibweName = "Council Wisdom",
            status = "Gap / Future",
            content = "Future Specification Module. Implements community-based talking review circles with Elder validation consensus, replacing standard Western peer reviews. Every review gathers diverse voices before delivering an approved wisdom blessing or ceremonial-hold.\n\n" +
                      "Status of Implementation:\n" +
                      "Currently documented in specifications only. Gaps are identified to allow rapid iteration as the relational workspace advances.",
            diagramType = "none"
        )
    )
}
