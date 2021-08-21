package com.zigis.labeledseekslider

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.os.Build
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.MotionEvent.*
import android.view.View
import androidx.core.content.res.ResourcesCompat
import com.zigis.labeledseekslider.custom.UnitPosition
import com.zigis.labeledseekslider.custom.vibrate
import kotlin.math.max
import kotlin.math.min
import kotlin.math.round

@Suppress("DEPRECATION")
@SuppressLint("DrawAllocation")
open class LabeledSeekSlider : View {

    //  Setting vars

    var minValue: Int = 0
        set(value) {
            if (field != value) {
                actualXPosition = null
            }
            field = value
            if (actualFractionalValue < value) {
                actualFractionalValue = value
            }
            invalidate()
        }
    var maxValue: Int = 100
        set(value) {
            if (field != value) {
                actualXPosition = null
            }
            field = value
            if (actualFractionalValue > value) {
                actualFractionalValue = value
            }
            invalidate()
        }
    var defaultValue: Int = 50
        set(value) {
            if (field != value || field != getDisplayValue()) {
                actualXPosition = null
            }
            val newValue = min(maxValue, max(minValue, value))
            field = newValue
            actualFractionalValue = newValue
            invalidate()
        }
    var limitValue: Int? = null
        set(value) {
            if (field != value) {
                actualXPosition = null
            }
            field = value
            if (value != null && actualFractionalValue > value) {
                actualFractionalValue = value
            }
            invalidate()
        }
    var limitValueIndicator: String = "Max"
        set(value) {
            field = value
            invalidate()
        }
    var allowLimitValueBypass: Boolean = false
    var vibrateOnLimitReached: Boolean = true
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
    var unitPosition = UnitPosition.BACK
        set(value) {
            field = value
            invalidate()
        }
    var isDisabled: Boolean = false
        set(value) {
            field = value
            invalidate()
        }

    var activeTrackColor = Color.parseColor("#FF2400")
        set(value) {
            field = value
            activeTrackPaint.color = value
            invalidate()
        }
    var inactiveTrackColor = Color.parseColor("#E8E8E8")
        set(value) {
            field = value
            inactiveTrackPaint.color = value
            invalidate()
        }
    var thumbSliderBackgroundColor = Color.parseColor("#FFFFFF")
        set(value) {
            field = value
            thumbSliderPaint.color = value
            invalidate()
        }

    var rangeValueTextFont = Typeface.create("sans-serif", Typeface.NORMAL)
        set(value) {
            field = value
            rangeTextPaint.typeface = value
            invalidate()
        }
    var rangeValueTextColor = Color.parseColor("#9FA7AD")
        set(value) {
            field = value
            rangeTextPaint.color = value
            invalidate()
        }
    var rangeValueTextSize = dp(12f)
        set(value) {
            field = value
            rangeTextPaint.textSize = value
            invalidate()
        }

    var titleTextFont = Typeface.create("sans-serif", Typeface.NORMAL)
        set(value) {
            field = value
            titleTextPaint.typeface = value
            invalidate()
        }
    var titleTextColor = Color.parseColor("#9FA7AD")
        set(value) {
            field = value
            titleTextPaint.color = value
            invalidate()
        }
    var titleTextSize = dp(12f)
        set(value) {
            field = value
            titleTextPaint.textSize = value
            invalidate()
        }

    var bubbleOutlineColor = Color.parseColor("#E8E8E8")
        set(value) {
            field = value
            bubblePaint.color = value
            invalidate()
        }
    var bubbleValueTextFont = Typeface.create("sans-serif", Typeface.BOLD)
        set(value) {
            field = value
            bubbleTextPaint.typeface = value
            invalidate()
        }
    var bubbleValueTextColor = Color.parseColor("#1A1A1A")
        set(value) {
            field = value
            bubbleTextPaint.color = value
            invalidate()
        }
    var bubbleValueTextSize = dp(14f)
        set(value) {
            field = value
            bubbleTextPaint.textSize = value
            invalidate()
        }

    var slidingInterval: Int = 1

    private var trackHeight = dp(4f)
    private var thumbSliderRadius = dp(12f)

    //  Read-only and public vars

    var onValueChanged: ((Int) -> Unit)? = null
    var currentValue: Int = 150
        private set

    //  Operational vars

    private var actualFractionalValue: Int = 150
    private var actualXPosition: Float? = null

