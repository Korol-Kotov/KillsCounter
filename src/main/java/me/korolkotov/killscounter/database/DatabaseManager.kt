package me.korolkotov.killscounter.database

import me.korolkotov.killscounter.Main
import java.sql.Connection
import java.sql.DriverManager

class DatabaseManager(
    private val plugin: Main
) {
    val connection: Connection?
        get() {
            val result: Connection

            try {
                Class.forName("com.mysql.cj.jdbc.Driver")

                val host = plugin.config.getString("database.host")
                val port = plugin.config.getString("database.port")
                val database = plugin.config.getString("database.database")

                result = DriverManager.getConnection(
                    "jdbc:mysql://$host:$port/$database",
                    plugin.config.getString("database.user"),
                    plugin.config.getString("database.password")
                )

                result.createStatement().execute("CREATE TABLE IF NOT EXISTS player_kills (id INT AUTO_INCREMENT PRIMARY KEY, player_name VARCHAR(255) NOT NULL, kills INT DEFAULT 0)")

                return result
            } catch (e: Exception) {
                plugin.logger.severe("Ошибка при подключении к базе данных:")
                e.printStackTrace()
                return null
            }
        }

    fun updatePlayerKills(killsMap: Map<String, Int>) {
        val connection = connection!!
        val updateQuery = "UPDATE player_kills SET kills = kills + ? WHERE player_name = ?"
        val insertQuery = "INSERT INTO player_kills (player_name, kills) VALUES (?, ?)"

        val updateStatement = connection.prepareStatement(updateQuery)
        val insertStatement = connection.prepareStatement(insertQuery)

        try {
            for ((playerName, kills) in killsMap) {
                if (playerExists(playerName)) {
                    updateStatement.setInt(1, kills)
                    updateStatement.setString(2, playerName)
                    updateStatement.addBatch()
                } else {
                    insertStatement.setString(1, playerName)
                    insertStatement.setInt(2, kills)
                    insertStatement.addBatch()
                }
            }
            updateStatement.executeBatch()
            insertStatement.executeBatch()
        } finally {
            updateStatement.close()
            insertStatement.close()
            connection.close()
        }
    }

    fun getPlayerKills(playerName: String): Int {
        val connection = connection!!
        val query = "SELECT kills FROM player_kills WHERE player_name = ?"
        val preparedStatement = connection.prepareStatement(query)

        return try {
            preparedStatement.setString(1, playerName)
            val resultSet = preparedStatement.executeQuery()
            var result = 0
            if (resultSet.next())
                result += resultSet.getInt("kills")

            result += plugin.counter.get(playerName)
            result
        } finally {
            preparedStatement.close()
            connection.close()
        }
    }

    fun playerExists(playerName: String): Boolean {
        val connection = connection!!

        val checkQuery = "SELECT EXISTS(SELECT 1 FROM player_kills WHERE player_name = ?)"
        val preparedStatement = connection.prepareStatement(checkQuery)

        return try {
            preparedStatement.setString(1, playerName)
            val resultSet = preparedStatement.executeQuery()
            resultSet.next() && resultSet.getBoolean(1)
        } finally {
            preparedStatement.close()
            connection.close()
        }
    }
}