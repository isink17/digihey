package hr.isinkovic.myapplication.ui.payment

import androidx.lifecycle.ViewModel
import hr.isinkovic.myapplication.db.Card

class PaymentViewModel : ViewModel() {
    var cards: List<Card>? = null
    var selectedCardPosition: Int? = null
}