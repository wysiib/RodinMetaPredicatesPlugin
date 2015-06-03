package de.stups.hhu.rodinmetapredicates.sc;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eventb.core.IEvent;
import org.eventb.core.IMachineRoot;
import org.eventb.core.ISCEvent;
import org.eventb.core.ISCGuard;
import org.eventb.core.ISCMachineRoot;
import org.eventb.core.ast.FormulaFactory;
import org.eventb.core.ast.IParseResult;
import org.eventb.core.ast.Predicate;
import org.eventb.core.ast.extension.IFormulaExtension;
import org.eventb.core.sc.SCCore;
import org.eventb.core.sc.SCProcessorModule;
import org.eventb.core.sc.state.ISCStateRepository;
import org.eventb.core.tool.IModuleType;
import org.rodinp.core.IInternalElement;
import org.rodinp.core.IRodinElement;
import org.rodinp.core.IRodinFile;
import org.rodinp.core.RodinDBException;

import de.stups.hhu.rodinmetapredicates.Activator;
import de.stups.hhu.rodinmetapredicates.attributes.ExtendedGuard;
import de.stups.hhu.rodinmetapredicates.formulas.Enabled;
import de.stups.hhu.rodinmetapredicates.formulas.ReplacementRewriter;

public class ExtendedGuardProcessor extends SCProcessorModule {
	public static final IModuleType<ExtendedGuardProcessor> MODULE_TYPE = SCCore
			.getModuleType(Activator.PLUGIN_ID + ".extendedGuardProcessor");
	public static int nextId = 0;

	@Override
	public void process(IRodinElement element, IInternalElement target,
			ISCStateRepository repository, IProgressMonitor monitor)
			throws CoreException {
		assert (element instanceof IRodinFile);
		assert (target instanceof ISCMachineRoot);

		IRodinFile machineFile = (IRodinFile) element;
		IMachineRoot machineRoot = (IMachineRoot) machineFile.getRoot();

		ISCMachineRoot scMachineRoot = (ISCMachineRoot) target;
		FormulaFactory ff = scMachineRoot.getFormulaFactory().withExtensions(
				getFormulaExtensions());

		IEvent[] events = machineRoot.getEvents();

		if (events.length == 0)
			return;

		for (IEvent evt : events) {
			String identifier = evt.getLabel();
			ISCEvent scEvt = getSCEvent(scMachineRoot, identifier);
			ExtendedGuard[] eGuards = evt
					.getChildrenOfType(ExtendedGuard.ELEMENT_TYPE);

			// might have been filtered out by previous modules
			// or might not have an extended guard
			if (scEvt != null && eGuards.length > 0) {
				for (ExtendedGuard eGuard : eGuards) {
					IParseResult parsed = ff.parsePredicate(
							eGuard.getPredicateString(), null);
					System.out.println(parsed.getParsedPredicate());
					Predicate rewritten = parsed.getParsedPredicate().rewrite(
							new ReplacementRewriter(scMachineRoot));

					ISCGuard newGuard = scEvt.createChild(
							ISCGuard.ELEMENT_TYPE, null, monitor);
					newGuard.setLabel(getNextLabel(), monitor);
					newGuard.setPredicateString(
							rewritten.toStringFullyParenthesized(), monitor);
					newGuard.setSource(eGuard, monitor);
				}
			}
		}

	}

	private String getNextLabel() {
		return "generated_guard_" + nextId++;
	}

	private Set<IFormulaExtension> getFormulaExtensions() {
		Set<IFormulaExtension> fes = new HashSet<IFormulaExtension>();
		fes.add(new Enabled());
		return fes;
	}

	private ISCEvent getSCEvent(ISCMachineRoot root, String label)
			throws RodinDBException {
		for (ISCEvent event : root.getSCEvents()) {
			if (label.equals(event.getLabel())) {
				return event;
			}
		}
		return null;
	}

	@Override
	public IModuleType<?> getModuleType() {
		return MODULE_TYPE;
	}

}
