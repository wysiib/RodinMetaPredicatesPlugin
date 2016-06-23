package de.stups.hhu.rodinmetapredicates.sc;

import java.util.HashSet;
import java.util.Set;

import org.eventb.core.ast.extension.IFormulaExtension;
import org.eventb.core.sc.SCProcessorModule;

import de.stups.hhu.rodinmetapredicates.formulas.Controller;
import de.stups.hhu.rodinmetapredicates.formulas.ControllerWithExtraArgument;
import de.stups.hhu.rodinmetapredicates.formulas.Deadlock;
import de.stups.hhu.rodinmetapredicates.formulas.DeadlockWithExtraArgument;
import de.stups.hhu.rodinmetapredicates.formulas.Deterministic;
import de.stups.hhu.rodinmetapredicates.formulas.DeterministicWithExtraArgument;
import de.stups.hhu.rodinmetapredicates.formulas.Enabled;
import de.stups.hhu.rodinmetapredicates.formulas.EnabledWithExtraArgument;

public abstract class Processor extends SCProcessorModule {

	public Processor() {
		super();
	}

	protected Set<IFormulaExtension> getFormulaExtensions() {
		Set<IFormulaExtension> fes = new HashSet<IFormulaExtension>();
		fes.add(new Controller());
		fes.add(new ControllerWithExtraArgument());
		fes.add(new Deterministic());
		fes.add(new DeterministicWithExtraArgument());
		fes.add(new Enabled());
		fes.add(new EnabledWithExtraArgument());
		fes.add(new Deadlock());
		fes.add(new DeadlockWithExtraArgument());
		return fes;
	}

}