    private val topPadding = dp(2f)
    private val sidePadding = dp(16f)
    private val bubbleHeight = dp(26f)
    private val minimumBubbleWidth = dp(84f)
    private val bubbleTextPadding = dp(16f)

    private val thumbSliderPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.style = Paint.Style.FILL
        it.setShadowLayer(dp(2f), 0f, 1f, Color.parseColor("#44444444"))
        setLayerType(LAYER_TYPE_SOFTWARE, it)
    }

    private val inactiveTrackPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.style = Paint.Style.FILL_AND_STROKE
    }
    private var inactiveTrackRect: RectF? = null

    private val activeTrackPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.style = Paint.Style.FILL_AND_STROKE
    }

    private val bubblePaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.style = Paint.Style.STROKE
        it.strokeWidth = dp(2f)
        it.strokeCap = Paint.Cap.ROUND
        it.pathEffect = CornerPathEffect(dp(4f))
    }
    private var bubblePath = Path()
    private var bubblePathWidth = 0f

    private var bubbleText: String = ""
    private val bubbleTextRect = Rect()
    private var bubbleTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)

    private val titleTextRect = Rect()
    private var titleTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)

    private val minRangeTextRect = Rect()
    private val maxRangeTextRect = Rect()
    private var rangeTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)

    private val disabledStatePaint = Paint()

    //  Constructors

    constructor(context: Context) : super(context) {
        init(context, null)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context, attrs)
    }

    //  Initialization

    private fun init(context: Context, attrs: AttributeSet?) {
        if (isInEditMode) return

        initializeDisabledStatePaint()
        val styledAttributes = context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.LabeledSeekSlider,
            0,
            0
        )

        minValue = styledAttributes.getInteger(
            R.styleable.LabeledSeekSlider_lss_minValue,
            minValue
        )
        maxValue = styledAttributes.getInteger(
            R.styleable.LabeledSeekSlider_lss_maxValue,
            maxValue
        )
        defaultValue = styledAttributes.getInteger(
            R.styleable.LabeledSeekSlider_lss_defaultValue,
            defaultValue
        ).also {
            actualFractionalValue = min(maxValue, max(minValue, it))
        }

        styledAttributes.getInteger(R.styleable.LabeledSeekSlider_lss_limitValue, -1).also {
            if (it != -1) {
                limitValue = it
            }
        }

        vibrateOnLimitReached = styledAttributes.getBoolean(
            R.styleable.LabeledSeekSlider_lss_vibrateOnLimitReached,
            true
        )

        limitValueIndicator = styledAttributes.getString(
            R.styleable.LabeledSeekSlider_lss_limitValueIndicator
        ) ?: limitValueIndicator
        title = styledAttributes.getString(
            R.styleable.LabeledSeekSlider_lss_title
        ) ?: title
        unit = styledAttributes.getString(
            R.styleable.LabeledSeekSlider_lss_unit
        ) ?: unit

        unitPosition = UnitPosition.parse(
            styledAttributes.getInt(
                R.styleable.LabeledSeekSlider_lss_unitPosition,
                UnitPosition.BACK.value
            )
        )
        isDisabled = styledAttributes.getBoolean(
            R.styleable.LabeledSeekSlider_lss_isDisabled,
            isDisabled
        )

        activeTrackColor = styledAttributes.getColor(
            R.styleable.LabeledSeekSlider_lss_activeTrackColor,
            activeTrackColor
        )
        inactiveTrackColor = styledAttributes.getColor(
            R.styleable.LabeledSeekSlider_lss_inactiveTrackColor,
            inactiveTrackColor
        )
        thumbSliderBackgroundColor = styledAttributes.getColor(
            R.styleable.LabeledSeekSlider_lss_thumbSliderBackgroundColor,
            thumbSliderBackgroundColor
        )
        bubbleValueTextColor = styledAttributes.getColor(
            R.styleable.LabeledSeekSlider_lss_bubbleValueTextColor,
            bubbleValueTextColor
        )
        bubbleOutlineColor = styledAttributes.getColor(
            R.styleable.LabeledSeekSlider_lss_bubbleOutlineColor,
            bubbleOutlineColor
        )
        titleTextColor = styledAttributes.getColor(
            R.styleable.LabeledSeekSlider_lss_titleTextColor,
            titleTextColor
        )
        rangeValueTextColor = styledAttributes.getColor(
            R.styleable.LabeledSeekSlider_lss_rangeValueTextColor,
            rangeValueTextColor
        )

        styledAttributes.getResourceId(R.styleable.LabeledSeekSlider_lss_titleTextFont, 0).also {
            if (it > 0) titleTextFont = ResourcesCompat.getFont(context, it)
        }
        styledAttributes.getResourceId(R.styleable.LabeledSeekSlider_lss_rangeValueTextFont, 0).also {
            if (it > 0) rangeValueTextFont = ResourcesCompat.getFont(context, it)
        }
        styledAttributes.getResourceId(R.styleable.LabeledSeekSlider_lss_bubbleValueTextFont, 0).also {
            if (it > 0) bubbleValueTextFont = ResourcesCompat.getFont(context, it)
        }

        slidingInterval = styledAttributes.getInteger(
            R.styleable.LabeledSeekSlider_lss_slidingInterval,
            slidingInterval
        )

        bubbleValueTextSize = styledAttributes.getDimension(
            R.styleable.LabeledSeekSlider_lss_bubbleValueTextSize,
            bubbleValueTextSize
        )
        titleTextSize = styledAttributes.getDimension(
            R.styleable.LabeledSeekSlider_lss_titleTextSize,
            titleTextSize
        )
        rangeValueTextSize = styledAttributes.getDimension(
            R.styleable.LabeledSeekSlider_lss_rangeValueTextSize,
            rangeValueTextSize
        )

        trackHeight = styledAttributes.getDimension(
            R.styleable.LabeledSeekSlider_lss_trackHeight,
            trackHeight
        )
        thumbSliderRadius = styledAttributes.getDimension(
            R.styleable.LabeledSeekSlider_lss_thumbSliderRadius,
            thumbSliderRadius
        )
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minimumDesiredWidth = suggestedMinimumWidth + paddingLeft + paddingRight
        val minimumDesiredHeight = dp(98f).toInt()
        setMeasuredDimension(
            measureDimension(minimumDesiredWidth, widthMeasureSpec),
            measureDimension(minimumDesiredHeight, heightMeasureSpec)
        )
    }

    private fun measureDimension(desiredSize: Int, measureSpec: Int): Int {
        var result: Int
        val specMode = MeasureSpec.getMode(measureSpec)
        val specSize = MeasureSpec.getSize(measureSpec)
        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize
        } else {
            result = desiredSize
            if (specMode == MeasureSpec.AT_MOST) {
                result = min(result, specSize)
            }
        }
        return result
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        (actualXPosition ?: getActiveX(actualFractionalValue)).also { x ->
            drawBubbleValue(canvas, x)
            drawBubbleOutline(canvas, x)
            drawTitleLabelText(canvas)
            drawInactiveTrack(canvas)
            drawActiveTrack(canvas, x)
            drawThumbSlider(canvas, x)
            drawMinRangeText(canvas)
            drawMaxRangeText(canvas)
        }
    }

    override fun draw(canvas: Canvas?) {
        if (isDisabled) {
            canvas?.saveLayer(null, disabledStatePaint)
        }
        super.draw(canvas)
        if (isDisabled) {
            canvas?.restore()
        }
    }

    override fun dispatchDraw(canvas: Canvas?) {
        if (isDisabled) {
            canvas?.saveLayer(null, disabledStatePaint)
        }
        super.dispatchDraw(canvas)
        if (isDisabled) {
            canvas?.restore()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (isDisabled) return false
        return when (event.action) {
            ACTION_DOWN, ACTION_MOVE, ACTION_UP -> handleSlidingMovement(event.x)
            else -> false
        }
    }

    private fun handleSlidingMovement(x: Float): Boolean {
        val relativeX = min(measuredWidth - sidePadding - thumbSliderRadius, max(sidePadding / 2 - thumbSliderRadius, x))
        val slidingAreaWidth = measuredWidth - sidePadding - thumbSliderRadius

        val newValue = min(maxValue, max(
            minValue,
            minValue + round((maxValue - minValue) * (relativeX / slidingAreaWidth)).toInt()
        ))
        actualFractionalValue = if (limitValue == null || allowLimitValueBypass) {
            newValue
        } else min(newValue, limitValue!!)

        if (limitValue != null && !allowLimitValueBypass) {
            if (newValue <= limitValue!!) {
                actualXPosition = x
            } else {
                actualXPosition = getActiveX(limitValue!!)
            }
        } else {
            actualXPosition = x
        }

        invalidate()
        return true
    }

    private fun getActiveX(currentValue: Int): Float {
        val slidingAreaWidth = measuredWidth - sidePadding - thumbSliderRadius
        val progress = (currentValue - minValue).toFloat() / (maxValue - minValue).toFloat()
        return slidingAreaWidth * progress
    }

    private fun drawThumbSlider(canvas: Canvas, x: Float) {
        val centerX = min(
            measuredWidth - thumbSliderRadius - sidePadding,
            max(sidePadding + thumbSliderRadius, x + sidePadding / 2)
        )
        canvas.drawCircle(
            centerX,
            inactiveTrackRect!!.centerY(),
            thumbSliderRadius,
            thumbSliderPaint
        )
    }

    //  Track drawing

    private fun drawActiveTrack(canvas: Canvas, x: Float) {
        val activeTrackRect = RectF(
            sidePadding,
            getSlidingTrackVerticalOffset(),
            min(measuredWidth.toFloat() - sidePadding, max(sidePadding, x + sidePadding)),
            getSlidingTrackVerticalOffset() + trackHeight
        )
        val cornerRadius = trackHeight / 2
        canvas.drawRoundRect(activeTrackRect, cornerRadius, cornerRadius, activeTrackPaint)
    }

    private fun drawInactiveTrack(canvas: Canvas) {
        inactiveTrackRect = RectF(
            sidePadding,
            getSlidingTrackVerticalOffset(),
            measuredWidth.toFloat() - sidePadding,
            getSlidingTrackVerticalOffset() + trackHeight
        )
        val cornerRadius = trackHeight / 2
        canvas.drawRoundRect(inactiveTrackRect!!, cornerRadius, cornerRadius, inactiveTrackPaint)
    }

    //  Bubble drawing

    private fun drawBubbleOutline(canvas: Canvas, x: Float) {
        bubblePath = Path().apply {
            fillType = Path.FillType.EVEN_ODD
            moveTo(getBubbleHorizontalOffset(x), topPadding)

            val comparatorVar1 = x - (bubblePathWidth / 2 - sidePadding / 2)
            val comparatorVar2 = sidePadding / 2
            val comparatorVar3 = measuredWidth - sidePadding / 2 - bubblePathWidth

            val tailStart = when {
                comparatorVar2 > comparatorVar1 -> {
                    bubblePathWidth / 2 + min(
                        sidePadding / 2 + thumbSliderRadius + dp(2f),
                        comparatorVar2 - comparatorVar1
                    )
                }
                comparatorVar1 > comparatorVar3 -> {
                    bubblePathWidth / 2 - min(
                        sidePadding / 2 + thumbSliderRadius + dp(2f),
                        comparatorVar1 - comparatorVar3
                    )
                }
                else -> bubblePathWidth / 2
            }
            val tailEnd = bubblePathWidth - tailStart

            rLineTo(bubblePathWidth, 0f)
            rLineTo(0f, bubbleHeight)
            rLineTo(-(tailStart - dp(3f)), 0f)
            rLineTo(-dp(3f), dp(4f))
            rLineTo(-dp(3f), -dp(4f))
            rLineTo(-(tailEnd - dp(3f)), 0f)
            rLineTo(0f, -bubbleHeight)

            close()
        }
        canvas.drawPath(bubblePath, bubblePaint)
    }

    //  Text value drawing

    private fun drawBubbleValue(canvas: Canvas, x: Float) {
        val displayValue = getDisplayValue()

        val previousText = bubbleText
        if (actualFractionalValue == limitValue && !allowLimitValueBypass) {
            if (vibrateOnLimitReached) {
                if (!bubbleText.contains(limitValue.toString()) && previousText.isNotEmpty()) {
                    context.vibrate(50)
                }
            }
            bubbleText = "$limitValueIndicator ${getUnitValue(limitValue!!)}"
            currentValue = limitValue!!
        } else {
            bubbleText = getUnitValue(displayValue)
            currentValue = displayValue
        }

        if (previousText != bubbleText && previousText.isNotEmpty()) {
            onValueChanged?.invoke(currentValue)
        }

        bubbleTextPaint.getTextBounds(bubbleText, 0, bubbleText.length, bubbleTextRect)
        canvas.apply {
            save()
            translate(getBubbleTextHorizontalOffset(x), getBubbleTextVerticalOffset())
            formTextLayout(bubbleText, bubbleTextPaint).draw(this)
            restore()
        }
    }

    private fun drawTitleLabelText(canvas: Canvas) {
        titleTextPaint.getTextBounds(title, 0, title.length, titleTextRect)
        canvas.apply {
            save()
            translate(sidePadding, getTitleLabelTextVerticalOffset())
            formTextLayout(title, titleTextPaint).draw(this)
            restore()
        }
    }

    private fun drawMinRangeText(canvas: Canvas) {
        val textString = getUnitValue(minValue)
        rangeTextPaint.getTextBounds(textString, 0, textString.length, minRangeTextRect)
        canvas.apply {
            save()
            translate(sidePadding, getRangeTextVerticalOffset())
            formTextLayout(textString, rangeTextPaint).draw(this)
            restore()
        }
    }

    private fun drawMaxRangeText(canvas: Canvas) {
        val textString = getUnitValue(maxValue)
        rangeTextPaint.getTextBounds(textString, 0, textString.length, maxRangeTextRect)
        canvas.apply {
            save()
            translate(getMaxRangeTextHorizontalOffset(), getRangeTextVerticalOffset())
            formTextLayout(textString, rangeTextPaint).draw(this)
            restore()
        }
    }

    private fun getDisplayValue(): Int {
        return actualFractionalValue.div(slidingInterval) * slidingInterval
    }

    //  Margin methods

    private fun getSlidingTrackVerticalOffset(): Float {
        return bubbleHeight + dp(8f) + titleTextRect.height() + dp(8f) + thumbSliderRadius
    }

    private fun getBubbleHorizontalOffset(x: Float): Float {
        return min(
            measuredWidth - sidePadding / 2 - bubblePathWidth,
            max(sidePadding / 2, x - (bubblePathWidth / 2 - sidePadding / 2))
        )
    }

    private fun getBubbleTextVerticalOffset(): Float {
        return (bubbleHeight - bubbleTextRect.height()) / 2 - dp(2f)
    }

    private fun getBubbleTextHorizontalOffset(x: Float): Float {
        bubblePathWidth = max(minimumBubbleWidth, bubbleTextRect.width() + bubbleTextPadding * 2)
        return min(
            measuredWidth - sidePadding / 2 - bubbleTextRect.width() - ((bubblePathWidth - bubbleTextRect.width()) / 2),
            max(
                bubblePathWidth / 2 - bubbleTextRect.width() / 2 + sidePadding / 2,
                x - bubbleTextRect.width() / 2 + sidePadding / 2
            )
        )
    }

    private fun getTitleLabelTextVerticalOffset(): Float {
        return bubbleHeight + topPadding + dp(5f)
    }

    private fun getRangeTextVerticalOffset(): Float {
        return inactiveTrackRect!!.bottom + thumbSliderRadius + dp(2f)
    }

    private fun getMaxRangeTextHorizontalOffset(): Float {
        return measuredWidth - maxRangeTextRect.width() - sidePadding
    }

    //  Disabled state

    private fun initializeDisabledStatePaint() {
        val colorMatrix = ColorMatrix()
        colorMatrix.set(
            floatArrayOf(
                0.33f, 0.33f, 0.33f, 0f, 0f,
                0.33f, 0.33f, 0.33f, 0f, 0f,
                0.33f, 0.33f, 0.33f, 0f, 0f,
                0f, 0f, 0f, 1f, 0f
            )
        )
        disabledStatePaint.colorFilter = ColorMatrixColorFilter(colorMatrix)
    }

    //  Helper methods

    private fun formTextLayout(text: String, paint: TextPaint): StaticLayout {
        return if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O_MR1) {
            val builder = StaticLayout.Builder.obtain(text, 0, text.length, paint, measuredWidth)
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            builder.build()
        } else {
            StaticLayout(text, paint, width, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false)
        }
    }

    private fun getUnitValue(value: Int): String {
        return if (unitPosition == UnitPosition.FRONT) {
            value.toString().plus(unit)
        } else {
            value.toString().plus(" ").plus(unit)
        }
    }

    private fun dp(dp: Float): Float {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics)
    }
}