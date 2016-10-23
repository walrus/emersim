package jmt.engine.jwat.trafficAnalysis;

import java.util.ArrayList;

import jmt.engine.jwat.MatrixObservations;
import jmt.gui.jwat.JWatModel;

public class ModelTrafficAnalysis implements JWatModel {
	private MatrixObservations matrix = null;
	private ArrayList<OnResetModel> resetModel = new ArrayList<OnResetModel>();

	public MatrixObservations getMatrix() {
		return matrix;
	}

	public void resetModel() {
		matrix = null;
		notifyResetModel();
	}

	public void addResetModelListener(OnResetModel listener) {
		if (!resetModel.contains(listener)) {
			resetModel.add(listener);
		}
	}

	public void removeResetModelListener(OnResetModel listener) {
		resetModel.remove(listener);
	}

	private void notifyResetModel() {
		for (int i = 0; i < resetModel.size(); i++) {
			resetModel.get(i).modelReset();
		}
	}

	public void setMatrix(MatrixObservations matrix) {
		this.matrix = matrix;
	}

}
