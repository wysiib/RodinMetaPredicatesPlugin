package de.stups.hhu.rodinmetapredicates.errormarkers;

import org.eclipse.core.resources.IMarker;
import org.rodinp.core.IRodinProblem;

import de.stups.hhu.rodinmetapredicates.Activator;

public class MetaPredicateNotParsedMarker implements IRodinProblem {

	private final String message;
	private final int severity = IMarker.SEVERITY_ERROR;
	public static final String ERROR_CODE = Activator.PLUGIN_ID + "."
			+ "multipleUnitsInferred";

	public MetaPredicateNotParsedMarker(String mPred) {
		this.message = "The meta-predicate could not be parsed: " + mPred;
	}

	@Override
	public String getErrorCode() {
		return ERROR_CODE;
	}

	@Override
	public String getLocalizedMessage(Object[] arg0) {
		return message;
	}

	@Override
	public int getSeverity() {
		return severity;
	}

}
