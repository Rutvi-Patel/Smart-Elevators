package passengers;
import cecs277.Simulation;
import buildings.Floor;
import buildings.FloorObserver;
import elevators.Elevator;
import elevators.ElevatorObserver;
import events.PassengerNextDestinationEvent;
import org.w3c.dom.ls.LSOutput;

/**
 * A passenger that is either waiting on a floor or riding an elevator.
 */
public abstract class Passenger implements FloorObserver, ElevatorObserver {
	// An enum for determining whether a Passenger is on a floor, an elevator, or busy (visiting a room in the building).
	public enum PassengerState {
		WAITING_ON_FLOOR,
		ON_ELEVATOR,
		BUSY
	}
	
	// A cute trick for assigning unique IDs to each object that is created. (See the constructor.)
	private static int mNextId;
	protected static int nextPassengerId() {
		return ++mNextId;
	}
	
	private int mIdentifier;
	private PassengerState mCurrentState;
	
	public Passenger() {
		mIdentifier = nextPassengerId();
		mCurrentState = PassengerState.WAITING_ON_FLOOR;
	}
	
	public void setState(PassengerState state) {
		mCurrentState = state;
	}
	
	/**
	 * Gets the passenger's unique identifier.
	 */
	public int getId() {
		return mIdentifier;
	}
	
	
	/**
	 * Handles an elevator arriving at the passenger's current floor.
	 */
	@Override
	public void elevatorArriving(Floor floor, Elevator elevator) {


		if (floor.getWaitingPassengers().contains(this) && mCurrentState == PassengerState.WAITING_ON_FLOOR) {

			if (elevator.getCurrentDirection() == Elevator.Direction.NOT_MOVING ) {
				elevator.addObserver(this);
			} else if (elevator.getCurrentDirection() == Elevator.Direction.MOVING_UP) {
				if (elevator.getCurrentFloor().getNumber() < this.getDestination()) {
					elevator.addObserver(this);
				}
			} else if (elevator.getCurrentDirection() == Elevator.Direction.MOVING_DOWN) {
				if (elevator.getCurrentFloor().getNumber() > this.getDestination()) {
					elevator.addObserver(this);
				}
			}
		}



//			Elevator.Direction d = elevatorDirection;
//			if (floor.getNumber()< this.getDestination()) {
//				d = Elevator.Direction.MOVING_UP;
//			} else if (floor.getNumber() > this.getDestination()) {
//				d = Elevator.Direction.MOVING_DOWN;
//			}
//
//				if (elevator.getCurrentDirection() == Elevator.Direction.NOT_MOVING || elevatorDirection == d) {
//					elevator.addObserver(this);
//					System.out.println("I was in Elevatoer arrving");
//				}
		// This else should not happen if your code is correct. Do not remove this branch; it reveals errors in your code.
		else {
			throw new RuntimeException("Passenger " + toString() + " is observing Floor " + floor.getNumber() + " but they are " +
			 "not waiting on that floor.");
		}
	}
	
	/**
	 * Handles an observed elevator opening its doors. Depart the elevator if we are on it; otherwise, enter the elevator.
	 */
	@Override
	public void elevatorDoorsOpened(Elevator elevator) {
		// The elevator is arriving at our destination. Remove ourselves from the elevator, and stop observing it.
		// Does NOT handle any "next" destination...
		if (mCurrentState == PassengerState.ON_ELEVATOR && elevator.getCurrentFloor().getNumber() == getDestination()) {
			// TODO: remove this passenger from the elevator, and as an observer of the elevator. Call the
			// leavingElevator method to allow a derived class to do something when the passenger departs.
			// Set the current state to BUSY.
			elevator.removeObserver(this);
			this.leavingElevator(elevator);
			elevator.getCurrentFloor().removeObserver(this);
			this.setState(PassengerState.BUSY);
		}
		// The elevator has arrived on the floor we are waiting on. If the elevator has room for us, remove ourselves
		// from the floor, and enter the elevator.
		else if (mCurrentState == PassengerState.WAITING_ON_FLOOR && elevator.getCurrentFloor().getWaitingPassengers().contains(this)) {

			// TODO: determine if the passenger will board the elevator using willBoardElevator.
			// If so, remove the passenger from the current floor, and as an observer of the current floor;
			// then add the passenger as an observer of and passenger on the elevator. Then set the mCurrentState
			// to ON_ELEVATOR.
//			System.out.println("I was here");
			boolean check = true;
			if (elevator.getCurrentDirection() == Elevator.Direction.MOVING_UP ){
				if (elevator.getCurrentFloor().getNumber()>this.getDestination()){
					check = false;
				}
			}else if (elevator.getCurrentDirection() == Elevator.Direction.MOVING_DOWN) {
				if (elevator.getCurrentFloor().getNumber() < this.getDestination()) {
					check = false;
				}
			}


			if (this.willBoardElevator(elevator)&& check) {

				//elevator.addObserver(this);
				elevator.addPassenger(this);

				elevator.getCurrentFloor().removeObserver(this);
				elevator.getCurrentFloor().removeWaitingPassenger(this);
				setState(PassengerState.ON_ELEVATOR);
			}
			else{
				elevator.removeObserver(this);
			}

		}
	}
	
	/**
	 * Returns the passenger's current destination (what floor they are travelling to).
	 */
	public abstract int getDestination();
	
	/**
	 * Called to determine whether the passenger will board the given elevator that is moving in the direction the
	 * passenger wants to travel.
	 */
	protected abstract boolean willBoardElevator(Elevator elevator);
	
	/**
	 * Called when the passenger is departing the given elevator.
	 */
	protected abstract void leavingElevator(Elevator elevator);
	
	// This will be overridden by derived types.
	@Override
	public String toString() {
		return Integer.toString(getDestination());
	}
	
	@Override
	public void directionRequested(Floor sender, Elevator.Direction direction) {
		// Don't care.
	}
	
	@Override
	public void elevatorWentIdle(Elevator elevator) {
		// Don't care about this.
	}
	
	// The next two methods allow Passengers to be used in data structures, using their id for equality. Don't change 'em.
	@Override
	public int hashCode() {
		return Integer.hashCode(mIdentifier);
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Passenger passenger = (Passenger)o;
		return mIdentifier == passenger.mIdentifier;
	}
	
}