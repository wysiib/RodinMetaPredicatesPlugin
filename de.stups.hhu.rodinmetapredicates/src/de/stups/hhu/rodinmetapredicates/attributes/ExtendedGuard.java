package de.stups.hhu.rodinmetapredicates.attributes;

import org.eventb.core.ICommentedElement;
import org.eventb.core.IDerivedPredicateElement;
import org.eventb.core.basis.EventBElement;
import org.rodinp.core.IInternalElement;
import org.rodinp.core.IInternalElementType;
import org.rodinp.core.IRodinElement;
import org.rodinp.core.RodinCore;

import de.stups.hhu.rodinmetapredicates.Activator;

public class ExtendedGuard extends EventBElement implements
		IDerivedPredicateElement, ICommentedElement {
	public static IInternalElementType<ExtendedGuard> ELEMENT_TYPE = RodinCore
			.getInternalElementType(Activator.PLUGIN_ID + ".extendedGuard");

	public ExtendedGuard(String name, IRodinElement parent) {
		super(name, parent);
	}

	@Override
	public IInternalElementType<? extends IInternalElement> getElementType() {
		return ELEMENT_TYPE;
	}

}
