package battleship.game

import org.scalatest.{Matchers, WordSpec}

/**
  * Created by jordidevos on 28/07/2017.
  */
class BoatSpec extends WordSpec with Matchers {
  "Boat coordinates" should {
    "be a set of the same size as the size of the boat for horizontally placed boats" in {
      val boatSize = 5
      val boat = Boat(boatSize)

      boat.coordinates(BoatLocation(0, 0, true)).size should be(boatSize)
    }
    "be a set of the same size as the size of the boat for vertically placed boats" in {
      val boatSize = 5
      val boat = Boat(boatSize)

      boat.coordinates(BoatLocation(0, 0, false)).size should be(boatSize)
    }
    "be a set containing all coordinates for horizontally placed boats" in {
      val boatSize = 3
      val boat = Boat(boatSize)

      boat.coordinates(BoatLocation(0, 0, true)).toSet should be(Set((0, 0), (1, 0), (2, 0)))
    }
    "be a set containing all coordinates for vertically placed boats" in {
      val boatSize = 3
      val boat = Boat(boatSize)

      boat.coordinates(BoatLocation(0, 0, false)).toSet should be(Set((0, 0), (0, 1), (0, 2)))
    }
  }
}