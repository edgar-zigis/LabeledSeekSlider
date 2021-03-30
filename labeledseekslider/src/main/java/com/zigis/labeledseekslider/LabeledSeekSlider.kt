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
import com.zigis.labeledseekslider.custom.BubblePointerAlignment
import com.zigis.labeledseekslider.custom.BubblePointerAlignment.CENTER
import com.zigis.labeledseekslider.custom.UnitPosition
import com.zigis.labeledseekslider.custom.vibrate
import kotlin.math.max
import kotlin.math.min

@Suppress("DEPRECATION")
@SuppressLint("DrawAllocation")
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
    var limitValueIndicator: String = "Max"
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

    //  Read-only public vars

    var currentValue: Int = 150
        private set

    //  Operational vars

    private val topPadding = dp(2f)
    private val sidePadding = dp(16f)
    private val bubbleHeight = dp(26f)
    private val minimumBubbleWidth = dp(84f)
    private val bubbleTextPadding = dp(16f)

    private val thumbSliderPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.style = Paint.Style.FILL
        it.setShadowLayer(dp(4f), 0f, 1f, Color.LTGRAY)
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

        val styledAttributes = context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.LabeledSeekSlider,
            0,
            0
        )

        minValue = styledAttributes.getInteger(R.styleable.LabeledSeekSlider_lss_minValue, minValue)
        maxValue = styledAttributes.getInteger(R.styleable.LabeledSeekSlider_lss_maxValue, maxValue)
        defaultValue = styledAttributes.getInteger(
            R.styleable.LabeledSeekSlider_lss_defaultValue,
            defaultValue
        ).also {
            currentValue = min(maxValue, max(minValue, it))
        }

        limitValue = styledAttributes.getInteger(R.styleable.LabeledSeekSlider_lss_limitValue, limitValue)
        limitValueIndicator = styledAttributes.getString(R.styleable.LabeledSeekSlider_lss_limitValueIndicator) ?: limitValueIndicator
        title = styledAttributes.getString(R.styleable.LabeledSeekSlider_lss_title) ?: title
        unit = styledAttributes.getString(R.styleable.LabeledSeekSlider_lss_unit) ?: unit
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

        val titleTextFontRes = styledAttributes.getResourceId(
            R.styleable.LabeledSeekSlider_lss_titleTextFont,
            0
        )
        if (titleTextFontRes > 0) {
            titleTextFont = ResourcesCompat.getFont(context, titleTextFontRes) ?: titleTextFont
        }
        val rangeValueTextFontRes = styledAttributes.getResourceId(
            R.styleable.LabeledSeekSlider_lss_rangeValueTextFont,
            0
        )
        if (rangeValueTextFontRes > 0) {
            rangeValueTextFont = ResourcesCompat.getFont(context, rangeValueTextFontRes) ?: rangeValueTextFont
        }
        val bubbleValueTextFontRes = styledAttributes.getResourceId(
            R.styleable.LabeledSeekSlider_lss_bubbleValueTextFont,
            0
        )
        if (bubbleValueTextFontRes > 0) {
            bubbleValueTextFont = ResourcesCompat.getFont(context, bubbleValueTextFontRes) ?: bubbleValueTextFont
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
        getActiveX(currentValue).also { x ->
            drawBubbleValue(canvas, x)
            drawBubbleOutline(canvas, x, CENTER)
            drawTitleLabelText(canvas)
            drawInactiveTrack(canvas)
            drawActiveTrack(canvas, x)
            drawThumbSlider(canvas, x)
            drawMinRangeText(canvas)
            drawMaxRangeText(canvas)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        return when (event.action) {
            ACTION_DOWN, ACTION_MOVE, ACTION_UP -> handleSlidingMovement(event.x)
            else -> false
        }
    }

    private fun handleSlidingMovement(x: Float): Boolean {
        val relativeX = min(measuredWidth - sidePadding - thumbSliderRadius, max(sidePadding / 2 - thumbSliderRadius, x))
        val slidingAreaWidth = measuredWidth - sidePadding - thumbSliderRadius

        val newValue = minValue + ((maxValue - minValue) * (relativeX / slidingAreaWidth)).toInt()
        currentValue = if (limitValue == -1) {
            newValue
        } else min(newValue, limitValue)

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

    private fun drawBubbleOutline(
        canvas: Canvas,
        x: Float,
        alignment: BubblePointerAlignment
    ) {
        bubblePath = Path().apply {
            fillType = Path.FillType.EVEN_ODD
            moveTo(getBubbleHorizontalOffset(x), topPadding)

            rLineTo(bubblePathWidth, 0f)
            rLineTo(0f, bubbleHeight)
            rLineTo(-(bubblePathWidth / 2 - dp(3f)), 0f)
            rLineTo(-dp(3f), dp(4f))
            rLineTo(-dp(3f), -dp(4f))
            rLineTo(-(bubblePathWidth / 2 - dp(3f)), 0f)
            rLineTo(0f, -bubbleHeight)

            close()
        }
        canvas.drawPath(bubblePath, bubblePaint)
    }

    //  Text value drawing

    private fun drawBubbleValue(canvas: Canvas, x: Float) {
        val textString = getUnitValue(currentValue)
        if (currentValue == limitValue) {
            if (!bubbleText.contains(limitValue.toString())) {
                context.vibrate(50)
            }
            bubbleText = "$limitValueIndicator $textString"
        } else bubbleText = textString

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