package de.stups.hhu.rodinmetapredicates.formulas;

import org.eventb.core.ast.extension.IExtensionKind;

import de.stups.hhu.rodinmetapredicates.Activator;

public class DeterministicWithExtraArgument extends Controller {
	@Override
	public String getId() {
		return Activator.PLUGIN_ID + ".deterministicWithExtraArgument";
	}

	@Override
	public IExtensionKind getKind() {
		return PARENTHESIZED_BINARY_PREDICATE;
	}

	@Override
	public String getSyntaxSymbol() {
		return "deterministicDontQuantify";
	}
}
