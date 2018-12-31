package br.com.simplepass.loading_button_lib.customViews

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import br.com.simplepass.loading_button_lib.animatedDrawables.CircularProgressAnimatedDrawable
import br.com.simplepass.loading_button_lib.animatedDrawables.CircularRevealAnimatedDrawable
import br.com.simplepass.loading_button_lib.disposeAnimator
import br.com.simplepass.loading_button_lib.presentation.ProgressButtonPresenter

class CircularProgressButton : AppCompatButton, ProgressButton {

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(attrs, defStyleAttr)
    }

    override var paddingProgress = 0F

    override var spinningBarWidth = 10F
    override var spinningBarColor = ContextCompat.getColor(context, android.R.color.black)

    override var finalCorner = 0F
    override var initialCorner = 0F

    override var doneFillColor: Int = ContextCompat.getColor(context, android.R.color.black)
    override lateinit var doneImage: Bitmap

    private val finalWidth: Int by lazy { finalHeight }
    var initialWidth = 0

    private val finalHeight: Int by lazy { height }
    private val initialHeight: Int by lazy { height }

    override lateinit var drawable: GradientDrawable
    val initialText: CharSequence = text

    private val presenter = ProgressButtonPresenter(this)

    internal val morphAnimator by lazy {
        AnimatorSet().apply {
            playTogether(
                cornerAnimator(drawable, initialCorner, finalCorner),
                widthAnimator(this@CircularProgressButton, initialWidth, finalWidth),
                heightAnimator(this@CircularProgressButton, initialHeight, finalHeight)
            )

            addListener(morphListener(presenter::morphStart, presenter::morphEnd))
        }
    }

    private val morphRevertAnimator by lazy {
        AnimatorSet().apply {
            playTogether(
                cornerAnimator(drawable, finalCorner, initialCorner),
                widthAnimator(this@CircularProgressButton, finalWidth, initialWidth),
                heightAnimator(this@CircularProgressButton, finalHeight, initialHeight)
            )

            addListener(morphListener(presenter::morphRevertStart, presenter::morphRevertEnd))
        }
    }

    private val progressAnimatedDrawable: CircularProgressAnimatedDrawable by lazy {
        createProgressDrawable()
    }

    private val revealAnimatedDrawable: CircularRevealAnimatedDrawable by lazy {
        createRevealAnimatedDrawable()
    }

    internal fun drawProgress(canvas: Canvas) {
        progressAnimatedDrawable.run {
            if (isRunning) {
                draw(canvas)
            } else {
                start()
            }
        }
    }

    internal fun drawDoneAnimation(canvas: Canvas) {
        revealAnimatedDrawable.draw(canvas)
    }

    internal fun startRevealAnimation() {
        revealAnimatedDrawable.start()
    }

    internal fun startMorphAnimation() {
        morphAnimator.start()
    }

    internal fun stopProgressAnimation() {
        progressAnimatedDrawable.stop()
    }

    internal fun stopMorphAnimation() {
        morphAnimator.end()
    }

    fun startAnimation() {
        presenter.startAnimation()
    }

    fun revertAnimation(onAnimationEndListener: () -> Unit = {}) {
        presenter.revertAnimation()

        morphRevertAnimator.apply {
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    onAnimationEndListener()
                }
            })
        }.start()
    }

    fun stopAnimation() {
        presenter.stopAnimation()
    }

    fun doneLoadingAnimation(fillColor: Int, bitmap: Bitmap) {
        presenter.doneLoadingAnimation(fillColor, bitmap)
    }

    fun dispose() {
        morphAnimator.disposeAnimator()
        morphRevertAnimator.disposeAnimator()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        presenter.onDraw(canvas)
    }
}
