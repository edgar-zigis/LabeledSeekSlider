package com.zigis.labeledseekslider

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import androidx.core.content.res.ResourcesCompat
import com.zigis.labeledseekslider.custom.UnitPosition

open class LabeledSeekSlider : View {

    //  Setting vars

    var minValue: Int = 0
        set(value) {
            field = value
            invalidate()
        }
    var maxValue: Int = 100
        set(value) {
            field = value
            invalidate()
        }
    var defaultValue: Int = 50
        set(value) {
            field = value
            invalidate()
        }
    var limitValue: Int = -1
        set(value) {
            field = value
            invalidate()
        }

    var title: String = ""
        set(value) {
            field = value
            invalidate()
        }
    var unit: String = ""
        set(value) {
            field = value
            invalidate()
        }
    var unitPosition: UnitPosition = UnitPosition.BACK
        set(value) {
            field = value
            invalidate()
        }
    var isDisabled = false
        set(value) {
            field = value
            invalidate()
        }

    var activeTrackColor = Color.parseColor("#FF2400")
    var inactiveTrackColor = Color.parseColor("#E8E8E8")
    var thumbSliderBackgroundColor = Color.parseColor("#E8E8E8")
    var bubbleValueTextColor = Color.parseColor("#1A1A1A")
    var bubbleOutlineColor = Color.parseColor("#E8E8E8")
    var labelTextColor = Color.parseColor("#AFB6BB")
    var rangeValueTextColor = Color.parseColor("#AFB6BB")

    var labelTextFont = Typeface.create("sans-serif-light", Typeface.NORMAL)
    var rangeValueTextFont = Typeface.create("sans-serif-light", Typeface.NORMAL)
    var bubbleValueTextFont = Typeface.create("sans-serif", Typeface.BOLD)

    var bubbleValueTextSize = dp(14f)
    var labelTextSize = dp(12f)
    var rangeValueTextSize = dp(12f)

    var slidingInterval: Int = 1

    private var trackHeight = dp(4f)
    private var thumbSliderSize = dp(24f)

    //  Constructors

    constructor(context: Context) : super(context) {
        init(context, null)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context, attrs)
    }

    //  Initialization

    private fun init(context: Context, attrs: AttributeSet?) {
        if (isInEditMode) return

        val styledAttributes = context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.LabeledSeekSlider,
            0,
            0
        )

        minValue = styledAttributes.getInteger(R.styleable.LabeledSeekSlider_lss_minValue, minValue)
        maxValue = styledAttributes.getInteger(R.styleable.LabeledSeekSlider_lss_maxValue, maxValue)
        defaultValue = styledAttributes.getInteger(R.styleable.LabeledSeekSlider_lss_defaultValue, defaultValue)
        limitValue = styledAttributes.getInteger(R.styleable.LabeledSeekSlider_lss_limitValue, limitValue)
        title = styledAttributes.getString(R.styleable.LabeledSeekSlider_lss_title) ?: title
        unit = styledAttributes.getString(R.styleable.LabeledSeekSlider_lss_unit) ?: unit
        unitPosition = UnitPosition.parse(
            styledAttributes.getInt(R.styleable.LabeledSeekSlider_lss_unitPosition, UnitPosition.BACK.value)
        )
        isDisabled = styledAttributes.getBoolean(R.styleable.LabeledSeekSlider_lss_isDisabled, isDisabled)

        activeTrackColor = styledAttributes.getColor(R.styleable.LabeledSeekSlider_lss_activeTrackColor, activeTrackColor)
        inactiveTrackColor = styledAttributes.getColor(R.styleable.LabeledSeekSlider_lss_inactiveTrackColor, inactiveTrackColor)
        thumbSliderBackgroundColor = styledAttributes.getColor(R.styleable.LabeledSeekSlider_lss_thumbSliderBackgroundColor, thumbSliderBackgroundColor)
        bubbleValueTextColor = styledAttributes.getColor(R.styleable.LabeledSeekSlider_lss_bubbleValueTextColor, bubbleValueTextColor)
        bubbleOutlineColor = styledAttributes.getColor(R.styleable.LabeledSeekSlider_lss_bubbleOutlineColor, bubbleOutlineColor)
        labelTextColor = styledAttributes.getColor(R.styleable.LabeledSeekSlider_lss_labelTextColor, labelTextColor)
        rangeValueTextColor = styledAttributes.getColor(R.styleable.LabeledSeekSlider_lss_rangeValueTextColor, rangeValueTextColor)

        val labelTextFontRes = styledAttributes.getResourceId(R.styleable.LabeledSeekSlider_lss_labelTextFont, 0)
        if (labelTextFontRes > 0) {
            labelTextFont = ResourcesCompat.getFont(context, labelTextFontRes) ?: labelTextFont
        }
        val rangeValueTextFontRes = styledAttributes.getResourceId(R.styleable.LabeledSeekSlider_lss_rangeValueTextFont, 0)
        if (rangeValueTextFontRes > 0) {
            rangeValueTextFont = ResourcesCompat.getFont(context, rangeValueTextFontRes) ?: rangeValueTextFont
        }
        val bubbleValueTextFontRes = styledAttributes.getResourceId(R.styleable.LabeledSeekSlider_lss_bubbleValueTextFont, 0)
        if (bubbleValueTextFontRes > 0) {
            bubbleValueTextFont = ResourcesCompat.getFont(context, bubbleValueTextFontRes) ?: bubbleValueTextFont
        }

        slidingInterval = styledAttributes.getInteger(R.styleable.LabeledSeekSlider_lss_slidingInterval, slidingInterval)

        bubbleValueTextSize = styledAttributes.getDimension(R.styleable.LabeledSeekSlider_lss_bubbleValueTextSize, bubbleValueTextSize)
        labelTextSize = styledAttributes.getDimension(R.styleable.LabeledSeekSlider_lss_labelTextSize, labelTextSize)
        rangeValueTextSize = styledAttributes.getDimension(R.styleable.LabeledSeekSlider_lss_rangeValueTextSize, rangeValueTextSize)

        trackHeight = styledAttributes.getDimension(R.styleable.LabeledSeekSlider_lss_trackHeight, trackHeight)
        thumbSliderSize = styledAttributes.getDimension(R.styleable.LabeledSeekSlider_lss_thumbSliderSize, thumbSliderSize)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        return super.onTouchEvent(event)
    }

    //  Helper methods

    private fun dp(dp: Float): Float {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics)
    }
}