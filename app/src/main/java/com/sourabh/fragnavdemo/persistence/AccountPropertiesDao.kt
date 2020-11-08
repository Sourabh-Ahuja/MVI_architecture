package com.sourabh.fragnavdemo.persistence

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sourabh.fragnavdemo.models.AccountProperties

@Dao
interface AccountPropertiesDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAndReplace(accountProperties: AccountProperties) : Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertOrReplace(accountProperties: AccountProperties) : Long

     @Query("SELECT * FROM account_properties where pk = :pk")
     fun searchByPk(pk : Int) : AccountProperties?

    @Query("SELECT * FROM account_properties where email = :email")
    fun searchByPk(email : String) :  AccountProperties?

    @Query("SELECT * FROM account_properties WHERE email = :email")
    suspend fun searchByEmail(email: String): AccountProperties?
}