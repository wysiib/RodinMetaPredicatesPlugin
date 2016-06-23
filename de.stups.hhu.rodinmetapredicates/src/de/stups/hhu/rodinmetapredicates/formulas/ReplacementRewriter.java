package de.stups.hhu.rodinmetapredicates.formulas;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eventb.core.ISCEvent;
import org.eventb.core.ISCGuard;
import org.eventb.core.ISCMachineRoot;
import org.eventb.core.ISCParameter;
import org.eventb.core.ast.BoundIdentDecl;
import org.eventb.core.ast.DefaultRewriter;
import org.eventb.core.ast.Expression;
import org.eventb.core.ast.ExtendedPredicate;
import org.eventb.core.ast.FormulaFactory;
import org.eventb.core.ast.FreeIdentifier;
import org.eventb.core.ast.IParseResult;
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
			Set<String> setOfIDsNotToBeQuantified = new HashSet<String>();

			Expression[] childExpressions = arg0.getChildExpressions();
			FormulaFactory ff = arg0.getFactory();
			for (Expression ex : ((SetExtension) childExpressions[0]).getMembers()) {
				setOfEvents.add(ex.toString());
			}

			if (childExpressions.length > 1) {
				for (Expression ex : ((SetExtension) childExpressions[1]).getMembers()) {
					setOfIDsNotToBeQuantified.add(ex.toString());
				}
			}

			if (extension.getSyntaxSymbol().startsWith("controller")) {
				return controllerPredicate(setOfEvents, setOfIDsNotToBeQuantified, ff);
			}
			if (extension.getSyntaxSymbol().startsWith("deadlock")) {
				return deadlockPredicate(setOfEvents, setOfIDsNotToBeQuantified, ff);
			}
			if (extension.getSyntaxSymbol().startsWith("deterministic")) {
				return deterministicPredicate(setOfEvents, setOfIDsNotToBeQuantified, ff);
			}
			if (extension.getSyntaxSymbol().startsWith("enabled")) {
				return enabledPredicate(setOfEvents, setOfIDsNotToBeQuantified, ff);
			}
		} catch (Exception e) {
			rewriteFailed = true;
		}

		return arg0;
	}

	private Predicate controllerPredicate(Set<String> setOfEvents, Set<String> setOfIDsNotToBeQuantified,
			FormulaFactory ff) throws CoreException {
		List<Predicate> subPredicates = new ArrayList<Predicate>();
		for (String evt : setOfEvents) {
			Set<String> setOfEventsWithoutEvt = new HashSet<String>();
			setOfEventsWithoutEvt.addAll(setOfEvents);
			setOfEventsWithoutEvt.remove(evt);

			Set<String> setOnlyE = new HashSet<String>();
			setOnlyE.add(evt);

			Predicate deadlock = deadlockPredicate(setOfEventsWithoutEvt, setOfIDsNotToBeQuantified, ff);
			Predicate enabled = enabledPredicate(setOnlyE, setOfIDsNotToBeQuantified, ff);
			subPredicates.add(ff.makeBinaryPredicate(Predicate.LAND, enabled, deadlock, null));
		}

		if (subPredicates.isEmpty()) {
			return ff.makeLiteralPredicate(Predicate.BTRUE, null);
		} else {
			return join(subPredicates, ff, Predicate.LOR);
		}
	}

	private Predicate join(List<Predicate> subPredicates, FormulaFactory ff, int tag) {
		if (subPredicates.size() == 1) {
			return subPredicates.get(0);
		}
		return ff.makeAssociativePredicate(tag, subPredicates, null);
	}

	private Predicate deadlockPredicate(Set<String> setOfEvents, Set<String> setOfIDsNotToBeQuantified,
			FormulaFactory ff) throws CoreException {
		List<Predicate> subFormulas = new ArrayList<Predicate>();
		List<Predicate> eventsFormulas = new ArrayList<Predicate>();

		for (String evt : setOfEvents) {
			List<Predicate> guardPredicates = new ArrayList<Predicate>();
			ISCEvent scEvent;
			scEvent = getSCEvent(evt);
			ISCGuard[] guards = scEvent.getChildrenOfType(ISCGuard.ELEMENT_TYPE);
			for (ISCGuard g : guards) {
				guardPredicates.add(getPredicateFromGuard(g, ff, scEvent));
			}

			Predicate joinedGuards = join(guardPredicates, ff, Predicate.LAND);
			subFormulas.add(ff.makeUnaryPredicate(Predicate.NOT, joinedGuards, null));
			Predicate allSubs = join(subFormulas, ff, Predicate.LAND);
			eventsFormulas.add(bindFreeVariables(allSubs, scEvent.getSCParameters(), setOfIDsNotToBeQuantified, ff));
		}

		if (eventsFormulas.isEmpty()) {
			return ff.makeLiteralPredicate(Predicate.BTRUE, null);
		} else {
			return join(eventsFormulas, ff, Predicate.LAND);
		}
	}

	private Predicate deterministicPredicate(Set<String> setOfEvents, Set<String> setOfIDsNotToBeQuantified,
			FormulaFactory ff) throws CoreException {
		Predicate controller = controllerPredicate(setOfEvents, setOfIDsNotToBeQuantified, ff);
		Predicate deadlock = deadlockPredicate(setOfEvents, setOfIDsNotToBeQuantified, ff);

		return ff.makeBinaryPredicate(Predicate.LOR, controller, deadlock, null);
	}

	private Predicate enabledPredicate(Set<String> setOfEvents, Set<String> setOfIDsNotToBeQuantified,
			FormulaFactory ff) throws CoreException {
		List<Predicate> eventsFormulas = new ArrayList<Predicate>();
		for (String evt : setOfEvents) {
			List<Predicate> subFormulas = new ArrayList<Predicate>();
			ISCEvent scEvent;
			scEvent = getSCEvent(evt);
			ISCGuard[] guards = scEvent.getChildrenOfType(ISCGuard.ELEMENT_TYPE);
			for (ISCGuard g : guards) {
				subFormulas.add(getPredicateFromGuard(g, ff, scEvent));
			}
			Predicate allSubs = join(subFormulas, ff, Predicate.LAND);
			eventsFormulas.add(bindFreeVariables(allSubs, scEvent.getSCParameters(), setOfIDsNotToBeQuantified, ff));
		}

		if (eventsFormulas.isEmpty()) {
			return ff.makeLiteralPredicate(Predicate.BTRUE, null);
		} else {
			return join(eventsFormulas, ff, Predicate.LAND);
		}
	}

	private Predicate getPredicateFromGuard(ISCGuard g, FormulaFactory ff, ISCEvent scEvent) throws CoreException {
		IParseResult parsePredicate = ff.parsePredicate(g.getPredicateString(), scEvent);
		Predicate p = parsePredicate.getParsedPredicate();
		return p;
	}

	private ISCEvent getSCEvent(String label) throws RodinDBException {
		for (ISCEvent event : scMachineRoot.getSCEvents()) {
			if (label.equals(event.getLabel())) {
				return event;
			}
		}
		return null;
	}

	private Predicate bindFreeVariables(Predicate p, ISCParameter[] iscParameters,
			Set<String> setOfIDsNotToBeQuantified, FormulaFactory ff) throws RodinDBException {
		List<FreeIdentifier> idsToBind = new ArrayList<FreeIdentifier>();
		List<BoundIdentDecl> boundDecls = new ArrayList<BoundIdentDecl>();

		for (FreeIdentifier freeIdentifier : p.getFreeIdentifiers()) {
			for (ISCParameter param : iscParameters) {
				if (freeIdentifier.getName().equals(param.getIdentifierString())) {
					if (!setOfIDsNotToBeQuantified.contains(freeIdentifier.getName())) {
						idsToBind.add(freeIdentifier);
						boundDecls.add(freeIdentifier.asDecl());
					}
				}
			}
		}

		if (boundDecls.isEmpty()) {
			return p;
		}

		p = p.bindTheseIdents(idsToBind);
		return ff.makeQuantifiedPredicate(Predicate.EXISTS, boundDecls, p, null);
	}
}
