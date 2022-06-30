package hr.isinkovic.myapplication.ui.progress

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import hr.isinkovic.myapplication.R
import hr.isinkovic.myapplication.databinding.FragmentProgressBinding
import hr.isinkovic.myapplication.ui.AnimatedSwitch
import hr.isinkovic.myapplication.ui.location.LocationFragment
import hr.isinkovic.myapplication.ui.payment.PaymentFragment

class ProgressFragment : Fragment() {


    private lateinit var progressViewModel: ProgressViewModel
    private lateinit var binding: FragmentProgressBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProgressBinding.inflate(inflater, container, false)
        activity!!.onBackPressedDispatcher.addCallback(this) {}
        progressViewModel = ViewModelProvider(this)[ProgressViewModel::class.java]
        arguments?.let {
            progressViewModel.apply {
                location = it.getString(LocationFragment.LOCATION)
                fuel = it.getInt(LocationFragment.FUEL)
                time = it.getInt(PaymentFragment.TIME)
                card = it.getString(PaymentFragment.CARD)
            }
            setUpTextViews()
        }

        binding.cancelBtn.setOnClickListener {
            Navigation.findNavController(it)
                .navigate(R.id.action_progressFragment_to_locationFragment)
        }

        return binding.root
    }

    private fun setUpTextViews() {
        binding.fuelValue.text =
            if ((progressViewModel.fuel == LocationFragment.PREMIUM)) {
                String.format(
                    getString(R.string.lbl_progress_fuel),
                    getString(R.string.lbl_fuel_premium),
                    getString(R.string.lbl_fuel_premium_price)
                )
            } else {
                String.format(
                    getString(R.string.lbl_progress_fuel),
                    getString(R.string.lbl_fuel_regular),
                    getString(R.string.lbl_fuel_regular_price)
                )
            }

        binding.locationText.text = progressViewModel.location
        binding.timeText.text =
            getString(if (progressViewModel.time == AnimatedSwitch.SELECTION_LEFT) R.string.lbl_progress_morning else R.string.lbl_progress_afternoon)
        binding.cardText.text = progressViewModel.card
    }
}