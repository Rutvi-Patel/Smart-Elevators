package elevators;

import cecs277.Simulation;
import buildings.Building;
import buildings.Floor;
import buildings.FloorObserver;
import events.ElevatorStateEvent;
import passengers.Passenger;

import java.util.*;
import java.util.stream.Collectors;

public class Elevator implements FloorObserver {
	
	public enum ElevatorState {
		IDLE_STATE,
		DOORS_OPENING,
		DOORS_CLOSING,
		DOORS_OPEN,
		ACCELERATING,
		DECELERATING,
		MOVING
	}
	
	public enum Direction {
		NOT_MOVING,
		MOVING_UP,
		MOVING_DOWN
	}
	
	
	private int mNumber;
	private Building mBuilding;

	private ElevatorState mCurrentState = ElevatorState.IDLE_STATE;
	private Direction mCurrentDirection = Direction.NOT_MOVING;
	private Floor mCurrentFloor;
	private List<Passenger> mPassengers = new ArrayList<>();
	private List<ElevatorObserver> mObservers = new ArrayList<>();
	
	// TODO: declare a field to keep track of which floors have been requested by passengers.
	private ArrayList<Boolean> mRequestedFloors;
	
	public Elevator(int number, Building bld) {
		mNumber = number;
		mBuilding = bld;

		mCurrentFloor = bld.getFloor(1);
        mRequestedFloors = new ArrayList<Boolean>(bld.getFloorCount()) ;
        for (int i = 0; i<bld.getFloorCount();i++){
            mRequestedFloors.add(false);
        }


		scheduleStateChange(ElevatorState.IDLE_STATE, 0);
	}
	
	/**
	 * Helper method to schedule a state change in a given number of seconds from now.
	 */
	private void scheduleStateChange(ElevatorState state, long timeFromNow) {
		Simulation sim = mBuilding.getSimulation();
		ElevatorStateEvent jk = new ElevatorStateEvent(sim.currentTime() + timeFromNow, state, this);
		sim.scheduleEvent(jk);

	}
	
	/**
	 * Adds the given passenger to the elevator's list of passengers, and requests the passenger's destination floor.
	 */

	public void addPassenger(Passenger passenger) {
		// TODO: add the passenger's destination to the set of requested floors.
		mPassengers.add(passenger);
		mRequestedFloors.set(passenger.getDestination()-1, true);

	}
	
