package com.netmanagement.dataprocessing;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.ml.clustering.Cluster;
import org.apache.commons.math3.ml.clustering.DBSCANClusterer;

import com.netmanagement.csvdatasets.ParseAccessPoints;
import com.netmanagement.csvdatasets.ParseGPS;
import com.netmanagement.entities.AccessPoints;
import com.netmanagement.entities.GPS;
import com.netmanagement.entities.PointsofInterest;
import com.netmanagement.entities.StayPoints;

public class PoICalculations {
	private static PoICalculations PoICalculationsinstance = null;
    private double esp;
    private int minPts;
    private double[][] matrix = null;
    String startDate;
    String endDate;
    String Tmin;
    String Tmax;
    Double Dmax;
    ArrayList<ArrayList<StayPoints>> neighbors = null;
	
	private PoICalculations(){}
	
	public static PoICalculations getInstance(){
		if(PoICalculationsinstance == null){
			PoICalculationsinstance = new PoICalculations();
		}
		return PoICalculationsinstance;
	}
	
	public void setAll(String startDate, String endDate, String Tmin, String Tmax, Double Dmax, Double esp, int minPts){
		this.startDate = startDate;
		this.endDate = endDate;
		this.Tmin = Tmin;
		this.Tmax = Tmax;
		this.Dmax = Dmax;
		this.esp = esp;
		this.minPts = minPts;
	}
	
	public double getEsp() {
		return esp;
	}

	public void setEsp(double esp) {
		this.esp = esp;
	}

	public int getMinPts() {
		return minPts;
	}

	public void setMinPts(int minPts) {
		this.minPts = minPts;
	}

	public double[][] getMatrix() {
		return matrix;
	}

	public void setMatrix(double[][] matrix) {
		this.matrix = matrix;
	}

	public String getStartDate() {
		return startDate;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	public String getEndDate() {
		return endDate;
	}

	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}

	public String getTmin() {
		return Tmin;
	}

	public void setTmin(String tmin) {
		Tmin = tmin;
	}

	public String getTmax() {
		return Tmax;
	}

	public void setTmax(String tmax) {
		Tmax = tmax;
	}

	public Double getDmax() {
		return Dmax;
	}

	public void setDmax(Double dmax) {
		Dmax = dmax;
	}
	
	public void neighborsInit(int size){
		neighbors = new ArrayList<ArrayList<StayPoints>>();
		for (int i=0;i<size;i++){
			neighbors.add(new ArrayList<StayPoints>());
		}
	}

	@SuppressWarnings("rawtypes")
	public ArrayList<PointsofInterest> CalculatePoI(){
		HashMap<String, ArrayList<GPS>> hgps = ParseGPS.getInstance().getHap();
		ArrayList<StayPoints> Lsp = new ArrayList<StayPoints>();
		if (!hgps.isEmpty()){
			Set<?> set = hgps.entrySet();
			Iterator<?> it = set.iterator();
			while(it.hasNext()){
				Map.Entry me = (Map.Entry)it.next();
				//System.out.println("Key : "+me.getKey()+" Value : "+me.getValue());
				ArrayList<GPS> array = GPSCalculations.getInstance().searchUser(me.getKey().toString(), startDate, endDate);
				ArrayList<StayPoints> temp = GPSCalculations.getInstance().findStayPoints(array, Tmin, Tmax, Dmax);
				if (!temp.isEmpty()){
					Lsp.addAll(temp);
				}
			}
		}
		generateDistanceMatrix(Lsp);
		return DBSCAN(Lsp);
	}
	
	public void generateDistanceMatrix(ArrayList<StayPoints> Lsp){
		double[][] matrix = new double[Lsp.size()][Lsp.size()];
		for (int i=0;i<Lsp.size();i++){
			for (int j=0;j<Lsp.size();j++){
				matrix[i][j]=Distance(Lsp.get(i), Lsp.get(j));
			}
		}
		setMatrix(matrix);
	}
	
	/*public ArrayList<PointsofInterest> DBSCAN(ArrayList<StayPoints> Lsp){
		ArrayList<PointsofInterest> poilist = new ArrayList<PointsofInterest>(); //cluster
		DBSCANClusterer<StayPoints> clusterer = new DBSCANClusterer<StayPoints>(esp, minPts);
		List<Cluster<StayPoints>> clusteredPoints = clusterer.cluster(Lsp);
		
		for (Cluster<StayPoints> iter : clusteredPoints) {
			List<StayPoints> points = iter.getPoints();
			double minLat, minLon, maxLat, maxLon;
			
			minLat = maxLat = points.get(0).getLat();
			minLon = maxLon = points.get(0).getLon();
			for (StayPoints iter2 : points) {
				if (iter2.getLat() < minLat)
					minLat = iter2.getLat();
				if (iter2.getLat() > maxLat)
					maxLat = iter2.getLat();
				if (iter2.getLon() < minLon)
					minLon = iter2.getLon();
				if (iter2.getLon() > maxLon)
					maxLon = iter2.getLon();
			}
			PointsofInterest poi = new PointsofInterest();
			poi.setAll(minLat, minLon, maxLat, maxLon, 0, 0);
			poilist.add(poi);
		}
		return poilist;
	}*/
	
