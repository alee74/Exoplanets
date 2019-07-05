
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
  
  
  void drawPlanet(TableRow planet, boolean selected) {
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
  
  
  int selectedPlanet() {
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
    
  
  boolean rangeNarrowed() {
    if (activeMaxOrbPer < maxOrbPer || activeMaxOrbRad < maxOrbRad ||
        (mass_radius && (activeMaxMass < maxMass || activeMaxRadius < maxRadius)))
      return true;
    return false;
  }
  
  
  void setActiveRanges() {
    activeMaxMass = massSlider.getValue();
    activeMaxRadius = radiusSlider.getValue();
    activeMaxOrbPer = orbPerSlider.getValue();
    activeMaxOrbRad = orbRadSlider.getValue();
  }
  
  
  void resetSliders() {
    massSlider.reset();
    radiusSlider.reset();
    orbPerSlider.reset();
    orbRadSlider.reset();
    resetButton.hide();
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
  
  
  void setTableValues(boolean firstCall) {
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
  
  
  void refilter() {
    for(TableRow planet : data.rows())
      if (planet.getFloat("orbital_period") > activeMaxOrbPer || planet.getFloat("semi_major_axis") > activeMaxOrbRad ||
          (!star.equals("") && !planet.getString("star_name").equals(star)) ||
          (mass_radius && (planet.getFloat("mass") > activeMaxMass || planet.getFloat("radius") > activeMaxRadius)))
        planet.setInt("inRange", 0);
      else
        planet.setInt("inRange", 1);
        
    setTableValues(false);
  }
  
  
  int planetsInRange() {
    int i = 0;
    for (TableRow planet : data.rows())
      i += planet.getInt("inRange");
    return i;
  }
  
  
  void outputSliderValues() {
    println("massSlider.getValue() = " + massSlider.getValue());
    println("radiusSlider.getValue() = " + radiusSlider.getValue());
    println("orbPerSlider.getValue() = " + orbPerSlider.getValue());
    println("orbRadSlider.getValue() = " + orbRadSlider.getValue());
    println("////////////////////////////////////////////////////");
  }
  
}
