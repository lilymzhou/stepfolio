google.charts.load('current', {'packages':['corechart']});
google.charts.setOnLoadCallback(drawChart);
 
/** Creates a chart and adds it to the page. */
function drawChart() {
  const data = new google.visualization.DataTable();
  data.addColumn('string', 'Items');
  data.addColumn('number', 'Count');
        data.addRows([
          ['Slice 1', 5],
          ['Slice 2', 5],
        ]);
 
  const options = {
    'title': 'Title',
    'width':500,
    'height':400
  };
 
  const chart = new google.visualization.PieChart(
      document.getElementById('chart-container'));
  chart.draw(data, options);
}