package robocop;

import java.awt.Color;
import robocode.*;
import robocode.util.Utils;
import java.awt.geom.Point2D;


// API help : https://robocode.sourceforge.io/docs/robocode/robocode/Robot.html

/**
 * Robocop - a robot by (your name here)
 */
public class Robocop extends AdvancedRobot
{
   /**
	* run: Robocop's default behavior
    */
   private byte moveDirection = 1;

   private boolean moved = false; // if we need to move or turn
   private boolean inCorner = false; // if we are in a corner
   private String targ; // what robot to target
   private byte spins = 0; // spin counter
   private byte dir = 1; // direction to move
   private short prevE; // previous energy of robot we're targeting
 
   @Override
   public void run(){
      setColors(Color.BLACK, Color.BLACK, Color.BLACK); // set the colors
      setAdjustGunForRobotTurn(true); // when the robot turns, adjust gun in opposite dir
      setAdjustRadarForGunTurn(true); // when the gun turns, adjust radar in opposite dir
      while(true){ // for radar lock (aka "Narrow Lock")
         turnRadarLeftRadians(1); // continually turn the radar left
		 doRadar();
      }
   }
 
   @Override
   public void onHitByBullet(HitByBulletEvent e){ // if hit buy a bullet
      targ = e.getName(); // target the one who hit us!
   }
 
   @Override
   public void onScannedRobot(ScannedRobotEvent e){
      if(targ == null || spins > 6){ // if we don't have a target
         targ = e.getName(); // choose the first robot scanned
      }
	  doMove(e);
	  
      if(getDistanceRemaining() == 0 && getTurnRemaining() == 0){ // not moving or turning
         if(inCorner){
            if(moved){ // if last movement cycle we were moving,
               setTurnLeft(90); // turn this cycle
               moved = false; // and move next cycle
            }
            else{ // else if last cycle we were turning
               setAhead(160 * dir); // move this cycle
               moved = true; // and turn next cycle
            }
         }
         else{
            // if we aren't going N/S go north or south
            if((getHeading() % 90) != 0){
               setTurnLeft((getY() > (getBattleFieldHeight() / 2)) ? getHeading()
                     : getHeading() - 180);
            }
            // if we aren't at the top or bottom, go to whichever is closer
            else if(getY() > 30 && getY() < getBattleFieldHeight() - 30){
               setAhead(getHeading() > 90 ? getY() - 20 : getBattleFieldHeight() - getY()
                     - 20);
            }
            // if we aren't facing toward East/West, face toward it
            else if(getHeading() != 90 && getHeading() != 270){
               if(getX() < 350){
                  setTurnLeft(getY() > 300 ? 90 : -90);
               }
               else{
                  setTurnLeft(getY() > 300? -90 : 90);
               }
            }
            // if we aren't at the left or right, go to whichever is closer
            else if(getX() > 30 && getX() < getBattleFieldWidth() - 30){
               setAhead(getHeading() < 180 ? getX() - 20 : getBattleFieldWidth() - getX()
                     - 20);
            }
            // we are in the corner; turn and start moving
            else if(getHeading() == 270){
               setTurnLeft(getY() > 200 ? 90 : 180);
               inCorner = true;
            }
            // we are in the corner; turn and start moving
            else if(getHeading() == 90){
               setTurnLeft(getY() > 200 ? 180 : 90);
               inCorner = true;
            }
         }
      }
      if(e.getName().equals(targ)){ // if the robot scanned is our target
         spins = 0; // reset radar spin counter
 
         // if the enemy fires, with a 15% chance, 
         if((prevE < (prevE = (short)e.getEnergy())) && Math.random() > .85){
            dir *= -1; // change direction
         }
 
         setTurnGunRightRadians(Utils.normalRelativeAngle((getHeadingRadians() + e
               .getBearingRadians()) - getGunHeadingRadians())); // move gun toward them
 
         if(e.getDistance() < 200){ // the the enemy is further than 200px
            setFire(3); // fire full power
         }
         else{
            setFire(2.4); // else fire 2.4
         }
 
         double radarTurn = getHeadingRadians() + e.getBearingRadians()
               - getRadarHeadingRadians();
         setTurnRadarRightRadians(2 * Utils.normalRelativeAngle(radarTurn)); // lock radar
      }
      else if(targ != null){ // else
         spins++; // add one to spin count
      }
   }
   



   
   void doRadar() {
		// rotate the radar
		setTurnRadarRight(360);
	}

	public void doMove(ScannedRobotEvent e) {
		// Sempre se posiciona contra o nosso inimigo, virando um pouco para ele
		setTurnRight(normalizeBearing(e.getBearing() + 90 - (15 * moveDirection)));

		// mudar de direção se paramos (também se afasta da parede se estiver muito perto)
		if (getVelocity() == 0) {
			setMaxVelocity(8);
			moveDirection *= -1;
			setAhead(10000 * moveDirection);
		}
	}


	// computes the absolute bearing between two points
	double absoluteBearing(double x1, double y1, double x2, double y2) {
		double xo = x2-x1;
		double yo = y2-y1;
		double hyp = Point2D.distance(x1, y1, x2, y2);
		double arcSin = Math.toDegrees(Math.asin(xo / hyp));
		double bearing = 0;

		if (xo > 0 && yo > 0) { // both pos: lower-Left
			bearing = arcSin;
		} else if (xo < 0 && yo > 0) { // x neg, y pos: lower-right
			bearing = 360 + arcSin; // arcsin is negative here, actually 360 - ang
		} else if (xo > 0 && yo < 0) { // x pos, y neg: upper-left
			bearing = 180 - arcSin;
		} else if (xo < 0 && yo < 0) { // both neg: upper-right
			bearing = 180 - arcSin; // arcsin is negative here, actually 180 + ang
		}

		return bearing;
	}

	// normalizes a bearing to between +180 and -180
	double normalizeBearing(double angle) {
		while (angle >  180) {
			angle -= 360;
		}
		while (angle < -180) {
			angle += 360;
		}
		return angle;
	}
}
