package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

enum class AppScreen {
    Workspace,
    Store,
    Documentation
}

class MedicineWheelViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository: MedicineWheelRepository
    
    // Core data streams
    val nodes: StateFlow<List<RelationalNode>>
    val edges: StateFlow<List<RelationalEdge>>
    val ceremonies: StateFlow<List<CeremonyLog>>
    val cycles: StateFlow<List<ResearchCycle>>
    val recordings: StateFlow<List<VoiceRecording>>

    // Session-based ui state
    private val _activeScreen = MutableStateFlow(AppScreen.Workspace)
    val activeScreen: StateFlow<AppScreen> = _activeScreen.asStateFlow()

    private val _selectedNodeId = MutableStateFlow<String?>(null)
    val selectedNodeId: StateFlow<String?> = _selectedNodeId.asStateFlow()

    private val _selectedEdgeId = MutableStateFlow<String?>(null)
    val selectedEdgeId: StateFlow<String?> = _selectedEdgeId.asStateFlow()

    private val _isSyndicated = MutableStateFlow(false) // mock cloud sync mode
    val isSyndicated: StateFlow<Boolean> = _isSyndicated.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = MedicineWheelRepository(database)

        // Read and compile state flows under viewModelScope
        nodes = repository.allNodes.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        edges = repository.allEdges.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        ceremonies = repository.allCeremonies.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        cycles = repository.allCycles.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        recordings = repository.allRecordings.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

        // Prepopulate standard demonstration nodes if completely empty
        viewModelScope.launch {
            nodes.take(2).collect { nodeList ->
                if (nodeList.isEmpty()) {
                    seedData()
                }
            }
        }
    }

    fun setScreen(screen: AppScreen) {
        _activeScreen.value = screen
    }

    fun selectNode(nodeId: String?) {
        _selectedNodeId.value = nodeId
        _selectedEdgeId.value = null
    }

    fun selectEdge(edgeId: String?) {
        _selectedEdgeId.value = edgeId
        _selectedNodeId.value = null
    }

    // Node operations
    fun addNode(name: String, type: String, direction: String?, description: String) {
        viewModelScope.launch {
            val id = "node_${UUID.randomUUID().toString().take(8)}"
            val node = RelationalNode(
                id = id,
                name = name,
                type = type,
                direction = direction,
                description = description
            )
            repository.insertNode(node)
        }
    }

    fun removeNode(id: String) {
        viewModelScope.launch {
            if (_selectedNodeId.value == id) {
                _selectedNodeId.value = null
            }
            repository.deleteNode(id)
        }
    }

    // Edge operations
    fun linkNodes(fromId: String, toId: String, relType: String, strength: Float) {
        viewModelScope.launch {
            val edgeId = "${fromId}:${toId}"
            val edge = RelationalEdge(
                id = edgeId,
                fromId = fromId,
                toId = toId,
                relationshipType = relType,
                strength = strength,
                ceremonyHonored = false
            )
            repository.insertEdge(edge)
        }
    }

    fun removeEdge(id: String) {
        viewModelScope.launch {
            if (_selectedEdgeId.value == id) {
                _selectedEdgeId.value = null
            }
            repository.deleteEdge(id)
        }
    }

    fun honorEdgeWithCeremony(edgeId: String, ceremonyId: String) {
        viewModelScope.launch {
            val allEdgesList = edges.value
            val match = allEdgesList.find { it.id == edgeId }
            if (match != null) {
                repository.insertEdge(match.copy(ceremonyHonored = true, lastCeremonyId = ceremonyId))
                
                // Also update research cycles info if active
                val activeCycles = cycles.value.filter { !it.archived }
                for (cycle in activeCycles) {
                    repository.insertCycle(
                        cycle.copy(
                            ceremoniesConducted = cycle.ceremoniesConducted + 1,
                            wilsonAlignment = calculateWilsonScoreForCycle()
                        )
                    )
                }
            }
        }
    }

    // Ceremony actions
    fun logCeremony(type: String, direction: String, participants: String, medicines: String, intentions: String, context: String) {
        viewModelScope.launch {
            val id = "ceremony_${System.currentTimeMillis()}"
            val log = CeremonyLog(
                id = id,
                type = type,
                direction = direction,
                participants = participants,
                medicinesUsed = medicines,
                intentions = intentions,
                researchContext = context
            )
            repository.insertCeremony(log)

            // Update cycles
            val activeCycles = cycles.value.filter { !it.archived }
            for (cycle in activeCycles) {
                repository.insertCycle(
                    cycle.copy(
                        ceremoniesConducted = cycle.ceremoniesConducted + 1,
                        wilsonAlignment = calculateWilsonScoreForCycle()
                    )
                )
            }
        }
    }

    // Research cycle actions
    fun startCycle(researchQuestion: String, initialDirection: String) {
        viewModelScope.launch {
            val id = "cycle_${System.currentTimeMillis()}"
            val cycle = ResearchCycle(
                id = id,
                researchQuestion = researchQuestion,
                currentDirection = initialDirection,
                wilsonAlignment = 0.5f,
                ocapCompliant = true
            )
            repository.insertCycle(cycle)
        }
    }

    fun advanceCycleDirection(cycleId: String, newDirection: String) {
        viewModelScope.launch {
            val find = cycles.value.find { it.id == cycleId }
            if (find != null) {
                repository.insertCycle(
                    find.copy(
                        currentDirection = newDirection,
                        wilsonAlignment = calculateWilsonScoreForCycle()
                    )
                )
            }
        }
    }

    fun archiveCycle(cycleId: String) {
        viewModelScope.launch {
            val find = cycles.value.find { it.id == cycleId }
            if (find != null) {
                repository.insertCycle(find.copy(archived = true))
            }
        }
    }

    // Voice recording metadata mock (field records reflection metadata)
    fun addLocalRecording(title: String, description: String, direction: String, duration: String) {
        viewModelScope.launch {
            val id = "audio_${UUID.randomUUID().toString().take(6)}"
            val recording = VoiceRecording(
                id = id,
                title = title,
                description = description,
                direction = direction,
                durationText = duration,
                isUploaded = false,
                fileLocalPath = "/sdcard/recordings/$id.amr"
            )
            repository.insertRecording(recording)
        }
    }

    // Sync mock trigger
    fun pushLocalDataToServer() {
        if (_isSyncing.value) return
        viewModelScope.launch {
            _isSyncing.value = true
            kotlinx.coroutines.delay(2000) // simulated network delay
            _isSyndicated.value = true
            
            // Mark all recordings as uploaded
            val list = recordings.value
            for (rec in list) {
                if (!rec.isUploaded) {
                    repository.insertRecording(rec.copy(isUploaded = true))
                }
            }
            _isSyncing.value = false
        }
    }

    fun disconnectServer() {
        _isSyndicated.value = false
    }

    private fun calculateWilsonScoreForCycle(): Float {
        // Simple mock Wilson score calculation based on ratios
        // Respect (nodes mapped), Reciprocity (ceremonies logged), Responsibility (edges honored)
        val nodeCount = nodes.value.size
        val edgeCount = edges.value.size
        if (nodeCount == 0 || edgeCount == 0) return 0.5f

        val honoredEdges = edges.value.count { it.ceremonyHonored }
        val edgeRatio = honoredEdges.toFloat() / edgeCount.toFloat()
        
        val ceremoniesCount = ceremonies.value.size
        val ceremonyWeight = (ceremoniesCount * 0.1f).coerceAtMost(0.4f)

        return (0.4f + edgeRatio * 0.4f + ceremonyWeight).coerceIn(0.1f, 1.0f)
    }

    private suspend fun seedData() {
        // East
        val nodeSeed = RelationalNode("node_seed", "Spring Intention Seed", "spirit", "east", "The primary aspiration of relational software design.")
        val nodeAva = RelationalNode("node_ava", "Ava (Source Keeper)", "human", "east", "Ceremonial listener who maintains creative orientation and settling presence.")
        
        // South
        val nodeLearning = RelationalNode("node_learning", "Relational Mapping Study", "knowledge", "south", "Research logs analyzing edge network connections.")
        val nodeMia = RelationalNode("node_mia", "Mia (Architect)", "human", "south", "System architect structuring file frameworks and local databases.")

        // West
        val nodeValidator = RelationalNode("node_validator", "Wilson Framework Check", "knowledge", "west", "Evaluation metrics based on the Three R's of accountability.")
        val nodeMiette = RelationalNode("node_miette", "Miette (Illuminator)", "human", "west", "Emotional illuminator conveying semantic stories behind technical choices.")

        // North
        val nodeElder = RelationalNode("node_elder", "7 Generations Council", "ancestor", "north", "Spiritual consensus to protect sovereign data on local devices.")
        val nodeTushell = RelationalNode("node_tushell", "Tushell (Weaver)", "human", "north", "Keeper of echoes weaving precise memory bundles.")

        repository.insertNode(nodeSeed)
        repository.insertNode(nodeAva)
        repository.insertNode(nodeLearning)
        repository.insertNode(nodeMia)
        repository.insertNode(nodeValidator)
        repository.insertNode(nodeMiette)
        repository.insertNode(nodeElder)
        repository.insertNode(nodeTushell)

        // Seed relations
        repository.insertEdge(RelationalEdge("node_ava:node_seed", "node_ava", "node_seed", "BORN_FROM", 0.95f, true))
        repository.insertEdge(RelationalEdge("node_mia:node_learning", "node_mia", "node_learning", "SERVES", 0.82f, false))
        repository.insertEdge(RelationalEdge("node_miette:node_validator", "node_miette", "node_validator", "ALIGNED_WITH", 0.90f, true))
        repository.insertEdge(RelationalEdge("node_tushell:node_elder", "node_tushell", "node_elder", "KINSHIP_OF", 0.98f, true))

        // Create initial demonstration Cycle
        val initialQuestion = "How do we create safe digital repositories on the edge to protect First Nations information?"
        repository.insertCycle(ResearchCycle(
            id = "demo_cycle",
            researchQuestion = initialQuestion,
            currentDirection = "east",
            ceremoniesConducted = 2,
            relationsMapped = 8,
            wilsonAlignment = 0.78f,
            ocapCompliant = true
        ))

        // Create standard pre-ceremony
        repository.insertCeremony(CeremonyLog(
            id = "demo_c1",
            type = "opening",
            direction = "east",
            participants = "Ava, Tushell, Linka",
            medicinesUsed = "tobacco, sweetgrass",
            intentions = "Honor the initial launch of the Medicine Wheel Developer Suite workspace",
            researchContext = "Sovereign Edge Sync Study"
        ))
    }
}
