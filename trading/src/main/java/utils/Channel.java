package utils;

import java.util.List;

/**
 * Created by jasonlim on 5/05/15.
 */
public class Channel {

    private List<Double> lows; //The list of lows from PriceChannelStrategy.
    private List<Double> highs; //The list of highs from PriceChannelStrategy.
    private Line lowLine;
    private Line highLine;

    public Channel (List<Double> lows, List<Double> highs) {
        this.lows = lows;
        this.highs = highs;
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
