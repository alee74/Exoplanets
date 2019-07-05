
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
  color textColor = color(255);
  color emptyColor = color(150);
  color fillColor = color(0,0,200);
  
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
  
  
  void display() {
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
  
  
  float getValue() { return hidden ? maxValue : map(marker.centerX, lowerBound,upperBound, minValue,maxValue); } //(marker.centerX-lowerBound)/(upperBound-lowerBound); }
  
  void setValueRange(float minVal, float maxVal) { minValue = minVal; maxValue = maxVal; }
  
  void reset() { marker.centerX = upperBound; }
  
  void hide() { hidden = true; }
  
  void show() { hidden = false; }
  
  void setPosition(float px, float py) { posX = px; posY = py; }
  
  void setSize(float sx, float sy) { sizeX = sx; sizeY = sy; }
  
  void setRadius(float r) { radius = r; }
  
  void setEmptyColor(color ec) { emptyColor = ec; }
  
  void setfillColor(color fc) { fillColor = fc; }
  
  void setText(String t) { text = t; }
    
  void setTextColor(color tc) { textColor = tc; }
  
  void setPadding(float p) { padding = p; }
  
  void setFont(PFont f) { font = f; }
  
}
