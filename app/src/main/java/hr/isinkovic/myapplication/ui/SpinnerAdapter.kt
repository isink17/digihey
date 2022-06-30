package hr.isinkovic.myapplication.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import hr.isinkovic.myapplication.R
import hr.isinkovic.myapplication.db.Card

open class SpinnerAdapter(context: Context, cards: List<Card>) : ArrayAdapter<Card>(context, 0, cards) {
    companion object {
        private const val VISA = 0
        private const val MASTERCARD = 1
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return initView(position, parent)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return initView(position, parent)
    }

    private fun initView(position: Int, parent: ViewGroup): View {

        val card = getItem(position)

        val view = LayoutInflater.from(context).inflate(R.layout.spinner_item, parent, false)
        when (card!!.cardProvider) {
            VISA -> view.findViewById<ImageView>(R.id.image).setImageDrawable(context.getDrawable(R.drawable.ic_cards_visa))
            MASTERCARD -> view.findViewById<ImageView>(R.id.image).setImageDrawable(context.getDrawable(R.drawable.ic_cards_mastercard))
        }

        view.findViewById<TextView>(R.id.cardNumber).text = card.cardNumber
        view.findViewById<TextView>(R.id.expiryDate).text = card.expiryDate

        return view
    }
}