	public void removePassenger(Passenger passenger) {
		mPassengers.remove(passenger);
	}
	
	
	/**
	 * Schedules the elevator's next state change based on its current state.
	 */
	public void tick() {

		//
	 // TODO: port the logic of your state changes from Project 1, accounting for the adjustments in the spec.
	 // TODO: State changes are no longer immediate; they are scheduled using scheduleStateChange().

	 // Example of how to trigger a state change:
	 // scheduleStateChange(ElevatorState.MOVING, 3); // switch to MOVING and call tick(), 3 seconds from now.

		switch(this.mCurrentState){
			case IDLE_STATE:


			    this.getCurrentFloor().addObserver(this);

				ArrayList<ElevatorObserver> copy = new ArrayList<>();
				for (ElevatorObserver each : mObservers){
					copy.add(each);
				}

                for (ElevatorObserver each : copy) {
					each.elevatorWentIdle(this);
				}

//				for (boolean each : mRequestedFloors){
//					System.out.println(each);
//				}
                break;
			case DOORS_OPEN:
			    int e0 = mPassengers.size();
                int f0= this.getCurrentFloor().getWaitingPassengers().size();
//				System.out.println(f0);

				ArrayList<ElevatorObserver> copy2 = new ArrayList<>();

				for (ElevatorObserver each: mObservers){
					copy2.add(each);
				}
				for (ElevatorObserver each: copy2){
					each.elevatorDoorsOpened(this);
				}

//				for (int i=0;i<mObservers.size();i++){
//				    mObservers.get(i).elevatorDoorsOpened(this);
//                }
                int e1 = mPassengers.size();
                int f1 = this.getCurrentFloor().getWaitingPassengers().size();
//				System.out.println(f1);
               int numLeft = (e0-e1) + (f0-f1);
               int numJoined = f0 - f1;
               int peopleThatMoved = numJoined + numLeft;

//                System.out.println(x);
				this.scheduleStateChange(ElevatorState.DOORS_CLOSING, 1 + peopleThatMoved / 2);

				break;

			case DOORS_CLOSING:

                boolean a = false;
                if (this.mPassengers.isEmpty()) {
                    this.scheduleStateChange(ElevatorState.IDLE_STATE, 2);
                    this.setCurrentDirection(Direction.NOT_MOVING);
                }
			    else if (!mPassengers.isEmpty()) {
			    	for (int i = 0; i < mRequestedFloors.size(); i++) {
                        if (i>getCurrentFloor().getNumber()-1 && mRequestedFloors.get(i) && (getCurrentDirection() == Direction.MOVING_UP ||getCurrentDirection() == Direction.NOT_MOVING)) {
							setCurrentDirection(Direction.MOVING_UP);
                        	a = true;
                            break;
                        }else if (i<getCurrentFloor().getNumber()-1 && mRequestedFloors.get(i) && (getCurrentDirection() == Direction.MOVING_DOWN ||getCurrentDirection() == Direction.NOT_MOVING)) {
                        	setCurrentDirection(Direction.MOVING_DOWN);
							a = true;
							break;
						}
                        else if (!mRequestedFloors.get(i)) {
                          a = false;
                        }
                    }
                    if (a){
                        scheduleStateChange(ElevatorState.ACCELERATING, 2);
                    }else if (mRequestedFloors.get(getCurrentFloor().getNumber()-1)) {
                        if (this.getCurrentDirection() == Direction.MOVING_UP) {
                            this.setCurrentDirection(Direction.MOVING_DOWN);
                        } else {
                            this.setCurrentDirection(Direction.MOVING_UP);
                        }
                        scheduleStateChange(ElevatorState.DOORS_OPENING, 2);
                    }
				}
			    if (this.mRequestedFloors.get(0) && this.getCurrentFloor().getNumber() ==1 ){
				setCurrentDirection(Direction.MOVING_UP);
			}
				if (this.mRequestedFloors.get(mBuilding.getFloorCount()-1) && this.getCurrentFloor().getNumber() == mBuilding.getFloorCount()){
					setCurrentDirection(Direction.MOVING_DOWN);
				}


			    break;
			case ACCELERATING:

                this.getCurrentFloor().removeObserver(this);
                scheduleStateChange(ElevatorState.MOVING,3);


//                if (this.getCurrentDirection() == Direction.MOVING_DOWN){
//                    int temp = getCurrentFloor().getNumber();
//                    setCurrentFloor(mBuilding.getFloor(temp-1));
//                }
//                if (this.getCurrentDirection() == Direction.MOVING_UP){
//                    int temp1 = getCurrentFloor().getNumber();
//                    setCurrentFloor(mBuilding.getFloor(temp1+1));
//                }

				break;
			case MOVING:

                if (getCurrentDirection() == Direction.MOVING_UP ){
                    int temp = this.getCurrentFloor().getNumber() +1;
                    if (temp <=this.getBuilding().getFloorCount())
                    setCurrentFloor(mBuilding.getFloor(temp));
                }else{
                    int temp1 = this.getCurrentFloor().getNumber()-1;
                    if (temp1>=1) {
						setCurrentFloor(mBuilding.getFloor(temp1));
					}
                }


//                for (boolean each : mRequestedFloors){
//				System.out.println(each);
//			}
//				System.out.println(mRequestedFloors.get(this.getCurrentFloor().getNumber()-1));
				if (mRequestedFloors.get(this.getCurrentFloor().getNumber()-1)){
//						System.out.println("I decelarate mrequest");
                    scheduleStateChange(ElevatorState.DECELERATING,2);
                }else if(mBuilding.getFloor(mCurrentFloor.getNumber()).directionIsPressed(getCurrentDirection())){
                    scheduleStateChange(ElevatorState.DECELERATING,2);
//					System.out.println("I decelarate mBuilding");
                }
				else{
                    scheduleStateChange(ElevatorState.MOVING,2);
				}



			break;

			case DECELERATING:
//			    boolean a;
				this.mRequestedFloors.set(getCurrentFloor().getNumber()-1,false);

				boolean keepGoing;
				if (mCurrentDirection == Direction.MOVING_UP &&
						(mRequestedFloors.lastIndexOf(true) >= getCurrentFloor().getNumber()
						|| getCurrentFloor().directionIsPressed(Direction.MOVING_UP))) {

					keepGoing = true;
				}
				else if (mCurrentDirection == Direction.MOVING_DOWN &&
						((mRequestedFloors.indexOf(true) < getCurrentFloor().getNumber()  && mRequestedFloors.indexOf(true)>-1 )
								||   getCurrentFloor().directionIsPressed(Direction.MOVING_DOWN)    )) { // finish this, but beware: indexOf returns -1 if not found
					keepGoing = true;
				}
				else {
					keepGoing = false;
				}

				if (!keepGoing){
					if (mRequestedFloors.indexOf(true)<0){
						setCurrentDirection(Direction.NOT_MOVING);
					}else{
						if (mCurrentDirection==Direction.MOVING_UP){
							setCurrentDirection(Direction.MOVING_DOWN);
						}else if (mCurrentDirection==Direction.MOVING_DOWN){
							setCurrentDirection(Direction.MOVING_UP);
						}
					}
				}
//				if (elevator.getCurrentFloor().getNumber() == this.getNumber()) {
//					if (!mPassengers.isEmpty() && this.getNumber() == 1){
//						elevator.setCurrentDirection(Elevator.Direction.MOVING_UP);
//					}else if (!mPassengers.isEmpty() && this.getNumber() == mBuilding.getFloorCount()){
//						elevator.setCurrentDirection(Elevator.Direction.MOVING_DOWN);
//					}

				ArrayList<ElevatorObserver> copy1 = new ArrayList<>();
				for (ElevatorObserver each: mObservers) {
						copy1.add(each);
				}

				for (ElevatorObserver each: copy1){
						each.elevatorDecelerating(this);

                }
                scheduleStateChange(ElevatorState.DOORS_OPENING,3);

				break;

            case DOORS_OPENING:
//				System.out.println(getCurrentFloor().getNumber());
//				for (boolean each : mRequestedFloors){
//					System.out.println(each);
//				}

                this.scheduleStateChange(ElevatorState.DOORS_OPEN,2);
                break;
		}





	 }


