package jmt.engine.jwat.input;

import jmt.engine.jwat.MatrixObservations;

public class EventFinishLoad implements EventStatus {

	private MatrixObservations m;
	private int valToRead;
	private int valReaded;

	public EventFinishLoad(MatrixObservations m, int valToRead, int valReaded) {
		this.m = m;
		this.valReaded = valReaded;
		this.valToRead = valToRead;
	}

	public MatrixObservations getSession() {
		return m;
	}

	public int valToRead() {
		return valToRead;
	}

	public int valReaded() {
		return valReaded;
	}

	public int getType() {
		return EventStatus.DONE_EVENT;
	}

}
