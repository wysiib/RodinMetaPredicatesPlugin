/**
 * (c) 2009 Lehrstuhl fuer Softwaretechnik und Programmiersprachen, Heinrich
 * Heine Universitaet Duesseldorf This software is licenced under EPL 1.0
 * (http://www.eclipse.org/org/documents/epl-v10.html)
 * */

package de.stups.hhu.rodinmetapredicates;

import org.eventb.core.IMachineRoot;
import org.rodinp.core.ElementChangedEvent;
import org.rodinp.core.IElementChangedListener;
import org.rodinp.core.IElementType;
import org.rodinp.core.IInternalElement;
import org.rodinp.core.IRodinDB;
import org.rodinp.core.IRodinElement;
import org.rodinp.core.IRodinElementDelta;
import org.rodinp.core.IRodinFile;
import org.rodinp.core.IRodinProject;
import org.rodinp.core.RodinDBException;

/**
 * Class that updates the configuration of files, to add the static checker
 * modules for our qualitative probabilistic reasoning plug-in.
 */
public class ConfSettor implements IElementChangedListener {

	private static final String CONFIG = Activator.PLUGIN_ID + ".mchBase";

	@Override
	public void elementChanged(ElementChangedEvent event) {

		final IRodinElementDelta d = event.getDelta();
		try {
			processDelta(d);
		} catch (final RodinDBException e) {
			// TODO add exception log
		}
	}

	private void processDelta(final IRodinElementDelta d)
			throws RodinDBException {
		final IRodinElement e = d.getElement();

		final IElementType<? extends IRodinElement> elementType = e
				.getElementType();
		if (elementType.equals(IRodinDB.ELEMENT_TYPE)
				|| elementType.equals(IRodinProject.ELEMENT_TYPE)) {
			for (final IRodinElementDelta de : d.getAffectedChildren()) {
				processDelta(de);
			}
		} else if (elementType.equals(IRodinFile.ELEMENT_TYPE)) {
			final IInternalElement root = ((IRodinFile) e).getRoot();

			if (root.getElementType().equals(IMachineRoot.ELEMENT_TYPE)) {
				final IMachineRoot ctx = (IMachineRoot) root;
				final String conf = ctx.getConfiguration();
				if (!conf.contains(CONFIG)) {
					ctx.setConfiguration(conf + ";" + CONFIG, null);
				}
			}
		}
	}
}