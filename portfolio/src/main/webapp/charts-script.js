const mapKey = config.MAP_KEY;

const COUNTRY_COL = 'Country';
const RICE_COL = 'Rice Supplies';
const CHART_URL = '/chart-data';
const CHART_ID = 'chart-container';
const STRING_TYPE = 'string';
const NUMBER_TYPE = 'number';

google.charts.load('current', {
  'packages':['geochart'], 
  'mapsApiKey': mapKey
});
google.charts.setOnLoadCallback(drawChart);
 
/** Creates a chart and adds it to the page. */
function drawChart() {
  fetch(CHART_URL).then(response => response.json())
  .then((countryMap) => {
    const data = new google.visualization.DataTable();
    data.addColumn(STRING_TYPE, COUNTRY_COL);
    data.addColumn(NUMBER_TYPE, RICE_COL);
    Object.keys(countryMap).forEach((country) => {
      data.addRow([country, countryMap[country]]);
    });
 
  const chart = new google.visualization.GeoChart(
      document.getElementById(CHART_ID));
  chart.draw(data, {});
  });
}