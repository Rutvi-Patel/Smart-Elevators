package buildings;

import elevators.ElevatorObserver;
import passengers.Passenger;
import elevators.Elevator;

import java.util.*;

public class Floor implements ElevatorObserver {
	private Building mBuilding;
	private List<Passenger> mPassengers = new ArrayList<>();
	private ArrayList<FloorObserver> mObservers = new ArrayList<>();
	private int mNumber;

	// TODO: declare a field(s) to help keep track of which direction buttons are currently pressed.
	// You can assume that every floor has both up and down buttons, even the ground and top floors.
	private boolean buttonUp = false;
	private boolean buttonDown = false;


	public Floor(int number, Building building) {
		mNumber = number;
		mBuilding = building;
	}


	/**
	 * Sets a flag that the given direction has been requested by a passenger on this floor. If the direction
	 * had NOT already been requested, then all observers of the floor are notified that directionRequested.
	 *
	 * @param direction
	 */
	public void requestDirection(Elevator.Direction direction) {
		//fixme more
		// TODO: implement this method as described in the comment.


		if (direction == Elevator.Direction.MOVING_UP) {
			buttonUp = true;
		} else {
			buttonDown = true;
		}


		ArrayList<FloorObserver> copy5 = new ArrayList<>();
		for (FloorObserver each : mObservers) {
			copy5.add(each);
		}

		for (FloorObserver each : copy5) {
			each.directionRequested(this, direction);
		}

	}

	/**
	 * Returns true if the given direction button has been pressed.
	 */
	public boolean directionIsPressed(Elevator.Direction direction) {
		// TODO: complete this method.
		if (direction == Elevator.Direction.MOVING_UP) {
			return buttonUp;
		} else if (direction == Elevator.Direction.MOVING_DOWN) {
			return buttonDown;
		} else {
			return false;
		}
	}

	/**
	 * Clears the given direction button so it is no longer pressed.
	 */
	public void clearDirection(Elevator.Direction direction) {
		// TODO: complete this method.
		if (direction == Elevator.Direction.MOVING_UP) {
			buttonUp = false;
		} else {
			buttonDown = false;
		}
	}

	/**
	 * Adds a given Passenger as a waiting passenger on this floor, and presses the passenger's direction button.
	 */
	public void addWaitingPassenger(Passenger p) {
		mPassengers.add(p);
		this.addObserver(p);
		p.setState(Passenger.PassengerState.WAITING_ON_FLOOR);

		// TODO: call requestDirection with the appropriate direction for this passenger's destination.
		if (p.getDestination() > this.getNumber()) {
			requestDirection(Elevator.Direction.MOVING_UP);
		} else {
			requestDirection(Elevator.Direction.MOVING_DOWN);
		}
	}

	/**
	 * Removes the given Passenger from the floor's waiting passengers.
	 */
	public void removeWaitingPassenger(Passenger p) {
		mPassengers.remove(p);
	}


	// Simple accessors.
	public int getNumber() {
		return mNumber;
	}

	public List<Passenger> getWaitingPassengers() {
		return mPassengers;
	}

	@Override
	public String toString() {
		return "Floor " + mNumber;
	}

	// Observer methods.
	public void removeObserver(FloorObserver observer) {
		mObservers.remove(observer);
	}

	public void addObserver(FloorObserver observer) {
		mObservers.add(observer);
	}

	// Observer methods.
	@Override
	public void elevatorDecelerating(Elevator elevator) {
		// TODO: if the elevator is arriving at THIS FLOOR, alert all the floor's observers that elevatorArriving.
		// TODO:    then clear the elevator's current direction from this floor's requested direction buttons.

		if (elevator.getCurrentFloor().getNumber() == this.getNumber()) {


			ArrayList<FloorObserver> copy4 = new ArrayList<>();
			for (FloorObserver each : mObservers) {
				each.elevatorArriving(this, elevator);
				copy4.add(each);

			}
			for (FloorObserver each : copy4) {

				each.elevatorArriving(this, elevator);
			}
			this.clearDirection(elevator.getCurrentDirection());

			if (!mPassengers.isEmpty() && this.getNumber() == 1) {
				elevator.setCurrentDirection(Elevator.Direction.MOVING_UP);
			} else if (!mPassengers.isEmpty() && this.getNumber() == mBuilding.getFloorCount()) {
				elevator.setCurrentDirection(Elevator.Direction.MOVING_DOWN);
			}
		}
	}

	
	@Override
	public void elevatorDoorsOpened(Elevator elevator) {
		// Not needed.
	}
	
	@Override
	public void elevatorWentIdle(Elevator elevator) {
		// Not needed.
	}
}
