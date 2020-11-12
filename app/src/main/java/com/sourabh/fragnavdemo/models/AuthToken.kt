package com.sourabh.fragnavdemo.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.CASCADE
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

@Entity(
    tableName = "auth_token",
    foreignKeys = [
       ForeignKey(
           entity = AccountProperties::class,
           parentColumns = ["pk"],
           childColumns = ["account_pk"],
           onDelete = CASCADE // we use cascade to destroy the foreign key relation if parent column
                              // gets deleted
       )
    ]

)
data class AuthToken(

    @PrimaryKey
    @ColumnInfo(name = "account_pk")
    var account_pk : Int? = -1,

    @SerializedName("token")
    @Expose
    @ColumnInfo(name = "token") var token: String? = null
)