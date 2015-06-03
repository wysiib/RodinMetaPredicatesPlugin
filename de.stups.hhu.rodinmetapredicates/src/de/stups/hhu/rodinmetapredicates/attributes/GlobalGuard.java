package de.stups.hhu.rodinmetapredicates.attributes;

import org.eventb.core.ICommentedElement;
import org.eventb.core.IPredicateElement;
import org.eventb.core.basis.EventBElement;
import org.rodinp.core.IInternalElement;
import org.rodinp.core.IInternalElementType;
import org.rodinp.core.IRodinElement;
import org.rodinp.core.RodinCore;

import de.stups.hhu.rodinmetapredicates.Activator;

public class GlobalGuard extends EventBElement implements IPredicateElement,
		ICommentedElement {
	public static IInternalElementType<GlobalGuard> ELEMENT_TYPE = RodinCore
			.getInternalElementType(Activator.PLUGIN_ID + ".globalGuard");

	public GlobalGuard(String name, IRodinElement parent) {
		super(name, parent);
	}

	@Override
	public IInternalElementType<? extends IInternalElement> getElementType() {
		return ELEMENT_TYPE;
	}

}
