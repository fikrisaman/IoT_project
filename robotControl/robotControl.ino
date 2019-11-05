#include <ArduinoRobot.h> // include the robot library

int commands[20];
int commandsDir[1];
int speedcar = 255;
int rotateDeg = 90;
void setup() {
  // put your setup code here, to run once:
  Robot.begin();
  Robot.beginTFT();
  Robot.beginSD();
}

void loop() {
  // put your main code here, to run repeatedly:
  iniCommands(); // remove commands from the array
  addCommands(); // add commands to the array
}

// empty the commands array
void iniCommands() {
  for (int i = 0; i < 20; i++) {
    commands[i] = -1;
  }
}

void addCommands() {
  Robot.stroke(0, 0, 0);
  // display text on the screen
  //Robot.text("1. Press buttons to\n add commands.\n\n 2. Middle to finish.", 5, 5);
  Robot.text("1. Choisir la \ndirection\n Forward, Back, Right\nLeft\n\n 2. Choisir la speed\n\n 3. Middle to finish", 5, 5);

  // read the buttons' state
  for (int i = 0; i < 1;) { //max 20 commands
    int key = Robot.keyboardRead();
    if (key == BUTTON_MIDDLE) { //finish input
      break;
    } else if (key == BUTTON_NONE) { //if no button is pressed
      continue;
    }
    commandsDir[i] = key; // save the button to the array
    PrintCommandI(i, 75); // print the command on the screen
    delay(100);
  }

  // read the desired speed/angle
  for (int j = 0; j < 20;) { //max 20 commands
    int key = Robot.keyboardRead();
    if (key == BUTTON_MIDDLE) { //finish input
      break;
    } else if (key  
    else if (key == BUTTON_NONE) { //if no button is pressed
      continue;
    }
    commands[j] = key; // save the button to the array
    PrintCommandJ(j, 100); // print the command on the screen
    delay(100);
  }
}

void goForward(){
  Robot.motorsWrite(speedcar, speedcar);
}

void goBackward(){
  Robot.motorsWrite(-speedcar, -speedcar);
}

void rotateLeft(){
  Robot.turn(rotateDeg);
}

void rotateRight(){
  Robot.turn(rotateDeg);
}

void stopRobot(){
  Robot.motorsWrite(0,0);
}

char keyToChar(int key) {
  switch (key) {
    case BUTTON_LEFT:
      return '<';
    case BUTTON_RIGHT:
      return '>';
    case BUTTON_UP:
      return '^';
    case BUTTON_DOWN:
      return 'v';
  }
}

int keyToSpeed(int key) {
  int forward, back, left, right;
  switch (key) {
    case BUTTON_LEFT:
      return forward+10;
    case BUTTON_RIGHT:
      return '>';
    case BUTTON_UP:
      return '^';
    case BUTTON_DOWN:
      return 'v';
  }
}

void PrintCommandI(int i, int originY) {
  Robot.text(keyToChar(commandsDir[i]), i % 14 * 8 + 5, i / 14 * 10 + originY);
}
