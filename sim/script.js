function undraw()
{
    var canvas = document.getElementById("canvas");
    var ctx = canvas.getContext("2d");
    ctx.clearRect(0, 0, canvas.width, canvas.height);
}

function draw_grid(min_x, min_y, max_x, max_y, rows, cols)
{
    var canvas = document.getElementById("canvas");
    var ctx = canvas.getContext("2d");
    if (min_x < 0 || max_x > canvas.width)
	throw "Invalid x-axis bounds: " + min_x + " - " + max_x;
    if (min_y < 0 || max_y > canvas.height)
	throw "Invalid y-axis bounds: " + min_y + " - " + max_y;
    // draw vertical lines
    for (var col = 0 ; col <= cols ; ++col) {
	ctx.beginPath();
	ctx.moveTo(min_x + col * (max_x - min_x) / cols, min_y);
	ctx.lineTo(min_x + col * (max_x - min_x) / cols, max_y);
	ctx.closePath();
	ctx.lineWidth = 2;
	ctx.strokeStyle = "grey";
	ctx.stroke();
    }
    // draw horizontal lines
    for (var row = 0 ; row <= rows ; ++row) {
	ctx.beginPath();
	ctx.moveTo(min_x, min_y + row * (max_y - min_y) / rows);
	ctx.lineTo(max_x, min_y + row * (max_y - min_y) / rows);
	ctx.closePath();
	ctx.lineWidth = 2;
	ctx.strokeStyle = "grey";
	ctx.stroke();
    }
}

function draw_shape(min_x, min_y, max_x, max_y, rows, cols, points, colors, types, highlight)
{
    var canvas = document.getElementById("canvas");
    var ctx = canvas.getContext("2d");
    if (min_x < 0 || max_x > canvas.width)
	throw "Invalid x-axis bounds: " + min_x + " - " + max_x;
    if (min_y < 0 || max_y > canvas.height)
	throw "Invalid y-axis bounds: " + min_y + " - " + max_y;
    // draw boxes
    for (var i = 0 ; i != points.length ; ++i) {
	var color = colors[types[i]];
	var diagonals = highlight && (i + 1 == points.length);
	var coord = points[i].split(",");
	var row = parse_int(coord[0]);
	var col = parse_int(coord[1]);
	if (row < 0 || row >= rows)
	    throw "Invalid shape point row: " + row;
	if (col < 0 || col >= cols)
	    throw "Invalid shape point col: " + col;
	var x1 = min_x + (col + 0) * (max_x - min_x) / cols + 1;
	var x2 = min_x + (col + 1) * (max_x - min_x) / cols - 1;
	var y1 = min_y + (row + 0) * (max_y - min_y) / rows + 1;
	var y2 = min_y + (row + 1) * (max_y - min_y) / rows - 1;
	ctx.beginPath();
	ctx.moveTo(x1, y1);
	ctx.lineTo(x1, y2);
	ctx.lineTo(x2, y2);
	ctx.lineTo(x2, y1);
	ctx.closePath();
	ctx.lineWidth = 2;
	ctx.strokeStyle = "black";
	ctx.stroke();
	if (color != null) {
	    ctx.fillStyle = color;
	    ctx.fill();
	}
	if (diagonals == true) {
	    ctx.beginPath();
	    ctx.moveTo(x1, y1);
	    ctx.lineTo(x2, y2);
	    ctx.stroke();
	    ctx.beginPath();
	    ctx.moveTo(x1, y2);
	    ctx.lineTo(x2, y1);
	    ctx.stroke();
	}
    }
}

function draw_side(min_x, min_y, max_x, max_y, group, score, cpu, colors)
{
    var canvas = document.getElementById("canvas");
    var ctx = canvas.getContext("2d");
    if (min_x < 0 || max_x > canvas.width)
	throw "Invalid x-axis bounds: " + min_x + " - " + max_x;
    if (min_y < 0 || max_y > canvas.height)
	throw "Invalid y-axis bounds: " + min_y + " - " + max_y;
    // draw message
    ctx.font = "32px Arial";
    ctx.textAlign = "left";
    ctx.lineWidth = 4;
    ctx.strokeStyle = "darkgrey";
    ctx.strokeText("Player: " + group,        min_x, min_y + 30);
    ctx.strokeText("Score: " + score,         min_x, min_y + 60);
    ctx.strokeText("CPU time: " + cpu + " s", min_x, min_y + 90);
    ctx.strokeText("Legend:", min_x, min_y + 150);    
    ctx.fillStyle = "darkblue";
    ctx.fillText("Player: " + group,        min_x, min_y + 30);
    ctx.fillText("Score: " + score,         min_x, min_y + 60);
    ctx.fillText("CPU time: " + cpu + " s", min_x, min_y + 90);
    ctx.fillText("Legend:", min_x, min_y+150);
    ctx.strokeText("Residence", min_x, min_y+180);
    ctx.strokeText("Factory", min_x, min_y+210);
    ctx.strokeText("Road", min_x, min_y+240);
    ctx.strokeText("Park", min_x, min_y+270);
    ctx.strokeText("Water", min_x, min_y+300);
    ctx.fillStyle = colors[0];
    ctx.fillText("Residence", min_x, min_y+180);
    ctx.fillStyle = colors[1];
    ctx.fillText("Factory", min_x, min_y+210);
    ctx.fillStyle = colors[2];
    ctx.fillText("Road", min_x, min_y+240);
    ctx.fillStyle = colors[3];
    ctx.fillText("Park", min_x, min_y+270);
    ctx.fillStyle = colors[4];
    ctx.fillText("Water", min_x, min_y+300);
}

