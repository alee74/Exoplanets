import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class exoplanets extends PApplet {


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


public void setup() {
  
  radial = true;
  mr = false;
  
  plotTitleFont = createFont("Arial Bold", 20);
  subtitleFont = createFont("Arial", 18);
  planetNameFont = createFont("Arial", 20);
  outputFont = createFont("Arial", 15);
  
  
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


public void draw() {
  
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

public void mousePressed() {
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


public void keyPressed() {
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


public String scientificNotation(float num, int sigfigs) {
  String ret = "";
  int scale = 0;
  // shift decimal
  while (num < 1.0f) {
    num *= 10;
    scale--;
  }
  while (num >= 10.0f) {
    num /= 10;
    scale++;
  }
  ret += PApplet.parseInt(num) + ".";
  // round to sigfigs decimal places
  for (int i = 1; i < sigfigs; i++) {
    num -= PApplet.parseInt(num);
    num *= 10;
    ret += PApplet.parseInt(num);
  }
  num -= PApplet.parseInt(num);
  num *= 10;
  ret += round(num);
  if (scale != 0)
    ret += "e" + scale;
  return ret;
}

// used by:
//    HorizontalSlider

class BoundedDragCircleX {
  
  // position and size
  float centerX; float centerY;
  float radius;
  
  // bounds
  float lowerBound;
  float upperBound;
  
  // color defaults
  int activeColor = color(255);
  int inactiveColor = color(230);
  
  // 
  boolean dragging = false;
  boolean hovering = false;
  boolean firstPress = false;
  boolean isPressed = false;
  
  
  BoundedDragCircleX(float cx, float cy, float r, float lb, float ub) {
    centerX = cx; centerY = cy;
    radius = r;
    lowerBound = lb;
    upperBound = ub;
  }
  
  BoundedDragCircleX(float cx, float cy, float r, float lb, float ub, int ac, int ic) {
    centerX = cx; centerY = cy;
    radius = r;
    activeColor = ac;
    inactiveColor = ic;
    lowerBound = lb;
    upperBound = ub;
  }  
  
  public void display() {
    
    if (!mousePressed) {
      dragging = false;
      firstPress = false;
      isPressed = false;
    } else {
      if (isPressed)
        firstPress = false;
      else {
        firstPress = true;
        isPressed = true;
      }
    }
    
    if (!dragging) {
      float dx = mouseX - centerX;
      float dy = mouseY - centerY;
      if (dx*dx + dy*dy < radius*radius) {
        if (firstPress)
          dragging = true;
        else
          hovering = true;
      } else
        hovering = false;
    } else {
      centerX += mouseX - pmouseX;
      if (centerX > upperBound)
        centerX = upperBound;
      if (centerX < lowerBound)
        centerX = lowerBound + 0.001f*(upperBound-lowerBound);
    }
      
    noStroke();
    ellipseMode(RADIUS);
    if (dragging || hovering)
      fill(activeColor);
    else
      fill(inactiveColor);
      
    ellipse(centerX,centerY, radius,radius);
    
  }
  
  
  public void setCenter(float cx, float cy) { centerX = cx; centerY = cy; }
  
  public void setRadius(float r) { radius = r; }
  
  public void setActiveColor(int ac) { activeColor = ac; }
  
  public void setInactiveColor(int ic) { inactiveColor = ic; }
  
  public void setLowerBound(float lb) { lowerBound = lb; }
  
  public void setUpperBound(float ub) { upperBound = ub; }
  
}

// used by:
//    VerticalRangeSlider

class BoundedDragRectY {
    
  // position and size
  float centerX;    float centerY;
  float sizeX = 20; float sizeY = 5;
  
  // bounds
  float lowerBound;
  float higherBound;
  
  // defaults
  float radius = 2.5f;
  int activeColor = color(56,183,255);
  int inactiveColor = color(125);
  
  // 
  boolean dragging = false;
  boolean hovering = false;
  boolean firstPress = false;
  boolean isPressed = false;
  
  
  BoundedDragRectY(float cx, float cy, float lb, float hb) {
    centerX = cx; centerY = cy;
    lowerBound = lb;
    higherBound = hb;
  }
  
  BoundedDragRectY(float cx, float cy, float sx, float sy, float lb, float hb) {
    centerX = cx; centerY = cy;
    sizeX = sx;   sizeY = sy;
    lowerBound = lb;
    higherBound = hb;
  }
  
  BoundedDragRectY(float cx, float cy, float sx, float sy, float lb, float hb, int ac, int ic) {
    centerX = cx; centerY = cy;
    sizeX = sx;   sizeY = sy;
    activeColor = ac;
    inactiveColor = ic;
    lowerBound = lb;
    higherBound = hb;
  }  
  
  public void display() {
    
    if (!mousePressed) {
      dragging = false;
      firstPress = false;
      isPressed = false;
    } else {
      if (isPressed)
        firstPress = false;
      else {
        firstPress = true;
        isPressed = true;
      }
    }
    
    if (!dragging) {
      if (mouseX > centerX-sizeX && mouseX < centerX+sizeX &&
          mouseY > centerY-sizeY && mouseY < centerY+sizeY) {
        if (firstPress)
          dragging = true;
        else
          hovering = true;
      } else
        hovering = false;
    } else {
      centerY += mouseY - pmouseY;
      if (centerY < higherBound)
        centerY = higherBound;
      if (centerY > lowerBound)
        centerY = lowerBound;
    }
      
    strokeWeight(1);
    rectMode(RADIUS);
    if (dragging || hovering)
      fill(activeColor);
    else
      fill(inactiveColor);
      
    rect(centerX,centerY, sizeX,sizeY, radius);
    
  }
  
  
  public float topEdge() { return centerY-sizeY; }
  
  public float bottomEdge() {return centerY+sizeY; }
  
  
  public void setCenter(float cx, float cy) { centerX = cx; centerY = cy; }
  
  public void setSize(float sx, float sy) { sizeX = sx; sizeY = sy; }
  
  public void setRadius(float r) { radius = r; }
  
  public void setActiveColor(int ac) { activeColor = ac; }
  
  public void setInactiveColor(int ic) { inactiveColor = ic; }
  
  public void setLowerBound(float lb) { lowerBound = lb; }
  
  public void setHigherBound(float hb) { higherBound = hb; }
  
}

// used by:
//    exoplanets
//    RadialPlot
//    ParallelCoordinatesPlot

// dependencies:
//    TextScale.java (for TextScale enum)

class Button {
  
  // position and size
  float posX;  float posY;
  float sizeX; float sizeY;
  String displayText;
  PFont font;

  
  // defaults
  float radius = 3;
  int btnColor = color(0,0,255);
  int hoverColor = color(150);
  int textColor = color(255);
  float textSize = 15;
  float minTextSize = 10;
  String text = "Hi!";
  TextScale scaleMode = TextScale.TRUNC;
  float padding = 1.75f;
  String fontName = "Arial Bold";
  
  // show/hide button
  boolean hidden = false;
    
  
  Button() {
    posX = 10;  posY = 10;
    sizeX = 50; sizeY = 50;
    scaleText();
  }
  
  Button(float px, float py, float sx, float sy) {
    posX = px;  posY = py;
    sizeX = sx; sizeY = sy;
    scaleText();
  }
  
  Button(float px, float py, float sx, float sy, int c, String t) {
    posX = px;  posY = py;
    sizeX = sx; sizeY = sy;
    btnColor = c;
    text = t;
    scaleText();
  }
  
  
  public void display() {
    if (!hidden) {
      noStroke();
      rectMode(CORNER);
      if (mouseOver())
        fill(hoverColor);
      else
        fill(btnColor);
      rect(posX,posY, sizeX,sizeY, radius);
      fill(textColor);
      textFont(font);
      textAlign(CENTER);
      text(displayText, posX+sizeX/2, posY+sizeY-padding-textDescent());
    }
  }
  
  
  public boolean mouseOver() {
    if (!hidden && mouseX > posX && mouseX < posX+sizeX
                && mouseY > posY && mouseY < posY+sizeY)
      return true;
    return false;
  }
  
  
  public void scaleText() {
    displayText = text;
    textSize(textSize);

    switch (scaleMode) {
      case STRETCH: {
        while (textWidth(text) > sizeX-(2*padding))
          sizeX++;
        while (textAscent()+textDescent() > sizeY-(2*padding))
          sizeY++;
      } break;
      
      case TRUNC: {
        while (textSize > minTextSize &&
              (textWidth(text) > sizeX-(2*padding) 
              || textAscent()+textDescent() > sizeY-(2*padding)))
          textSize(--textSize);
        if (textAscent()+textDescent() > sizeY-(2*padding))
          displayText = "";
        else if (textWidth(text) > sizeX-(2*padding)) {
          while (textWidth(displayText) > sizeX-(2*padding) && displayText.length() > 1)
            displayText = displayText.substring(0, displayText.length()-2);
        }
      } break;
      
      default: break;
    }
    font = createFont(fontName, textSize);
  }
  
      
  public void hide() { hidden = true; }
  
  public void show() { hidden = false; }
  
  public void setPosition(float px, float py) { posX = px; posY = py; }
  
  public void setSize(float sx, float sy) { sizeX = sx; sizeY = sy; }
  
  public void setRadius(float r) { radius = r; }
    
  public void setColor(int c) { btnColor = c; }
  
  public void setTextColor(int tc) { textColor = tc; }
  
  public void setTextSize(float ts) { textSize = ts; scaleText(); }
  
  public void setText(String t) { text = t; scaleText(); }
  
  public void setScaleMode(TextScale sm) { scaleMode = sm; scaleText(); }
  
  public void setPadding(float p) { padding = p; scaleText(); }
  
  public void setMinTextSize(float mts) { minTextSize = mts; scaleText(); }
  
  public void setHoverColor(int hc) { hoverColor = hc; }

}

// used by:
//    RadialPlot

// dependencies:
//    BoundedDragCircleX

// possible update:
//   add enum for text location

class HorizontalSlider {
  
  // position and size
  float posX;  float posY;
  float sizeX = 100; float sizeY = 10;
  
  BoundedDragCircleX marker;
  
  // bounds for marker center
  float lowerBound;
  float upperBound;
  
  // value range
  float minValue;  // minimum of value represented
  float maxValue;  // minimum of value represented
  
  // defaults
  float radius = 5;
  float padding = 7;
  String text = "Hellooo";
  int textColor = color(255);
  int emptyColor = color(150);
  int fillColor = color(0,0,200);
  
  // font
  PFont font = createFont("Arial", 13);
  
  // show/hide slider
  boolean hidden = false;
  
  
  HorizontalSlider() {
    posX = 10;  posY = 10;
    lowerBound = posX + sizeY;
    upperBound = posX + sizeX - sizeY;
    marker = new BoundedDragCircleX(upperBound,posY+sizeY/2, sizeY, lowerBound,upperBound);
  }
  
  HorizontalSlider(float px, float py, String t) {
    posX = px;  posY = py;
    text = t;
    lowerBound = posX + sizeY;
    upperBound = posX + sizeX - sizeY;
    marker = new BoundedDragCircleX(upperBound,posY+sizeY/2, sizeY, lowerBound,upperBound);
  }
  
  HorizontalSlider(float px, float py, float sx, float sy, String t) {
    posX = px;  posY = py;
    sizeX = sx; sizeY = sy;
    text = t;
    lowerBound = posX + sizeY;
    upperBound = posX + sizeX - sizeY;
    marker = new BoundedDragCircleX(upperBound,posY+sizeY/2, sizeY, lowerBound,upperBound);
  }
  
  
  public void display() {
    if (!hidden) {
      noStroke();
      // draw base rect
      fill(emptyColor);
      rect(posX,posY, sizeX,sizeY, radius);
      // draw overlaying "fill" rect
      fill(fillColor);
      rect(posX,posY, marker.centerX-posX,sizeY, radius);
      // draw marker
      marker.display();
      // draw text
      fill(textColor);
      textAlign(LEFT);
      textFont(font);
      text(text, posX-padding-textWidth(text), posY+sizeY);
    }
  }
  
  
  public float getValue() { return hidden ? maxValue : map(marker.centerX, lowerBound,upperBound, minValue,maxValue); } //(marker.centerX-lowerBound)/(upperBound-lowerBound); }
  
  public void setValueRange(float minVal, float maxVal) { minValue = minVal; maxValue = maxVal; }
  
  public void reset() { marker.centerX = upperBound; }
  
  public void hide() { hidden = true; }
  
  public void show() { hidden = false; }
  
  public void setPosition(float px, float py) { posX = px; posY = py; }
  
  public void setSize(float sx, float sy) { sizeX = sx; sizeY = sy; }
  
  public void setRadius(float r) { radius = r; }
  
  public void setEmptyColor(int ec) { emptyColor = ec; }
  
  public void setfillColor(int fc) { fillColor = fc; }
  
  public void setText(String t) { text = t; }
    
  public void setTextColor(int tc) { textColor = tc; }
  
  public void setPadding(float p) { padding = p; }
  
  public void setFont(PFont f) { font = f; }
  
}

// used by:
//    exoplanets

// dependencies:
//    Button -> TextScale.java
//    VerticalRangeSlider -> BoundedDragRectY


class ParallelCoordinatesPlot {
  
  // data
  String dataFile;
  Table data;
  String title;
  String subtitle;
  
  // slider x coordinates
  float massX;
  float radiusX;
  float orbPerX;
  float orbRadX;
  
  // buttons
  Button resetButton;
  
  // sliders
  VerticalRangeSlider massSlider;
  VerticalRangeSlider radiusSlider;
  VerticalRangeSlider orbPerSlider;
  VerticalRangeSlider orbRadSlider;
  
  // defaults
  float drawY = 125;
  float planetRadius = 5;
  float lineWeight = 2;
  float horizontalPadding = 100;
  int numSliders = 4;
  float sliderWidth = 6;
  float sliderHeight = height-275;
  int defaultColor = color(255);
  int selectedColor = color(200,0,0);
  //color selectedColor = color(56,183,255);
  
  // range values
  float minMass;          float maxMass;    
  float minRadius;        float maxRadius;  
  float minOrbPer;        float maxOrbPer;  
  float minOrbRad;        float maxOrbRad;  
  float activeMinMass;    float activeMaxMass;
  float activeMinRadius;  float activeMaxRadius;
  float activeMinOrbPer;  float activeMaxOrbPer;
  float activeMinOrbRad;  float activeMaxOrbRad;
  
  
  ParallelCoordinatesPlot(String fileName) {
    dataFile = fileName;
    data = loadTable(dataFile, "header, csv");
    title = "Planetary Radii, Planetary Masses, Orbital Radii and Orbital Periods of Confirmed Exoplanets";
    subtitle = "(natural log scale)";

    setRanges();
    setTableValues();
    
    // initial, where slider will be drawn
    massX = horizontalPadding;
    radiusX = horizontalPadding + (width-2*horizontalPadding)/3;
    orbPerX = horizontalPadding + 2*(width-2*horizontalPadding)/3;
    orbRadX = width-horizontalPadding;
    
    // create reset button
    resetButton = new Button(width/2-25,height-60, 50,20, color(0,200,0), "Reset");
    resetButton.setTextColor(color(0));
    resetButton.hide();
    
    // create sliders
    massSlider = new VerticalRangeSlider(massX,drawY, sliderWidth,sliderHeight, "Mass (Mjup)");
    massSlider.setValueRange(adjustMin(log(minMass)),adjustMax(log(maxMass)));
    radiusSlider = new VerticalRangeSlider(radiusX,drawY, sliderWidth,sliderHeight, "Radius (Rjup)");
    radiusSlider.setValueRange(adjustMin(log(minRadius)),adjustMax(log(maxRadius)));
    orbPerSlider = new VerticalRangeSlider(orbPerX,drawY, sliderWidth,sliderHeight, "Orbital Period (days)");
    orbPerSlider.setValueRange(adjustMin(log(minOrbPer)),adjustMax(log(maxOrbPer)));
    orbRadSlider = new VerticalRangeSlider(orbRadX,drawY, sliderWidth,sliderHeight, "Orbital Radius (AU)");
    orbRadSlider.setValueRange(adjustMin(log(minOrbRad)),adjustMax(log(maxOrbRad)));
    
    // adjust, where data points will be drawn
    massX += sliderWidth/2;
    radiusX += sliderWidth/2;
    orbPerX += sliderWidth/2;
    orbRadX += sliderWidth/2;
    
  }
  
  
  public void display() {
    
    // adjust data to be viewed
    setActiveRanges();
    refilter();
    
    // (de)activate reset button
    if (rangeNarrowed())
      resetButton.show();
    else
      resetButton.hide();
    
    // draw title
    fill(255);
    textFont(plotTitleFont);
    textAlign(CENTER);
    text(title, 0,10, width,30);
    textFont(subtitleFont);
    text(subtitle, 0,40, width,25);
    
    // draw button
    resetButton.display();
    
    // draw sliders
    massSlider.display();
    radiusSlider.display();
    orbPerSlider.display();
    orbRadSlider.display();
    
    // determine if a planet is selected
    int id = selectedPlanet();
    
    // handle click events
    if (mousePressed) {
      if (resetButton.mouseOver()) {
        resetSliders();
        //return;
      }
      // if doing anything with star names, do here
    }
    
    // draw lines for planets
    for (TableRow planet : data.rows())
      drawPlanet(planet,false);
    // draw selected planet above others
    if (id >= 0)
      drawPlanet(data.getRow(id),true);
      
      
    // output number of planets in active range
    fill(255);
    textAlign(RIGHT);
    textFont(outputFont);
    text("planets in range: " + planetsInRange(), width-10,height-10);
    
    // planet output - only if a planet is selected
    if (id >= 0) {
      textAlign(LEFT);
      textFont(planetNameFont);
      text(data.getString(id, "# name"), 5,height-90);
      textFont(outputFont);
      text("Mass:   " + data.getFloat(id, "mass") + " Mjup", 5,height-70); 
      text("Radius: " + data.getFloat(id, "radius") + " Rjup", 5,height-50);
      text("Orbital Period: "  + data.getFloat(id, "orbital_period") + " days", 5,height-30);
      text("Orbital Radius: "  + data.getFloat(id, "semi_major_axis") + " AU", 5,height-10);
    }
    
  }
  
 
  public void setActiveRanges() {
    // using exp() to undo the log() scaling
    
    activeMinMass = exp(massSlider.getLowerValue());
    activeMinRadius = exp(radiusSlider.getLowerValue());
    activeMinOrbPer = exp(orbPerSlider.getLowerValue());
    activeMinOrbRad = exp(orbRadSlider.getLowerValue());
    
    activeMaxMass = exp(massSlider.getHigherValue());
    activeMaxRadius = exp(radiusSlider.getHigherValue());
    activeMaxOrbPer = exp(orbPerSlider.getHigherValue());
    activeMaxOrbRad = exp(orbRadSlider.getHigherValue());
  }
  
  
  public void resetSliders() {
    massSlider.reset();
    radiusSlider.reset();
    orbPerSlider.reset();
    orbRadSlider.reset();
    resetButton.hide();
  }
  
  
  public boolean rangeNarrowed() {
    if (activeMinMass > minMass || activeMaxMass < maxMass ||
        activeMinRadius > minRadius || activeMaxRadius < maxRadius ||
        activeMinOrbPer > minOrbPer || activeMaxOrbPer < maxOrbPer ||
        activeMinOrbRad > minOrbRad || activeMaxOrbRad < maxOrbRad)
      return true;
    return false;
  }
  
  
  public void drawPlanet(TableRow planet, boolean selected) {
    // only draw planets in currently filtered range
    if (planet.getInt("inRange") == 0)
      return;
    
    // lines
    strokeWeight(lineWeight);
    if (selected)
      stroke(selectedColor);
    else
      stroke(255,150);
    line(massX,planet.getFloat("massY"), radiusX,planet.getFloat("radiusY"));
    line(radiusX,planet.getFloat("radiusY"), orbPerX,planet.getFloat("orbPerY"));
    line(orbPerX,planet.getFloat("orbPerY"), orbRadX,planet.getFloat("orbRadY"));
    
    // data points
    stroke(0);
    strokeWeight(1);
    if (selected)
      fill(selectedColor);
    else
      fill(defaultColor);
    ellipseMode(RADIUS);
    ellipse(massX,planet.getFloat("massY"), planetRadius,planetRadius);
    ellipse(radiusX,planet.getFloat("radiusY"), planetRadius,planetRadius);
    ellipse(orbPerX,planet.getFloat("orbPerY"), planetRadius,planetRadius);
    ellipse(orbRadX,planet.getFloat("orbRadY"), planetRadius,planetRadius);

  }
  
  
  public int selectedPlanet() {
    
    // TODO: check for overlapping on line?
    
    int id = -1;
    if (!mousePressed) {
      float planetDist;
      float closestPlanetDist = Float.MAX_VALUE;
      for (TableRow planet : data.rows()) {
        // skip planets that are out of range
        if (planet.getInt("inRange") == 0)
          continue;
        // check mass
        planetDist = dist(massX,planet.getFloat("massY"), mouseX,mouseY);
        if (planetDist <= planetRadius && planetDist < closestPlanetDist) {
          id = planet.getInt("ID");
          closestPlanetDist = planetDist;
        }
        // check radius
        planetDist = dist(radiusX,planet.getFloat("radiusY"), mouseX,mouseY);
        if (planetDist <= planetRadius && planetDist < closestPlanetDist) {
          id = planet.getInt("ID");
          closestPlanetDist = planetDist;
        }
        // check orbPer
        planetDist = dist(orbPerX,planet.getFloat("orbPerY"), mouseX,mouseY);
        if (planetDist <= planetRadius && planetDist < closestPlanetDist) {
          id = planet.getInt("ID");
          closestPlanetDist = planetDist;
        }
        // check orbRad
        planetDist = dist(orbRadX,planet.getFloat("orbRadY"), mouseX,mouseY);
        if (planetDist <= planetRadius && planetDist < closestPlanetDist) {
          id = planet.getInt("ID");
          closestPlanetDist = planetDist;
        }
      }
    }
    return id;
  }
  
  
  public void setRanges() {
        
    minMass = Float.MAX_VALUE;    maxMass = -1;
    minRadius = Float.MAX_VALUE;  maxRadius = -1;
    minOrbPer = Float.MAX_VALUE;  maxOrbPer = -1;
    minOrbRad = Float.MAX_VALUE;  maxOrbRad = -1;
    
    float mass;   float radius;
    float orbPer; float orbRad;
    
    for (TableRow planet : data.rows()) {
      
      mass = planet.getFloat("mass");
      radius = planet.getFloat("radius");
      orbPer = planet.getFloat("orbital_period");
      orbRad = planet.getFloat("semi_major_axis");
      
      if (mass > maxMass)
        maxMass = mass;
      if (mass < minMass)
        minMass = mass;
      if (radius > maxRadius)
        maxRadius = radius;
      if (radius < minRadius)
        minRadius = radius;
      if (orbPer > maxOrbPer)
        maxOrbPer = orbPer;
      if (orbPer < minOrbPer)
        minOrbPer = orbPer;
      if (orbRad > maxOrbRad)
        maxOrbRad = orbRad;
      if (orbRad < minOrbRad)
        minOrbRad = orbRad;
    }
    
    activeMinMass = minMass;      activeMaxMass = maxMass;
    activeMinRadius = minRadius;  activeMaxRadius = maxRadius;
    activeMinOrbPer = minOrbPer;  activeMaxOrbPer = maxOrbPer;
    activeMinOrbRad = minOrbRad;  activeMaxOrbRad = maxOrbRad;
    
  }
  

  public void setTableValues() {
    int i = 0;
    for (TableRow planet : data.rows()) {
      planet.setInt("ID", i++);
      planet.setInt("inRange", 1);
      planet.setFloat("massY", map(log(planet.getFloat("mass")), adjustMin(log(minMass)),adjustMax(log(maxMass)), drawY+sliderHeight,drawY));
      planet.setFloat("radiusY", map(log(planet.getFloat("radius")), adjustMin(log(minRadius)),adjustMax(log(maxRadius)), drawY+sliderHeight,drawY));
      planet.setFloat("orbPerY", map(log(planet.getFloat("orbital_period")), adjustMin(log(minOrbPer)),adjustMax(log(maxOrbPer)), drawY+sliderHeight,drawY));
      planet.setFloat("orbRadY", map(log(planet.getFloat("semi_major_axis")), adjustMin(log(minOrbRad)),adjustMax(log(maxOrbRad)), drawY+sliderHeight,drawY));
    }
  }
  

  public float adjustMin(float min) {
    float ret = PApplet.parseInt(min);
    // drop int, shift decimal
    min = (min-ret)* 10;
    if (min < 0) {
      if (min < -5)
        ret -= 1.0f;
      else
        ret -= 0.5f;
    } else if (min > 0) {
      if (min > 5)
        ret += 0.5f;
      else
        ret += 1.0f;
    }
    return ret;
  }
  
  
  public float adjustMax(float max) {
    float ret = PApplet.parseInt(max);
    max = (max-ret)*10;
    if (max < 0) {
      if (max < -5)
        ret -= 0.5f;
      else
        ret -= 1.0f;
    } else if (max > 0) {
      if (max > 5)
        ret += 1.0f;
      else
        ret += 0.5f;
    }
    return ret;
  }
  
  
  public void refilter() {
    for (TableRow planet : data.rows()) {
      if (planet.getFloat("mass") >= activeMinMass && planet.getFloat("mass") <= activeMaxMass &&
          planet.getFloat("radius") >= activeMinRadius && planet.getFloat("radius") <= activeMaxRadius &&
          planet.getFloat("orbital_period") >= activeMinOrbPer && planet.getFloat("orbital_period") <= activeMaxOrbPer &&
          planet.getFloat("semi_major_axis") >= activeMinOrbRad && planet.getFloat("semi_major_axis") <= activeMaxOrbRad)
        planet.setInt("inRange", 1);
      else
        planet.setInt("inRange", 0);
    }
  }
  
  
  public int planetsInRange() {
    int i = 0;
    for (TableRow planet : data.rows())
      i += planet.getInt("inRange");
    return i;
  }
  
}

// used by:
//    exoplanets

// dependencies:
//    Button -> TextScale.java
//    HorizontalSlider -> BoundedDragCircleX

class RadialPlot {
  
  String dataFile;
  Table data;
  String star;
  boolean mass_radius;
  String title;
  String subtitle;
  
  float minMass;    float maxMass;    float activeMaxMass;
  float minRadius;  float maxRadius;  float activeMaxRadius;
  float minOrbPer;  float maxOrbPer;  float activeMaxOrbPer;
  float minOrbRad;  float maxOrbRad;  float activeMaxOrbRad;
  
  // buttons
  Button backButton;
  Button resetButton;
  
  // sliders
  HorizontalSlider massSlider;
  HorizontalSlider radiusSlider;
  HorizontalSlider orbPerSlider;
  HorizontalSlider orbRadSlider;
  
  
  RadialPlot(String fileName, boolean mr) {
    
    dataFile = fileName;
    data = loadTable(dataFile, "header, csv");
    star = "";
    mass_radius = mr;
    title = "Orbital Radii and Orbital Periods of Confirmed Exoplanets";
    if (mass_radius)
      title = "Planetary Radii, Planetary Masses, " + title;
    subtitle = "(natural log scale)";

    setRanges();
    setTableValues(true);
    
    backButton = new Button(width/2-70,height-90, 65,20, color(0,200,0), "Back");
    backButton.setTextColor(color(0));
    backButton.hide();
    resetButton = new Button(width/2+5,height-90, 65,20, color(0,200,0), "Reset");
    resetButton.setTextColor(color(0));
    resetButton.hide();
    
    massSlider = new HorizontalSlider(width-220,height-80, 200,7, "Mass");
    massSlider.setValueRange(minMass,maxMass);
    radiusSlider = new HorizontalSlider(width-220,height-60, 200,7, "Radius");
    radiusSlider.setValueRange(minRadius,maxRadius);
    orbPerSlider = new HorizontalSlider(width-220,height-40, 200,7, "Orbital Period");
    orbPerSlider.setValueRange(minOrbPer,maxOrbPer);
    orbRadSlider = new HorizontalSlider(width-220,height-20, 200,7, "Orbital Radius");
    orbRadSlider.setValueRange(minOrbRad,maxOrbRad);
    
    if (!mass_radius) {
      massSlider.hide();
      radiusSlider.hide();
    }
    
  }

  
  public void display() {
    
    // adjust data to be viewed
    setActiveRanges();
    refilter();
    
    // (de)activate reset button
    if (rangeNarrowed())
      resetButton.show();
    else
      resetButton.hide();
    
    // draw title
    fill(255);
    textFont(plotTitleFont);
    textAlign(CENTER);
    text(title, 0,10, width,30);
    textFont(subtitleFont);
    text(subtitle, 0,40, width,25);
    
    // draw buttons
    backButton.display();
    resetButton.display();
    
    // draw sliders
    massSlider.display();
    radiusSlider.display();
    orbPerSlider.display();
    orbRadSlider.display();
    
    pushMatrix();
        
    // set origin
    translate(width/2, width/2+75); // width for both bc added region at bottom for text output
    rotate(3*PI/2); // to coincide with 12 on a clock
  
    // draw star in the middle
    ellipseMode(RADIUS);
    fill(255, 203, 34);
    ellipse(0, 0, 25, 25);
    
    // determine if a planet is selected
    int id = selectedPlanet();

    // mouse click events
    if (mousePressed) {
      // refliter if click on a selected planet
      if (!mass_radius && id >= 0 && star.equals("")) {
        star = data.getString(id, "star_name");
        backButton.show();
        //return;
      } else if (backButton.mouseOver()) {
        star = "";
        backButton.hide();
        //return;
      } else if (resetButton.mouseOver()) {
        resetSliders();
        //return;
      }
    }
  
    // draw planets
    for (TableRow planet : data.rows())
        drawPlanet(planet, false);
    // draw selected planet above others
    if (id >= 0)
      drawPlanet(data.getRow(id), true);
      
    popMatrix();

    // star name - only if selected
    fill(255);
    textAlign(LEFT);
    if (!star.equals("")) {
      //textSize(25);
      textFont(plotTitleFont);
      text(star, 5, id >= 0 ? height-75 : height-10);
    }
    
    // planet output - only if planet selected
    if (id >= 0) {
      textFont(planetNameFont);
      text(data.getString(id, "# name"), 5, mass_radius ? height-90 : height-50);
      textFont(outputFont);
      if (mass_radius) {
        text("Mass:   " + data.getFloat(id, "mass") + " Mjup", 5, height-70); 
        text("Radius: " + data.getFloat(id, "radius") + " Rjup", 5, height-50);
      }
      text("Orbital Period: "  + data.getFloat(id, "orbital_period") + " days", 5, height-30);
      text("Orbital Radius: "  + data.getFloat(id, "semi_major_axis") + " AU", 5, height-10);
    }
    
    // output number of planets in active range
    textFont(outputFont);
    text("planets in range: " + planetsInRange(), width-200, mass_radius ? height-90 : height-50);
    
  }
  
  
  public void drawPlanet(TableRow planet, boolean selected) {
    // skip planets that are out of range
    if (planet.getInt("inRange") == 0)
      return;
      
    float radius = planet.getFloat("draw_radius");
  
    pushMatrix();
    rotate(planet.getFloat("draw_angle"));
    translate(planet.getFloat("draw_x"), 0);
  
    // set fill
    if (selected)
      fill(200, 0, 0);
    else if (mass_radius)
      fill(map(planet.getFloat("mass"), minMass, activeMaxMass, 75, 255));
    else
      fill(255, 100);
  
    // draw planet
    stroke(1);
    ellipse(0, 0, radius, radius);
  
    popMatrix();
  }
  
  
  public int selectedPlanet() {
    int id = -1;
    
    // define here so we don't redifine in each iteration
    PMatrix adjustedCoordinateSystem;
    PVector mousePos;
    float planetDist;
    
    float closestPlanetDist = Float.MAX_VALUE;
    for (TableRow planet : data.rows()) {
      
      // skip out of range planets
      if (planet.getInt("inRange") == 0)
        continue;
        
      pushMatrix();
      rotate(planet.getFloat("draw_angle"));
      translate(planet.getFloat("draw_x"), 0);
      
      // determine mouse position
      adjustedCoordinateSystem = getMatrix();
      adjustedCoordinateSystem.invert();
      mousePos = adjustedCoordinateSystem.mult(new PVector(mouseX, mouseY), null);
      
      // check distance to planet
      planetDist = dist(mousePos.x, mousePos.y, 0,0);
      if (planetDist <= planet.getFloat("draw_radius") && planetDist < closestPlanetDist) {
        closestPlanetDist = planetDist;
        id = planet.getInt("ID");
      }
      
      popMatrix();
    }
    
    return id;
  }
    
  
  public boolean rangeNarrowed() {
    if (activeMaxOrbPer < maxOrbPer || activeMaxOrbRad < maxOrbRad ||
        (mass_radius && (activeMaxMass < maxMass || activeMaxRadius < maxRadius)))
      return true;
    return false;
  }
  
  
  public void setActiveRanges() {
    activeMaxMass = massSlider.getValue();
    activeMaxRadius = radiusSlider.getValue();
    activeMaxOrbPer = orbPerSlider.getValue();
    activeMaxOrbRad = orbRadSlider.getValue();
  }
  
  
  public void resetSliders() {
    massSlider.reset();
    radiusSlider.reset();
    orbPerSlider.reset();
    orbRadSlider.reset();
    resetButton.hide();
  }
  
  
  public void setRanges() {
        
    minMass = Float.MAX_VALUE;    maxMass = -1;
    minRadius = Float.MAX_VALUE;  maxRadius = -1;
    minOrbPer = Float.MAX_VALUE;  maxOrbPer = -1;
    minOrbRad = Float.MAX_VALUE;  maxOrbRad = -1;
    
    float mass;   float radius;
    float orbPer; float orbRad;
    
    for (TableRow planet : data.rows()) {
      
      mass = planet.getFloat("mass");
      radius = planet.getFloat("radius");
      orbPer = planet.getFloat("orbital_period");
      orbRad = planet.getFloat("semi_major_axis");
      
      if (mass_radius) {
        if (mass > maxMass)
          maxMass = mass;
        if (mass < minMass)
          minMass = mass;
        if (radius > maxRadius)
          maxRadius = radius;
        if (radius < minRadius)
          minRadius = radius;
      }
      
      if (orbPer > maxOrbPer)
        maxOrbPer = orbPer;
      if (orbPer < minOrbPer)
        minOrbPer = orbPer;
      if (orbRad > maxOrbRad)
        maxOrbRad = orbRad;
      if (orbRad < minOrbRad)
        minOrbRad = orbRad;
    }
    
    activeMaxMass = maxMass;
    activeMaxRadius = maxRadius;
    activeMaxOrbPer = maxOrbPer;
    activeMaxOrbRad = maxOrbRad;
    
  }
  
  
  public void setTableValues(boolean firstCall) {
    int i = 0;
    for (TableRow planet : data.rows()) {
      if (firstCall) {
        planet.setInt("ID", i++);
        planet.setInt("inRange", 1);
      }
      planet.setFloat("draw_angle",
          map(log(planet.getFloat("orbital_period")), log(minOrbPer),log(activeMaxOrbPer), 0,TWO_PI));
      planet.setFloat("draw_radius",
          mass_radius ? map(planet.getFloat("radius"), minRadius, activeMaxRadius, 3, 8) : 5);
      planet.setFloat("draw_x",
          map(log(planet.getFloat("semi_major_axis")), log(minOrbRad), log(activeMaxOrbRad), 60, (width/2)-10));
    }
  }
  
  
  public void refilter() {
    for(TableRow planet : data.rows())
      if (planet.getFloat("orbital_period") > activeMaxOrbPer || planet.getFloat("semi_major_axis") > activeMaxOrbRad ||
          (!star.equals("") && !planet.getString("star_name").equals(star)) ||
          (mass_radius && (planet.getFloat("mass") > activeMaxMass || planet.getFloat("radius") > activeMaxRadius)))
        planet.setInt("inRange", 0);
      else
        planet.setInt("inRange", 1);
        
    setTableValues(false);
  }
  
  
  public int planetsInRange() {
    int i = 0;
    for (TableRow planet : data.rows())
      i += planet.getInt("inRange");
    return i;
  }
  
  
  public void outputSliderValues() {
    println("massSlider.getValue() = " + massSlider.getValue());
    println("radiusSlider.getValue() = " + radiusSlider.getValue());
    println("orbPerSlider.getValue() = " + orbPerSlider.getValue());
    println("orbRadSlider.getValue() = " + orbRadSlider.getValue());
    println("////////////////////////////////////////////////////");
  }
  
}

// used by:
//  ParallelCoordinatesPlot

// dependencies:
//    BoundedDragRectY

class VerticalRangeSlider {
  
  // size and position
  float posX;       float posY;
  float sizeX = 10; float sizeY = 100;
  
  BoundedDragRectY highMarker;
  BoundedDragRectY lowMarker;
  
  // bounds for markers
  float lowerBound;   // lower screen position, not y value
  float higherBound;  // higher screen position, not y value
  
  // value range
  float minValue;  // minimum of value represented
  float maxValue;  // minimum of value represented
  
  // fonts
  PFont titleFont = createFont("Arial Bold", 15);
  PFont labelFont = createFont("Arial",12);
  
  // defaults
  int numTicks = 10;
  float tickIncrement = 0.5f;
  float radius = 1;
  float titlePadding = 10;
  float labelPadding = 7;
  String title = "Var A";
  float labelStrokeWeight = 3;
  float labelIndicatorLength = 20;
  int textColor = color(255);
  int baseColor = color(80);
  
  // show/hide slider
  boolean hidden = false;
  
  
  VerticalRangeSlider() {
    posX = 10; posY = 10;
    lowerBound = posY + sizeY;
    higherBound = posY;
    labelIndicatorLength = sizeX*4;
    highMarker = new BoundedDragRectY(posX+sizeX/2,higherBound, sizeX*2,sizeX, lowerBound,higherBound);
    lowMarker = new BoundedDragRectY(posX+sizeX/2,lowerBound, sizeX*2,sizeX, lowerBound,higherBound);
  }
  
  VerticalRangeSlider(float px, float py, String t) {
    posX = px; posY = py;
    title = t;
    lowerBound = posY + sizeY;
    higherBound = posY;
    labelIndicatorLength = sizeX*4;
    highMarker = new BoundedDragRectY(posX+sizeX/2,higherBound, sizeX*2,sizeX, lowerBound,higherBound);
    lowMarker = new BoundedDragRectY(posX+sizeX/2,lowerBound, sizeX*2,sizeX, lowerBound,higherBound);
  }
  
  VerticalRangeSlider(float px, float py, float sx, float sy, String t) {
    posX = px;  posY = py;
    sizeX = sx; sizeY = sy;
    title = t;
    lowerBound = posY + sizeY;
    higherBound = posY;
    labelIndicatorLength = sizeX*3;
    highMarker = new BoundedDragRectY(posX+sizeX/2,higherBound, sizeX*2,sizeX/2, lowerBound,higherBound);
    lowMarker = new BoundedDragRectY(posX+sizeX/2,lowerBound, sizeX*2,sizeX/2, lowerBound,higherBound);
  }
  
  
  public void display() {
    if (!hidden) {
      // prevent marker overlap
      highMarker.setLowerBound(lowMarker.topEdge()-sizeX);
      lowMarker.setHigherBound(highMarker.bottomEdge()+sizeX);
      
      // draw base rect
      noStroke();
      rectMode(CORNER);
      fill(baseColor);
      rect(posX,posY, sizeX,sizeY, radius);
      
      // draw labels
      drawTickMarks();
      
      // draw markers
      highMarker.display();
      lowMarker.display();
      
      // draw title
      fill(textColor);
      textAlign(CENTER);
      textFont(titleFont);
      text(title, posX+sizeX/2,posY-sizeX-titlePadding);
    }
  }
  
  
  public void drawTickMarks() {
    // determine tick increment
    if ((maxValue-minValue)/tickIncrement > numTicks)
      tickIncrement = (maxValue-minValue)/numTicks;
    
    fill(textColor);
    textAlign(RIGHT);
    textFont(labelFont);
    strokeCap(SQUARE);
    stroke(baseColor);
    strokeWeight(labelStrokeWeight);
    float y;
    float val = minValue;
    do {
      y = map(val, minValue,maxValue, posY+sizeY,posY);
      line(posX-labelIndicatorLength,y, posX+sizeX,y);
      text(val, posX-labelIndicatorLength-labelPadding,y+textAscent()/2);
      val += tickIncrement;
    } while (val <= maxValue);
  }
  
  
  public void reset() { highMarker.centerY = higherBound; lowMarker.centerY = lowerBound; }
  
  public void setValueRange(float minVal, float maxVal) { minValue = minVal; maxValue = maxVal; }

  public float getHigherValue() { return hidden ? maxValue : map(highMarker.centerY+sizeX/2, lowerBound,higherBound, minValue,maxValue); }
  
  public float getLowerValue() { return hidden ? minValue : map(lowMarker.centerY-sizeX/2, lowerBound,higherBound, minValue,maxValue); }
  
  public void show() { hidden = false; }
  
  public void hide() { hidden = true; }
  
  public void setPosition(float px, float py) { posX = px; posY = py; }
  
  public void setSize(float sx, float sy) { sizeX = sx; sizeY = sy; }
  
  public void setRadius(float r) { radius = r; }
  
  public void setLabelStrokeWeight(float sw) { labelStrokeWeight = sw; }
  
  public void setTitlePadding(float tp) { titlePadding = tp; }
  
  public void setTitle(String t) { title = t; }
  
  public void setTextColor(int tc) { textColor = tc; }
  
  public void setColor(int c) { baseColor = c; }
  
}
  public void settings() {  size(910, 1000); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "exoplanets" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
