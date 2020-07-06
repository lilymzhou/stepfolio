google.charts.load('current', {
  'packages':['geochart'], 
  'mapsApiKey': 'AIzaSyARIbGzg1ObeVicxm6Txr1mqhUR4w12LQQ'
});
google.charts.setOnLoadCallback(drawChart);
 
/** Creates a chart and adds it to the page. */
function drawChart() {
  fetch('/chart-data').then(response => response.json())
  .then((countryMap) => {
    const data = new google.visualization.DataTable();
    data.addColumn('string', 'Country');
    data.addColumn('number', 'Rice Production');
    Object.keys(countryMap).forEach((country) => {
      data.addRow([country, countryMap[country]]);
    });

  const options = {};
 
  const chart = new google.visualization.GeoChart(
      document.getElementById('chart-container'));
  chart.draw(data, options);
  });
}