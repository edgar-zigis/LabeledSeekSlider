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
import android.view.View
import androidx.core.content.res.ResourcesCompat
import com.zigis.labeledseekslider.custom.BubblePointerAlignment
import com.zigis.labeledseekslider.custom.BubblePointerAlignment.*
import com.zigis.labeledseekslider.custom.UnitPosition
import kotlin.math.max


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
        set(value) {
            field = value
            activeTrackPaint.color = value
        }
    var inactiveTrackColor = Color.parseColor("#E8E8E8")
    var thumbSliderBackgroundColor = Color.parseColor("#FFFFFF")
    var bubbleValueTextColor = Color.parseColor("#1A1A1A")
    var bubbleOutlineColor = Color.parseColor("#E8E8E8")
        set(value) {
            field = value
            bubblePaint.color = value
        }
    var titleTextColor = Color.parseColor("#AFB6BB")
    var rangeValueTextColor = Color.parseColor("#AFB6BB")

    var titleTextFont = Typeface.create("sans-serif", Typeface.NORMAL)
    var rangeValueTextFont = Typeface.create("sans-serif", Typeface.NORMAL)
    var bubbleValueTextFont = Typeface.create("sans-serif", Typeface.BOLD)

    var bubbleValueTextSize = dp(14f)
    var titleTextSize = dp(12f)
    var rangeValueTextSize = dp(12f)

    var slidingInterval: Int = 1

    private var trackHeight = dp(4f)
    private var thumbSliderSize = dp(24f)

    //  Operational vars

    private val topPadding = dp(2f)
    private val sidePadding = dp(16f)
    private val bubbleHeight = dp(34f)
    private val minimumBubbleWidth = dp(84f)
    private val bubbleTextPadding = dp(16f)

    private val activeTrackPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        it.style = Paint.Style.FILL_AND_STROKE
    }

    private val bubblePaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.style = Paint.Style.STROKE
        it.strokeWidth = dp(2f)
        it.strokeCap = Paint.Cap.ROUND
        it.pathEffect = CornerPathEffect(dp(4f))
    }
    private var bubblePath = Path()

    private val labelRect = Rect()
    private var labelPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)

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
        defaultValue = styledAttributes.getInteger(R.styleable.LabeledSeekSlider_lss_defaultValue, defaultValue)
        limitValue = styledAttributes.getInteger(R.styleable.LabeledSeekSlider_lss_limitValue, limitValue)
        title = styledAttributes.getString(R.styleable.LabeledSeekSlider_lss_title) ?: title
        unit = styledAttributes.getString(R.styleable.LabeledSeekSlider_lss_unit) ?: unit
        unitPosition = UnitPosition.parse(styledAttributes.getInt(
            R.styleable.LabeledSeekSlider_lss_unitPosition,
            UnitPosition.BACK.value
        ))
        isDisabled = styledAttributes.getBoolean(R.styleable.LabeledSeekSlider_lss_isDisabled, isDisabled)

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

        val titleTextFontRes = styledAttributes.getResourceId(R.styleable.LabeledSeekSlider_lss_titleTextFont, 0)
        if (titleTextFontRes > 0) {
            titleTextFont = ResourcesCompat.getFont(context, titleTextFontRes) ?: titleTextFont
        }
        val rangeValueTextFontRes = styledAttributes.getResourceId(R.styleable.LabeledSeekSlider_lss_rangeValueTextFont, 0)
        if (rangeValueTextFontRes > 0) {
            rangeValueTextFont = ResourcesCompat.getFont(context, rangeValueTextFontRes) ?: rangeValueTextFont
        }
        val bubbleValueTextFontRes = styledAttributes.getResourceId(R.styleable.LabeledSeekSlider_lss_bubbleValueTextFont, 0)
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
        thumbSliderSize = styledAttributes.getDimension(
            R.styleable.LabeledSeekSlider_lss_thumbSliderSize,
            thumbSliderSize
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val activeTrackRect = RectF(dp(16f), 0f, measuredWidth.toFloat() - dp(16f), trackHeight)
        val cornerRadius = trackHeight / 2

        canvas.drawRoundRect(activeTrackRect, cornerRadius, cornerRadius, activeTrackPaint)

        drawTitle(canvas)

        drawBubbleOutline(x = getActiveX(defaultValue), wrappedTextWidth = dp(40f), alignment = CENTER)
        canvas.drawPath(bubblePath, bubblePaint)
    }

    private fun getActiveX(currentValue: Int): Float {
        val slidingAreaWidth = measuredWidth - sidePadding
        val progress = (currentValue - minValue).toFloat() / (maxValue - minValue).toFloat()
        return slidingAreaWidth * progress
    }

    private fun drawBubbleOutline(
        x: Float,
        wrappedTextWidth: Float,
        alignment: BubblePointerAlignment
    ) {
        val bubbleWidth = max(minimumBubbleWidth, wrappedTextWidth + bubbleTextPadding * 2)

        bubblePath = Path()
        bubblePath.fillType = Path.FillType.EVEN_ODD

        bubblePath.moveTo(x - (bubbleWidth / 2 - sidePadding / 2), topPadding)
        bubblePath.rLineTo(bubbleWidth, 0f)
        bubblePath.rLineTo(0f, bubbleHeight - dp(8f))
        bubblePath.rLineTo(-(bubbleWidth / 2 - dp(4f)), 0f)
        bubblePath.rLineTo(-dp(4f), dp(8f))
        bubblePath.rLineTo(-dp(4f), -dp(8f))
        bubblePath.rLineTo(-(bubbleWidth / 2 - dp(4f)), 0f)
        bubblePath.rLineTo(0f, -(bubbleHeight - dp(8f)))

        bubblePath.close()
    }

    private fun drawTitle(canvas: Canvas) {
        labelPaint.color = titleTextColor
        labelPaint.typeface = titleTextFont
        labelPaint.textSize = titleTextSize
        labelPaint.getTextBounds(title, 0, title.length, labelRect)

        val textLayout = if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O_MR1) {
            val builder = StaticLayout.Builder.obtain(title, 0, title.length, labelPaint, measuredWidth)
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            builder.build()
        } else {
            StaticLayout(title, labelPaint, width, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false)
        }

        canvas.save()

        canvas.translate(sidePadding, 0f)
        textLayout.draw(canvas)

        canvas.restore()
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