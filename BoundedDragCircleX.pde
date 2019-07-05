
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
  color activeColor = color(255);
  color inactiveColor = color(230);
  
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
  
  BoundedDragCircleX(float cx, float cy, float r, float lb, float ub, color ac, color ic) {
    centerX = cx; centerY = cy;
    radius = r;
    activeColor = ac;
    inactiveColor = ic;
    lowerBound = lb;
    upperBound = ub;
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
        centerX = lowerBound + 0.001*(upperBound-lowerBound);
    }
      
    noStroke();
    ellipseMode(RADIUS);
    if (dragging || hovering)
      fill(activeColor);
    else
      fill(inactiveColor);
      
    ellipse(centerX,centerY, radius,radius);
    
  }
  
  
  void setCenter(float cx, float cy) { centerX = cx; centerY = cy; }
  
  void setRadius(float r) { radius = r; }
  
  void setActiveColor(color ac) { activeColor = ac; }
  
  void setInactiveColor(color ic) { inactiveColor = ic; }
  
  void setLowerBound(float lb) { lowerBound = lb; }
  
  void setUpperBound(float ub) { upperBound = ub; }
  
}
