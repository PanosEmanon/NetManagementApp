map = null;
var view = -1; // 0-> markers, 1->polyline, 2->cells
mapSP = null;
function Markers(data) {

	
			
			view = 0;
			var latlng = null;
			sessionStorage.setItem('sent',JSON.stringify(data));
	 		
	 		$.each(data,function(i,item){
	 			latlng = new google.maps.LatLng(item.APlatitude, item.APlongtitude);
	 			if(i==0){
	 				
	 				
	 		      	var mapOptions = {
	 		    		zoom: 8,
	 		    		center: latlng,
	 		    		mapTypeId: google.maps.MapTypeId.TERRAIN
	 		 		 };
	 		      	
	 		 		 map = new google.maps.Map(document.getElementById('map-canvas'),mapOptions);
	 	
	 			}
	 			
	 			var marker = new google.maps.Marker({
		                position: latlng,
		                map: map,
		               title: item.ssid,
		                animation: google.maps.Animation.DROP,
		            });
			 		
			 		var content = "<p>" + 'SSID: ' + item.ssid + "<br />" + ' Mean RSSI: ' + item.rssi + '<br />' + ' frequency: ' + item.frequency + 
			 		 "<br />" + "lat:  " + item.APlatitude + "<br />" + "lon:  " + item.APlongtitude + "</p>";     

		 			var infowindow = new google.maps.InfoWindow();

		 			google.maps.event.addListener(marker,'click', (function(marker,content,infowindow){ 
		 			        return function() {
		 			           infowindow.setContent(content);
		 			           infowindow.open(map,marker);
		 			        };
		 			    })(marker,content,infowindow)); 
	 			
	 			
	 		});
			console.log('ending scipt');

}


function Polyline(){
	
		
		if(map == null) {
			Markers(data);
		}
		else {
			view = 1;
			var sentdata = JSON.parse(sessionStorage.getItem('sent'));
			var flightPlanCoordinates = [];
			$.each(sentdata,function(i,item){
				
				flightPlanCoordinates.push(new google.maps.LatLng(item.APlatitude, item.APlongtitude));
				
				
			});
			
			var flightPath = new google.maps.Polyline({
				path: flightPlanCoordinates,
				geodesic: true,
				strokeColor: '#FF0000',
				strokeOpacity: 1.0,
				strokeWeight: 2
				});
			flightPath.setMap(map);
	
		}
	
}

function BatteryGraph() {
	getBatteryInfo(); 
	
}

function Cells() {

		view = 2;
		//console.log("Trying to gather cell info")
		getCellsInfo();
		
	

}

function DrawCells() {
	//console.log("DrawCells function");
	var latlng = null;
	//latlng = new google.maps.LatLng('37.58','23.46');
	var bsdata = JSON.parse(sessionStorage.getItem('bs'));
	if(map == null) { // if a map does not exists create it
		
		for(var item in bsdata) {
			latlng = new google.maps.LatLng(item.BSlatitude, item.BSlongtitude);
			break;
		}
		var mapOptions = {
		    		zoom: 8,
		    		center: latlng,
		    		mapTypeId: google.maps.MapTypeId.TERRAIN
		 		 };
		      	
		  map = new google.maps.Map(document.getElementById('map-canvas'),mapOptions);
	}
	
	
	
	
	var pinColor = "52bdb0";
	var pinImage = new google.maps.MarkerImage("http://chart.apis.google.com/chart?chst=d_map_pin_letter&chld=%E2%80%A2|" + pinColor,
	        new google.maps.Size(21, 40),
	        new google.maps.Point(0,0),
	        new google.maps.Point(10, 34));
	$.each(bsdata,function(i,item){
			latlng = new google.maps.LatLng(item.BSlatitude, item.BSlongtitude);
	
			   var marker = new google.maps.Marker({
                position: latlng,
                map: map,
                icon: pinImage,
                title: item.Operator,
                animation: google.maps.Animation.DROP,
            });
			   
			   
			 
	 		var content = "<p>" + "Operator: " + item.Operator + "<br />" + " MCC: " + item.mcc + "<br />"  + " MNC: " + item.mnc + "<br />" 
	 		+  " cid: " + item.cid + "<br />" + " LAC: " + item.lac + "</p>";     

 			var infowindow = new google.maps.InfoWindow();

 			google.maps.event.addListener(marker,'click', (function(marker,content,infowindow){ 
 			        return function() {
 			           infowindow.setContent(content);
 			           infowindow.open(map,marker);
 			        };
 			    })(marker,content,infowindow)); 
			
			
		});
	console.log('Ending DrawCells');
	
}


function DrawStayPoints(data) {
	//var spdata = null;
	//var latlng = null;
	//var marker = null;
	if(data == null){
		console.log('Data is null');
		 //data = JSON.parse(sessionStorage.getItem('stay-points'));
	}
	else {
		
		var pinColor = "0000FF";
		var pinImage = new google.maps.MarkerImage("http://chart.apis.google.com/chart?chst=d_map_pin_letter&chld=%E2%80%A2|" + pinColor,
		        new google.maps.Size(21, 40),
		        new google.maps.Point(0,0),
		        new google.maps.Point(10, 34));
		
		$.each(data,function(i,item){
			
			var latlng = new google.maps.LatLng(item.lat, item.lon);
			if(i==0){
 				
 				
 		      	var mapOptions = {
 		    		zoom: 8,
 		    		center: latlng,
 		    		mapTypeId: google.maps.MapTypeId.TERRAIN
 		 		 };
 		      	
 		 		 mapSP = new google.maps.Map(document.getElementById('map-canvas2'),mapOptions);
 	
 				}
			    var marker = new google.maps.Marker({
                position: latlng,
                map: mapSP,
               icon: pinImage,
               title: 'Stay Point',
                animation: google.maps.Animation.DROP,
            });
		//console.log('mapSP: ', mapSP);
		

		 		var content = "<p>" + "Tstart: " + item.Tstart + "<br />" + " Tend: " + item.Tend + "</p>";  

	 			var infowindow = new google.maps.InfoWindow();

	 			google.maps.event.addListener(marker,'click', (function(marker,content,infowindow){ 
	 			        return function() {
	 			           infowindow.setContent(content);
	 			           infowindow.open(mapSP,marker);
	 			        };
	 			    })(marker,content,infowindow)); 
				
				
		});
		

		
	}
	console.log('Ending Draw stay points');
	
}






function fullscreenMode(){
	
		window.open('fullscreenMap', '_blank');
	
}


