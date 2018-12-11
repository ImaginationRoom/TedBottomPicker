package gun0912.tedbottompicker.view

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.widget.ImageView

import gun0912.tedbottompicker.R


/**
 * Created by Gil on 09/06/2014.
 */
class TedSquareImageView : ImageView {

    internal var fit_mode: String? = null
    private var mForeground: Drawable? = null

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {

        val a = context.theme.obtainStyledAttributes(
                attrs,
                R.styleable.TedBottomPickerImageView,
                0, 0)

        val foreground = a.getDrawable(R.styleable.TedBottomPickerImageView_foreground)
        if (foreground != null) {
            setForeground(foreground)
        }


        try {
            fit_mode = a.getString(R.styleable.TedBottomPickerImageView_fit_mode)

        } finally {
            a.recycle()
        }
    }


    //Squares the thumbnail
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)


        if ("height" == fit_mode) {
            setMeasuredDimension(heightMeasureSpec, heightMeasureSpec)

        } else {
            setMeasuredDimension(widthMeasureSpec, widthMeasureSpec)

        }


        if (mForeground != null) {
            mForeground!!.setBounds(0, 0, measuredWidth, measuredHeight)
            invalidate()
        }


    }


    /**
     * Supply a Drawable that is to be rendered on top of all of the child views
     * in the frame layout.
     *
     * @param drawable The Drawable to be drawn on top of the children.
     */
    override fun setForeground(drawable: Drawable?) {
        if (mForeground === drawable) {
            return
        }
        if (mForeground != null) {
            mForeground!!.callback = null
            unscheduleDrawable(mForeground)
        }

        mForeground = drawable

        if (drawable != null) {
            drawable.callback = this
            if (drawable.isStateful) {
                drawable.state = drawableState
            }
        }
        requestLayout()
        invalidate()
    }


    override fun verifyDrawable(who: Drawable): Boolean {
        return super.verifyDrawable(who) || who === mForeground
    }

    override fun jumpDrawablesToCurrentState() {
        super.jumpDrawablesToCurrentState()
        if (mForeground != null)
            mForeground!!.jumpToCurrentState()
    }

    override fun drawableStateChanged() {
        super.drawableStateChanged()
        if (mForeground != null && mForeground!!.isStateful) {
            mForeground!!.state = drawableState
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (mForeground != null) {
            mForeground!!.setBounds(0, 0, w, h)
            invalidate()
        }
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)

        if (mForeground != null) {
            mForeground!!.draw(canvas)
        }
    }


}
