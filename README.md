- [Trigger finger](#trigger-finger)
- [Game Objects](#game-object-framework)
- [Features](#features)
  * ["Physics"](#physics)
    + [PhysicsBody](#physicsbody)
    + [Collision](#collision)
  * [Lava](#lava)
  * [Sprite animation](#sprite-animation)
    + [Sprite](#sprite)
    + [Animation](#animation)
    + [Animator](#animator)
<hr>
# Trigger finger
<b>Trigger finger</b> is a game that is meant to be played on the go with one hand. The player controls a gun, and by tapping the screen the gun fires a bullet which applies force to the gun in the opposite direction (along with some rotation). In order to progress the player must time their shots as to not shoot themselves upwards. Also there is lava.

## Game Object "framework"
I created kind of an overkill system for creating game objects. The solution is inspired by the game-objects and component system in the Unity Engine. Everything that's in the game is a <b>game object</b>, and a <b>game object</b> consists of some number of <i>components</i>.
<details open>
  <summary>Simplified class diagram</summary>

![game objects](https://user-images.githubusercontent.com/45757491/116861602-9d5e1480-ac03-11eb-88c3-1f4f50d82f89.png)
</details>

To create objects I implemented my first <i>builder-pattern</i>. It's fairly simple but very useful: 
<details open>
  <summary>Example code</summary>
  
```
val player = GameObject.Builder("Player")
  .withComponent(Sprite(bitmap, scale))
  .withComponent(Animator())
  .withComponent(PhysicsBody())
  .withComponent(CollisionShape())
  .WithComponent(PlayerBehaviour(maxAmmo, shootAnimation))
  .build()
```
</details>

I transitioned into this game-object and component system as my previous solution was inconventient and difficult to work with (ei. having GameObject be an interface that implemented update()- and draw()-functions) when dealing with different collision behaviours. Colliding with a game object is much easier than colliding with some object. 

This solution is also very <b>reusable</b>, which means I can use it for future game projects.

## Features
### <i>"Physics"</i>
#### PhysicsBody
<i>PhysicsBody</i> is a component that handles:
- Velocity (directional speed)
- Angle velocity (rotational speed)
- Gravity (if applies to object, - bullets for instance do not use gravity.)
- Damping (if applies to body, - bullets for instance do not apply damping.)

Physics bodies apply gravity to velocity, and applies velocity to position - and angle velocity to rotation - each frame by time delta.
PhysicsBody also implements addForce() and addAngleForce() in order to knock the game object around the scene. AddForce is used to launch the gun the player controls.

#### Collision
As I'm dealing with simple overlap-collision I decided to implement two types of collision shapes, (Collision)Rectangle and (Collision)Circle. These inherit from a sealed class CollisionShape that implements:
```
collidesWith(circle: CollisionCircle)
collidesWith(rectangle: CollisionRectangle)
collidesWith(shape: CollisionShape) = if (shape is CollisionCircle) collidesWith(shape as CollisionCircle) else collidesWith(shape as CollisionRectangle)
```
### Lava
As I mentioned earlier, yes there is lava in the game. The lava is a potential failure state - if the gun falls into it, the player loses. It moves upwards and the player needs to keep going uwpard in order to not lose. 

I'm not great with visuals, so I decided to render the lava as a filled path using some <i>Paint</i> and generate a moving path using <i>sin</i> with an increasing offset. Probably not the most efficient way to handle it, but it works. Also here's a demo of the game so far: 

https://user-images.githubusercontent.com/45757491/116869691-74dd1700-ac11-11eb-820f-8b6873c7fe15.mp4

### Sprite animation
#### Sprite
<i>Sprite</i> is another component that implements the <i>draw(...)</i> function of Component. It draws a bitmap and rotates it according to the belonging game objects rotation. 
#### Animation
There are many types of animations that can be implemented, with and without interpolation. In this game I use simple frame by frame animation of a sprite sheet. When instantiating an animation the user must define <i>how many frames</i> that are in the animation, <i>where the first frame is</i> (0, y), how many <i>rows</i> and <i>columns</i> in case the animation is too large to have on the same row. 

This animation for instance has 8 frames, 8 columns, 1 row and the first frame is at (0, 0).
![gun_player_shoot](https://user-images.githubusercontent.com/45757491/116876497-dbb3fd80-ac1c-11eb-80dd-dafdd7646fe6.png)

#### Animator
<i>Animator</i> is a component that uses an <i>animation</i> to update the bitmap of the Sprite-component that exists on the same game object.
