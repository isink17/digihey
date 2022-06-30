package hr.isinkovic.myapplication.db

import androidx.room.*

@Dao
interface CardDAO {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun addCards(cards: List<Card>)

    @Query("SELECT * FROM cards ORDER BY cardId DESC")
    fun getCards(): List<Card>
}