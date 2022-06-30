package hr.isinkovic.myapplication.ui.location

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import hr.isinkovic.myapplication.db.Card
import hr.isinkovic.myapplication.db.CardDAO
import hr.isinkovic.myapplication.db.CryptoUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class LocationViewModel(private val preferences: SharedPreferences, database: CardDAO) :
    ViewModel() {
    var position: LatLng? = null
    var selectedFuel: Int = -1
    val db = database

    init {
        if (!preferences.getBoolean(LocationFragment.CARDS_ADDED, false)) {
            mockCards()
        }
    }

    private fun mockCards() {
        CoroutineScope(Dispatchers.IO).launch {
            val cards = generateCards()
            insert(cards)
            preferences.edit().putBoolean(LocationFragment.CARDS_ADDED, true).commit()
        }
    }

    private suspend fun insert(cards: List<Card>) {
        db.addCards(cards)
    }

    private suspend fun generateCards(): List<Card> {
        val cards = ArrayList<Card>()
        cards.add(
            Card(
                0,
                CryptoUtil().encrypt("0123456789012345"),
                CryptoUtil().encrypt("01/26"),
                0
            )
        )
        cards.add(
            Card(
                0,
                CryptoUtil().encrypt("0167823923450145"),
                CryptoUtil().encrypt("02/26"),
                1
            )
        )
        cards.add(
            Card(
                0,
                CryptoUtil().encrypt("3201678904512345"),
                CryptoUtil().encrypt("03/26"),
                1
            )
        )
        cards.add(
            Card(
                0,
                CryptoUtil().encrypt("4016789234550123"),
                CryptoUtil().encrypt("04/26"),
                1
            )
        )
        cards.add(
            Card(
                0,
                CryptoUtil().encrypt("8467597265486264"),
                CryptoUtil().encrypt("05/26"),
                0
            )
        )
        cards.add(
            Card(
                0,
                CryptoUtil().encrypt("6357541865454354"),
                CryptoUtil().encrypt("06/26"),
                1
            )
        )
        cards.add(
            Card(
                0,
                CryptoUtil().encrypt("7752572874110870"),
                CryptoUtil().encrypt("07/26"),
                0
            )
        )
        cards.add(
            Card(
                0,
                CryptoUtil().encrypt("8484210480442454"),
                CryptoUtil().encrypt("08/26"),
                0
            )
        )
        return cards
    }
}