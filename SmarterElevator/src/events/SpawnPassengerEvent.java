package events;

import buildings.Building;
import elevators.Elevator;
import passengers.Passenger;
import cecs277.Simulation;
import passengers.VisitorPassenger;
import passengers.WorkerPassenger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * A simulation event that adds a new random passenger on floor 1, and then schedules the next spawn event.
 */
public class SpawnPassengerEvent extends SimulationEvent {
	private static long SPAWN_MEAN_DURATION = 10_800;
	private static long SPAWN_STDEV_DURATION = 3_600;

	// After executing, will reference the Passenger object that was spawned.
	private Passenger mPassenger;
	private Building mBuilding;
	
	public SpawnPassengerEvent(long scheduledTime, Building building) {
		super(scheduledTime);
		mBuilding = building;
	}
	
	@Override
	public String toString() {
		return super.toString() + "Adding " + mPassenger + " to floor 1.";
	}
	
	@Override
	public void execute(Simulation sim) {
		Random r = mBuilding.getSimulation().getRandom();

		// 75% of all passengers are normal Visitors.
		if (r.nextInt(4) <= 2) {
			mPassenger = getVisitor();

		}
		else {
			mPassenger = getWorker();

		}

		mBuilding.getFloor(1).addWaitingPassenger(mPassenger);

		/*
		 Finished
		 with a scheduled time that is X seconds in the future, where X is a uniform random integer from
		 1 to 30 inclusive.
		*/
		int low = 1;
		int high = 30;
		int x = r.nextInt(30)+1;
//		System.out.println(x);
		SpawnPassengerEvent  newEvent =  new SpawnPassengerEvent(sim.currentTime()+x,mBuilding);
		sim.scheduleEvent(newEvent);
	}


	private Passenger getVisitor() {
        /*
         TODO: construct a VisitorPassenger and return it.
         The visitor should have a random destination floor that is not floor 1 (generate a random int from 2 to N).
         The visitor's visit duration should follow a NORMAL (GAUSSIAN) DISTRIBUTION with a mean of 1 hour
         and a standard deviation of 20 minutes.
         */
        Random r = mBuilding.getSimulation().getRandom();
        int lowFloor = 2;
        int highFloor = mBuilding.getFloorCount();
        int randomFLoorNumber = r.nextInt((highFloor-lowFloor)+1)+lowFloor;

        int vTime = (int)(r.nextGaussian() * 1200 + 3600);
        VisitorPassenger visitor = new VisitorPassenger(randomFLoorNumber,vTime);
        return visitor;
        // Look up the documentation for the .nextGaussian() method of the Random class.
    }
	
	private Passenger getWorker() {
		/*
		done
		To generate the list of destinations, first generate a random number from 2 to 5 inclusive. Call this "X",
		how many floors the worker will visit before returning to floor 1.
		X times, generate an integer from 2 to N (number of floors) that is NOT THE SAME as the previously-generated floor.
		Add those X integers to a list.
		To generate the list of durations, generate X integers using a NORMAL DISTRIBUTION with a mean of 10 minutes
		and a standard deviation of 3 minutes.
		 */
		ArrayList<Integer> f = new ArrayList<>();
		ArrayList<Long> d = new ArrayList<>();
		Random r = mBuilding.getSimulation().getRandom();
		long duration;
		int lowFloor = 2;
		int highFloor = mBuilding.getFloorCount();
		int rfloor = 1;
		int x = r.nextInt(4) +2;
		int lastFloor = 0;
		for (int i =0; i<x; i++) {
			rfloor = r.nextInt((highFloor-lowFloor)+1)+lowFloor;
			if (lastFloor==rfloor){
				rfloor = r.nextInt((highFloor-lowFloor)+1)+lowFloor;
			}
			lastFloor = rfloor;

			f.add(rfloor);
		}

for (int i=0;i<x;i++) {

	duration = (long)(r.nextGaussian() * 180 + 600);
//	long duration = r.nextGaussian()*(20SPAWN_STDEV_DURATION) +(60*SPAWN_MEAN_DURATION);
//	duration = (int)(r.nextGaussian()*(20*SPAWN_STDEV_DURATION)+(60*SPAWN_MEAN_DURATION));
	d.add(duration);
}

		System.out.println(f);
	WorkerPassenger WorkPass = new WorkerPassenger(f,d);
		return WorkPass;
				}
}
