package ml.yike.yueyin.util;

import android.graphics.Color;


public class ColorUtils {

    /**
     * 获得一个随机的颜色
     */
    public static int getRandomColor() {
        int r = (int) (Math.random() * 255); //产生一个255以内的整数
        int g = (int) (Math.random() * 255); //产生一个255以内的整数
        int b = (int) (Math.random() * 255); //产生一个255以内的整数
        return Color.rgb(r, g, b);
    }

    /**
     * 获得一个比较暗的随机颜色
     */
    public static int getRandomBrunetColor() {
        int r = (int) (Math.random() * 100); //产生一个100以内的整数
        int g = (int) (Math.random() * 100);
        int b = (int) (Math.random() * 100);
        return Color.rgb(r, g, b);
    }

    /**
     * r g b >= 160 时返回 true
     */
    public static boolean isBrightSeriesColor(int color) {

        double d = androidx.core.graphics.ColorUtils.calculateLuminance(color);
        if (d - 0.400 > 0.000001) {
            return true;
        } else {
            return false;
        }
    }

}
