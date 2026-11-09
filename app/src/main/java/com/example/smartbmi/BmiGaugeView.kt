package com.example.smartbmi

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import kotlin.math.cos
import kotlin.math.sin

class BmiGaugeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val gaugePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 24f
        strokeCap = Paint.Cap.ROUND
    }

    private val needlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        strokeWidth = 5f
        strokeCap = Paint.Cap.ROUND
    }

    private val centerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.FILL
    }

    private val valuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 48f
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
    }

    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(85, 214, 232)
        textSize = 20f
        textAlign = Paint.Align.CENTER
    }

    private var bmiValue = 0f

    fun setBmi(value: Float) {
        bmiValue = value.coerceIn(10f, 40f)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val cx = width / 2f
        val radius = minOf(width * 0.36f, height * 0.72f)

        // Position the gauge so the entire upper semicircle fits.
        val cy = height * 0.82f

        val oval = RectF(
            cx - radius,
            cy - radius,
            cx + radius,
            cy + radius
        )

        val colors = listOf(
            Color.rgb(85, 184, 232),
            Color.rgb(76, 175, 80),
            Color.rgb(255, 193, 7),
            Color.rgb(244, 67, 54)
        )

        // Draw the four coloured gauge sections.
        val sectionSweep = 45f

        for (i in colors.indices) {
            gaugePaint.color = colors[i]

            canvas.drawArc(
                oval,
                180f + (i * sectionSweep),
                sectionSweep,
                false,
                gaugePaint
            )
        }

        // Keep the initial needle at the beginning of the gauge.
        val angle = if (bmiValue > 0f) {
            180f + ((bmiValue - 10f) / 30f) * 180f
        } else {
            180f
        }

        val radians = Math.toRadians(angle.toDouble())
        val needleLength = radius * 0.78f

        val needleX = cx + cos(radians).toFloat() * needleLength
        val needleY = cy + sin(radians).toFloat() * needleLength

        canvas.drawLine(
            cx,
            cy,
            needleX,
            needleY,
            needlePaint
        )

        canvas.drawCircle(cx, cy, 10f, centerPaint)

        val displayValue = if (bmiValue > 0f) {
            String.format(java.util.Locale.US, "%.1f", bmiValue)
        } else {
            "--"
        }

        canvas.drawText(
            displayValue,
            cx,
            cy - 30f,
            valuePaint
        )

        canvas.drawText(
            "BMI",
            cx,
            cy + 30f,
            labelPaint
        )
    }
}