	public ArrayList<PointsofInterest> DBSCAN(ArrayList<StayPoints> Lsp){
		ArrayList<PointsofInterest> poilist = new ArrayList<PointsofInterest>(); //cluster
		//ArrayList<PointsofInterest> noiselist = new ArrayList<PointsofInterest>();
		ArrayList<StayPoints> visited = new ArrayList<StayPoints>();
		final int size = Lsp.size();
		neighborsInit(size);
		int c=0;
		for (int i=0;i<size;i++){
			if (visited.contains(Lsp.get(i))){
				continue;
			}
			visited.add(Lsp.get(i));
			neighbors.get(i).addAll(regionQuery(Lsp, i));
			if (neighbors.get(i).size()>minPts){
				poilist.add(new PointsofInterest());
				poilist.get(poilist.size()-1).getPoints().add(Lsp.get(i));
				updatePoint(poilist.get(c), Lsp.get(i));
				expandCluster(poilist.get(poilist.size()-1), Lsp, visited, neighbors.get(i));
				c++;
			}
		}
		return poilist;
	}
	
	/*double Distance(StayPoints sp1, StayPoints sp2){
		//Find Pythagorean distance calculation between given variables
		double distance=0,lat=0,lon=0;
		if (sp1.getLat()>sp2.getLat()){
			lat=sp1.getLat()-sp2.getLat();
		}
		else {
			lat=sp2.getLat()-sp1.getLat();
		}
		lat=lat*lat;
		if (sp1.getLon()>sp2.getLon()){
			lon=sp1.getLon()-sp2.getLon();
		}
		else {
			lon=sp2.getLon()-sp1.getLon();
		}
		lon=lon*lon;
		distance=Math.sqrt(lat+lon);
		return distance;
	}*/
	
	double Distance(StayPoints sp1, StayPoints sp2){
		//Find distance calculation between given variables from http://www.movable-type.co.uk/scripts/latlong.html
		double glat=sp1.getLat()*Math.PI/180;
		double alat=sp2.getLat()*Math.PI/180;
		double glon=sp1.getLon()*Math.PI/180;
		double alon=sp2.getLon()*Math.PI/180;
		double R = 6371000;
		double df = alat - glat;
		double dl = alon - glon;
		double a = Math.sin(df/2)*Math.sin(df/2)+Math.cos(glat)*Math.cos(alat)*Math.sin(dl/2)*Math.sin(dl/2);
		double c = 2*Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
		double d=R*c;
		return Math.abs(d);
	}
	
	ArrayList<StayPoints> regionQuery(ArrayList<StayPoints> Lsp, int pos){
		ArrayList<StayPoints> list = new ArrayList<StayPoints>();
		for (int j=0;j<Lsp.size();j++){
			if (matrix[pos][j] <= esp){
				list.add(Lsp.get(j));
			}
		}
		return list;
	}
	
	void expandCluster(PointsofInterest cluster, ArrayList<StayPoints> Lsp, ArrayList<StayPoints> visited, ArrayList<StayPoints> neighbours){
		ArrayList<StayPoints> currentneighbours = new ArrayList<StayPoints>();
		currentneighbours.addAll(neighbours);
		for (int i=0;i<neighbours.size();i++){
			if (visited.contains(neighbours.get(i))){
				continue;
			}
			visited.add(neighbours.get(i));
			ArrayList<StayPoints> extendedneighbours = regionQuery(Lsp, Lsp.indexOf(neighbours.get(i)));
			currentneighbours.addAll(extendedneighbours);
			neighbours.addAll(extendedneighbours);
			cluster.getPoints().add(neighbours.get(i));
			updatePoint(cluster, neighbours.get(i));
		}
	}
	
	PointsofInterest updatePoint(PointsofInterest point, StayPoints spoint){
		if (point.getNumofPoints()==0){
			point.setAll(spoint.getLat(), spoint.getLon(), spoint.getLat(), spoint.getLon(), 1,point.getNumofPoints()+1);
		}
		else {
			if (point.getStartlat()>spoint.getLat()){
				point.setStartlat(spoint.getLat());
			}
			if (point.getEndlat()<spoint.getLat()){
				point.setEndlat(spoint.getLat());
			}
			if (point.getStartlon()>spoint.getLon()){
				point.setStartlon(spoint.getLon());
			}
			if (point.getEndlon()<spoint.getLon()){
				point.setEndlon(spoint.getLon());
			}
		}
		return point;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public String minmaxTimestamp(){
		//Find minimum and maximum Date of the user return MIN#MAX
		String DATE=null,MIN=null,MAX=null;
		HashMap<String, ArrayList<AccessPoints>> hap = ParseAccessPoints.getInstance().getHap();
		int first=1;
		if (!hap.isEmpty()){
			Set<?> set = hap.entrySet();
			Iterator<?> it = set.iterator();
			while(it.hasNext()){
				Map.Entry me = (Map.Entry)it.next();
				//System.out.println("Key : "+me.getKey()+" Value : "+me.getValue());
				ArrayList<AccessPoints> array = (ArrayList<AccessPoints>) me.getValue();
				for (int i=0;i<array.size();i++){
					
					if (first==1){
						MIN=array.get(i).getTimestamp();
						MAX=array.get(i).getTimestamp();
						first=0;
					}
					else {
						try {
							SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
							Date datemin = sdf.parse(MIN);
							Date datemax = sdf.parse(MAX);
							Date datenow = sdf.parse(array.get(i).getTimestamp());
							//System.out.println(datemin+" | "+datemax+" | "+datenow);
							if (datemin.after(datenow)){
								MIN=array.get(i).getTimestamp();
							}
							if (datemax.before(datenow)){
								MAX=array.get(i).getTimestamp();
							}
						} catch (ParseException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
		DATE=MIN+"#"+MAX;
		//System.out.println("MIN-MAX DATES: " + DATE);
		return DATE;
	}
	

}
