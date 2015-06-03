package de.stups.hhu.rodinmetapredicates.formulas;

import java.util.ArrayList;
import java.util.List;

import org.eventb.core.ISCEvent;
import org.eventb.core.ISCGuard;
import org.eventb.core.ISCMachineRoot;
import org.eventb.core.ast.DefaultRewriter;
import org.eventb.core.ast.Expression;
import org.eventb.core.ast.ExtendedPredicate;
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

		if ("controller".equals(extension.getSyntaxSymbol())) {
			return controllerPredicate(arg0);
		}
		if ("deadlock".equals(extension.getSyntaxSymbol())) {
			return deadlockPredicate(arg0);
		}
		if ("deterministic".equals(extension.getSyntaxSymbol())) {
			return deterministicPredicate(arg0);
		}
		if ("enabled".equals(extension.getSyntaxSymbol())) {
			return enabledPredicate(arg0);
		}

		return arg0;
	}

	private Predicate controllerPredicate(ExtendedPredicate arg0) {
		throw new UnsupportedOperationException();
	}

	private Predicate deadlockPredicate(ExtendedPredicate arg0) {
		throw new UnsupportedOperationException();
	}

	private Predicate deterministicPredicate(ExtendedPredicate arg0) {
		throw new UnsupportedOperationException();
	}

	private Predicate enabledPredicate(ExtendedPredicate arg0) {
		// there should be one child expression, namely the set of events
		Expression[] childExpressions = arg0.getChildExpressions();
		SetExtension setOfEvents = (SetExtension) childExpressions[0];
		List<String> subFormulas = new ArrayList<String>();
		for (Expression expression : setOfEvents.getMembers()) {
			ISCEvent scEvent;
			try {
				scEvent = getSCEvent(expression.toString());
				ISCGuard[] guards = scEvent
						.getChildrenOfType(ISCGuard.ELEMENT_TYPE);
				for (ISCGuard g : guards) {
					subFormulas.add(g.getPredicateString());
				}
			} catch (RodinDBException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if (subFormulas.isEmpty()) {
			return arg0.getFactory()
					.makeLiteralPredicate(Predicate.BTRUE, null);
		} else {
			String formula = conjoinStrings(subFormulas);

			IParseResult parsePredicate = arg0.getFactory().parsePredicate(
					formula, null);

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
