package utils;

import java.util.List;

/**
 * Created by Jason Lim on 5/05/15.
 * Edited by Edwin Li on 6/05/15.
 */
public class Channel {

    private List<Point> lowPoints; //The set of low points.
    private List<Point> highPoints; //The set of high points.
    private Line lowLine;   //The lower line of the channel.
    private Line highLine;  //The higher line of the channel.

    /**
     * Constructor for a Channel.
     * @param lows      //The set of Points for the lower line.
     * @param highs     //The set of Points for the higher line.
     */
    public Channel (List<Point> lows, List<Point> highs) {
        this.lowPoints = lows;
        this.highPoints = highs;
        createChannel();
    }

    /**
     * This method will create a channel with two parallel lines.
     */
    private void createChannel() {
        Line rawLow = GeometryUtils.createLine(lowPoints);
        Line rawHigh = GeometryUtils.createLine(highPoints);
        double averageSlope = (rawLow.getSlope() + rawHigh.getSlope())/2; //Basic parallel making stuff
        this.lowLine = GeometryUtils.normaliseLine(averageSlope, lowPoints);
        this.highLine = GeometryUtils.normaliseLine(averageSlope, highPoints);

    }

    /**
     * Returns the higher line of the Channel.
     * @return  The high line of the Channel.
     */
    public Line getHighLine() {
        return highLine;
    }

    /**
     * Returns the lower line of the Channel.
     * @return  The low line of the Channel.
     */
    public Line getLowLine() {
        return lowLine;
    }




}
