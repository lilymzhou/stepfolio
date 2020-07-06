google.charts.load('current', {
  'packages':['geochart'], 
  'mapsApiKey': 'AIzaSyARIbGzg1ObeVicxm6Txr1mqhUR4w12LQQ'
});
google.charts.setOnLoadCallback(drawChart);
 
/** Creates a chart and adds it to the page. */
function drawChart() {
  var data = google.visualization.arrayToDataTable([
    ['Country', 'Popularity'],
    ['Germany', 200],
    ['United States', 300],
    ['Brazil', 400],
    ['Canada', 500],
    ['France', 600],
    ['RU', 700]
  ]);
 
  const options = {};
 
  const chart = new google.visualization.GeoChart(
      document.getElementById('chart-container'));
  chart.draw(data, options);
}