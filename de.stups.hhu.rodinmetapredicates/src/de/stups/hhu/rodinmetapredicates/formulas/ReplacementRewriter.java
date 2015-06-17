package de.stups.hhu.rodinmetapredicates.formulas;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eventb.core.ISCEvent;
import org.eventb.core.ISCGuard;
import org.eventb.core.ISCMachineRoot;
import org.eventb.core.ast.BoundIdentDecl;
import org.eventb.core.ast.DefaultRewriter;
import org.eventb.core.ast.Expression;
import org.eventb.core.ast.ExtendedPredicate;
import org.eventb.core.ast.FormulaFactory;
import org.eventb.core.ast.Predicate;
import org.eventb.core.ast.SetExtension;
import org.eventb.core.ast.extension.IPredicateExtension;
import org.rodinp.core.RodinDBException;

public class ReplacementRewriter extends DefaultRewriter {
	private final ISCMachineRoot scMachineRoot;
	private boolean rewriteFailed = false;

	public ReplacementRewriter(ISCMachineRoot scMachineRoot) {
		super(false);
		this.scMachineRoot = scMachineRoot;
	}

	public boolean rewriteFailed() {
		return rewriteFailed;
	}

	@Override
	public Predicate rewrite(ExtendedPredicate arg0) {
		try {
			IPredicateExtension extension = arg0.getExtension();
			// there should be one child expression, namely the set of events
			Set<String> setOfEvents = new HashSet<String>();
			Expression[] childExpressions = arg0.getChildExpressions();
			FormulaFactory ff = arg0.getFactory();
			for (Expression ex : ((SetExtension) childExpressions[0])
					.getMembers()) {
				setOfEvents.add(ex.toString());
			}

			if ("controller".equals(extension.getSyntaxSymbol())) {
				return bindFreeVariables(controllerPredicate(setOfEvents, ff),
						ff);
			}
			if ("deadlock".equals(extension.getSyntaxSymbol())) {
				return bindFreeVariables(deadlockPredicate(setOfEvents, ff), ff);
			}
			if ("deterministic".equals(extension.getSyntaxSymbol())) {
				return bindFreeVariables(
						deterministicPredicate(setOfEvents, ff), ff);
			}
			if ("enabled".equals(extension.getSyntaxSymbol())) {
				return bindFreeVariables(enabledPredicate(setOfEvents, ff), ff);
			}
		} catch (Exception e) {
			rewriteFailed = true;
		}

		return arg0;
	}

	private Predicate controllerPredicate(Set<String> setOfEvents,
			FormulaFactory ff) throws CoreException {
		List<Predicate> subPredicates = new ArrayList<Predicate>();
		for (String evt : setOfEvents) {
			Set<String> setOfEventsWithoutEvt = new HashSet<String>();
			setOfEventsWithoutEvt.addAll(setOfEvents);
			setOfEventsWithoutEvt.remove(evt);

			Set<String> setOnlyE = new HashSet<String>();
			setOnlyE.add(evt);

			Predicate deadlock = deadlockPredicate(setOfEventsWithoutEvt, ff);
			Predicate enabled = enabledPredicate(setOnlyE, ff);
			subPredicates.add(ff.makeBinaryPredicate(Predicate.LAND, enabled,
					deadlock, null));
		}

		if (subPredicates.isEmpty()) {
			return ff.makeLiteralPredicate(Predicate.BTRUE, null);
		} else {
			return join(subPredicates, ff, Predicate.LOR);
		}
	}

	private Predicate join(List<Predicate> subPredicates, FormulaFactory ff,
			int tag) {
		if (subPredicates.size() == 1) {
			return subPredicates.get(0);
		}
		return ff.makeAssociativePredicate(tag, subPredicates, null);
	}

	private Predicate deadlockPredicate(Set<String> setOfEvents,
			FormulaFactory ff) throws CoreException {
		List<Predicate> subFormulas = new ArrayList<Predicate>();
		for (String evt : setOfEvents) {
			List<Predicate> guardPredicates = new ArrayList<Predicate>();
			ISCEvent scEvent;
			scEvent = getSCEvent(evt);
			ISCGuard[] guards = scEvent
					.getChildrenOfType(ISCGuard.ELEMENT_TYPE);
			for (ISCGuard g : guards) {
				guardPredicates.add(g.getPredicate(ff.makeTypeEnvironment()));
			}

			// \u00ac = logical not
			Predicate joinedGuards = join(guardPredicates, ff, Predicate.LAND);
			subFormulas.add(ff.makeUnaryPredicate(Predicate.NOT, joinedGuards,
					null));
		}

		if (subFormulas.isEmpty()) {
			return ff.makeLiteralPredicate(Predicate.BTRUE, null);
		} else {
			return join(subFormulas, ff, Predicate.LAND);
		}
	}

	private Predicate deterministicPredicate(Set<String> setOfEvents,
			FormulaFactory ff) throws CoreException {
		Predicate controller = controllerPredicate(setOfEvents, ff);
		Predicate deadlock = deadlockPredicate(setOfEvents, ff);

		return ff
				.makeBinaryPredicate(Predicate.LOR, controller, deadlock, null);
	}

	private Predicate enabledPredicate(Set<String> setOfEvents,
			FormulaFactory ff) throws CoreException {
		List<Predicate> subFormulas = new ArrayList<Predicate>();
		for (String evt : setOfEvents) {
			ISCEvent scEvent;
			scEvent = getSCEvent(evt);
			ISCGuard[] guards = scEvent
					.getChildrenOfType(ISCGuard.ELEMENT_TYPE);
			for (ISCGuard g : guards) {
				subFormulas.add(g.getPredicate(ff.makeTypeEnvironment()));
			}

		}

		if (subFormulas.isEmpty()) {
			return ff.makeLiteralPredicate(Predicate.BTRUE, null);
		} else {
			return join(subFormulas, ff, Predicate.LAND);
		}
	}

	private ISCEvent getSCEvent(String label) throws RodinDBException {
		for (ISCEvent event : scMachineRoot.getSCEvents()) {
			if (label.equals(event.getLabel())) {
				return event;
			}
		}
		return null;
	}

	private Predicate bindFreeVariables(Predicate p, FormulaFactory ff) {
		List<BoundIdentDecl> theBoundOnes = new ArrayList<BoundIdentDecl>();
		p = p.bindAllFreeIdents(theBoundOnes);
		return ff.makeQuantifiedPredicate(Predicate.EXISTS, theBoundOnes, p,
				null);
	}
}
