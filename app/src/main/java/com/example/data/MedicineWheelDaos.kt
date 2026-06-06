package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RelationalNodeDao {
    @Query("SELECT * FROM nodes ORDER BY name ASC")
    fun getAllNodes(): Flow<List<RelationalNode>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNode(node: RelationalNode)

    @Query("DELETE FROM nodes WHERE id = :id")
    suspend fun deleteNodeById(id: String)
}

@Dao
interface RelationalEdgeDao {
    @Query("SELECT * FROM edges")
    fun getAllEdges(): Flow<List<RelationalEdge>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEdge(edge: RelationalEdge)

    @Query("DELETE FROM edges WHERE id = :id")
    suspend fun deleteEdgeById(id: String)

    @Query("DELETE FROM edges WHERE fromId = :nodeId OR toId = :nodeId")
    suspend fun deleteEdgesWithNode(nodeId: String)
}

@Dao
interface CeremonyLogDao {
    @Query("SELECT * FROM ceremonies ORDER BY timestamp DESC")
    fun getAllCeremonies(): Flow<List<CeremonyLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCeremony(ceremony: CeremonyLog)

    @Query("DELETE FROM ceremonies WHERE id = :id")
    suspend fun deleteCeremonyById(id: String)
}

@Dao
interface ResearchCycleDao {
    @Query("SELECT * FROM cycles ORDER BY startDate DESC")
    fun getAllCycles(): Flow<List<ResearchCycle>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCycle(cycle: ResearchCycle)

    @Query("DELETE FROM cycles WHERE id = :id")
    suspend fun deleteCycleById(id: String)
}

@Dao
interface VoiceRecordingDao {
    @Query("SELECT * FROM recordings ORDER BY timestamp DESC")
    fun getAllRecordings(): Flow<List<VoiceRecording>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecording(recording: VoiceRecording)

    @Query("DELETE FROM recordings WHERE id = :id")
    suspend fun deleteRecordingById(id: String)
}
