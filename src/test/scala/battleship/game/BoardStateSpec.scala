package battleship.game

import org.scalatest.{Matchers, WordSpec}

/**
  * Created by jordidevos on 28/07/2017.
  */
class BoardStateSpec extends WordSpec with Matchers {
  "placeBoat" should {
    "throw an exception if a boat is placed on an already used coordinate" in {
      val state = BoardState.empty.placeBoat(Boat(3), BoatLocation(0, 0, true))

      assertThrows[IllegalArgumentException] {
        state.placeBoat(Boat(2), BoatLocation(2, 0, true))
      }
    }
    "return an updated instance of BoatState with the newly placed boat included" in {
      val state = BoardState.empty.placeBoat(Boat(3), BoatLocation(0, 0, true))

      state.boats should be(Set((Boat(3), BoatLocation(0, 0, true))))
    }
  }

  "shoot" should {
    "throw an exception if a shot is made to a coordinate for the second time" in {
      val state = BoardState.empty.processShot(0, 0)

      assertThrows[IllegalArgumentException] {
        state.processShot(0, 0)
      }
    }
    "return an updated instance of BoardState with the shot included in the history" in {
      val state = BoardState.empty

      val result = state.processShot(1, 1).processShot(3, 4)

      result should be(BoardState(Set.empty, Seq(((3, 4), Miss), ((1, 1), Miss))))
    }
  }

  "gameOver" should {
    "return true when the amount of Sunk Shotresults in the history is equals to the amount of boats on the board" in {
      val state = BoardState.empty

      state.allShipsSunk should be(true)
    }
    "return false when the amount of Sunk Shotresults in the history is smaller than the amount of boats on the board" in {
      val state = BoardState.empty.placeBoat(Boat(3), BoatLocation(0, 0, true))

      state.allShipsSunk should be(false)
    }
  }

  "shotsOnBoatSoFar" should {
    "return the amount of shots fired on a particular boat" in {
      val boat = Boat(3)
      val placement = BoatLocation(0, 0, true)
      val state = BoardState.empty
        .placeBoat(boat, placement)
        .processShot(0,0)
        .processShot(1,1)
        .processShot(1,0)
        .processShot(0,2)

      state.shotsOnBoatSoFar(boat, placement) should be(2)
    }
  }

  "calculateShotResult" should {
    "return Miss when a shot missed" in {
      val state = BoardState.empty.placeBoat(Boat(3), BoatLocation(0, 0, true))

      state.calculateShotResult(1,1) should be(Miss)
    }
    "return Hit when a shot hits but does not sink for 1st shot" in {
      val state = BoardState.empty.placeBoat(Boat(3), BoatLocation(0, 0, true))

      state.calculateShotResult(0,0) should be(Hit)
    }
    "return Hit when a shot hits but does not sink for 2nd shot on size 3 boat" in {
      val state = BoardState.empty
        .placeBoat(Boat(3), BoatLocation(0, 0, true))
        .processShot(2,0)

      state.calculateShotResult(0,0) should be(Hit)
    }
    "return Sink when a shot hits for the x'th time on a x-size boat" in {
      val state = BoardState.empty
        .placeBoat(Boat(2), BoatLocation(0, 0, true))
        .processShot(1,0)

      state.calculateShotResult(0,0) should be(Sink(Boat(2)))
    }
  }
}
