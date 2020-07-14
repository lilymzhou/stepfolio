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
        {center: {lat: 37.422, lng: -122.084}, zoom: 5});

    bigfootArr.forEach((sighting) => {
      const marker = new google.maps.Marker({
        position: {lat: sighting.lat, lng: sighting.lng},
        map: map,
        title: sighting.title,
        animation: google.maps.Animation.DROP
      });
      let info = '<div>' +
          '<p><b>Title:</b> ' + sanitize(sighting.title) + '</p>' +
          '<p><b>Date:</b> ' + sanitize(sighting.date) + '</p>' +
          '<p><b>Location:</b> ' + sanitize(sighting.location) + '</p>' +
          '<p><b>Description:</b> ' + sanitize(sighting.description) + '</p>' +
          '</div>';
      const infoWindow = new google.maps.InfoWindow({content: info});
      marker.addListener('click', () => {
        infoWindow.open(map, marker);
        animateMarkerBounce(marker);
      })
    })
  });
}

function sanitize(unsafecontent) {
  let element = document.createElement('span');
  element.innerText = unsafecontent;
  return element.innerHtml;
}

/* Animate a single bounce for marker when clicked on. */
function animateMarkerBounce(marker) {
  marker.setAnimation(google.maps.Animation.BOUNCE);
  setTimeout(marker.setAnimation(null), 1000);
}

function addScriptToHead() {
  var newScript = document.createElement('script');
  newScript.src = 'https://maps.googleapis.com/maps/api/js?key=' + mapKey;
  document.getElementsByTagName('head')[0].appendChild(newScript);
}