package com.bjorn.claudepad

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs

class TrackpadView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    interface Listener {
        fun onMove(dx: Int, dy: Int)
        fun onLeftClick()
        fun onRightClick()
        fun onScroll(notches: Int)
        fun onDragStart()
        fun onDragEnd()
    }

    var listener: Listener? = null
    var sensitivity = 1.4f

    private val paint = Paint().apply {
        color = Color.parseColor("#66FFFFFF")
        textSize = 42f
        textAlign = Paint.Align.CENTER
    }

    private var lastX = 0f
    private var lastY = 0f
    private var downX = 0f
    private var downY = 0f
    private var downTime = 0L
    private var lastTapTime = 0L
    private var maxPointers = 0
    private var moved = false
    private var dragging = false
    private var scrollAccum = 0f

    private val tapTimeout = 250L
    private val doubleTapTimeout = 300L
    private val touchSlop = 24f
    private val scrollStep = 60f

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(Color.parseColor("#1E1E28"))
        canvas.drawText(
            "Trackpad", width / 2f, height / 2f - 20f, paint
        )
        paint.textSize = 28f
        canvas.drawText(
            "1 jari: gerak/tap · 2 jari: scroll/klik kanan · tap 2x tahan: drag",
            width / 2f, height / 2f + 30f, paint
        )
        paint.textSize = 42f
    }

    override fun onTouchEvent(e: MotionEvent): Boolean {
        when (e.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                lastX = e.x; lastY = e.y
                downX = e.x; downY = e.y
                downTime = System.currentTimeMillis()
                maxPointers = 1
                moved = false
                scrollAccum = 0f
                if (downTime - lastTapTime < doubleTapTimeout) {
                    dragging = true
                    listener?.onDragStart()
                }
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                maxPointers = maxOf(maxPointers, e.pointerCount)
                lastX = e.getX(0); lastY = e.getY(0)
            }
            MotionEvent.ACTION_MOVE -> {
                val x = e.getX(0); val y = e.getY(0)
                val dx = x - lastX; val dy = y - lastY
                if (abs(x - downX) > touchSlop || abs(y - downY) > touchSlop) moved = true
                if (e.pointerCount >= 2) {
                    scrollAccum += dy
                    while (scrollAccum >= scrollStep) {
                        listener?.onScroll(120); scrollAccum -= scrollStep
                    }
                    while (scrollAccum <= -scrollStep) {
                        listener?.onScroll(-120); scrollAccum += scrollStep
                    }
                } else if (moved || dragging) {
                    listener?.onMove((dx * sensitivity).toInt(), (dy * sensitivity).toInt())
                }
                lastX = x; lastY = y
            }
            MotionEvent.ACTION_UP -> {
                val now = System.currentTimeMillis()
                if (dragging) {
                    dragging = false
                    listener?.onDragEnd()
                } else if (!moved && now - downTime < tapTimeout) {
                    if (maxPointers >= 2) listener?.onRightClick()
                    else { listener?.onLeftClick(); lastTapTime = now }
                }
            }
            MotionEvent.ACTION_CANCEL -> {
                if (dragging) { dragging = false; listener?.onDragEnd() }
            }
        }
        return true
    }
}
