package signin;

public class DistanceUtils {

	private static final double EARTH_RADIUS = 6378.137; 

    private static double rad(double d) { return d * Math.PI / 180.0; } 

    public static double getDistance(double longitude1, double latitude1, double longitude2, double latitude2) {
        double Lat1 = rad(latitude1);
        double Lat2 = rad(latitude2);
        double a = Lat1 - Lat2;
        double b = rad(longitude1) - rad(longitude2);
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2) + Math.cos(Lat1) * Math.cos(Lat2) * Math.pow(Math.sin(b / 2), 2)));
        s = s * EARTH_RADIUS; 
        s = Math.round(s * 10000d) / 10000d;
        s = s * 1000;
        return s;
    }
    
    public static boolean isBetweenDistance(double longitude1,double latitude1,double longitude2,double latitude2){
    	if(getDistance(longitude1, latitude1, longitude2, latitude2)<200)
    		return true;
    	return false;
    }
    
}
