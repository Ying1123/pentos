package pentos.sim;

import java.io.*;
import java.net.*;
import java.util.*;
import javax.tools.*;
import java.awt.Desktop;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

class Simulator {

    private static boolean log = false; // print output/scores and stuff. use --verbose to set to true

    private static final String root = "pentos";

    public static void main(String[] args) throws Exception
    {
	boolean gui = false;
	boolean gui_manual_refresh_on_cutter = false;
	String group = "g0";
	Class <Player> g_class = null;
	Class <Sequencer> s_class = null;
	String sequencer = "random";
	long cpu_time_ms = 300 * 1000;
	String tournament_path = null;
	// long[] timeout = new long [] {1000, 10000, 1000};
	long gui_refresh = 250;
	try {
	    for (int a = 0 ; a != args.length ; ++a)
		if (args[a].equals("-g") || args[a].equals("--groups")) {
		    if (a + 1 >= args.length)
			throw new IllegalArgumentException("Missing group name");
		    group = args[++a];
		}
		else if (args[a].equals("-s") || args[a].equals("--sequencer")) {
		    if (a+1 >= args.length)
			throw new IllegalArgumentException("Missing sequencer name");
		    sequencer = args[++a];
		}
		else if (args[a].equals("--gui-fps")) {
		    if (++a == args.length)
			throw new IllegalArgumentException("Missing GUI FPS");
		    double gui_fps = Double.parseDouble(args[a]);
		    gui_refresh = gui_fps > 0.0 ? (long) Math.round(1000.0 / gui_fps) : -1;
		    gui = true;
		} else if (args[a].equals("--tournament")) {
		    if (++a == args.length)
			throw new IllegalArgumentException("Missing tournament file");
		    tournament_path = args[a];
		} else if (args[a].equals("--gui")) gui = true;
		else if (args[a].equals("--gui-mrc"))
		    gui = gui_manual_refresh_on_cutter = true;
		else if (args[a].equals("--verbose"))
		    log = true;
		else throw new IllegalArgumentException("Unknown argument: " + args[a]);
	    g_class = load_player(group);
	    s_class = load_sequencer(sequencer);
	} catch (Exception e) {
	    System.err.println("Exception during setup: " + e.getMessage());
	    e.printStackTrace();
	    System.err.println("Exiting the simulator ...");
	    System.exit(1);
	}
	if (tournament_path != null) {
	    System.out.close();
	    System.err.close();
	} else if (!gui && log)
	    System.err.println("GUI: disabled");
	else if (gui_refresh < 0 && log)
	    System.err.println("GUI: enabled  (0 FPS)");
	else if (gui_refresh == 0 && log)
	    System.err.println("GUI: enabled  (maximum FPS)");
	else {
	    double gui_fps = 1000.0 / gui_refresh;
	    if (log)
		System.err.println("GUI: enabled  (up to " + gui_fps + " FPS)");
	}
	AtomicInteger score = new AtomicInteger(0);
	boolean timeout = false;
	try {
	    timeout = play(group, g_class, sequencer, s_class,
			   gui, gui_manual_refresh_on_cutter,
			   gui_refresh, cpu_time_ms, score);
	} catch (Exception e) {
	    if (tournament_path != null) throw e;
	    System.err.println("Exception during play: " + e.getMessage());
	    e.printStackTrace();
	    System.err.println("Exiting the simulator ...");
	    System.exit(1);
	}
	if (tournament_path == null) {
	    System.err.println("Player " + group + " scored " + score.get());
	    if (timeout) 
		System.err.println("Player timed out!");
	} else {
	    PrintStream file = new PrintStream(new FileOutputStream(tournament_path, true));
	    file.println(group + "," + score.get() + "," + (timeout == true ? "yes" : "no"));			 
	    file.close();
	}
	System.exit(0);
    }

