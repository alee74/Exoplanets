
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
  float radius = 2.5;
  color activeColor = color(56,183,255);
  color inactiveColor = color(125);
  
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
  
  BoundedDragRectY(float cx, float cy, float sx, float sy, float lb, float hb, color ac, color ic) {
    centerX = cx; centerY = cy;
    sizeX = sx;   sizeY = sy;
    activeColor = ac;
    inactiveColor = ic;
    lowerBound = lb;
    higherBound = hb;
  }  
  
  void display() {
    
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
  
  
  float topEdge() { return centerY-sizeY; }
  
  float bottomEdge() {return centerY+sizeY; }
  
  
  void setCenter(float cx, float cy) { centerX = cx; centerY = cy; }
  
  void setSize(float sx, float sy) { sizeX = sx; sizeY = sy; }
  
  void setRadius(float r) { radius = r; }
  
  void setActiveColor(color ac) { activeColor = ac; }
  
  void setInactiveColor(color ic) { inactiveColor = ic; }
  
  void setLowerBound(float lb) { lowerBound = lb; }
  
  void setHigherBound(float hb) { higherBound = hb; }
  
}
