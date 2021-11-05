package com.thewildzoo.espressotracker.brewlogging

import OnSwipeTouchListener
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import com.thewildzoo.espressotracker.MainActivity
import com.thewildzoo.espressotracker.R
import com.thewildzoo.espressotracker.helper.MainActivityViewPagerStateInterface
import kotlinx.coroutines.launch
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.* // ktlint-disable no-wildcard-imports
import java.util.concurrent.TimeUnit
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.properties.Delegates

/**
 * A simple [Fragment] subclass.
 * Use the [BrewLoggingBrewingFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class BrewLoggingBrewingFragment : Fragment() {

    private val viewModel: BrewingLogViewModel by viewModels(
        ownerProducer = { requireParentFragment() }
    )

    private lateinit var toTasteButton: Button

    private var durationMillis: Long = 0
    private var startMillis: Long = 0
    private var isTimerRunning = false
    private val timerDelay: Long = 10

    private lateinit var timeLabel: TextView
    private lateinit var startButton: Button

    private lateinit var coffeeOutLabel: TextView
    private lateinit var coffeeOutAdd: ImageButton
    private lateinit var coffeeOutSubtract: ImageButton

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var accelerometerListener: SensorEventListener? = null
    private var threshold by Delegates.notNull<Float>()
    private var scaleFactor by Delegates.notNull<Int>()
    private var sampleSize by Delegates.notNull<Int>()
    private var directionOverThreshold = true
    private var accelerometerListen by Delegates.notNull<Boolean>()

    private var mainActivityViewPagerStateInterface: MainActivityViewPagerStateInterface? = null
    private val decimalFormat: DecimalFormat = DecimalFormat("00").apply {
        roundingMode = RoundingMode.CEILING
        val symbols = DecimalFormatSymbols(Locale.getDefault())
        symbols.decimalSeparator = '.'
        decimalFormatSymbols = symbols
    }

    private val stopWatchHandler = Handler()
    private val stopWatchRunnable = object : Runnable {
        override fun run() {
            durationMillis = System.currentTimeMillis() - startMillis
            val seconds = TimeUnit.MILLISECONDS.toSeconds(durationMillis)
            val minutes = TimeUnit.MILLISECONDS.toMinutes(durationMillis)
            timeLabel.text =
                decimalFormat.format(minutes) + ":" + decimalFormat.format(seconds) + "." + decimalFormat.format(
                (durationMillis % 1000) / 10
            )

            stopWatchHandler.postDelayed(this, timerDelay)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mainActivityViewPagerStateInterface = requireParentFragment().activity as MainActivity
        sensorManager = requireActivity().getSystemService(Context.SENSOR_SERVICE) as SensorManager

        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        accelerometerListen =
            PreferenceManager.getDefaultSharedPreferences(requireContext()).getBoolean(
                resources.getString(R.string.pref_use_accelerometer_key),
                true
            )

        sampleSize = resources.getInteger(R.integer.sample_size)

        scaleFactor = (
            PreferenceManager.getDefaultSharedPreferences(requireContext()).getString(
                resources.getString(R.string.scale_factor_key),
                resources.getInteger(R.integer.scale_factor_default).toString()
            )
            )!!.toInt()

        threshold = (
            PreferenceManager.getDefaultSharedPreferences(requireContext()).getString(
                resources.getString(R.string.pref_threshold_key),
                resources.getInteger(R.integer.threshold_default).toString()
            )
            )!!.toFloat()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_brew_logging_brewing, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        startButton = view.findViewById(R.id.start_time_button)
        timeLabel = view.findViewById(R.id.time_label)

        toTasteButton = view.findViewById(R.id.to_taste_button)

        coffeeOutLabel = view.findViewById(R.id.label_coffee_out)
        coffeeOutAdd = view.findViewById(R.id.plus_button_coffee_out)
        coffeeOutSubtract = view.findViewById(R.id.minus_button_coffee_out)

        startButton.apply {
            setOnClickListener {
                toggleStopWatch()
            }
        }

        viewModel.coffeeOut.observe(
            requireParentFragment().viewLifecycleOwner,
            { coffeeOut ->
                coffeeOutLabel.text = coffeeOut.toString()
            }
        )

        coffeeOutAdd.setOnClickListener {
            viewModel.changeCoffeeOutBy(0.10)
        }

        coffeeOutSubtract.setOnClickListener {
            viewModel.changeCoffeeOutBy(-0.10)
        }

        coffeeOutLabel.apply {
            setOnTouchListener(object : OnSwipeTouchListener(context) {
                override fun onSwipeRight() {
                    super.onSwipeRight()
                    viewModel.changeCoffeeOutBy(-1.0)
                }

                override fun onSwipeLeft() {
                    super.onSwipeLeft()
                    viewModel.changeCoffeeOutBy(1.0)
                }

                override fun onSwipeTop() {
                    super.onSwipeTop()
                    viewModel.changeCoffeeOutBy(10.0)
                }

                override fun onSwipeBottom() {
                    super.onSwipeBottom()
                    viewModel.changeCoffeeOutBy(-10.0)
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

        toTasteButton.setOnClickListener {
            viewModel.updateDuration(durationMillis)
            stopWatchHandler.removeCallbacks(stopWatchRunnable)
            lifecycleScope.launch {
                viewModel.saveRecipe()
            }
            findNavController().navigate(BrewLoggingBrewingFragmentDirections.actionBrewLoggingBrewingFragmentToBrewLoggingSetupFragment())
        }
    }

    override fun onResume() {
        super.onResume()

        var takenSamples = 0
        val values = mutableListOf<Double>()

        accelerometer?.also {
            accelerometerListener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent?) {

                    event?.let {

                        if (accelerometerListen) {

                            takenSamples++
                            values.add(
                                (it.values[2].toDouble() - 9.81).times(scaleFactor).pow(3)
                            )

                            if (takenSamples >= sampleSize) {
                                values.sort()

                                var sum = 0.0
                                for (value in values) {
                                    sum += value.pow(2)
                                }

                                val avg = sqrt(sum / sampleSize)

                                // start stopwatch
                                if (avg > threshold && directionOverThreshold) {
                                    directionOverThreshold = false
                                    toggleStopWatch()

                                    // stop stopwatch
                                } else if (avg < threshold && !directionOverThreshold) {
                                    directionOverThreshold = true
                                    toggleStopWatch()

                                    // watch run at least for ten seconds
                                    if (durationMillis > 15000) {

                                        // disable stopwatch to start again by accelerometer
                                        accelerometerListen = false
                                    }
                                }

                                takenSamples = 0
                                values.removeAll(values)
                            }
                        }
                    }
                }

                override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
                }
            }
        }

        sensorManager.registerListener(
            accelerometerListener,
            accelerometer,
            SensorManager.SENSOR_DELAY_FASTEST
        )
    }

    private fun toggleStopWatch() {
        if (durationMillis > 0 && isTimerRunning) {
            stopWatchHandler.removeCallbacks(stopWatchRunnable)
            isTimerRunning = false
            startButton.text = resources.getString(R.string.button_title_start_brew)
        } else {

            startMillis = System.currentTimeMillis() - durationMillis
            startButton.text = resources.getString(R.string.button_title_stop_brew)

            isTimerRunning = true
            stopWatchHandler.postDelayed(stopWatchRunnable, timerDelay)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(accelerometerListener)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         *
         * @return A new instance of fragment BrewLoggingBrewingFragment.
         */
        @JvmStatic
        fun newInstance() =
            BrewLoggingBrewingFragment()
    }
}
