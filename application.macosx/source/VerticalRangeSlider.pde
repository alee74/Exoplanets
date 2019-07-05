
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
  float tickIncrement = 0.5;
  float radius = 1;
  float titlePadding = 10;
  float labelPadding = 7;
  String title = "Var A";
  float labelStrokeWeight = 3;
  float labelIndicatorLength = 20;
  color textColor = color(255);
  color baseColor = color(80);
  
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
  
  
  void display() {
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
  
  
  void drawTickMarks() {
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
  
  
  void reset() { highMarker.centerY = higherBound; lowMarker.centerY = lowerBound; }
  
  void setValueRange(float minVal, float maxVal) { minValue = minVal; maxValue = maxVal; }

  float getHigherValue() { return hidden ? maxValue : map(highMarker.centerY+sizeX/2, lowerBound,higherBound, minValue,maxValue); }
  
  float getLowerValue() { return hidden ? minValue : map(lowMarker.centerY-sizeX/2, lowerBound,higherBound, minValue,maxValue); }
  
  void show() { hidden = false; }
  
  void hide() { hidden = true; }
  
  void setPosition(float px, float py) { posX = px; posY = py; }
  
  void setSize(float sx, float sy) { sizeX = sx; sizeY = sy; }
  
  void setRadius(float r) { radius = r; }
  
  void setLabelStrokeWeight(float sw) { labelStrokeWeight = sw; }
  
  void setTitlePadding(float tp) { titlePadding = tp; }
  
  void setTitle(String t) { title = t; }
  
  void setTextColor(color tc) { textColor = tc; }
  
  void setColor(color c) { baseColor = c; }
  
}
