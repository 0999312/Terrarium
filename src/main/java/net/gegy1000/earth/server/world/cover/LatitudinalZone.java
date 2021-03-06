package net.gegy1000.earth.server.world.cover;

public enum LatitudinalZone {
    TROPICS(0.0, 23.0),
    SUBTROPICS(23.0, 35.0),
    TEMPERATE(35.0, 66.0),
    FRIGID(66.0, 90.0);

    public static final LatitudinalZone[] ZONES = values();

    private final double lowerLatitude;
    private final double upperLatitude;
    private final double centerLatitude;

    LatitudinalZone(double lowerLatitude, double upperLatitude) {
        this.lowerLatitude = lowerLatitude;
        this.upperLatitude = upperLatitude;
        this.centerLatitude = (lowerLatitude + upperLatitude) / 2.0;
    }

    public double getLowerLatitude() {
        return this.lowerLatitude;
    }

    public double getUpperLatitude() {
        return this.upperLatitude;
    }

    public double getCenterLatitude() {
        return this.centerLatitude;
    }

    public static LatitudinalZone get(double latitude) {
        double absoluteLatitude = Math.abs(latitude);
        for (LatitudinalZone zone : ZONES) {
            if (absoluteLatitude >= zone.lowerLatitude && absoluteLatitude < zone.upperLatitude) {
                return zone;
            }
        }
        return LatitudinalZone.TEMPERATE;
    }
}
