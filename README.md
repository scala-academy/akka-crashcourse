[![Stories in Ready](https://badge.waffle.io/scala-academy/akka-crashcourse.png?label=ready&title=Ready)](https://waffle.io/scala-academy/akka-crashcourse?utm_source=badge) [![Build Status](https://travis-ci.org/scala-academy/akka-crashcourse.svg?branch=master)](https://travis-ci.org/scala-academy/akka-crashcourse) [![Codacy Badge](https://api.codacy.com/project/badge/Grade/b7aa1b504752470e9d29ce517f8bdce6)](https://www.codacy.com/app/devos-jordi/akka-crashcourse?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=scala-academy/akka-crashcourse&amp;utm_campaign=Badge_Grade) [![Codacy Badge](https://api.codacy.com/project/badge/Coverage/b7aa1b504752470e9d29ce517f8bdce6)](https://www.codacy.com/app/devos-jordi/akka-crashcourse?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=scala-academy/akka-crashcourse&amp;utm_campaign=Badge_Coverage)

# akka-crashcourse

Project for getting a feeling of Akka for a small group of engineers

Basic idea is to together develop a game server that hosts games of Battleship.
As a sarting point, the game logic is provided.

TODO before starting:
 * refine user stories, make automatable acceptance criteria

It is up to the engineers to:
 * (Reactive principles) rewrite some of the game logic to reactive, using asynchronuous message passing
 * (Akka-http) implement an http api based on akka-http
 * (CQRS / Sharding / Persistence) implement event sourcing for the gamestate (incl refactoring of gamestate to suit event sourcing)
 * (Actor design) implement a distributed version of placeBoats, every boat being an actor and finding their own way on the battlefield
   * (Timeouts, supervision) When one of the boat actors does not respond, manage this
 * (Routing / Scaling) use routing actor to let multiple random players play the game (these have bad performance)
 * (Akka FSM) Creating a new game should be in iterative process: first the players are chosen, then (optionally) the size of the grid and the types of boats are defined, and then the game can be started
 * (Akka FSM) The Game actor has states in which it is waiting for a player, this can be done using Akka FSM
 * (Gatling) We need a performance test to finetune the implemented features
 * (Streams) - replayen/spectaten van games, data analyse op games, misschien score-/ladder-/mostactive-dashboardgeneratie oid?
 * (Replicated Data) - Maintain a list of all players in the game and how much they've won/lost
