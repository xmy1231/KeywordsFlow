package com.keywordsflow.app.view;

import android.content.Context;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.util.TypedValue;
import android.view.ViewTreeObserver;
import android.view.animation.*;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.LinkedList;
import java.util.Random;
import java.util.Vector;

/**
 * Created by Jacky.Xu on 2015/9/18.
 * 文字飞入飞去效果
 */
public class KeywordsFlow extends FrameLayout implements ViewTreeObserver.OnGlobalLayoutListener {
    public static final int IDX_X = 0;
    public static final int IDX_Y = 1;
    public static final int IDX_TXT_LENGTH = 2;
    public static final int IDX_DIS_Y = 3;
    /** 由外至内的动画。 */
    public static final int ANIMATION_IN = 1;
    /** 由内至外的动画。 */
    public static final int ANIMATION_OUT = 2;
    /** 位移动画类型：从外围移动到坐标点。 */
    public static final int OUTSIDE_TO_LOCATION = 1;
    /** 位移动画类型：从坐标点移动到外围。 */
    public static final int LOCATION_TO_OUTSIDE = 2;
    /** 位移动画类型：从中心点移动到坐标点。 */
    public static final int CENTER_TO_LOCATION = 3;
    /** 位移动画类型：从坐标点移动到中心点。 */
    public static final int LOCATION_TO_CENTER = 4;
    public static final long ANIM_DURATION = 800l;
    public static final int MAX = 10;
    public static final int TEXT_SIZE_MAX = 25;
    public static final int TEXT_SIZE_MIN = 15;
    private OnClickListener itemClickListener;
    private static Interpolator interpolator;
    private static AlphaAnimation animAlpha2Opaque;
    private static AlphaAnimation animAlpha2Transparent;
    private static ScaleAnimation animScaleLarge2Normal, animScaleNormal2Large, animScaleZero2Normal,
            animScaleNormal2Zero;
    /** 存储显示的关键字 */
    private Vector<String> vecKeywords;
    private int width, height;
    /**
     * go2Show()中被赋值为true，标识开发人员触发其开始动画显示。
     * 本标识的作用是防止在填充keywrods未完成的过程中获取到width和height后提前启动动画。
     * 在show()方法中其被赋值为false。
     * 真正能够动画显示的另一必要条件：width 和 height不为0。
     */
    private boolean enableShow;
    private Random random;

    private int txtAnimInType, txtAnimOutType;
    /** 最近一次启动动画显示的时间。 */
    private long lastStartAnimationTime;
    /** 动画运行时间。 */
    private long animDuration;

