const MAP_ID = 'map-container';

/** Creates a map and adds it to the page. */
function createMap() {
  //addScriptToHead();
  const map = new google.maps.Map(
      document.getElementById(MAP_ID),
      // Centered at Googleplex (Mountain View).
      {center: {lat: 37.422, lng: -122.084}, zoom: 16});
}

function addScriptToHead() {
  var newScript = document.createElement('script');
  newScript.src = 'https://maps.googleapis.com/maps/api/js?key=AIzaSyARIbGzg1ObeVicxm6Txr1mqhUR4w12LQQ';
  document.getElementsByTagName('head')[0].appendChild(newScript);
}