    private static boolean play(String group,
			    Class <Player> g_class,
			    String sequencer,
			    Class <Sequencer> s_class,
			    boolean gui,
			    boolean gui_manual_refresh_on_cutter,
			    long gui_refresh,
			    long cpu_time_ms,
			    AtomicInteger score) throws Exception
    {
	int numRejects = 0;
	List <Move> moves = gui ? new ArrayList <Move> () : null;
	// initialize player
	Timer timer = new Timer();
	timer.start();
	final Class <Player> player_class = g_class;
	final Class <Sequencer> sequencer_class = s_class;
	Player player;
	Sequencer generator;
	try {
	    player = timer.call(new Callable <Player> () {

		    public Player call() throws Exception
		    {
			return player_class.newInstance();
		    }
		}, cpu_time_ms);
	} catch (TimeoutException e) {return true;}

	try {
	    generator = timer.call(new Callable <Sequencer> () {

		    public Sequencer call() throws Exception
		    {
			return sequencer_class.newInstance();
		    }
		}, cpu_time_ms);
	} catch (TimeoutException e) {return true;}


	// initialise GUI
	HTTPServer server = null;
	if (gui) {
	    server = new HTTPServer();
	    System.err.println("HTTP port: " + server.port());
	    // try to open web browser automatically
	    if (!Desktop.isDesktopSupported())
		System.err.println("Desktop operations not supported");
	    else if (!Desktop.getDesktop().isSupported(Desktop.Action.BROWSE))
		System.err.println("Desktop browsing not supported");
	    else {
		URI uri = new URI("http://localhost:" + server.port());
		Desktop.getDesktop().browse(uri);
	    }
	    gui(server, state(group, new AtomicInteger(0), 0, moves,
			      gui_refresh, -1));
	}
	// initialize score and termination
	// initialize land
	int land_side = 50;
	Land land = new Land(land_side);
	if (log)
	    System.err.println("Initializing player...");
	player.init();
	if (log)
	    System.err.println("Initializing sequencer...");
	generator.init();
	if (log)
	    System.err.println("Construction begins ...");
	do {
	    // get next build request
	    Building request = generator.next();
	    // call the play method of player
	    long timeout_ms = 0;
	    if (cpu_time_ms > 0) {
		long timeout_ns = cpu_time_ms * 1000000 - timer.time();
		if (timeout_ns <= 0) return true;
		timeout_ms = (timeout_ns / 1000000) + 1;
	    }
	    Move move = null;
	    try {
		move = timer.call(new Callable <Move> () {
			public Move call() throws Exception
			{
			    return player.play(request, land);
			}
		    }, timeout_ms);
	    } catch (TimeoutException e) { return true; }
	    if (!move.accept) {
		numRejects++;
		if (log)
		    System.err.println("Player " + group + " rejected building request. " + numRejects + " of 3 rejected.");
	    }
	    else {
		Building[] building_rotations = request.rotations();
		// check if rotation is valid
		if (move.rotation < 0 || move.rotation >= building_rotations.length)
		    throw new RuntimeException("Invalid building rotation");
		Building rotated_building = building_rotations[move.rotation];
		// play move. First build auxiliary structures.
		Iterator<Cell> water_cells = move.water.iterator();
		Iterator<Cell> park_cells = move.park.iterator();
		Iterator<Cell> road_cells = move.road.iterator();
		String roadCells = "";		
		while (water_cells.hasNext())
		    land.buildWater(water_cells.next());
		while (park_cells.hasNext())
		    land.buildPark(park_cells.next());
		while (road_cells.hasNext()){
		    Cell x = road_cells.next();
		    roadCells = roadCells + " " + x.i + "," + x.j;
		}
		road_cells = move.road.iterator();
		while (road_cells.hasNext()) 
		    land.buildRoad(road_cells.next());
		if (!land.validateRoads()) 
		    throw new RuntimeException("Roads not connected");
		String buildingCells = "";
		for (Cell p : rotated_building)
		    buildingCells = buildingCells + " (" + (p.i+move.location.i) + "," + (p.j+move.location.j) + ")";
		int delta = land.build(rotated_building, move.location);
		if (delta == -1)
		    throw new RuntimeException("Invalid building placement");
		if (log)
		    System.err.println("Player " + group + " built building on cells" + buildingCells + " and scored " + delta + ".");
		score.addAndGet(delta); 	
		if (!gui) continue;
		moves.add(move);
		gui(server, state(group, score, timer.time(), moves, gui_refresh, -1));
	    }
	} while (numRejects < 3);
	// final GUI frame
	if (gui) {
	    gui_refresh = -1;
	    gui(server, state(group, score, timer.time(), moves, gui_refresh, -1));
	    server.close();
	}
	return false;
    }

    public static String state(String group, AtomicInteger score, long cpu, List <Move> moves, long gui_refresh, int highlight)
    {
	StringBuffer buf = new StringBuffer();
	buf.append(group + "; " + score.get() + "; " + human_no_power(cpu / 1.0e9, 2) + "; " + moves.size() + "\n");
	// send cuts
	for (Move m : moves) {
	    Building b = m.request.rotations()[m.rotation];
	    buf.append("s");
	    buf.append(b.toString(m.location));
	    buf.append("\n");
	    buf.append("s");
	    buf.append(Cell.toString(m.road));
	    buf.append("\n");
	    buf.append("s");	    
	    buf.append(Cell.toString(m.park));
	    buf.append("\n");
	    buf.append("s");	    
	    buf.append(Cell.toString(m.water));
	    buf.append("\n");
	}
	buf.append(gui_refresh + "; " + highlight);
	return buf.toString();
    }

    public static void gui(HTTPServer server, String content)
	throws UnknownServiceException
    {
	String path = null;
	for (;;) {
	    // get request
	    for (;;)
		try {
		    path = server.request();
		    break;
		} catch (IOException e) {
		    System.err.println("HTTP request error: " + e.getMessage());
		}
	    // dynamic content
	    if (path.equals("data.txt")) {
		// send dynamic content
		try {
		    server.reply(content);
		    return;
		} catch (IOException e) {
		    System.err.println("HTTP dynamic reply error: " + e.getMessage());
		    continue;
		}
	    }
	    // static content
	    if (path.equals("")) path = "webpage.html";
	    else if (!path.equals("favicon.ico") &&
		     !path.equals("apple-touch-icon.png") &&
		     !path.equals("script.js")) break;
	    // send file
	    File file = new File(root + File.separator + "sim"
				 + File.separator + path);
	    try {
		server.reply(file);
	    } catch (IOException e) {
		System.err.println("HTTP static reply error: " + e.getMessage());
	    }
	}
	if (path == null)
	    throw new UnknownServiceException("Unknown HTTP request (null path)");
	else
	    throw new UnknownServiceException("Unknown HTTP request: \"" + path + "\"");
    }

