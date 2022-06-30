package hr.isinkovic.myapplication.ui

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import hr.isinkovic.myapplication.R
import hr.isinkovic.myapplication.databinding.ViewAnimatedSwitchBinding


class AnimatedSwitch(context: Context, attrs: AttributeSet?) : ConstraintLayout(context, attrs) {

    companion object {
        private const val DEFAULT_BACKGROUND_COLOR = Color.LTGRAY
        private const val DEFAULT_SWITCH_COLOR = Color.CYAN
        private const val DEFAULT_SELECTED_TEXT_COLOR = Color.WHITE
        private const val DEFAULT_UNSELECTED_TEXT_COLOR = Color.BLACK
        private const val DEFAULT_LEFT_TEXT = "OFF"
        private const val DEFAULT_RIGHT_TEXT = "ON"
        const val SELECTION_LEFT = 0
        const val SELECTION_RIGHT = 1
    }

    var selectedTextColor: Int = DEFAULT_SELECTED_TEXT_COLOR
    var unselectedTextColor: Int = DEFAULT_UNSELECTED_TEXT_COLOR
    private var binding: ViewAnimatedSwitchBinding
    var switchColor: Int = DEFAULT_SWITCH_COLOR
        set(value) {
            field = value
            binding.overlay.background.setTint(value)
        }
    var leftText: String = DEFAULT_LEFT_TEXT
        set(value) {
            field = value
            binding.leftTextView.text = value
        }
    var rightText: String = DEFAULT_RIGHT_TEXT
        set(value) {
            field = value
            binding.rightTextView.text = value
        }

    var selectedItem: Int = SELECTION_LEFT

    init {
        binding = ViewAnimatedSwitchBinding.inflate(LayoutInflater.from(context), this, true)
        setUpAttributes(attrs)
        LayoutInflater.from(context)
        val states: Array<IntArray> = arrayOf(
            intArrayOf(android.R.attr.state_selected),
            intArrayOf(-android.R.attr.state_selected),
        )
        val colors = intArrayOf(
            selectedTextColor,
            unselectedTextColor
        )

        val colorStateList = ColorStateList(states, colors)
        binding.leftTextView.apply {
            setTextColor(colorStateList)
            isSelected = true
            text = leftText
            setOnClickListener {
                binding.leftTextView.isSelected = true
                binding.rightTextView.isSelected = false
                selectedItem = SELECTION_LEFT
                moveOverlay(selectedItem)

            }
        }
        binding.rightTextView.apply {
            setTextColor(colorStateList)
            isSelected = false
            text = rightText
            setOnClickListener {
                binding.rightTextView.isSelected = true
                binding.leftTextView.isSelected = false
                selectedItem = SELECTION_RIGHT
                moveOverlay(selectedItem)
            }
        }
    }

    private fun setUpAttributes(attrs: AttributeSet?) {
        val typedArray = context.theme.obtainStyledAttributes(
            attrs, R.styleable.AnimatedSwitch,
            0, 0
        )
        binding.parent.apply {
            background = ContextCompat.getDrawable(context, R.drawable.bg_switch)
            background.setTint(
                typedArray.getColor(
                    R.styleable.AnimatedSwitch_backgroundColor,
                    DEFAULT_BACKGROUND_COLOR
                )
            )
        }

        switchColor =
            typedArray.getColor(R.styleable.AnimatedSwitch_switchColor, DEFAULT_SWITCH_COLOR)
        selectedTextColor = typedArray.getColor(
            R.styleable.AnimatedSwitch_selectedTextColor,
            DEFAULT_SELECTED_TEXT_COLOR
        )
        unselectedTextColor = typedArray.getColor(
            R.styleable.AnimatedSwitch_unselectedTextColor,
            DEFAULT_UNSELECTED_TEXT_COLOR
        )
        leftText = typedArray.getString(R.styleable.AnimatedSwitch_leftText) ?: DEFAULT_LEFT_TEXT
        rightText = typedArray.getString(R.styleable.AnimatedSwitch_rightText) ?: DEFAULT_RIGHT_TEXT
        typedArray.recycle()
    }

    private fun moveOverlay(selection: Int) {
        val set = ConstraintSet()
        set.clone(binding.parent)
        set.connect(
            R.id.overlay,
            ConstraintSet.START,
            if (selection == SELECTION_LEFT)
                R.id.leftTextView
            else
                R.id.rightTextView,
            ConstraintSet.START
        )
        set.connect(
            R.id.overlay,
            ConstraintSet.END,
            if (selection == SELECTION_LEFT)
                R.id.leftTextView
            else
                R.id.rightTextView,
            ConstraintSet.END
        )
        set.applyTo(binding.parent)
    }


}