package me.jamilalrasyidis.instagramfilter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import me.jamilalrasyidis.instagramfilter.interfaces.EditImageFragmentListener

class EditImageFragment : Fragment(), SeekBar.OnSeekBarChangeListener {

    private var listener: EditImageFragmentListener? = null
    private lateinit var seekBarBrightness: SeekBar
    private lateinit var seekBarContrast: SeekBar
    private lateinit var seekBarSaturation: SeekBar

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_edit_image, container, false)

        seekBarBrightness = view.findViewById(R.id.seek_bar_brightness)
        seekBarContrast = view.findViewById(R.id.seek_bar_contrast)
        seekBarSaturation = view.findViewById(R.id.seek_bar_saturation)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        seekBarBrightness.max = 200
        seekBarBrightness.progress = 100

        seekBarContrast.max = 20
        seekBarContrast.progress = 0

        seekBarSaturation.max = 30
        seekBarSaturation.progress = 10

        seekBarBrightness.setOnSeekBarChangeListener(this)
        seekBarContrast.setOnSeekBarChangeListener(this)
        seekBarSaturation.setOnSeekBarChangeListener(this)
    }

    fun setListener(listener: EditImageFragmentListener) {
        this.listener = listener
    }

    override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
        var progress = p1
        if (listener != null) {
            when (p0?.id) {
                R.id.seek_bar_brightness -> {
                    listener?.onBrightnessChanged(progress - 100)
                }
                R.id.seek_bar_contrast -> {
                    progress += 10
                    listener?.onContrastChanged(.1f * progress)
                }
                R.id.seek_bar_saturation -> {
                    listener?.onSaturationChanged(.1f * progress)
                }
            }
        }
    }

    override fun onStartTrackingTouch(p0: SeekBar?) {
        if (listener != null) {
            listener?.onEditStarted()
        }
    }

    override fun onStopTrackingTouch(p0: SeekBar?) {
        if (listener != null) {
            listener?.onEditCompleted()
        }
    }

    fun resetControls() {
        seekBarBrightness.progress = 100
        seekBarContrast.progress = 0
        seekBarSaturation.progress = 10
    }

}