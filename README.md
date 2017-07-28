# akka-crashcourse
Project for getting a feeling of Akka for a small group of engineers

Basic idea is to together develop a game server that hosts games of Battleship.
As a sarting point, the game logic is provided.

It is up to the engineers to:
 * (Reactive principles) rewrite some of the game logic to reactive, using asynchronuous message passing
 * (Akka-http) implement an http api based on akka-http
 * (CQRS / Sharding / Persistence) implement event sourcing for the gamestate (incl refactoring of gamestate to suit event sourcing)
 * (Actor design) implement a distributed version of placeBoats, every boat being an actor and finding their own way on the battlefield
   * (Timeouts, supervision) When one of the boat actors does not respond, manage this
 * (Routing / Scaling) use routing actor to let multiple random players play the game (these have bad performance)
 * (Akka FSM) Creating a new game should be in iterative process: first the players are chosen, then (optionally) the size of the grid and the types of boats are defined, and then the game can be started 
 * (Gatling) We need a performance test to finetune the implemented features