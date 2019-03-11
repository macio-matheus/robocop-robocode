## Robocop - Robocode

Efficient implementation of Robocode, with a combination of circular motion strategy and circular shooting strategy. The results look promising, in several rounds of 30 rounds, Robocop ranked first among all others.

![](https://raw.githubusercontent.com/macio-matheus/robocop-robocode/master/docs/robocop-robocode.png)

----
### Circular movement

The idea of circular movement is to frame the enemy like a shark. If we lean against a wall or other obstacle, Robocop does the opposite way by calculating the angle in radians. Below, 3 frames illustrating the movement.

```java
public void circularMove(ScannedRobotEvent e) {
		// Sempre se posiciona contra o nosso inimigo, virando um pouco  		// para ele
		setTurnRight(normalizeBearing(e.getBearing() + 90 - (15 * moveDirection)));

		// mudar de direção se paramos (também se afasta da parede se 			// estiver muito perto)
		if (getVelocity() == 0) {
			setMaxVelocity(8); // muda a velocidade para 8
			moveDirection *= -1;
			setAhead(10000 * moveDirection);
		}
	}
```

![frame 1](https://raw.githubusercontent.com/macio-matheus/robocop-robocode/master/docs/frame1.png)

![frame 2](https://raw.githubusercontent.com/macio-matheus/robocop-robocode/master/docs/frame2.png)

![frame 3](https://raw.githubusercontent.com/macio-matheus/robocop-robocode/master/docs/frame3.png)

### Circular shooting

Circular targeting is used to hit bots that often move in circles or large arcs. The first step is to measure the turn rate of your target bot by subtracting its current heading with its previous one. If you do not get a fresh scan of your target, you need to divide this value by the time between current scan and your last scan to obtain an average turn rate:

![frame 3](http://robowiki.net/w/images/math/1/5/d/15dc1ceff745ffd56566f78cc361065d.png)

Now we need to know the current position of the target bot. To keep things easy, we only calculate the position of your target relative to your position. Keep in mind that in Robocode, "north" is zero degrees (radians) and clockwise is a positive angle. This is exactly the inverse of unit-circle math, where the x-axis is zero degrees and counter-clockwise is a positive angle.

![](http://robowiki.net/w/images/math/0/a/e/0aed4b19d71b1a33e1cd0e82367ff163.png)

To see the complete calculation step by step, see this link: http://robowiki.net/wiki/Circular_Targeting/Walkthrough

Robocop implementation:

```java
	 // Estratégia de tiro circular
      if(getDistanceRemaining() == 0 && getTurnRemaining() == 0){ // não 		 //se movendo ou girando

	     // Se tivermos nas paredes, verificamos se nos movemos antes
         if(inCorner){
		    // Se nos movemos antes, então movemos de forma circular pra 			// esquerda num angulo de 90 graus 
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
            // se não estivermos no topo ou no fundo, vá para o que estiver 			// mais perto
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
            // se não estivermos à esquerda ou à direita, vá para o que 					 //estiver mais perto
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
 		 
		 // Se o inimigo está além de 200px, atire com força máxima, se 			// não, reduza
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
```

### Results (30 rounds)

The various tests run in 30 rounds with all the thefts implemented by default show Robocop's great advantage in battles. This has all been achieved by combining two classic and basic strategies available in the manuals.

![](https://raw.githubusercontent.com/macio-matheus/robocop-robocode/master/docs/result.png)
