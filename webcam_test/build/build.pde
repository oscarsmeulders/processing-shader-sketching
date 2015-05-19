import processing.video.*;

Capture cam;

void setup() {
  size(640, 480);


  
  cam = new Capture(this, "name=FaceTime HD Camera (Display),size=640x360,fps=30");
    cam.start();
    
    if (cam.available() == true) {
    cam.read();
    image(cam, 0, 0);
  }
}

void draw() {
  if (cam.available() == true) {
    cam.read();
  }
  
  image(cam, 0, 0);
  // The following does the same, and is faster when just drawing the image
  // without any additional resizing, transformations, or tint.
  //set(0, 0, cam);
}

void stop() {
  cam.stop();
}



//Available cameras:
//name=FaceTime HD Camera (Built-in),size=1280x720,fps=30
//name=FaceTime HD Camera (Built-in),size=1280x720,fps=15
//name=FaceTime HD Camera (Built-in),size=1280x720,fps=1
//name=FaceTime HD Camera (Built-in),size=640x360,fps=30
//name=FaceTime HD Camera (Built-in),size=640x360,fps=15
//name=FaceTime HD Camera (Built-in),size=640x360,fps=1
//name=FaceTime HD Camera (Built-in),size=320x180,fps=30
//name=FaceTime HD Camera (Built-in),size=320x180,fps=15
//name=FaceTime HD Camera (Built-in),size=320x180,fps=1
//name=FaceTime HD Camera (Built-in),size=160x90,fps=30
//name=FaceTime HD Camera (Built-in),size=160x90,fps=15
//name=FaceTime HD Camera (Built-in),size=160x90,fps=1
//name=FaceTime HD Camera (Built-in),size=80x45,fps=30
//name=FaceTime HD Camera (Built-in),size=80x45,fps=15
//name=FaceTime HD Camera (Built-in),size=80x45,fps=1
//name=FaceTime HD Camera (Display),size=1280x720,fps=30
//name=FaceTime HD Camera (Display),size=1280x720,fps=15
//name=FaceTime HD Camera (Display),size=1280x720,fps=1
//name=FaceTime HD Camera (Display),size=640x360,fps=30
//name=FaceTime HD Camera (Display),size=640x360,fps=15
//name=FaceTime HD Camera (Display),size=640x360,fps=1
//name=FaceTime HD Camera (Display),size=320x180,fps=30
//name=FaceTime HD Camera (Display),size=320x180,fps=15
//name=FaceTime HD Camera (Display),size=320x180,fps=1
//name=FaceTime HD Camera (Display),size=160x90,fps=30
//name=FaceTime HD Camera (Display),size=160x90,fps=15
//name=FaceTime HD Camera (Display),size=160x90,fps=1
//name=FaceTime HD Camera (Display),size=80x45,fps=30
//name=FaceTime HD Camera (Display),size=80x45,fps=15
//name=FaceTime HD Camera (Display),size=80x45,fps=1
