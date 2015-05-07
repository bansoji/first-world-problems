package utils;

import java.util.List;

/**
 * Created by jasonlim on 5/05/15.
 */
public class Channel {

    private Line lowLine;
    private Line highLine;

    public Channel (List<Double> lows, List<Double> highs) {
        this.lowLine = GeometryUtils.createLine(lows);
        this.highLine = GeometryUtils.createLine(highs);
    }


    public Line getHighLine() {
        return highLine;
    }

    public Line getLowLine() {
        return lowLine;
    }




}
