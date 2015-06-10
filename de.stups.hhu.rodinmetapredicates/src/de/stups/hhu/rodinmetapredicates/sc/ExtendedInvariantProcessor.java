package de.stups.hhu.rodinmetapredicates.sc;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eventb.core.IMachineRoot;
import org.eventb.core.ISCInvariant;
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

import de.stups.hhu.rodinmetapredicates.Activator;
import de.stups.hhu.rodinmetapredicates.attributes.ExtendedInvariant;
import de.stups.hhu.rodinmetapredicates.errormarkers.MetaPredicateNotParsedMarker;
import de.stups.hhu.rodinmetapredicates.errormarkers.ReplacementFailedMarker;
import de.stups.hhu.rodinmetapredicates.formulas.Controller;
import de.stups.hhu.rodinmetapredicates.formulas.Deadlock;
import de.stups.hhu.rodinmetapredicates.formulas.Deterministic;
import de.stups.hhu.rodinmetapredicates.formulas.Enabled;
import de.stups.hhu.rodinmetapredicates.formulas.ReplacementRewriter;

public class ExtendedInvariantProcessor extends SCProcessorModule {
	public static final IModuleType<ExtendedInvariantProcessor> MODULE_TYPE = SCCore
			.getModuleType(Activator.PLUGIN_ID + ".extendedInvariantProcessor");
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

		ExtendedInvariant[] extendedInvariants = machineRoot
				.getChildrenOfType(ExtendedInvariant.ELEMENT_TYPE);

		if (extendedInvariants.length == 0)
			return;

		FormulaFactory ff = scMachineRoot.getFormulaFactory();
		ff = ff.withExtensions(getFormulaExtensions());

		for (ExtendedInvariant ei : extendedInvariants) {
			IParseResult parsed = ff.parsePredicate(ei.getPredicateString(),
					null);
			if (parsed.getProblems().isEmpty()) {
				ReplacementRewriter rr = new ReplacementRewriter(scMachineRoot);
				Predicate rewritten = parsed.getParsedPredicate().rewrite(rr);

				if (rr.rewriteFailed()) {
					ei.createProblemMarker(new ReplacementFailedMarker(ei
							.getPredicateString()));
					return;
				}

				ISCInvariant newInvariant = scMachineRoot.createChild(
						ISCInvariant.ELEMENT_TYPE, null, monitor);
				newInvariant.setLabel(getNextLabel(), monitor);
				newInvariant.setPredicate(rewritten, monitor);
				newInvariant.setPredicateString(
						rewritten.toStringFullyParenthesized(), monitor);
				newInvariant.setSource(ei, monitor);
				newInvariant.setTheorem(ei.isTheorem(), monitor);
			} else {
				ei.createProblemMarker(new MetaPredicateNotParsedMarker(ei
						.getPredicateString()));
			}
		}

	}

	private Set<IFormulaExtension> getFormulaExtensions() {
		Set<IFormulaExtension> fes = new HashSet<IFormulaExtension>();
		fes.add(new Controller());
		fes.add(new Deterministic());
		fes.add(new Enabled());
		fes.add(new Deadlock());
		return fes;
	}

	private String getNextLabel() {
		return "generated_invariant_" + nextId++;
	}

	@Override
	public IModuleType<?> getModuleType() {
		return MODULE_TYPE;
	}

}
