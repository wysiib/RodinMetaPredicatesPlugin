package de.stups.hhu.rodinmetapredicates.formulas;

import org.eventb.core.ast.ExtendedPredicate;
import org.eventb.core.ast.Predicate;
import org.eventb.core.ast.extension.ICompatibilityMediator;
import org.eventb.core.ast.extension.IExtendedFormula;
import org.eventb.core.ast.extension.IExtensionKind;
import org.eventb.core.ast.extension.IPredicateExtension;
import org.eventb.core.ast.extension.IPriorityMediator;
import org.eventb.core.ast.extension.ITypeCheckMediator;
import org.eventb.core.ast.extension.IWDMediator;

public class Deterministic implements IPredicateExtension {

	@Override
	public void addCompatibilities(ICompatibilityMediator arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addPriorities(IPriorityMediator arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean conjoinChildrenWD() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getGroupId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IExtensionKind getKind() {
		return PARENTHESIZED_UNARY_PREDICATE;
	}

	@Override
	public Object getOrigin() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getSyntaxSymbol() {
		return "deterministic";
	}

	@Override
	public Predicate getWDPredicate(IExtendedFormula arg0, IWDMediator arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void typeCheck(ExtendedPredicate arg0, ITypeCheckMediator arg1) {
		// TODO Auto-generated method stub

	}

}
