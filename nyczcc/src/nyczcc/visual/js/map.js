/**
 * Created by HuanYe on 16/3/22.
 */
var map;
// var pickupIcon = MapIconMaker.createMarkerIcon({width: 1, height: 1, primaryColor: "#ff0000"});
// var dropoffIcon = MapIconMaker.createMarkerIcon({width: 1, height: 1, primaryColor: "#0000FF"});

function initMap() {
    map = new google.maps.Map(document.getElementById('map'), {
        center: {lat: 40.758896, lng: -73.985130},
        zoom: 13,
        mapTypeId : google.maps.MapTypeId.ROADMAP,
        mapTypeControl: true
    });
}

function parseData2Map(date)
{
    d3.csv("data/"+date+".csv",function(err, points)
    {
        if(err) console.log(err);

        var drawPoints = function(){
            if (points.length == 0)
            {
                clearInterval(interval);
                return;
            }

            points.splice(0, Math.min(1000,points.length)).forEach(
                function(d)
                {
                    pickLon = parseFloat(d.pickup_longitude);
                    pickLat = parseFloat(d.pickup_latitude);
                    dropLon = parseFloat(d.dropoff_longitude);
                    dropLat =parseFloat(d.dropoff_latitude);

                    pickup = new google.maps.Marker({
                        position: {lat: pickLat, lng: pickLon},
                        map: map,
                        icon:'image/pickup.png'
                    });

                    dropoff = new google.maps.Marker({
                        position: {lat: dropLat, lng: dropLon},
                        map: map,
                        icon:'image/dropoff.png'
                    });
                }
            )
        };

        drawPoints();
        var interval = setInterval(drawPoints, 5000);
    })
}