package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "nodes")
data class RelationalNode(
    @PrimaryKey val id: String,
    val name: String,
    val type: String, // human, land, spirit, ancestor, future, knowledge
    val direction: String?, // east, south, west, north
    val description: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val xOffset: Float = 0f,
    val yOffset: Float = 0f
)

@Entity(tableName = "edges")
data class RelationalEdge(
    @PrimaryKey val id: String, // "fromId:toId"
    val fromId: String,
    val toId: String,
    val relationshipType: String,
    val strength: Float, // 0.0 to 1.0
    val ceremonyHonored: Boolean = false,
    val lastCeremonyId: String? = null,
    val obligations: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "ceremonies")
data class CeremonyLog(
    @PrimaryKey val id: String,
    val type: String, // smudging, talking_circle, spirit_feeding, opening, closing
    val direction: String, // east, south, west, north
    val participants: String, // Comma-separated names or IDs
    val medicinesUsed: String, // Comma-separated list (tobacco, cedar, sage, sweetgrass, strawberry)
    val intentions: String, 
    val timestamp: Long = System.currentTimeMillis(),
    val researchContext: String = ""
)

@Entity(tableName = "cycles")
data class ResearchCycle(
    @PrimaryKey val id: String,
    val researchQuestion: String,
    val currentDirection: String, // east, south, west, north
    val startDate: Long = System.currentTimeMillis(),
    val ceremoniesConducted: Int = 0,
    val relationsMapped: Int = 0,
    val wilsonAlignment: Float = 0.5f, // 0.0 to 1.0 (Three R's check)
    val ocapCompliant: Boolean = false,
    val archived: Boolean = false
)

@Entity(tableName = "recordings")
data class VoiceRecording(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val direction: String, // east, south, west, north
    val durationText: String, // e.g., "1:24"
    val timestamp: Long = System.currentTimeMillis(),
    val isUploaded: Boolean = false,
    val fileLocalPath: String = ""
)
