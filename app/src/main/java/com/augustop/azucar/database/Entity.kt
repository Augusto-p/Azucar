package com.augustop.azucar.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "measurement")
data class Measurement(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    var value: Int,
    val date: Long,

    )