    // scan directory (and subdirectories) for files with given extension
    private static Set <File> directory(String path, String extension)
    {
	Set <File> files = new HashSet <File> ();
	Set <File> prev_dirs = new HashSet <File> ();
	prev_dirs.add(new File(path));
	do {
	    Set <File> next_dirs = new HashSet <File> ();
	    for (File dir : prev_dirs)
		for (File file : dir.listFiles())
		    if (!file.canRead()) ;
		    else if (file.isDirectory())
			next_dirs.add(file);
		    else if (file.getPath().endsWith(extension))
			files.add(file);
	    prev_dirs = next_dirs;
	} while (!prev_dirs.isEmpty());
	return files;
    }

    // last modified
    private static long last_modified(Iterable <File> files)
    {
	long last_date = 0;
	for (File file : files) {
	    long date = file.lastModified();
	    if (last_date < date)
		last_date = date;
	}
	return last_date;
    }
    
    // compile and load
    private static Class <Player> load_player(String group) throws IOException, ReflectiveOperationException {
	String sep = File.separator;
	Set <File> player_files = directory(root + sep + group, ".java");
	File class_file = new File(root + sep + group + sep + "Player.class");
	long class_modified = class_file.exists() ? class_file.lastModified() : -1;
	if (class_modified < 0 || class_modified < last_modified(player_files) ||
	    class_modified < last_modified(directory(root + sep + "sim", ".java"))) {
	    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
	    if (compiler == null)
		throw new IOException("Cannot find Java compiler");
	    StandardJavaFileManager manager = compiler.
		getStandardFileManager(null, null, null);
	    long files = player_files.size();
	    if (log)
		System.err.print("Compiling " + files + " .java files ... ");
	    if (!compiler.getTask(null, manager, null, null, null,
				  manager.getJavaFileObjectsFromFiles(player_files)).call())
		throw new IOException("Compilation failed");
	    System.err.println("done!");
	    class_file = new File(root + sep + group + sep + "Player.class");
	    if (!class_file.exists())
		throw new FileNotFoundException("Missing class file");
	}
	ClassLoader loader = Simulator.class.getClassLoader();
	if (loader == null)
	    throw new IOException("Cannot find Java class loader");
	@SuppressWarnings("rawtypes")
	    Class raw_class = loader.loadClass(root + "." + group + ".Player");
	@SuppressWarnings("unchecked")
	    Class <Player> player_class = raw_class;
	return player_class;
    }

    private static Class <Sequencer> load_sequencer(String sequencer) throws IOException, ReflectiveOperationException {
    
	String sep = File.separator;
	Set <File> sequencer_files = directory(root + sep + sequencer, ".java");
	File class_file = new File(root + sep + sequencer + sep + "Sequencer.class");
	long class_modified = class_file.exists() ? class_file.lastModified() : -1;
	if (class_modified < 0 || class_modified < last_modified(sequencer_files) ||
	    class_modified < last_modified(directory(root + sep + "sim", ".java"))) {
	    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
	    if (compiler == null)
		throw new IOException("Cannot find Java compiler");
	    StandardJavaFileManager manager = compiler.
		getStandardFileManager(null, null, null);
	    long files = sequencer_files.size();
	    if (log)
		System.err.print("Compiling " + files + " .java files ... ");
	    if (!compiler.getTask(null, manager, null, null, null,
				  manager.getJavaFileObjectsFromFiles(sequencer_files)).call())
		throw new IOException("Compilation failed");
	    if (log)
		System.err.println("done!");
	    class_file = new File(root + sep + sequencer + sep + "Sequencer.class");
	    if (!class_file.exists())
		throw new FileNotFoundException("Missing class file");
	}
	ClassLoader loader = Simulator.class.getClassLoader();
	if (loader == null)
	    throw new IOException("Cannot find Java class loader");
	@SuppressWarnings("rawtypes")
	    Class raw_class = loader.loadClass(root + "." + sequencer + ".Sequencer");
	@SuppressWarnings("unchecked")
	    Class <Sequencer> sequencer_class = raw_class;
	return sequencer_class;

    }

    // parse a real number and cut the number of decimals
    private static String human_no_power(double x, int d)
    {
	if (x == 0.0) return "0";
	if (d < 0) throw new IllegalArgumentException();
	int e = 1;
	double b = 10.0;
	while (b <= x) {
	    b *= 10.0;
	    e++;
	}
	StringBuffer buf = new StringBuffer();
	do {
	    b *= 0.1;
	    int i = (int) (x / b);
	    x -= b * i;
	    if (e == 0) buf.append(".");
	    buf.append(i);
	} while (--e != -d);
	return buf.toString();
    }
}
