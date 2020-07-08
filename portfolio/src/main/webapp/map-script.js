const MAP_ID = 'map-container';

/** Creates a map and adds it to the page. */
function createMap() {
  const map = new google.maps.Map(
      document.getElementById(MAP_ID),
      // Centered at Googleplex (Mountain View).
      {center: {lat: 37.422, lng: -122.084}, zoom: 16});
}