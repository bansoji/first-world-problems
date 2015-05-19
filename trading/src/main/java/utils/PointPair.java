package utils;

import java.util.List;

/**
 * Created by jasonlim on 19/05/15.
 */
public class PointPair{
    private List<Point> highs;
    private List<Point> lows;

    public PointPair(List<Point> highs, List<Point> lows){
        this.highs = highs;
        this.lows = lows;
    }

    public List<Point> getHighs(){
        return highs;
    }

    public List<Point> getLows(){
        return lows;
    }

}
