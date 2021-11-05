package com.thewildzoo.espressotracker

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.TypedValue
import android.widget.Button
import android.widget.SeekBar
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.GridLabelRenderer
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.DataPointInterface
import com.jjoe64.graphview.series.LineGraphSeries
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.properties.Delegates

class SetupActivity : AppCompatActivity() {

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var accelerometerListener: SensorEventListener? = null

    private lateinit var graphView: GraphView
    private lateinit var limitSeekBar: SeekBar
    private lateinit var scaleFactorSeekBar: SeekBar
    private lateinit var saveButton: Button

    private lateinit var sharedPreferences: SharedPreferences
    private val accelerationData = LineGraphSeries<DataPointInterface>()
    private val limit = LineGraphSeries<DataPointInterface>()

    private var currentLimit by Delegates.notNull<Int>()
    private var scaleFactor by Delegates.notNull<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setup)

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)

        graphView = findViewById(R.id.accelerometer_graph)
        limitSeekBar = findViewById(R.id.scale_factor)
        scaleFactorSeekBar = findViewById(R.id.limit)
        saveButton = findViewById(R.id.save_button)

        currentLimit = sharedPreferences.getString(
            resources.getString(R.string.pref_threshold_key),
            resources.getInteger(R.integer.threshold_default).toString()
        )!!.toInt()
        scaleFactor = sharedPreferences.getString(
            resources.getString(R.string.scale_factor_key),
            resources.getInteger(R.integer.scale_factor_default).toString()
        )!!.toInt()

        limitSeekBar.apply {
            progress = currentLimit
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                    currentLimit = p1
                }

                override fun onStartTrackingTouch(p0: SeekBar?) {
                }

                override fun onStopTrackingTouch(p0: SeekBar?) {
                }
            })
        }

        scaleFactorSeekBar.apply {
            progress = scaleFactor
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                    scaleFactor = p1
                }

                override fun onStartTrackingTouch(p0: SeekBar?) {
                }

                override fun onStopTrackingTouch(p0: SeekBar?) {
                }
            })
        }

        saveButton.setOnClickListener {
            val editor = sharedPreferences.edit()

            editor.putString(
                resources.getString(R.string.pref_threshold_key),
                currentLimit.toString()
            )
            editor.putString(resources.getString(R.string.scale_factor_key), scaleFactor.toString())
            editor.putBoolean(resources.getString(R.string.pref_setup_complete_key), true)

            editor.apply()

            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        accelerationData.apply {
            color = getColorFromAttr(R.attr.acceleration_data_line)
        }

        limit.apply {
            color = getColorFromAttr(R.attr.limit_data_line)
        }

        graphView.apply {
            addSeries(accelerationData)
            addSeries(limit)
            viewport.apply {
                isXAxisBoundsManual = true
                setMaxX(5000.0)
                setMinX(0.0)
            }

            gridLabelRenderer.apply {
                gridStyle = GridLabelRenderer.GridStyle.NONE
                isHorizontalLabelsVisible = false
                padding = 120
            }
        }

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    }

    override fun onResume() {
        super.onResume()
        var takenSamples = 0
        val sampleSize = resources.getInteger(R.integer.sample_size)
        val values = mutableListOf<Double>()

        accelerometer?.also {
            accelerometerListener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent?) {

                    event?.let {

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

                            accelerationData.appendData(
                                DataPoint(
                                    (System.currentTimeMillis()).toDouble(),
                                    sqrt(sum / sampleSize)
                                ),
                                true,
                                10000
                            )

                            limit.appendData(
                                DataPoint(
                                    (System.currentTimeMillis()).toDouble(),
                                    currentLimit.toDouble()
                                ),
                                true,
                                10000
                            )

                            takenSamples = 0
                            values.removeAll(values)
                        }
                    }
                }

                override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
                }
            }

            sensorManager.registerListener(
                accelerometerListener,
                accelerometer,
                SensorManager.SENSOR_DELAY_FASTEST
            )
        }
    }

    override fun onPause() {
        super.onPause()

        sensorManager.unregisterListener(accelerometerListener)
    }

    @ColorInt
    fun Context.getColorFromAttr(
        @AttrRes attrColor: Int,
        typedValue: TypedValue = TypedValue(),
        resolveRefs: Boolean = true
    ): Int {
        theme.resolveAttribute(attrColor, typedValue, resolveRefs)
        return typedValue.data
    }
}