    public KeywordsFlow(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public KeywordsFlow(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public KeywordsFlow(Context context) {
        super(context);
        init();
    }

    private void init() {
        lastStartAnimationTime = 0l;
        animDuration = ANIM_DURATION;
        random = new Random();
        vecKeywords = new Vector<String>(MAX);
        getViewTreeObserver().addOnGlobalLayoutListener(this);
        interpolator = AnimationUtils.loadInterpolator(getContext(),
                android.R.anim.decelerate_interpolator);
        animAlpha2Opaque = new AlphaAnimation(0.0f, 1.0f);
        animAlpha2Transparent = new AlphaAnimation(1.0f, 0.0f);
        animScaleLarge2Normal = new ScaleAnimation(2.0f, 1.0f, 2.0f, 1.0f);
        animScaleNormal2Large = new ScaleAnimation(1.0f, 2.0f, 1.0f, 2.0f);
        animScaleZero2Normal = new ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f);
        animScaleNormal2Zero = new ScaleAnimation(1.0f, 0.0f, 1.0f, 0.0f);
    }

    public long getDuration() {
        return animDuration;
    }

    public void setDuration(long duration) {
        animDuration = duration;
    }

    public boolean feedKeyword(String keyword) {
        boolean result = false;
        if (vecKeywords.size() < MAX) {
            result = vecKeywords.add(keyword);
        }
        return result;
    }

    /**
     * 开始动画显示
     * 之前已经存在的TextView将会显示退出动画。
     *
     * @return 正常显示动画返回true；反之为false。返回false原因如下
     *         1.时间上不允许，受lastStartAnimationTime的制约；
     *         2.未获取到width和height的值。
     */
    public boolean go2Show(int animType) {
        if (System.currentTimeMillis() - lastStartAnimationTime > animDuration) {
            enableShow = true;
            if (animType == ANIMATION_IN) {
                txtAnimInType = OUTSIDE_TO_LOCATION;
                txtAnimOutType = LOCATION_TO_CENTER;
            } else if (animType == ANIMATION_OUT) {
                txtAnimInType = CENTER_TO_LOCATION;
                txtAnimOutType = LOCATION_TO_OUTSIDE;
            }
            disapper();
            boolean result = show();
            return result;
        }
        return false;
    }

    private void disapper() {
        int size = getChildCount();
        for (int i = size - 1; i >= 0; i--) {
            final TextView txt = (TextView) getChildAt(i);
            if (txt.getVisibility() == View.GONE) {
                removeView(txt);
            }
            LayoutParams layParams = (LayoutParams) txt.getLayoutParams();

            int[] xy = new int[] { layParams.leftMargin, layParams.topMargin, txt.getWidth() };
            AnimationSet animSet = getAnimationSet(xy, (width >> 1), (height >> 1), txtAnimOutType);
            txt.startAnimation(animSet);
            animSet.setAnimationListener(new Animation.AnimationListener() {
                public void onAnimationStart(Animation animation) {
                }

                public void onAnimationRepeat(Animation animation) {
                }

                public void onAnimationEnd(Animation animation) {
                    txt.setOnClickListener(null);
                    txt.setClickable(false);
                    txt.setVisibility(View.GONE);
                }
            });
        }
    }

    private boolean show() {
        if (width > 0 && height > 0 && vecKeywords != null && vecKeywords.size() > 0 && enableShow) {
            enableShow = false;
            lastStartAnimationTime = System.currentTimeMillis();
            //找到中心点
            int xCenter = width >> 1, yCenter = height >> 1;
            //关键字的个数。
            int size = vecKeywords.size();
            int xItem = width / 8, yItem = height / 8;
            Log.d("ANDROID_LAB", "--------------------------width=" + width +
                    " height=" + height + "  xItem=" + xItem
                    + " yItem=" + yItem + "---------------------------");
            LinkedList<Integer> listX = new LinkedList<Integer>(), listY = new LinkedList<Integer>();
            for (int i = 0; i < size; i++) {
                // 准备随机候选数，分别对应x/y轴位置
                listX.add(i * xItem);
                listY.add(i * yItem + (yItem >> 2));
                Log.e("Change", "ListX:" + (i * xItem) + "#listY:" + (i * yItem + (yItem >> 2)));
            }
            // TextView[] txtArr = new TextView[size];
            LinkedList<TextView> listTxtTop = new LinkedList<TextView>();
            LinkedList<TextView> listTxtBottom = new LinkedList<TextView>();
            for (int i = 0; i < size; i++) {
                String keyword = vecKeywords.get(i);
                // 随机颜色
                int ranColor = 0xff000000 | random.nextInt(0x0077ffff);
                // 随机位置，糙值
                int xy[] = randomXY(random, listX, listY, xItem);

                switch (i){
                    case 0:
                        xy[IDX_X] = (int)Math.round(0.5185185185185185d * ((double) xItem));
                        xy[IDX_Y] = (int)Math.round(3.3333333333333335d * ((double) yItem));
                        break;
                    case 1:
                        xy[IDX_X] = (int)Math.round(1.1111111111111112d * ((double) xItem));
                        xy[IDX_Y] = (int)Math.round(6.0d * ((double) yItem));
                        break;
                    case 2:
                        xy[IDX_X] = (int)Math.round(0.9629629629629629d * ((double) xItem));
                        xy[IDX_Y] = (int)Math.round(1.1666666666666667d * ((double) yItem));
                        break;
                    case 3:
                        xy[IDX_X] = (int)Math.round(3.037037037037037d * ((double) xItem));
                        xy[IDX_Y] = (int)Math.round(4.333333333333333d  * ((double) yItem));
                        break;
                    case 4:
                        xy[IDX_X] = (int)Math.round(3.8518518518518516d * ((double) xItem));
                        xy[IDX_Y] = (int)Math.round(6.166666666666667d * ((double) yItem));
                        break;
                    case 5:
                        xy[IDX_X] = (int)Math.round(5.777777777777778d * ((double) xItem));
                        xy[IDX_Y] = (int)Math.round(3.5d * ((double) yItem));
                        break;
                    case 6:
                        xy[IDX_X] = (int)Math.round(6.111111111111111d * ((double) xItem));
                        xy[IDX_Y] = (int)Math.round(6.166666666666667d * ((double) yItem));
                        break;
                    case 7:
                        xy[IDX_X] = (int)Math.round(6.0d * ((double) xItem));
                        xy[IDX_Y] = (int)Math.round(1.0d * ((double) yItem));
                        break;
                    case 8:
                        xy[IDX_X] = (int)Math.round(3.185185185185185d * ((double) xItem));
                        xy[IDX_Y] = (int)Math.round(0.9166666666666666d * ((double) yItem));
                        break;
                    case 9:
                        xy[IDX_X] = (int)Math.round(4.296296296296297d * ((double) xItem));
                        xy[IDX_Y] = (int)Math.round(2.5d * ((double) yItem));
                        break;
                }
                // 随机字体大小
                int txtSize = TEXT_SIZE_MIN ;
                // 实例化TextView
                final TextView txt = new TextView(getContext());
                txt.setOnClickListener(itemClickListener);
                txt.setText(keyword);
                txt.setTextColor(ranColor);
                txt.setTextSize(TypedValue.COMPLEX_UNIT_SP, txtSize);
//                txt.setShadowLayer(1, 1, 1, 0xdd696969);
                txt.setGravity(Gravity.CENTER);

//                txt.setBackgroundColor(Color.RED);
                 //获取文本长度
                Paint paint = txt.getPaint();
                int strWidth = (int) Math.ceil(paint.measureText(keyword));
                xy[IDX_TXT_LENGTH] = strWidth;
                // 第一次修正:修正x坐标
//                if (xy[IDX_X] + strWidth > width - (xItem >> 1)) {
//                    int baseX = width - strWidth;
//                    // 减少文本右边缘一样的概率
//                    xy[IDX_X] = baseX - xItem + random.nextInt(xItem >> 1);
//                } else if (xy[IDX_X] == 0) {
//                    // 减少文本左边缘一样的概率
//                    xy[IDX_X] = Math.max(random.nextInt(xItem), xItem / 3);
//                }
                xy[IDX_DIS_Y] = Math.abs(xy[IDX_Y] - yCenter);
                txt.setTag(xy);
                if (xy[IDX_Y] > yCenter) {
                    listTxtBottom.add(txt);
                } else {
                    listTxtTop.add(txt);
                }
            }
            attach2Screen(listTxtTop, xCenter, yCenter, yItem);
            attach2Screen(listTxtBottom, xCenter, yCenter, yItem);
            return true;
        }
        return false;
    }

    /** 修正TextView的Y坐标将将其添加到容器上。 */
    private void attach2Screen(LinkedList<TextView> listTxt, int xCenter, int yCenter, int yItem) {
        int size = listTxt.size();
        sortXYList(listTxt, size);
        for (int i = 0; i < size; i++) {
            TextView txt = listTxt.get(i);
            int[] iXY = (int[]) txt.getTag();
            LayoutParams layParams = new LayoutParams
                    (LayoutParams.WRAP_CONTENT,
                            LayoutParams.WRAP_CONTENT);
            layParams.gravity = Gravity.LEFT | Gravity.TOP;
            layParams.leftMargin = iXY[IDX_X];
            layParams.topMargin = iXY[IDX_Y];
            addView(txt, layParams);
            // 动画
            AnimationSet animSet = getAnimationSet(iXY, xCenter, yCenter, txtAnimInType);
            txt.startAnimation(animSet);
        }
    }

    public AnimationSet getAnimationSet(int[] xy, int xCenter, int yCenter, int type) {
        AnimationSet animSet = new AnimationSet(true);
        animSet.setInterpolator(interpolator);
        if (type == OUTSIDE_TO_LOCATION) {
            animSet.addAnimation(animAlpha2Opaque);
            animSet.addAnimation(animScaleLarge2Normal);
            TranslateAnimation translate = new TranslateAnimation(
                    (xy[IDX_X] + (xy[IDX_TXT_LENGTH] >> 1) - xCenter) << 1, 0, (xy[IDX_Y] - yCenter)
                    << 1, 0);
            animSet.addAnimation(translate);
        } else if (type == LOCATION_TO_OUTSIDE) {
            animSet.addAnimation(animAlpha2Transparent);
            animSet.addAnimation(animScaleNormal2Large);
            TranslateAnimation translate = new TranslateAnimation(0,
                    (xy[IDX_X] + (xy[IDX_TXT_LENGTH] >> 1) - xCenter) << 1, 0, (xy[IDX_Y] - yCenter)
                    << 1);
            animSet.addAnimation(translate);
        } else if (type == LOCATION_TO_CENTER) {
            animSet.addAnimation(animAlpha2Transparent);
            animSet.addAnimation(animScaleNormal2Zero);
            TranslateAnimation translate = new TranslateAnimation(0, (-xy[IDX_X] + xCenter), 0, (-xy
                    [IDX_Y] + yCenter));
            animSet.addAnimation(translate);
        } else if (type == CENTER_TO_LOCATION) {
            animSet.addAnimation(animAlpha2Opaque);
            animSet.addAnimation(animScaleZero2Normal);
            TranslateAnimation translate = new TranslateAnimation((-xy[IDX_X] + xCenter), 0, (-xy
                    [IDX_Y] + yCenter), 0);
            animSet.addAnimation(translate);
        }
        animSet.setDuration(animDuration);
        return animSet;
    }

    /**
     * 根据与中心点的距离由近到远进行冒泡排序。
     *
     * @param endIdx 起始位置。
     *
     *
     */
    private void sortXYList(LinkedList<TextView> listTxt, int endIdx) {
        for (int i = 0; i < endIdx; i++) {
            for (int k = i + 1; k < endIdx; k++) {
                if (((int[]) listTxt.get(k).getTag())[IDX_DIS_Y] < ((int[]) listTxt.get(i).getTag())
                        [IDX_DIS_Y]) {
                    TextView iTmp = listTxt.get(i);
                    TextView kTmp = listTxt.get(k);
                    listTxt.set(i, kTmp);
                    listTxt.set(k, iTmp);
                }
            }
        }
    }

    /** A线段与B线段所代表的直线在X轴映射上是否有交集。 */
    private boolean isXMixed(int startA, int endA, int startB, int endB) {
        boolean result = false;
        if (startB >= startA && startB <= endA) {
            result = true;
        } else if (endB >= startA && endB <= endA) {
            result = true;
        } else if (startA >= startB && startA <= endB) {
            result = true;
        } else if (endA >= startB && endA <= endB) {
            result = true;
        }
        return result;
    }

    private int[] randomXY(Random ran, LinkedList<Integer> listX, LinkedList<Integer> listY, int
            xItem) {
        int[] arr = new int[4];
        arr[IDX_X] = listX.remove(ran.nextInt(listX.size()));
        arr[IDX_Y] = listY.remove(ran.nextInt(listY.size()));
        return arr;
    }

    public void onGlobalLayout() {
        int tmpW = getWidth();
        int tmpH = getHeight();
        if (width != tmpW || height != tmpH) {
            width = tmpW;
            height = tmpH;
            show();
        }
    }

    public Vector<String> getKeywords() {
        return vecKeywords;
    }

    public void rubKeywords() {
        vecKeywords.clear();
    }

    /** 直接清除所有的TextView。在清除之前不会显示动画。 */
    public void rubAllViews() {
        removeAllViews();
    }

    public void setOnItemClickListener(OnClickListener listener) {
        itemClickListener = listener;
    }

}