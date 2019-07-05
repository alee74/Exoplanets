
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
  color btnColor = color(0,0,255);
  color hoverColor = color(150);
  color textColor = color(255);
  float textSize = 15;
  float minTextSize = 10;
  String text = "Hi!";
  TextScale scaleMode = TextScale.TRUNC;
  float padding = 1.75;
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
  
  Button(float px, float py, float sx, float sy, color c, String t) {
    posX = px;  posY = py;
    sizeX = sx; sizeY = sy;
    btnColor = c;
    text = t;
    scaleText();
  }
  
  
  void display() {
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
  
  
  boolean mouseOver() {
    if (!hidden && mouseX > posX && mouseX < posX+sizeX
                && mouseY > posY && mouseY < posY+sizeY)
      return true;
    return false;
  }
  
  
  void scaleText() {
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
  
      
  void hide() { hidden = true; }
  
  void show() { hidden = false; }
  
  void setPosition(float px, float py) { posX = px; posY = py; }
  
  void setSize(float sx, float sy) { sizeX = sx; sizeY = sy; }
  
  void setRadius(float r) { radius = r; }
    
  void setColor(color c) { btnColor = c; }
  
  void setTextColor(color tc) { textColor = tc; }
  
  void setTextSize(float ts) { textSize = ts; scaleText(); }
  
  void setText(String t) { text = t; scaleText(); }
  
  void setScaleMode(TextScale sm) { scaleMode = sm; scaleText(); }
  
  void setPadding(float p) { padding = p; scaleText(); }
  
  void setMinTextSize(float mts) { minTextSize = mts; scaleText(); }
  
  void setHoverColor(color hc) { hoverColor = hc; }

}
