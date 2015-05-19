
/*

 GLSL Sphere Displacement by Amnon Owed (May 2013)
 https://github.com/AmnonOwed
 http://vimeo.com/amnon
 
 Creating a sphere by subdividing an icosahedron. Storing it in a PShape.
 Displacing it outwards with GLSL (fragment and vertex) shaders based on input textures.
 The input textures for both the color and the displacement can be changed in realtime.
 
 c = cycle through the color maps
 d = cycle through the displacement maps
 
 Built with Processing 2.0b8 / 2.0b9 / 2.0 Final
 
 Photographs by Folkert Gorter (@folkertgorter / http://superfamous.com/) made available under a CC Attribution 3.0 license.
 
 */



import ddf.minim.*;
import ddf.minim.analysis.*;

Minim minim;
AudioPlayer ap;
BeatDetect bd;
BeatListener bl;

float[] fNums = new float[13];

int minScale = 0;
int maxScale = 900;
int xStart  = 10;
int xOffset = 90;

int rotationYExtra;
String scaleExtra = "outside";

int subdivisionLevel = 8; // number of times the icosahedron will be subdivided
int dim = 300; // the grid dimensions of the heightmap
int blurFactor = 1; // the blur for the displacement map (to make it smoother)
float resizeFactor = 0.2; // the resize factor for the displacement map (to make it smoother)
float displaceStrength = 3.5; // the displace strength of the GLSL shader displacement effect

PShape sphere; // PShape to hold the geometry, textures, texture coordinates etc.
PShader displace; // GLSL shader

PImage[] colorMaps = new PImage[3]; // array to hold 3 colorMaps
PImage[] displacementMaps = new PImage[3]; // array to hold 3 displacementMap 
int currentColorMap, currentDisplacementMap = 0; // variables to keep track of the current maps (also used for setting them)

void setup() {
  size(1280, 720, P3D); // use the P3D OpenGL renderer
  frameRate(15);
  //
  minim = new Minim(this);
  ap = minim.loadFile("../../_muzak/ISeeYouBaby.mp3", 2048);
  ap.play();

  bd = new BeatDetect(ap.bufferSize(), ap.sampleRate());
  bd.setSensitivity(0);  
  bl = new BeatListener(bd, ap);

  // load the images from the _Images folder (relative path from this sketch's folder)
  colorMaps[0] = loadImage("../../_images/texture_os_1.jpg");
  colorMaps[1] = loadImage("../../_images/texture_os_2.jpg");
  colorMaps[2] = loadImage("../../_images/texture_os_3.jpg");

  // create the displacement maps from the images
  displacementMaps[0] = imageToDisplacementMap(colorMaps[0]);
  displacementMaps[1] = imageToDisplacementMap(colorMaps[1]);
  displacementMaps[2] = imageToDisplacementMap(colorMaps[2]);

  displace = loadShader("displaceFrag.glsl", "displaceVert.glsl"); // load the PShader with a fragment and a vertex shader
  displace.set("displaceStrength", displaceStrength); // set the displaceStrength
  resetMaps(); // set the color and displacement maps
  sphere = createIcosahedron(subdivisionLevel); // create the subdivided icosahedron PShape (see custom creation method) and put it in the global sphere reference
}

void draw() {
  if ( bd.isOnset( 2 ) ) {
    rotationYExtra = 10;
  } else {
    rotationYExtra = 0;
  }
  if ( bd.isOnset( 2 ) ) {
    scaleExtra = "inside";
  } else {
    scaleExtra = "outside";
  }
  if ( bd.isOnset( 8 ) ) {
    currentColorMap = ++currentColorMap%colorMaps.length;
    currentDisplacementMap = ++currentDisplacementMap%displacementMaps.length; 
    resetMaps();
  }
  
  pointLight(255, 255, 255, 2*(width/2), 2*(height/2), 500); // required for texLight shader
  translate(width/2, height/2); // translate to center of the screen
  //rotateX(radians(60)); // fixed rotation of 60 degrees over the X axis
    //rotateX(frameCount*0.01); // fixed rotation of 60 degrees over the X axis
    rotateY(  (frameCount*-0.01) + radians(rotationYExtra) );
    //rotateZ(frameCount*0.01); // dynamic frameCount-based rotation over the Z axis
  
    background(255); // black background
    perspective(PI/3.0, (float) width/height, 0.1, 1000000); // perspective for close shapes
    
  pushMatrix();
    
    
    if (scaleExtra == "inside") {
      scale(700); // scale by 100
    } else {
      scale(50); // scale by 100
    }
  
  
    shader(displace); // use shader
    shape(sphere); // display the PShape
  popMatrix();

  scale(60); // scale by 100
  shape(sphere); // display the PShape

    // write the fps, the current colorMap and the current displacementMap in the top-left of the window
  frame.setTitle(" " + int(frameRate) + " | colorMap: " + currentColorMap + " | displacementMap: " + currentDisplacementMap);
}



void stop() {
  ap.close();
  minim.stop();
  super.stop();
}

class BeatListener implements AudioListener {
  private BeatDetect bd;
  private AudioPlayer source;

  BeatListener(BeatDetect bd, AudioPlayer source) {
    this.source = source;
    this.source.addListener(this);
    this.bd = bd;
  }

  void samples(float[] samps) {
    bd.detect(source.mix);
  }

  void samples(float[] sampsL, float[] sampsR) {
    bd.detect(source.mix);
  }
}


// a separate resetMaps() method, so the images can be change dynamically
void resetMaps() {
  displace.set("colorMap", colorMaps[currentColorMap]);
  displace.set("displacementMap", displacementMaps[currentDisplacementMap]);
}

// convenience method to create a smooth displacementMap
PImage imageToDisplacementMap(PImage img) {
  PImage imgCopy = img.get(); // get a copy so the original remains intact
  imgCopy.resize(int(imgCopy.width*resizeFactor), int(imgCopy.height*resizeFactor)); // resize
  if (blurFactor >= 1) { 
    imgCopy.filter(BLUR, blurFactor);
  } // apply blur
  return imgCopy;
}

void keyPressed() {
  if (key == 'c') { 
    currentColorMap = ++currentColorMap%colorMaps.length; 
    resetMaps();
  } // cycle through colorMaps (set variable and call resetMaps() method)
  if (key == 'd') { 
    currentDisplacementMap = ++currentDisplacementMap%displacementMaps.length; 
    resetMaps();
  } // cycle through displacementMaps (set variable and call resetMaps() method)
}

