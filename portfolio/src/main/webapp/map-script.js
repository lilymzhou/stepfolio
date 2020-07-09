const MAP_ID = 'map-container';
const mapKey = config.MAP_KEY;
const BIGFOOT_URL = '/bigfoot-data';

window.onload = addScriptToHead();

/** Creates a map and adds it to the page. */
function createMap() {
  fetch(BIGFOOT_URL).then(response => response.json()).then((bigfootArr) => {
    const map = new google.maps.Map(
        document.getElementById(MAP_ID),
        // Centered at Googleplex (Mountain View).
        {center: {lat: 37.422, lng: -122.084}, zoom: 3});

    bigfootArr.forEach((sighting) => {
      new google.maps.Marker({
        position: {lat: sighting.lat, lng: sighting.lng},
        map: map,
        title: sighting.title
      });
    })
  });
}

function addScriptToHead() {
  var newScript = document.createElement('script');
  newScript.src = 'https://maps.googleapis.com/maps/api/js?key=' + mapKey;
  document.getElementsByTagName('head')[0].appendChild(newScript);
}