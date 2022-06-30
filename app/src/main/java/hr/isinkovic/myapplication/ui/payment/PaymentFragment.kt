package hr.isinkovic.myapplication.ui.payment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import hr.isinkovic.myapplication.R
import hr.isinkovic.myapplication.databinding.FragmentPaymentBinding
import hr.isinkovic.myapplication.db.Card
import hr.isinkovic.myapplication.db.CardDatabase
import hr.isinkovic.myapplication.db.CryptoUtil
import hr.isinkovic.myapplication.ui.SpinnerAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PaymentFragment : Fragment(), AdapterView.OnItemSelectedListener {

    companion object {
        const val TIME = "time"
        const val CARD = "card"
    }

    private lateinit var _binding: FragmentPaymentBinding
    private val cardDatabase by lazy { CardDatabase.getDatabase(context!!).cardDao() }
    private lateinit var paymentViewModel: PaymentViewModel


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPaymentBinding.inflate(inflater, container, false)

        paymentViewModel = ViewModelProvider(this)[PaymentViewModel::class.java]

        if (paymentViewModel.cards != null) {
            setUpVisibility(paymentViewModel.cards!!)
        } else {
            CoroutineScope(Dispatchers.IO).launch {
                getCardsFromDatabase()
            }
        }

        _binding.proceedBtn.setOnClickListener {
            val bundle: Bundle = arguments ?: Bundle()
            bundle.putInt(TIME, _binding.animatedSwitch.selectedItem)
            bundle.putString(CARD,
                (_binding.cardPicker.adapter.getItem(paymentViewModel.selectedCardPosition!!) as Card).cardNumber)
            Navigation.findNavController(it).apply {
                enableOnBackPressed(false)
                navigate(R.id.action_paymentFragment_to_progressFragment, bundle)
            }
        }
        return _binding.root
    }

    private fun getCardsFromDatabase() {
        val list = cardDatabase.getCards()
        if (list.isNotEmpty()) {
            decryptData(list)
        }
    }

    private fun decryptData(encryptedCards: List<Card>) {
        val decryptedCards = ArrayList<Card>()

        for (card in encryptedCards) {
            val cardNumber = CryptoUtil().decrypt(card.cardNumber)
            decryptedCards.add(
                Card(
                    card.cardId,
                    cardNumber.chunked(4).joinToString(" "),
                    CryptoUtil().decrypt(card.expiryDate),
                    card.cardProvider
                )
            )
        }

        paymentViewModel.cards = decryptedCards

        CoroutineScope(Dispatchers.Main).launch {
            setUpVisibility(decryptedCards)
        }
    }

    private fun setUpVisibility(decryptedCards: List<Card>) {
        if (decryptedCards.isEmpty()) {
            _binding.progressBar.visibility = View.VISIBLE
        } else {
            _binding.cardPicker.visibility = View.VISIBLE
            setUpSpinner()
        }
        _binding.cardPicker.visibility =
            if (decryptedCards.isEmpty()) View.GONE else View.VISIBLE
        _binding.progressBar.visibility =
            if (decryptedCards.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun setUpSpinner() {
        paymentViewModel.cards?.let {
            val adapter = SpinnerAdapter(context!!, it)
            _binding.cardPicker.apply {
                this.adapter = adapter
                setSelection(paymentViewModel.selectedCardPosition ?: 0)
                onItemSelectedListener = this@PaymentFragment
            }
        }
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        paymentViewModel.selectedCardPosition = position
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        //do nothing
    }
}