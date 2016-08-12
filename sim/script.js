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

function draw_shape(min_x, min_y, max_x, max_y, rows, cols, patterns, points, colors, highlight)
{
	var canvas = document.getElementById("canvas");
	var ctx = canvas.getContext("2d");
	if (min_x < 0 || max_x > canvas.width)
		throw "Invalid x-axis bounds: " + min_x + " - " + max_x;
	if (min_y < 0 || max_y > canvas.height)
		throw "Invalid y-axis bounds: " + min_y + " - " + max_y;
	// draw boxes
	for (var i = 0 ; i != points.length ; ++i) {
		var color = null;
		var diagonals = highlight && (i + 1 == points.length);
		for (var j = 0 ; j != patterns.length ; ++j)
			if (points[i].length == patterns[j].length) {
				color = colors[j];
				break;
			}
		for (var j = 0 ; j != points[i].length ; ++j) {
			if (points[i][j].length != 2)
				throw "Invalid point format: " + points[i][j].length;
			row = points[i][j][0];
			col = points[i][j][1];
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
}

function draw_side(min_x, min_y, max_x, max_y, group, score, cpu, cutters, colors)
{
	var canvas = document.getElementById("canvas");
	var ctx = canvas.getContext("2d");
	if (min_x < 0 || max_x > canvas.width)
		throw "Invalid x-axis bounds: " + min_x + " - " + max_x;
	if (min_y < 0 || max_y > canvas.height)
		throw "Invalid y-axis bounds: " + min_y + " - " + max_y;
	// draw message
	ctx.font = "18px Arial";
	ctx.textAlign = "left";
	ctx.lineWidth = 4;
	ctx.strokeStyle = "black";
	ctx.strokeText("Player: " + group,        min_x, min_y + 30);
	ctx.strokeText("Score: " + score,         min_x, min_y + 60);
	ctx.strokeText("CPU time: " + cpu + " s", min_x, min_y + 90);
	ctx.fillStyle = colors[0];
	ctx.fillText("Player: " + group,        min_x, min_y + 30);
	ctx.fillText("Score: " + score,         min_x, min_y + 60);
	ctx.fillText("CPU time: " + cpu + " s", min_x, min_y + 90);
	// draw cutters
	min_y += 140;
	for (var i = 0 ; i != cutters.length ; ++i) {
		var size = cutters[i].length;
		max_x = min_x + size * 15;
		max_y = min_y + size * 15;
		draw_grid(min_x, min_y, max_x, max_y, size, size);
		var cu = [cutters[i]];
		var co = [colors[i]];
		draw_shape(min_x, min_y, max_x, max_y, size, size, cu, cu, co);
		min_y = max_y + 50;
	}
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
	if (data.length < 3)
		throw "Invalid data format";
	for (var i = 0 ; i != data.length ; ++i)
		data[i] = data[i].split(",");
	var i = data.length - 1;
	if (data[0].length != 5 || data[1].length != 5 || data[i].length != 2)
		throw "Invalid data format";
	var refresh   = parse_int(data[i][0]);
	var highlight = parse_int(data[i][1]);
	if (refresh < 0.0) refresh = -1;
	else refresh = Math.round(refresh);
	var group_1 = data[0][0].trim();
	var group_2 = data[1][0].trim();
	var score_1 = parse_int(data[0][1]);
	var score_2 = parse_int(data[1][1]);
	var cpu_1 = data[0][2].trim();
	var cpu_2 = data[1][2].trim();
	var n_cutters_1 = parse_int(data[0][3]);
	var n_cutters_2 = parse_int(data[1][3]);
	var n_cuts_1    = parse_int(data[0][4]);
	var n_cuts_2    = parse_int(data[1][4]);
	if (n_cuts_1 + n_cutters_1 + 3 +
	    n_cuts_2 + n_cutters_2 != data.length)
		throw "Invalid data format (invalid total lines)"
	i = 2;
	var cutters_1 = [], cuts_1 = [];
	var cutters_2 = [], cuts_2 = [];
	for (var j = 0 ; j != n_cutters_1 ; ++i, ++j)
		cutters_1.push(parse_points(data[i]));
	for (var j = 0 ; j != n_cutters_2 ; ++i, ++j)
		cutters_2.push(parse_points(data[i]));
	for (var j = 0 ; j != n_cuts_1 ; ++i, ++j)
		cuts_1.push(parse_points(data[i]));
	for (var j = 0 ; j != n_cuts_2 ; ++i, ++j)
		cuts_2.push(parse_points(data[i]));
	// draw grid
	undraw();
	draw_grid(250, 50, 850, 650, 50, 50, "black");
	// draw for 1st player
	var colors_1 = ["salmon", "red", "darkred"];
	var colors_2 = ["greenyellow", "olive", "darkgreen"];
	draw_side ( 20,  40,  190, 690, group_1, score_1, cpu_1, cutters_1, colors_1);
	draw_shape(250,  50,  850, 650, 50, 50, cutters_1, cuts_1, colors_1, highlight == 0);
	// draw for 2nd player
	draw_side (920,  40, 1090, 690, group_2, score_2, cpu_2, cutters_2, colors_2);
	draw_shape(250,  50,  850, 650, 50, 50, cutters_2, cuts_2, colors_2, highlight == 1);
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
