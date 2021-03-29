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
        set(value) {
            field = value
            thumbSliderPaint.color = value
        }
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

    private val bubbleValueRect = Rect()
    private var bubbleValuePaint = TextPaint(Paint.ANTI_ALIAS_FLAG)

    private val titleRect = Rect()
    private var titlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG)

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
        thumbSliderRadius = styledAttributes.getDimension(
            R.styleable.LabeledSeekSlider_lss_thumbSliderRadius,
            thumbSliderRadius
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)


        getActiveX(currentValue).also { x ->
            drawBubbleValue(canvas, x)
            drawBubbleOutline(canvas, x, CENTER)
            drawTitle(canvas)

            val activeTrackRect = RectF(
                sidePadding,
                bubbleHeight + dp(8f) + titleRect.height() + dp(8f) + thumbSliderRadius,
                measuredWidth.toFloat() - sidePadding,
                bubbleHeight + dp(8f) + titleRect.height() + dp(8f) + thumbSliderRadius + trackHeight
            )
            val cornerRadius = trackHeight / 2
            canvas.drawRoundRect(activeTrackRect, cornerRadius, cornerRadius, activeTrackPaint)

            drawThumbSlider(canvas, x, activeTrackRect.centerY())
        }
    }

    private fun getActiveX(currentValue: Int): Float {
        val slidingAreaWidth = measuredWidth - sidePadding
        val progress = (currentValue - minValue).toFloat() / (maxValue - minValue).toFloat()
        return slidingAreaWidth * progress
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        return super.onTouchEvent(event)
    }

    private fun moveContent(x: Float, canvas: Canvas) {

    }

    private fun drawThumbSlider(
        canvas: Canvas,
        x: Float,
        y: Float
    ) {
        canvas.drawCircle(
            max(sidePadding + thumbSliderRadius, x + sidePadding / 2 + thumbSliderRadius),
            y,
            thumbSliderRadius,
            thumbSliderPaint
        )
    }

    //  Bubble drawing

    private fun drawBubbleOutline(
        canvas: Canvas,
        x: Float,
        alignment: BubblePointerAlignment
    ) {
        bubblePath = Path()
        bubblePath.fillType = Path.FillType.EVEN_ODD

        val bubbleStartX = max(sidePadding / 2, x - (bubblePathWidth / 2 - sidePadding / 2))

        bubblePath.moveTo(bubbleStartX, topPadding)
        bubblePath.rLineTo(bubblePathWidth, 0f)
        bubblePath.rLineTo(0f, bubbleHeight)
        bubblePath.rLineTo(-(bubblePathWidth / 2 - dp(3f)), 0f)
        bubblePath.rLineTo(-dp(3f), dp(4f))
        bubblePath.rLineTo(-dp(3f), -dp(4f))
        bubblePath.rLineTo(-(bubblePathWidth / 2 - dp(3f)), 0f)
        bubblePath.rLineTo(0f, -bubbleHeight)

        bubblePath.close()

        canvas.drawPath(bubblePath, bubblePaint)
    }

    private fun drawBubbleValue(canvas: Canvas, x: Float) {
        val bubbleValueText = if (unitPosition == UnitPosition.FRONT) {
            currentValue.toString().plus(unit)
        } else {
            currentValue.toString().plus(" ").plus(unit)
        }

        bubbleValuePaint.color = bubbleValueTextColor
        bubbleValuePaint.typeface = bubbleValueTextFont
        bubbleValuePaint.textSize = bubbleValueTextSize
        bubbleValuePaint.getTextBounds(bubbleValueText, 0, bubbleValueText.length, bubbleValueRect)

        val textLayout = if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O_MR1) {
            val builder = StaticLayout.Builder.obtain(bubbleValueText, 0, bubbleValueText.length, bubbleValuePaint, measuredWidth)
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            builder.build()
        } else {
            StaticLayout(bubbleValueText, bubbleValuePaint, width, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false)
        }

        canvas.save()

        bubblePathWidth = max(minimumBubbleWidth, bubbleValueRect.width() + bubbleTextPadding * 2)

        val startX = max(
            bubblePathWidth / 2 - bubbleValueRect.width() / 2 + sidePadding / 2,
            x - bubbleValueRect.width() / 2 + sidePadding / 2
        )
        canvas.translate(
            startX,
            (bubbleHeight - bubbleValueRect.height()) / 2 - dp(1f)
        )
        textLayout.draw(canvas)

        canvas.restore()
    }

    private fun drawTitle(canvas: Canvas) {
        titlePaint.apply {
            color = titleTextColor
            typeface = titleTextFont
            textSize = titleTextSize
            getTextBounds(title, 0, title.length, titleRect)
        }
        canvas.apply {
            save()
            translate(sidePadding, bubbleHeight + dp(7f))
            formTextLayout(title, titlePaint).draw(this)
            restore()
        }
    }

    private fun drawMinRangeValue(canvas: Canvas) {

    }

    private fun drawMaxRangeValue(canvas: Canvas) {

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

    private fun dp(dp: Float): Float {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics)
    }
}