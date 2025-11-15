package com.augustop.azucar.database


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface MeasurementDao {
    @Insert
    suspend fun insert(measurement: Measurement): Long

    @Query("SELECT * FROM Measurement ORDER BY date DESC")
    suspend fun getAll(): List<Measurement>

    @Query("SELECT * FROM Measurement WHERE id = :id")
    suspend fun getById(id: Int): Measurement

    @Query("DELETE FROM Measurement WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Update
    suspend fun update(measurement: Measurement): Int

}
