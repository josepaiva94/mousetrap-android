package pt.up.fc.dcc.mousetrap.activity;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

/**
 * Resizable image view which keeps aspect ratio
 *
 * @author José C. Paiva <up201200272@fc.up.pt>
 */
public class ResizableImageView extends AppCompatImageView {

    public ResizableImageView(Context context) {
        super(context);
    }

    public ResizableImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ResizableImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        Drawable d = getDrawable();
        if (d != null) {
            int w = MeasureSpec.getSize(widthMeasureSpec);
            int h = Math.max(w * d.getIntrinsicHeight() / d.getIntrinsicWidth(), w);
            setMeasuredDimension(w, h);
        }
        else super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
