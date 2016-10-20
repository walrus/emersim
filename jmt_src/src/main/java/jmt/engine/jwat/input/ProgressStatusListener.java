package jmt.engine.jwat.input;

import jmt.engine.jwat.MatrixObservations;

public interface ProgressStatusListener {

	public void abortEvent();

	public void finishedEvent(MatrixObservations m, int MaxToRead, int Readed);
}
