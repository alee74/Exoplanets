
// plots
RadialPlot oporRadialPlot;  // filtered for orbital period and orbital radius
RadialPlot mrRadialPlot;    // filtered further for mass and radius
ParallelCoordinatesPlot parCoordsPlot;

// buttons for switching between plots
Button opor2mr;      // switch from oporRadialPlot to mrRadialPlot
Button mr2opor;      // switch from mrRadialPlot to oporRadialPlot
Button toParCoords;  // switch to ParallelCoordinatesPlot
Button toRadial;     // switch to previous RadialPlot

// booleans for determining which plot to display
boolean radial;
boolean mr;

// font for titles
PFont plotTitleFont;
PFont subtitleFont;
PFont planetNameFont;
PFont outputFont;


void setup() {
  
  radial = true;
  mr = false;
  
  plotTitleFont = createFont("Arial Bold", 20);
  subtitleFont = createFont("Arial", 18);
  planetNameFont = createFont("Arial", 20);
  outputFont = createFont("Arial", 15);
  
  size(910, 1000);
  String opsdDataFile = "data/kepler_opor.csv";
  String mrDataFile = "data/kepler_mr.csv";

  // load data from kepler.csv
  Table data = loadTable("kepler.csv", "header, csv");
  // add columns to data
  data.addColumn("ID", Table.INT);
  // columns for RadialPlot
  data.addColumn("draw_angle", Table.FLOAT);
  data.addColumn("draw_radius", Table.FLOAT);
  data.addColumn("draw_x", Table.FLOAT);
  // columns for ParallelCoordinatesPlot
  data.addColumn("massY", Table.FLOAT);
  data.addColumn("radiusY", Table.FLOAT);
  data.addColumn("orbPerY", Table.FLOAT);
  data.addColumn("orbRadY", Table.FLOAT);
  // column for "filtering"
  data.addColumn("inRange", Table.INT);
  
  // filter data and create oporRadialPlot
  for (int i = 0; i < data.getRowCount(); i++)
    if (Float.isNaN(data.getFloat(i, "orbital_period")) || Float.isNaN(data.getFloat(i, "semi_major_axis")))
      data.removeRow(i--);  // i-- to ensure no skipped rows
  saveTable(data, opsdDataFile, "csv");
  oporRadialPlot = new RadialPlot(opsdDataFile, false);

  // filter further and create mrRadialPlot
  for (int i = 0; i < data.getRowCount(); i++)
    if (Float.isNaN(data.getFloat(i, "mass")) || Float.isNaN(data.getFloat(i, "radius")))
      data.removeRow(i--);  // i-- to ensure no skipped rows
  saveTable(data, mrDataFile, "csv");
  mrRadialPlot = new RadialPlot(mrDataFile, true);
  
  // create parCoordsPlot
  parCoordsPlot = new ParallelCoordinatesPlot(mrDataFile);
  
  // create Button for switching between RadialPlots
  opor2mr = new Button(width/2-70,height-60, 140,20, color(0,0,200), "Show Mass and Radius");
  mr2opor = new Button(width/2-70,height-60, 140,20, color(0,0,200), "Hide Mass and Radius");
  mr2opor.hide();
  toParCoords = new Button(width/2-70,height-30, 140,20, color(102,51,153), "Parallel Coordinates");
  toRadial = new Button(width/2-70,height-30, 140,20, color(102,51,153), "Radial");
  toRadial.hide();
  

}


void draw() {
  
  background(0);
  
  // display buttons
  opor2mr.display();
  mr2opor.display();
  toParCoords.display();
  toRadial.display();
  
  // display proper plot
  if (radial) {
    if (mr)
      mrRadialPlot.display();
    else
      oporRadialPlot.display();
  } else
    parCoordsPlot.display();

  
}

void mousePressed() {
  if (opor2mr.mouseOver()) {
    mr = true;
    opor2mr.hide();
    mr2opor.show();
    return;
  } else if (mr2opor.mouseOver()) {
    mr = false;
    mr2opor.hide();
    opor2mr.show();
    return;
  } else if (toParCoords.mouseOver()) {
    radial = false;
    mr2opor.hide();
    opor2mr.hide();
    toParCoords.hide();
    toRadial.show();
    return;
  } else if (toRadial.mouseOver()) {
    radial = true;
    if (mr)
      mr2opor.show();
    else
      opor2mr.show();
    toParCoords.show();
    toRadial.hide();
    return;
  }
}


void keyPressed() {
  if (key == BACKSPACE) {
    if (radial) {
      if (mr)
        mrRadialPlot.resetSliders();
       else {
         if (!oporRadialPlot.star.equals(""))
           oporRadialPlot.star = "";
         else
           oporRadialPlot.resetSliders();
       }
    } else
      parCoordsPlot.resetSliders();
  }
  
  else if (key == ENTER) {
    if (radial) {
      if (mr)
        mrRadialPlot.outputSliderValues();
      else
        oporRadialPlot.outputSliderValues();
    }
  }
}


String scientificNotation(float num, int sigfigs) {
  String ret = "";
  int scale = 0;
  // shift decimal
  while (num < 1.0) {
    num *= 10;
    scale--;
  }
  while (num >= 10.0) {
    num /= 10;
    scale++;
  }
  ret += int(num) + ".";
  // round to sigfigs decimal places
  for (int i = 1; i < sigfigs; i++) {
    num -= int(num);
    num *= 10;
    ret += int(num);
  }
  num -= int(num);
  num *= 10;
  ret += round(num);
  if (scale != 0)
    ret += "e" + scale;
  return ret;
}
