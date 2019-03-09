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

   private boolean moved = false; // se precisarmos nos mover ou virar
   private boolean inCorner = false; // se estiver nas paredes
   private String targ; // que robô mirar
   private byte spins = 0; // contador de giros do robô
   private byte dir = 1; // Direção para mover
   private short prevE; //energia anterior do robô que estamos atirando
 
   @Override
   public void run(){
      setColors(Color.BLACK, Color.BLACK, Color.BLACK); // Robocop black
      setAdjustGunForRobotTurn(true); // quando o robô se virar, ajuste a arma na direção oposta
      setAdjustRadarForGunTurn(true); // quando a arma gira, ajuste o radar no dir oposto
      while(true){ // para bloqueio de radar
         turnRadarLeftRadians(1); // continuamente gire o radar para a esquerda
		 setTurnRadarRight(360); // Roda o radar
		 //execute();
      }
   }
 
   @Override
   public void onHitByBullet(HitByBulletEvent e){ 
      /**
	   * Se for atingido por uma bala, pegamos o atirador e setamos como alvo
	   */
      targ = e.getName();
   }
 
   @Override
   public void onScannedRobot(ScannedRobotEvent e){
      // se não tivermos um alvo, vamos escolher o primeiro robô escaneado
      if(targ == null || spins > 6){ 
         targ = e.getName();
      }
	  
	  circularMove(e);
	  
      // Estratégia de tiro circular
      if(getDistanceRemaining() == 0 && getTurnRemaining() == 0){ // não se movendo ou girando
	  	 
	     // Se tivermos nas paredes, verificamos se nos movemos antes
         if(inCorner){
		    // Se nos movemos antes, então movemos de forma circular pra esquerda num angulo de 90 graus 
            if(moved){
               setTurnLeft(90); // turn this cycle
               moved = false; // and move next cycle
            } else { // else if last cycle we were turning
               setAhead(160 * dir); // move this cycle
               moved = true; // and turn next cycle
            }
         }
         else{
            // se não estamos indo N / S ir para o norte ou para o sul
            if((getHeading() % 90) != 0){
               setTurnLeft((getY() > (getBattleFieldHeight() / 2)) ? getHeading()
                     : getHeading() - 180);
            }
            // se não estivermos no topo ou no fundo, vá para o que estiver mais perto
            else if(getY() > 30 && getY() < getBattleFieldHeight() - 30){
               setAhead(getHeading() > 90 ? getY() - 20 : getBattleFieldHeight() - getY()
                     - 20);
            }
            // se não estivermos voltados para leste / oeste, viramos para ele
            else if(getHeading() != 90 && getHeading() != 270){
               if(getX() < 350){
                  setTurnLeft(getY() > 300 ? 90 : -90);
               }
               else{
                  setTurnLeft(getY() > 300? -90 : 90);
               }
            }
            // se não estivermos à esquerda ou à direita, vá para o que estiver mais perto
            else if(getX() > 30 && getX() < getBattleFieldWidth() - 30){
               setAhead(getHeading() < 180 ? getX() - 20 : getBattleFieldWidth() - getX()
                     - 20);
            }
            // estamos no canto; vire e comece a se mover
            else if(getHeading() == 270){
               setTurnLeft(getY() > 200 ? 90 : 180);
               inCorner = true;
            }
            // estamos no canto; vire e comece a se mover
            else if(getHeading() == 90){
               setTurnLeft(getY() > 200 ? 180 : 90);
               inCorner = true;
            }
         }
      }
      if(e.getName().equals(targ)){ // if the robot scanned is our target
         spins = 0; // reset radar spin counter
 
         // se o inimigo disparar, com 15% de chance muda a direção 
         if((prevE < (prevE = (short)e.getEnergy())) && Math.random() > .85){
            dir *= -1;
         }
 		 
         // Mova a arma na direção deles
         setTurnGunRightRadians(Utils.normalRelativeAngle((getHeadingRadians() + e
               .getBearingRadians()) - getGunHeadingRadians()));
 		 
		 // Se o inimigo está além de 200px, atire com força máxima, se não, reduza
		 // a força do tiro para poupar energina em caso de erro
         if(e.getDistance() < 200){
            setFire(3);
         } else {
            setFire(2.4);
         }
 		 
         // Calcula o angulo para o radar retornar e trava o radar
         double radarTurn = getHeadingRadians() + e.getBearingRadians() - getRadarHeadingRadians();
         setTurnRadarRightRadians(2 * Utils.normalRelativeAngle(radarTurn));
		 
        // Se não tiver alvo, incrementa a variável de rotação
      } else if(targ != null){
         spins++;
      }
   }
    
    // Implementação da estratégia de movimento circular com fuga de paredes
	public void circularMove(ScannedRobotEvent e) {
		// Sempre se posiciona contra o nosso inimigo, virando um pouco para ele
		setTurnRight(normalizeBearing(e.getBearing() + 90 - (15 * moveDirection)));

		// mudar de direção se paramos (também se afasta da parede se estiver muito perto)
		if (getVelocity() == 0) {
			setMaxVelocity(8); // muda a velocidade para 8
			moveDirection *= -1;
			setAhead(10000 * moveDirection);
		}
	}


	// Calcula o rolamento absoluto entre dois pontos
	// Na navegação, o rolamento absoluto é o ângulo no sentido horário entre o norte e um objeto 
    // observado a partir do robô
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

	// normaliza um rolamento entre +180 e -180
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
