package hr.isinkovic.myapplication.ui.progress

import androidx.lifecycle.ViewModel

class ProgressViewModel: ViewModel() {
    var card: String? = null
    var time: Int? = null
    var fuel: Int? = null
    var location: String? = null
}