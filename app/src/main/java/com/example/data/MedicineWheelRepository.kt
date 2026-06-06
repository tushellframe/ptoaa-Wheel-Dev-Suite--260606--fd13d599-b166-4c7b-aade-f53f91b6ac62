package com.example.data

import kotlinx.coroutines.flow.Flow

class MedicineWheelRepository(private val db: AppDatabase) {
    
    val allNodes: Flow<List<RelationalNode>> = db.nodeDao().getAllNodes()
    val allEdges: Flow<List<RelationalEdge>> = db.edgeDao().getAllEdges()
    val allCeremonies: Flow<List<CeremonyLog>> = db.ceremonyDao().getAllCeremonies()
    val allCycles: Flow<List<ResearchCycle>> = db.cycleDao().getAllCycles()
    val allRecordings: Flow<List<VoiceRecording>> = db.recordingDao().getAllRecordings()

    // Node actions
    suspend fun insertNode(node: RelationalNode) {
        db.nodeDao().insertNode(node)
    }

    suspend fun deleteNode(id: String) {
        db.nodeDao().deleteNodeById(id)
        // Clean up any edge containing this node
        db.edgeDao().deleteEdgesWithNode(id)
    }

    // Edge actions
    suspend fun insertEdge(edge: RelationalEdge) {
        db.edgeDao().insertEdge(edge)
    }

    suspend fun deleteEdge(id: String) {
        db.edgeDao().deleteEdgeById(id)
    }

    // Ceremony actions
    suspend fun insertCeremony(ceremony: CeremonyLog) {
        db.ceremonyDao().insertCeremony(ceremony)
    }

    suspend fun deleteCeremony(id: String) {
        db.ceremonyDao().deleteCeremonyById(id)
    }

    // Cycle actions
    suspend fun insertCycle(cycle: ResearchCycle) {
        db.cycleDao().insertCycle(cycle)
    }

    suspend fun deleteCycle(id: String) {
        db.cycleDao().deleteCycleById(id)
    }

    // Recording actions
    suspend fun insertRecording(recording: VoiceRecording) {
        db.recordingDao().insertRecording(recording)
    }

    suspend fun deleteRecording(id: String) {
        db.recordingDao().deleteRecordingById(id)
    }
}
