package de.stups.hhu.rodinmetapredicates.formulas;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eventb.core.ISCEvent;
import org.eventb.core.ISCGuard;
import org.eventb.core.ISCMachineRoot;
import org.eventb.core.ast.DefaultRewriter;
import org.eventb.core.ast.Expression;
import org.eventb.core.ast.ExtendedPredicate;
import org.eventb.core.ast.FormulaFactory;
import org.eventb.core.ast.IParseResult;
import org.eventb.core.ast.Predicate;
import org.eventb.core.ast.SetExtension;
import org.eventb.core.ast.extension.IPredicateExtension;
import org.rodinp.core.RodinDBException;

public class ReplacementRewriter extends DefaultRewriter {
	private final ISCMachineRoot scMachineRoot;

	public ReplacementRewriter(ISCMachineRoot scMachineRoot) {
		super(false);
		this.scMachineRoot = scMachineRoot;
	}

	@Override
	public Predicate rewrite(ExtendedPredicate arg0) {
		IPredicateExtension extension = arg0.getExtension();
		// there should be one child expression, namely the set of events
		Set<String> setOfEvents = new HashSet<String>();
		Expression[] childExpressions = arg0.getChildExpressions();
		for (Expression ex : ((SetExtension) childExpressions[0]).getMembers()) {
			setOfEvents.add(ex.toString());
		}

		try {
			if ("controller".equals(extension.getSyntaxSymbol())) {
				return controllerPredicate(setOfEvents, arg0.getFactory());
			}
			if ("deadlock".equals(extension.getSyntaxSymbol())) {

				return deadlockPredicate(setOfEvents, arg0.getFactory());

			}
			if ("deterministic".equals(extension.getSyntaxSymbol())) {
				return deterministicPredicate(setOfEvents, arg0.getFactory());
			}
			if ("enabled".equals(extension.getSyntaxSymbol())) {
				return enabledPredicate(setOfEvents, arg0.getFactory());
			}
		} catch (RodinDBException e) {
			e.printStackTrace();
		}

		return arg0;
	}

	private Predicate controllerPredicate(Set<String> setOfEvents,
			FormulaFactory ff) {
		throw new UnsupportedOperationException();
	}

	private Predicate deadlockPredicate(Set<String> setOfEvents,
			FormulaFactory ff) throws RodinDBException {
		List<String> subFormulas = new ArrayList<String>();
		for (String evt : setOfEvents) {
			List<String> guardPredicates = new ArrayList<String>();
			ISCEvent scEvent;
			scEvent = getSCEvent(evt);
			ISCGuard[] guards = scEvent
					.getChildrenOfType(ISCGuard.ELEMENT_TYPE);
			for (ISCGuard g : guards) {
				guardPredicates.add(g.getPredicateString());
			}

			// \u00ac = logical not
			subFormulas.add("(\u00ac " + conjoinStrings(guardPredicates) + ")");

		}

		if (subFormulas.isEmpty()) {
			return ff.makeLiteralPredicate(Predicate.BTRUE, null);
		} else {
			String formula = conjoinStrings(subFormulas);

			IParseResult parsePredicate = ff.parsePredicate(formula, null);

			return parsePredicate.getParsedPredicate();
		}
	}

	private Predicate deterministicPredicate(Set<String> setOfEvents,
			FormulaFactory ff) {
		throw new UnsupportedOperationException();
	}

	private Predicate enabledPredicate(Set<String> setOfEvents,
			FormulaFactory ff) throws RodinDBException {
		List<String> subFormulas = new ArrayList<String>();
		for (String evt : setOfEvents) {
			ISCEvent scEvent;
			scEvent = getSCEvent(evt);
			ISCGuard[] guards = scEvent
					.getChildrenOfType(ISCGuard.ELEMENT_TYPE);
			for (ISCGuard g : guards) {
				subFormulas.add(g.getPredicateString());
			}

		}

		if (subFormulas.isEmpty()) {
			return ff.makeLiteralPredicate(Predicate.BTRUE, null);
		} else {
			String formula = conjoinStrings(subFormulas);

			IParseResult parsePredicate = ff.parsePredicate(formula, null);

			return parsePredicate.getParsedPredicate();
		}
	}

	private String conjoinStrings(List<String> subFormulas) {
		StringBuilder result = new StringBuilder(subFormulas.get(0));
		for (int i = 1; i < subFormulas.size(); i++) {
			result.append(" & ");
			result.append(subFormulas.get(i));
		}
		return result.toString();
	}

	private ISCEvent getSCEvent(String label) throws RodinDBException {
		for (ISCEvent event : scMachineRoot.getSCEvents()) {
			if (label.equals(event.getLabel())) {
				return event;
			}
		}
		return null;
	}
}
