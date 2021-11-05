package com.thewildzoo.espressotracker.brewlogging

import OnSwipeTouchListener
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.thewildzoo.espressotracker.MainActivity
import com.thewildzoo.espressotracker.R
import com.thewildzoo.espressotracker.helper.MainActivityViewPagerStateInterface

/**
 * A simple [Fragment] subclass.
 * Use the [BrewLoggingSetupFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class BrewLoggingSetupFragment : Fragment() {

    private lateinit var toBrewingFragmentButton: Button

    private lateinit var coffeeInLabel: TextView
    private lateinit var coffeeInAdd: ImageButton
    private lateinit var coffeeInSubtract: ImageButton

    private lateinit var brewRationLabel: TextView
    private lateinit var brewRatioAdd: ImageButton
    private lateinit var brewRationSubtract: ImageButton

    private var mainActivityViewPagerStateInterface: MainActivityViewPagerStateInterface? = null

    private val viewModel: BrewingLogViewModel by viewModels(
        ownerProducer = { requireParentFragment() }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainActivityViewPagerStateInterface = requireParentFragment().activity as MainActivity
    }

    override fun onResume() {
        super.onResume()
        viewModel.resetValues()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_brew_logging_setup, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toBrewingFragmentButton = view.findViewById(R.id.to_brewing_button)
        coffeeInLabel = view.findViewById(R.id.label_coffee_in)
        coffeeInAdd = view.findViewById(R.id.plus_button_coffe_in)
        coffeeInSubtract = view.findViewById(R.id.minus_button_coffe_in)

        brewRationLabel = view.findViewById(R.id.label_brew_ratio)
        brewRatioAdd = view.findViewById(R.id.plus_button_brew_ratio)
        brewRationSubtract = view.findViewById(R.id.minus_button_brew_ratio)

        toBrewingFragmentButton.apply {
            setOnClickListener {
                findNavController().navigate(BrewLoggingSetupFragmentDirections.actionBrewLoggingSetupFragmentToBrewLoggingBrewingFragment())
            }
        }

        viewModel.coffeeIn.observe(
            requireParentFragment().viewLifecycleOwner,
            { coffeeIn ->
                coffeeInLabel.text = coffeeIn.toString()
            }
        )

        viewModel.plannedBrewRatio.observe(
            requireParentFragment().viewLifecycleOwner,
            { plannedBrewRatio ->
                brewRationLabel.text = resources.getString(R.string.label_brew_ratio_number, plannedBrewRatio.toString())
            }
        )

        coffeeInAdd.setOnClickListener {
            viewModel.changeCoffeeInBy(0.10)
        }

        coffeeInSubtract.setOnClickListener {
            viewModel.changeCoffeeInBy(-0.10)
        }

        coffeeInLabel.apply {
            setOnTouchListener(object : OnSwipeTouchListener(context) {
                override fun onSwipeRight() {
                    super.onSwipeRight()
                    viewModel.changeCoffeeInBy(-1.0)
                }

                override fun onSwipeLeft() {
                    super.onSwipeLeft()
                    viewModel.changeCoffeeInBy(1.0)
                }

                override fun onSwipeTop() {
                    super.onSwipeTop()
                    viewModel.changeCoffeeInBy(10.0)
                }

                override fun onSwipeBottom() {
                    super.onSwipeBottom()
                    viewModel.changeCoffeeInBy(-10.0)
                }

                override fun onDown() {
                    super.onDown()
                    mainActivityViewPagerStateInterface?.disableViewPager()
                }

                override fun onUp() {
                    super.onUp()
                    mainActivityViewPagerStateInterface?.enableViewPager()
                }
            })
        }

        brewRatioAdd.setOnClickListener {
            viewModel.changePlannedBrewRatioBy(0.10)
        }

        brewRationSubtract.setOnClickListener {
            viewModel.changePlannedBrewRatioBy(-0.10)
        }

        brewRationLabel.apply {
            setOnTouchListener(object : OnSwipeTouchListener(context) {
                override fun onSwipeRight() {
                    super.onSwipeRight()
                    viewModel.changePlannedBrewRatioBy(-1.0)
                }

                override fun onSwipeLeft() {
                    super.onSwipeLeft()
                    viewModel.changePlannedBrewRatioBy(1.0)
                }

                override fun onDown() {
                    super.onDown()
                    mainActivityViewPagerStateInterface?.disableViewPager()
                }

                override fun onUp() {
                    super.onUp()
                    mainActivityViewPagerStateInterface?.enableViewPager()
                }
            })
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         *
         * @return A new instance of fragment BrewLoggingSetupFragment.
         */
        @JvmStatic
        fun newInstance() =
            BrewLoggingSetupFragment()
    }
}
