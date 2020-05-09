package passengers;

import cecs277.Simulation;
import elevators.Elevator;
import events.PassengerNextDestinationEvent;

/**
 * A VisitorPassenger has a single destination and a single duration (in seconds), which is how long the Visitor
 * will "disappear" for after departing the elevator on their destination floor. After that duration, the Visitor
 * will reappear on the original destination floor, set its new destination to floor 1, then leave the building when it
 * arrives on floor 1.
 */
public class VisitorPassenger extends Passenger {
	// TODO: add fields, constructors, and accessors to implement this class.
	private int destinationFloor;
	private int durationTime;
	public VisitorPassenger(int destinationFloor, int durationTime) {
		super();
		// TODO: change this constructor's  body.
		this.destinationFloor = destinationFloor;
		this.durationTime = durationTime;
	}
	
	@Override
	public int getDestination() {
		//fixme
		// TODO: finish this method to return the visitor's destination, which changes to floor 1 when they
		// "reappear"

		return destinationFloor;
	}
	
	// TODO: implement this template method variant. A Visitor will join an elevator whose passenger count is less than its capacity.
	@Override

	protected boolean willBoardElevator(Elevator elevator) {
		if (elevator.getPassengerCount()<elevator.getCapacity() ){
			return true;
		}else {
			return false;
		}
	}
	
	/*
	 TODO: implement this template method variant, which is called when the passenger is leaving the elevator it
	 is on. A Visitor that is departing on floor 1 just leaves the building, printing a message to System.out.
	 A visitor that is departing on any other floor sets their new destination to floor 1, and then schedules a
	 PassengerNextDestinationEvent to occur when they are supposed to "reappear" (their duration field).
	*/
	@Override
	protected void leavingElevator(Elevator elevator) {
		if (getDestination() == 1) {
			elevator.removePassenger(this);
			System.out.println("Visitor leaving the building");
		} else {
			elevator.removePassenger(this);
			Simulation s = elevator.getBuilding().getSimulation();
			PassengerNextDestinationEvent event = new PassengerNextDestinationEvent(s.currentTime() + durationTime, this, elevator.getCurrentFloor());
			destinationFloor = 1;
			s.scheduleEvent(event);

		}



		/* Example of how to schedule a PassengerNextDestinationEvent:
		Simulation s = elevator.getBuilding().getSimulation();
		PassengerNextDestinationEvent ev = new PassengerNextDestinationEvent(s.currentTime() + 10, this,
		 elevator.getCurrentFloor());
		s.scheduleEvent(ev);
		
		Schedules this passenger to reappear on this floor 10 seconds from now.
		 */
	}
	
	// TODO: return "Visitor heading to floor {destination}", replacing {destination} with the floor number.
	@Override
	public String toString() {
		return "Visitor " + this.getId()+" heading to floor {"+ this.getDestination()+"}";
	}
	
	@Override
	public void elevatorDecelerating(Elevator elevator) {
		// Don't care.
	}
}