function parse_int(x)
{
    if (isNaN(parseFloat(x)) || !isFinite(x))
	throw "Not a number: " + x;
    var n = +x;
    if (n != Math.round(n))
	throw "Not an integer: " + n;
    return Math.round(n);
}

function parse_points(data)
{
    if (data.length % 2 != 0)
	throw "Invalid length: " + data.length;
    var points = new Array(data.length / 2);
    for (var i = 0 ; i != points.length ; ++i) {
	var x = parse_int(data[i + i + 0]);
	var y = parse_int(data[i + i + 1]);
	points[i] = [x, y];
    }
    return points;
}

function process(data)
{
    // parse data
    data = data.split("\n");
    if (data.length < 2)
	throw "Invalid data format (not enough rows)";
    for (var i = 0 ; i != data.length ; ++i)
	data[i] = data[i].split(";");
    var i = data.length - 1;
    if (data[0].length != 4  || data[i].length != 2)
	throw "Invalid data format";
    var refresh   = parse_int(data[i][0]);
    var highlight = parse_int(data[i][1]);
    if (refresh < 0.0) refresh = -1;
    else refresh = Math.round(refresh);
    var group = data[0][0].trim();
    var score = parse_int(data[0][1]);
    var cpu = data[0][2].trim();
    var n_cuts    = parse_int(data[0][3]);
    if (4*n_cuts + 2 != data.length)
	throw "Invalid data format (invalid total lines)"
    i = 2;
    var cuts = [];
    var types = [];

    for (var i = 0 ; i<n_cuts ; i++) {
	building_index = 4*i+1;
	road_index = 4*i+2;
	park_index = 4*i+3;
	water_index = 4*i+4;
	// add building points. residence type = 0, factory type = 1.
	for (var k=1; k<data[building_index].length-1; k++) {
	    cuts.push(data[building_index][k]);
	    types.push(parse_int(data[building_index][data[building_index].length-1]));
	}
	// add road points. color type = 2;
	for (var k=1; k<data[road_index].length; k++) {
	    cuts.push(data[road_index][k]);
	    types.push(2);
	}
	// add park points. color type = 3;
	for (var k=1; k<data[park_index].length; k++) {
	    cuts.push(data[park_index][k]);
	    types.push(3);
	}
	// add pond points. color type = 4;
	for (var k=1; k<data[water_index].length; k++) {
	    cuts.push(data[water_index][k]);
	    types.push(4);
	}

    }

    
    // draw grid
    undraw();
    draw_grid(250, 50, 850, 650, 50, 50, "black");
    // draw for 1st player
    var colors = ["orange", "black", "purple", "green", "blue"];
    draw_side ( 20,  40,  190, 690, group, score, cpu, colors);
    draw_shape(250,  50,  850, 650, 50, 50, cuts, colors, types, highlight == 0);
    return refresh;
}

var latest_version = -1;

function ajax(version, retries, timeout)
{
    var xhr = new XMLHttpRequest();
    xhr.onload = (function() {
	var refresh = -1;
	try {
	    if (xhr.readyState != 4)
		throw "Incomplete HTTP request: " + xhr.readyState;
	    if (xhr.status != 200)
		throw "Invalid HTTP status: " + xhr.status;
	    refresh = process(xhr.responseText);
	    if (latest_version < version)
		latest_version = version;
	    else
		refresh = -1;
	} catch (message) { alert(message); }
	if (refresh >= 0)
	    setTimeout(function() { ajax(version + 1, 10, 100); }, refresh);
    });
    xhr.onabort   = (function() { location.reload(true); });
    xhr.onerror   = (function() { location.reload(true); });
    xhr.ontimeout = (function() {
	if (version <= latest_version)
	    console.log("AJAX timeout (version " + version + " <= " + latest_version + ")");
	else if (retries == 0)
	    location.reload(true);
	else {
	    console.log("AJAX timeout (version " + version + ", retries: " + retries + ")");
	    ajax(version, retries - 1, timeout * 2);
	}
    });
    xhr.open("GET", "data.txt", true);
    xhr.responseType = "text";
    xhr.timeout = timeout;
    xhr.send();
}

ajax(0, 10, 100);
