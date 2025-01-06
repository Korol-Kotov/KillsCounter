package me.korolkotov.killscounter.domain

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.korolkotov.killscounter.data.DatabaseManager
import java.util.UUID

/**
 * The `Counter` class is responsible for tracking and managing player kill counts.
 *
 * This class provides in-memory tracking of kills with the ability to persist the data into a database.
 * It supports both adding kills and retrieving total kills for a specific player.
 *
 * @property databaseManager Provides access to the database for storing and retrieving kill data.
 */
class Counter(private val databaseManager: DatabaseManager) {
    private val killsMap = mutableMapOf<Pair<UUID, UUID>, Int>()

    /**
     * Adds a kill to the map for a given killer and victim UUID.
     *
     * @param killerUuid The UUID of the killer.
     * @param victimUuid The UUID of the victim.
     */
    fun addKill(killerUuid: UUID, victimUuid: UUID) {
        val key = killerUuid to victimUuid
        killsMap[key] = killsMap.getOrDefault(key, 0) + 1
    }

    /**
     * Gets the total number of kills for a given killer UUID.
     *
     * @param killerUuid The UUID of the killer.
     * @return The total number of kills for the killer.
     */
    fun getKills(killerUuid: UUID): Int {
        return killsMap.filterKeys { it.first == killerUuid }.values.sum() +
                databaseManager.getConnection().use { conn ->
                    conn.prepareStatement(
                        "SELECT SUM(kills) AS total_kills FROM player_kills WHERE killer_uuid = ?"
                    ).use { statement ->
                        statement.setString(1, killerUuid.toString())
                        val resultSet = statement.executeQuery()
                        if (resultSet.next()) resultSet.getInt("total_kills") else 0
                    }
                }
    }

    /**
     * Saves all kills from the map into the database.
     *
     * This method uses a batch insert statement to efficiently save all kills.
     */
    fun save() {
        CoroutineScope(Dispatchers.IO).launch {
            databaseManager.getConnection().use { conn ->
                conn.prepareStatement(
                    """
                    INSERT INTO player_kills (killer_uuid, victim_uuid, kills, timestamp)
                    VALUES (?, ?, ?, CURRENT_TIMESTAMP)
                    ON DUPLICATE KEY UPDATE kills = kills + VALUES(kills)
                    """
                ).use { statement ->
                    killsMap.forEach { (key, kills) ->
                        val (killerUuid, victimUuid) = key
                        statement.setString(1, killerUuid.toString())
                        statement.setString(2, victimUuid.toString())
                        statement.setInt(3, kills)
                        statement.addBatch()
                    }
                    statement.executeBatch()
                }
            }
            killsMap.clear()
        }
    }
}
