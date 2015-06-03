package de.stups.hhu.rodinmetapredicates.sc;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eventb.core.IMachineRoot;
import org.eventb.core.ISCMachineRoot;
import org.eventb.core.sc.SCCore;
import org.eventb.core.sc.SCProcessorModule;
import org.eventb.core.sc.state.ISCStateRepository;
import org.eventb.core.tool.IModuleType;
import org.rodinp.core.IInternalElement;
import org.rodinp.core.IRodinElement;
import org.rodinp.core.IRodinFile;

import de.stups.hhu.rodinmetapredicates.Activator;
import de.stups.hhu.rodinmetapredicates.attributes.ExtendedInvariant;

public class ExtendedInvariantProcessor extends SCProcessorModule {
	public static final IModuleType<ExtendedInvariantProcessor> MODULE_TYPE = SCCore
			.getModuleType(Activator.PLUGIN_ID + ".extendedInvariantProcessor");

	@Override
	public void process(IRodinElement element, IInternalElement target,
			ISCStateRepository repository, IProgressMonitor monitor)
			throws CoreException {
		assert (element instanceof IRodinFile);
		assert (target instanceof ISCMachineRoot);

		IRodinFile machineFile = (IRodinFile) element;
		IMachineRoot machineRoot = (IMachineRoot) machineFile.getRoot();

		ISCMachineRoot scMachineRoot = (ISCMachineRoot) target;

		ExtendedInvariant[] globalGuards = machineRoot
				.getChildrenOfType(ExtendedInvariant.ELEMENT_TYPE);

		if (globalGuards.length == 0)
			return;

		for (ExtendedInvariant g : globalGuards) {
			System.out.println("global guard:" + g);
		}

	}

	@Override
	public IModuleType<?> getModuleType() {
		return MODULE_TYPE;
	}

}
