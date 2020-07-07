const mapKey = config.MAP_KEY;

google.charts.load('current', {
  'packages':['geochart'], 
  'mapsApiKey': mapKey
});
google.charts.setOnLoadCallback(drawChart);
 
/** Creates a chart and adds it to the page. */
function drawChart() {
  fetch('/chart-data').then(response => response.json())
  .then((countryMap) => {
    const data = new google.visualization.DataTable();
    data.addColumn('string', 'Country');
    data.addColumn('number', 'Rice Supplies');
    Object.keys(countryMap).forEach((country) => {
      data.addRow([country, countryMap[country]]);
    });
 
  const chart = new google.visualization.GeoChart(
      document.getElementById('chart-container'));
  chart.draw(data, {});
  });
}