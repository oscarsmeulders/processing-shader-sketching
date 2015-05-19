import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import processing.video.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class build extends PApplet {


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



Capture cam;

int subdivisionLevel = 8; // number of times the icosahedron will be subdivided
int dim = 300; // the grid dimensions of the heightmap
int blurFactor = 1; // the blur for the displacement map (to make it smoother)
float resizeFactor = 0.2f; // the resize factor for the displacement map (to make it smoother)
float displaceStrength = 3.5f; // the displace strength of the GLSL shader displacement effect

PShape sphere; // PShape to hold the geometry, textures, texture coordinates etc.
PShader displace; // GLSL shader

PImage[] colorMaps = new PImage[1]; // array to hold 3 colorMaps
PImage[] displacementMaps = new PImage[1]; // array to hold 3 displacementMap 
int currentColorMap, currentDisplacementMap = 0; // variables to keep track of the current maps (also used for setting them)

public void setup() {
  size(1280, 720, P3D); // use the P3D OpenGL renderer
  frameRate(90);
  //
  // camera activation + select the logitech cam, 30fps from list
  String[] cameras = Capture.list();
  if (cameras.length == 0) {
    println("There are no cameras available for capture.");
    exit();
  } else {
    println("Available cameras:");
    for (int i = 0; i < cameras.length; i++) {
      println(cameras[i]);
    }

    // The camera can be initialized directly using an 
    // element from the array returned by list():
    cam = new Capture(this, cameras[18]);
    cam.start();
  } 
  //
  //

  // load the images from the _Images folder (relative path from this sketch's folder)
  colorMaps[0] = loadImage("../../_images/texture_os_3.jpg");

  // create the displacement maps from the images
  displacementMaps[0] = imageToDisplacementMap(colorMaps[0]);

  displace = loadShader("displaceFrag.glsl", "displaceVert.glsl"); // load the PShader with a fragment and a vertex shader
  displace.set("displaceStrength", displaceStrength); // set the displaceStrength
  resetMaps(); // set the color and displacement maps
  sphere = createIcosahedron(subdivisionLevel); // create the subdivided icosahedron PShape (see custom creation method) and put it in the global sphere reference
}

public void draw() {
  pointLight(255, 255, 255, 2*(width/2), 2*(height/2), 500); // required for texLight shader
  if (cam.available() == true) {
    cam.read();
    colorMaps[0] = cam;
    displacementMaps[0] = imageToDisplacementMap(colorMaps[0]);
    resetMaps();
    //println("cam is available");
  }


  translate(width/2, height/2); // translate to center of the screen
  //rotateX(radians(60)); // fixed rotation of 60 degrees over the X axis
  //rotateX(frameCount*0.01); // fixed rotation of 60 degrees over the X axis
  rotateY(frameCount*-0.01f);
  //rotateZ(frameCount*0.01); // dynamic frameCount-based rotation over the Z axis

  background(255); // black background
  perspective(PI/3.0f, (float) width/height, 0.1f, 1000000); // perspective for close shapes
  scale(70); // scale by 100

  shader(displace); // use shader
  shape(sphere); // display the PShape

    // write the fps, the current colorMap and the current displacementMap in the top-left of the window
  frame.setTitle(" " + PApplet.parseInt(frameRate) + " | colorMap: " + currentColorMap + " | displacementMap: " + currentDisplacementMap);
}

// a separate resetMaps() method, so the images can be change dynamically
public void resetMaps() {
  displace.set("colorMap", colorMaps[currentColorMap]);
  displace.set("displacementMap", displacementMaps[currentDisplacementMap]);
}

// convenience method to create a smooth displacementMap
public PImage imageToDisplacementMap(PImage img) {
  PImage imgCopy = img.get(); // get a copy so the original remains intact
  imgCopy.resize(PApplet.parseInt(imgCopy.width*resizeFactor), PApplet.parseInt(imgCopy.height*resizeFactor)); // resize
  if (blurFactor >= 1) { 
    imgCopy.filter(BLUR, blurFactor);
  } // apply blur
  return imgCopy;
}

public void keyPressed() {
  if (key == 'c') { 
    currentColorMap = ++currentColorMap%colorMaps.length; 
    resetMaps();
  } // cycle through colorMaps (set variable and call resetMaps() method)
  if (key == 'd') { 
    currentDisplacementMap = ++currentDisplacementMap%displacementMaps.length; 
    resetMaps();
  } // cycle through displacementMaps (set variable and call resetMaps() method)
}


// ported to Processing 2.0b8 by Amnon Owed (10/05/2013)
// from code by Gabor Papp (13/03/2010): http://git.savannah.gnu.org/cgit/fluxus.git/tree/libfluxus/src/GraphicsUtils.cpp
// based on explanation by Paul Bourke (01/12/1993): http://paulbourke.net/geometry/platonic
// using vertex/face list by Craig Reynolds: http://paulbourke.net/geometry/platonic/icosahedron.vf

class Icosahedron {
  ArrayList <PVector> positions = new ArrayList <PVector> ();
  ArrayList <PVector> normals = new ArrayList <PVector> ();
  ArrayList <PVector> texCoords = new ArrayList <PVector> ();

  Icosahedron(int level) {
    float sqrt5 = sqrt(5);
    float phi = (1 + sqrt5) * 0.5f;
    float ratio = sqrt(10 + (2 * sqrt5)) / (4 * phi);
    float a = (1 / ratio) * 0.5f;
    float b = (1 / ratio) / (2 * phi);

    PVector[] vertices = {
      new PVector( 0,  b, -a), 
      new PVector( b,  a,  0), 
      new PVector(-b,  a,  0), 
      new PVector( 0,  b,  a), 
      new PVector( 0, -b,  a), 
      new PVector(-a,  0,  b), 
      new PVector( 0, -b, -a), 
      new PVector( a,  0, -b), 
      new PVector( a,  0,  b), 
      new PVector(-a,  0, -b), 
      new PVector( b, -a,  0), 
      new PVector(-b, -a,  0)
    };

    int[] indices = { 
      0,1,2,    3,2,1,
      3,4,5,    3,8,4,
      0,6,7,    0,9,6,
      4,10,11,  6,11,10,
      2,5,9,    11,9,5,
      1,7,8,    10,8,7,
      3,5,2,    3,1,8,
      0,2,9,    0,7,1,
      6,9,11,   6,10,7,
      4,11,5,   4,8,10
    };

    for (int i=0; i<indices.length; i += 3) {
      makeIcosphereFace(vertices[indices[i]],  vertices[indices[i+1]],  vertices[indices[i+2]],  level);
    }
  }

  public void makeIcosphereFace(PVector a, PVector b, PVector c, int level) {

    if (level <= 1) {
      
      // cartesian to spherical coordinates
      PVector ta = new PVector(atan2(a.z, a.x) / TWO_PI + 0.5f, acos(a.y) / PI);
      PVector tb = new PVector(atan2(b.z, b.x) / TWO_PI + 0.5f, acos(b.y) / PI);
      PVector tc = new PVector(atan2(c.z, c.x) / TWO_PI + 0.5f, acos(c.y) / PI);

      // texture wrapping coordinate limits
      float mint = 0.25f;
      float maxt = 1 - mint;

      // fix north and south pole textures
      if ((a.x == 0) && ((a.y == 1) || (a.y == -1))) {
        ta.x = (tb.x + tc.x) / 2;
        if (((tc.x < mint) && (tb.x > maxt)) || ((tb.x < mint) && (tc.x > maxt))) { ta.x += 0.5f; }
      } else if ((b.x == 0) && ((b.y == 1) || (b.y == -1))) {
        tb.x = (ta.x + tc.x) / 2;
        if (((tc.x < mint) && (ta.x > maxt)) || ((ta.x < mint) && (tc.x > maxt))) { tb.x += 0.5f; }
      } else if ((c.x == 0) && ((c.y == 1) || (c.y == -1))) {
        tc.x = (ta.x + tb.x) / 2;
        if (((ta.x < mint) && (tb.x > maxt)) || ((tb.x < mint) && (ta.x > maxt))) { tc.x += 0.5f; }
      }

      // fix texture wrapping
      if ((ta.x < mint) && (tc.x > maxt)) {
        if (tb.x < mint) { tc.x -= 1; } else { ta.x += 1; }
      } else if ((ta.x < mint) && (tb.x > maxt)) {
        if (tc.x < mint) { tb.x -= 1; } else { ta.x += 1; }
      } else if ((tc.x < mint) && (tb.x > maxt)) {
        if (ta.x < mint) { tb.x -= 1; } else { tc.x += 1; }
      } else if ((ta.x > maxt) && (tc.x < mint)) {
        if (tb.x < mint) { ta.x -= 1; } else { tc.x += 1; }
      } else if ((ta.x > maxt) && (tb.x < mint)) {
        if (tc.x < mint) { ta.x -= 1; } else { tb.x += 1; }
      } else if ((tc.x > maxt) && (tb.x < mint)) {
        if (ta.x < mint) { tc.x -= 1; } else { tb.x += 1; }
      }

      addVertex(a, a, ta);
      addVertex(c, c, tc);
      addVertex(b, b, tb);

    } else { // level > 1

      PVector ab = midpointOnSphere(a, b);
      PVector bc = midpointOnSphere(b, c);
      PVector ca = midpointOnSphere(c, a);

      level--;
      makeIcosphereFace(a, ab, ca, level);
      makeIcosphereFace(ab, b, bc, level);
      makeIcosphereFace(ca, bc, c, level);
      makeIcosphereFace(ab, bc, ca, level);
    }
  }

  public void addVertex(PVector p, PVector n, PVector t) {
    positions.add(p);
    normals.add(n);
    t.set(1.0f-t.x, 1.0f-t.y, t.z);
    texCoords.add(t);
  }

  public PVector midpointOnSphere(PVector a, PVector b) {
    PVector midpoint = PVector.add(a, b);
    midpoint.mult(0.5f);
    midpoint.normalize();
    return midpoint;
  }
}

public PShape createIcosahedron(int level) {
  // the icosahedron is created with positions, normals and texture coordinates in the above class
  Icosahedron ico = new Icosahedron(level);

  textureMode(NORMAL); // set textureMode to normalized (range 0 to 1);
  
  PShape mesh = createShape(); // create the initial PShape
  mesh.beginShape(TRIANGLES); // define the PShape type: TRIANGLES
  mesh.noStroke();
  mesh.texture(colorMaps[0]); // set the texture
  // put all the vertices, uv texture coordinates and normals into the PShape
  for (int i=0; i<ico.positions.size(); i++) {
    PVector p = ico.positions.get(i);
    PVector t = ico.texCoords.get(i);
    PVector n = ico.normals.get(i);
    mesh.normal(n.x, n.y, n.z);
    mesh.vertex(p.x, p.y, p.z, t.x, t.y);
  }
  mesh.endShape();

  return mesh; // our work is done here, return DA MESH! ;-)
}

  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "build" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
