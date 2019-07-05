
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
  color defaultColor = color(255);
  color selectedColor = color(200,0,0);
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
  
  
  void display() {
    
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
  
 
  void setActiveRanges() {
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
  
  
  void resetSliders() {
    massSlider.reset();
    radiusSlider.reset();
    orbPerSlider.reset();
    orbRadSlider.reset();
    resetButton.hide();
  }
  
  
  boolean rangeNarrowed() {
    if (activeMinMass > minMass || activeMaxMass < maxMass ||
        activeMinRadius > minRadius || activeMaxRadius < maxRadius ||
        activeMinOrbPer > minOrbPer || activeMaxOrbPer < maxOrbPer ||
        activeMinOrbRad > minOrbRad || activeMaxOrbRad < maxOrbRad)
      return true;
    return false;
  }
  
  
  void drawPlanet(TableRow planet, boolean selected) {
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
  
  
  int selectedPlanet() {
    
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
  
  
  void setRanges() {
        
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
  

  void setTableValues() {
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
  

  float adjustMin(float min) {
    float ret = int(min);
    // drop int, shift decimal
    min = (min-ret)* 10;
    if (min < 0) {
      if (min < -5)
        ret -= 1.0;
      else
        ret -= 0.5;
    } else if (min > 0) {
      if (min > 5)
        ret += 0.5;
      else
        ret += 1.0;
    }
    return ret;
  }
  
  
  float adjustMax(float max) {
    float ret = int(max);
    max = (max-ret)*10;
    if (max < 0) {
      if (max < -5)
        ret -= 0.5;
      else
        ret -= 1.0;
    } else if (max > 0) {
      if (max > 5)
        ret += 1.0;
      else
        ret += 0.5;
    }
    return ret;
  }
  
  
  void refilter() {
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
  
  
  int planetsInRange() {
    int i = 0;
    for (TableRow planet : data.rows())
      i += planet.getInt("inRange");
    return i;
  }
  
}