	 /**
	 * Sends an idle elevator to the given floor.
	 */
	public void dispatchTo(Floor floor) {
		// TODO: if we are currently idle and not on the given floor, change our direction to move towards the floor.
		// TODO: set a floor request for the given floor, and schedule a state change to ACCELERATING immediately.
		if (mCurrentState == ElevatorState.IDLE_STATE && this.getCurrentFloor() != floor){
			if (this.getCurrentFloor().getNumber() > floor.getNumber()){
				this.setCurrentDirection(Direction.MOVING_DOWN);
			}
			else{
				this.setCurrentDirection(Direction.MOVING_UP);
			}
			//Fixme
		    mRequestedFloors.set(floor.getNumber()-1,true);
			this.scheduleStateChange(ElevatorState.ACCELERATING,0);
		}


	}
	
	// Simple accessors
	public Floor getCurrentFloor() {
		return mCurrentFloor;
	}
	
	public Direction getCurrentDirection() {
		return mCurrentDirection;
	}
	
	public Building getBuilding() {
		return mBuilding;
	}

	
	/**
	 * Returns true if this elevator is in the idle state.
	 * @return
	 */
	public boolean isIdle() {
		// TODO: complete this method.
		if (this.mCurrentState == ElevatorState.IDLE_STATE){
			return true;
		}
		return false;
	}
	
	// All elevators have a capacity of 10, for now.
	public int getCapacity() {
		return 10;
	}
	
	public int getPassengerCount() {
		return mPassengers.size();
	}
	
	// Simple mutators
	public void setState(ElevatorState newState) {
		mCurrentState = newState;
	}
	
	public void setCurrentDirection(Direction direction) {
		mCurrentDirection = direction;
	}
	
	public void setCurrentFloor(Floor floor) {
		mCurrentFloor = floor;
	}
	
	// Observers
	public void addObserver(ElevatorObserver observer) {
		mObservers.add(observer);
	}
	
	public void removeObserver(ElevatorObserver observer) {
		mObservers.remove(observer);
	}
	
	
	// FloorObserver methods
	@Override
	public void elevatorArriving(Floor floor, Elevator elevator) {
		// Not used.
	}
	
	/**
	 * Triggered when our current floor receives a direction request.
	 */
	@Override
	public void directionRequested(Floor sender, Direction direction) {
		// TODO: if we are currently idle, change direction to match the request. Then alert all our observers that we are decelerating,
		// TODO: then schedule an immediate state change to DOORS_OPENING.
		if (this.isIdle()){
			this.setCurrentDirection(direction);
		}
		for(int i = 0; i<mObservers.size();i++){
			mObservers.get(i).elevatorDecelerating(this);
		}
		this.scheduleStateChange(ElevatorState.DOORS_OPENING,0);
	}
	
	
	
	
	// Voodoo magic.
	@Override
	public String toString() {
		return "Elevator " + mNumber + " - " + mCurrentFloor + " - " + mCurrentState + " - " + mCurrentDirection + " - "
		 + "[" + mPassengers.stream().map(p -> Integer.toString(p.getDestination())).collect(Collectors.joining(", "))
		 + "]";
	}
	
}
