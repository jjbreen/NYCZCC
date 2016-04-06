//
// <style>
// body {
//   font: 10px sans-serif;
// }
// .axis path,
// .axis line {
//   fill: none;
//   stroke: #000;
//   shape-rendering: crispEdges;
// }
// .x.axis path {
//   display: none;
// }
// .line {
//   fill: none;
//   stroke: steelblue;
//   stroke-width: 1.5px;
// }
// .states {
//             fill: #ccc;
//             stroke: #fff;
//         }
// text{
//   font-family: "Times New Roman", Georgia, Serif;
//   font-size: 15px;
// }
// p {
//   font-size: 50px;
//   padding-left: 300px;
//   font-weight: bold;
//   font-family: "Times New Roman", Georgia, Serif;
// }
// .symbol{
//     fill: #0066FF;
//     opacity: 0.75;
// }
// </style>
//
// <body>
//   <p>Population of USA</p>
// </body>
//
// <script>
  console.log(d3); // test if d3 is loaded
var mapScale=700,
    margin = {top: 20, bottom: 20, right: 20,  left: 20},
    axis_cross=50,
    width = 1200 - margin.left - margin.right,
    height = 840 - margin.top - margin.bottom;;
var stateById, ageRange;
var file_name="data.csv";
var color = d3.scale.category20();
// x and y generator
var xRange = function(min, max){
  var interval = (max-min) / parseFloat(48);
  var result = [];
  var start = min;
  while (start < max) {
    result.push(start);
    start = start + interval;
  }
  return result;
}
var x=d3.scale.ordinal()
		.range(xRange(axis_cross, width-100));
var y=d3.scale.log()
        .range([height/2-10,20])
        
// background
var svg = d3.select("body").append("svg")
        .attr("width", width)
        .attr("height", height);
// three Gs used for map, line chart and pie chart
var mapgroup = svg.append("g"),
    lGroup=svg.append("g"),
    pieGroup=svg.append("g");
// draw a usa map
var projection = d3.geo.albersUsa()
            .translate([width/4+margin.left, height*3/4])
            .scale([mapScale]);
               
var path = d3.geo.path()
            .projection(projection);
// load the us.json
queue().defer(d3.json, "us.json").await(ready);
function ready(error, us) {           
            mapgroup.append("path")
            .attr("class", "states")
            .datum(topojson.feature(us, us.objects.states))
            .attr("d", path);
                        
            loadCsv(file_name);
        }
function loadCsv(file_name){
   d3.csv(file_name, function(error, states) {
  	if (error) throw error;
  	console.log(states);
  
  // aggregate data by state ID
  	stateById = d3.map();
  	states.forEach(function(d) { stateById.set(d.id, d); });
  	console.log(stateById);
  
  // aggregate data by age category
  	var age=d3.keys(states[0]).filter(function(key) { return key !== "id"&&key!="lon"&&key!="lat"; });
  	ageRange=age.map(function(age){
  		  return{
  		    age:age,
  		    values:states.map(function(d){
  			     return {state:d.id,population:+d[age]};
  		})
  	}
  });
    
    x.domain(states.map(function(d){return d.id;}));
    y.domain([d3.min(ageRange,function(d){return d3.min(d.values, function(p){return +p.population;})}),
 	       d3.max(ageRange,function(d){return d3.max(d.values, function(p){return +p.population;})})
 ]);
  console.log(ageRange);
  
  // call methods to draw scatter_map and line chart
  CreateMap(stateById);
  linesChart(ageRange);
  });
}
 
function CreateMap(data){
  var scale = d3.scale.log()
  	.domain(d3.extent(data.values(), function(d){return +d.total;}))
  	.range([3, 13]);
    
  var projection = d3.geo.albersUsa()
    .translate([width/4+margin.left, height*3/4])
    .scale([mapScale]);
  var symbols = mapgroup.selectAll("circle")
      .data(data.values())
      .enter()
      .append("circle")
      .attr("cx", function(d) {
            return projection([d.lon, d.lat])[0];
            })
      .attr("cy", function(d) {
            return projection([d.lon, d.lat])[1];
            })
      .attr("r", function(d) {
            return scale(d.total);
            })    
      .attr("class", function(d){return "symbol "+d.id;});
    
  symbols.on("mouseover", function(d) {
      d3.select(this).style("stroke", "black")
      .style("stroke-width",2.5);
      highlightLine(d.id);
      pie(d.id);
      })
  symbols.on("mouseout", function(d) {
      removeLine();
      d3.select(this).style("stroke", "")
      })  
}
// A line chart to show population by age group; uses the "pie" namespace.
function linesChart(data) {
  var stateID=function(stateById){
    return stateById.values().map(function(state){
      return state.id;
    })
  };
  
  var xAxis = d3.svg.axis()
        .scale(x)
        .orient("bottom");
  var yAxis = d3.svg.axis()
        .scale(y)
        .orient("left");
  var line = d3.svg.line()
        .interpolate("basis")
        .x(function(d) { return x(d.state); })
        .y(function(d) { return y(d.population); });
  lGroup.append("g")
      .attr("class", "x axis")
      .attr("transform", "translate(0," + (height/2-10) + ")")
      .call(xAxis)
      .selectAll("text")
      .attr("transform", "rotate(-90)")
      .attr("text-anchor","start")
      .attr("dx","-1em")
      .attr("dy","-.3em");
   
  lGroup.append("g")
      .attr("class", "y axis")
      .attr("transform","translate("+axis_cross+",-10)")
      .call(yAxis)
      .append("text")
      .attr("transform", "rotate(-90)")
      .attr("y", 20)
      .attr("x",-10)
      .attr("dy", ".4em")
      .style("text-anchor", "end")
      .text("Poplulation");
  var ageLine = lGroup.selectAll(".age")
      .data(data)
      .enter().append("g")
      .attr("class", "age");
  ageLine.append("path")
        .attr("class","line")
        .attr("d",function(d){
          return line(d.values);
        })
        .style("stroke", function(d) { return color(d.age); });
// create legends
var legend = lGroup.selectAll(".legend")
      .data(data)
      .enter().append("g")
      .attr("class", "legend")
      .attr("transform", function(d, i) { return "translate("+(width-170)+"," + i * 17 + ")"; });
  
  legend.append("line")
      .attr("x1",5)
      .attr("x2",35)
      .attr('y1',5)
      .attr('y2',5)
      .style("stroke", function(d){return color(d.age);})
      .style("stroke-width","4px");
  
  legend.append("text")  
      .attr("x", 40)
      .attr("y", 7)
      .attr("dy", ".35em")
      .style("text-anchor", "start")
      .text(function(d) { return d.age; })
      .attr("font-size","20px");   
     d3.selectAll(".x.axis .tick text")
      .on("mouseover", function(stateId){
      d3.select('.symbol.'+stateId).style("stroke", "black")
      .style("stroke-width",2.5);
      highlightLine(stateId);
      pie(stateId);
  }) ;
    d3.selectAll(".x.axis .tick text")
      .on("mouseout", function(stateId){
      d3.select('.symbol.'+stateId).style("stroke", "");
      removeLine();
  }) ;
}
var pie=function(stateId){
  var getStateData = function(stateById) {
    var result = [];
    var state = stateById.values().filter(function(d){return d.id==stateId;})[0];
    console.log("test: "+state);
    Object.keys(state).forEach(function(attr) {
      if (attr.indexOf("Years") !== -1) {
        result.push({
          age: attr,
          population: state[attr]
        });
      }
    })
    return result;
  };
  
  d3.selectAll(".arc").remove();
  // compute radius of the piechart
    var radius = Math.min(width/2, height/2)/2;
  
// create a arc
    var arc = d3.svg.arc()
              .outerRadius(radius - 50)
              .innerRadius(70);
// create a pie 
    var pie = d3.layout.pie()
              .sort(null)
              .value(function(d){return d.population;});
    var g = pieGroup.selectAll(".arc")
          .data(pie(getStateData(stateById)))
          .enter().append("g")
          .attr("class", "arc")
          .attr("transform","translate("+width*3/4+","+(height*3/4+5)+")");
   
      g.append("path")
       .attr("d", arc)
        .style("fill", function(d) { return color(d.data.age); });
        g.append("text")
            .attr("transform", function(d) { 
                if(arc.centroid(d)[0]<0)
                  return "translate(" + (arc.centroid(d)[0]*1.5-20)+","+(arc.centroid(d)[1]*1.5) + ")";
                  else return "translate(" + (arc.centroid(d)[0]*1.5+20)+","+(arc.centroid(d)[1]*1.5) + ")"; })
            .attr("text-anchor", "middle")
            .text(function(d) { return d.data.age+": "+(+d.data.population/1000).toFixed(0)+"K"; })
            .style('font-size','13px');
        
        pieGroup.append("text")
          .attr("text-anchor","middle")
          .text(stateId)
          .style('font-size','30px')
          .attr("transform","translate("+width*3/4+","+(height)+")")
          .attr("class","arc State");
}
function highlightLine(state){  
    lGroup.append('line')
      .attr('class','signal')
      .attr('x1',x(state))
      .attr("x2",x(state))
      .attr('y1',20)
      .attr('y2',height/2)
      .attr('stroke-width','2px')
      .attr('stroke','black');
}
function removeLine(){
    d3.select('.signal').remove();
}
</script>