package soot.jimple;
import soot.*;

public abstract class PointerStmtSwitch extends AbstractStmtSwitch {
    Stmt statement;

    /** A statement of the form l = constant; */
    protected abstract void caseAssignConstStmt( Value dest, Constant c );

    /** A statement of the form l = v; */
    protected abstract void caseCopyStmt( Local dest, Local src );

    /** A statement of the form l = (cl) v; */
    protected void caseCastStmt( Local dest, Local src, CastExpr c ) {
	// default is to just ignore the cast
	caseCopyStmt( dest, src );
    }
    /** An identity statement assigning a parameter to a local. */
    protected abstract void caseIdentityStmt( Local dest, IdentityRef src );

    /** A statement of the form l1 = l2.f; */
    protected abstract void caseLoadStmt( Local dest, InstanceFieldRef src );

    /** A statement of the form l1.f = l2; */
    protected abstract void caseStoreStmt( InstanceFieldRef dest, Local src );

    /** A statement of the form l1 = l2[i]; */
    protected abstract void caseArrayLoadStmt( Local dest, ArrayRef src );
    /** A statement of the form l1[i] = l2; */
    protected abstract void caseArrayStoreStmt( ArrayRef dest, Local src );
    /** A statement of the form l = cl.f; */
    protected abstract void caseGlobalLoadStmt( Local dest, StaticFieldRef src );
    /** A statement of the form cl.f = l; */
    protected abstract void caseGlobalStoreStmt( StaticFieldRef dest, Local src );
    /** A return statement. e is null if a non-reference type is returned. */
    protected abstract void caseReturnStmt( Local val );
    /** A return statement returning a constant. */
    protected void caseReturnConstStmt( Constant val ) {
	// default is uninteresting
	caseUninterestingStmt( statement );
    }
    /** Any type of new statement (NewStmt, NewArrayStmt, NewMultiArrayStmt) */
    protected abstract void caseAnyNewStmt( Local dest, Expr e );
    /** A new statement */
    protected void caseNewStmt( Local dest, NewExpr e ) {
	caseAnyNewStmt( dest, e );
    }
    /** A newarray statement */
    protected void caseNewArrayStmt( Local dest, NewArrayExpr e ) {
	caseAnyNewStmt( dest, e );
    }
    /** A anewarray statement */
    protected void caseNewMultiArrayStmt( Local dest, NewMultiArrayExpr e ) {
	caseAnyNewStmt( dest, e );
    }
    /** A method invocation. dest is null if there is no reference type return value. */
    protected abstract void caseInvokeStmt( Local dest, InvokeExpr e );
    /** Any other statement */
    protected void caseUninterestingStmt( Stmt s ) {};

    public final void caseAssignStmt( AssignStmt s ) {
	statement = s;
	Value lhs = s.getLeftOp();
	Value rhs = s.getRightOp();
	if( ! (lhs.getType() instanceof RefType)
	&&  ! (lhs.getType() instanceof ArrayType) ) {
	    caseUninterestingStmt( s );
	    return;
	}
	if( lhs instanceof Local ) {
	    if( rhs instanceof Local ) {
		caseCopyStmt( (Local) lhs, (Local) rhs );
	    } else if( rhs instanceof InstanceFieldRef ) {
		caseLoadStmt( (Local) lhs, (InstanceFieldRef) rhs );
	    } else if( rhs instanceof ArrayRef ) {
		caseArrayLoadStmt( (Local) lhs, (ArrayRef) rhs );
	    } else if( rhs instanceof StaticFieldRef ) {
		caseGlobalLoadStmt( (Local) lhs, (StaticFieldRef) rhs );
	    } else if( rhs instanceof NewExpr ) {
		caseNewStmt( (Local) lhs, (NewExpr) rhs );
	    } else if( rhs instanceof NewArrayExpr ) {
		caseNewArrayStmt( (Local) lhs, (NewArrayExpr) rhs );
	    } else if( rhs instanceof NewMultiArrayExpr ) {
		caseNewMultiArrayStmt( (Local) lhs, (NewMultiArrayExpr) rhs );
	    } else if( rhs instanceof InvokeExpr ) {
		caseInvokeStmt( (Local) lhs, (InvokeExpr)rhs );
	    } else if( rhs instanceof CastExpr ) {
		CastExpr r = (CastExpr) rhs;
		Value rv = r.getOp();
		if( rv instanceof Constant ) {
		    caseAssignConstStmt( (Local) lhs, (Constant) rv );
		} else {
		    caseCastStmt( (Local) lhs, (Local) rv, r );
		}
	    }
	} else if( lhs instanceof InstanceFieldRef ) {
	    if( rhs instanceof Local ) {
		caseStoreStmt( (InstanceFieldRef) lhs, (Local) rhs );
	    }
	} else if( lhs instanceof ArrayRef ) {
	    if( rhs instanceof Local ) {
		caseArrayStoreStmt( (ArrayRef) lhs, (Local) rhs );
	    }
	} else if( lhs instanceof StaticFieldRef ) {
	    if( rhs instanceof Local ) {
		caseGlobalStoreStmt( (StaticFieldRef) lhs, (Local) rhs );
	    }
	} else if( rhs instanceof InvokeExpr ) {
	    caseInvokeStmt( null, (InvokeExpr) rhs );
	} else if( rhs instanceof Constant ) {
	    caseAssignConstStmt( lhs, (Constant) rhs );
	}
    }
    public final void caseReturnStmt(ReturnStmt s) {
	statement = s;
	Value op = s.getOp();
	if( op.getType() instanceof RefType 
	|| op.getType() instanceof ArrayType ) { 
	    if( op instanceof Constant ) {
		caseReturnConstStmt( (Constant) op );
	    } else {
		caseReturnStmt( (Local) op );
	    }
	} else {
	    caseReturnStmt( (Local) null );
	}
    }
    public final void caseReturnVoidStmt(ReturnVoidStmt s) {
	statement = s;
	caseReturnStmt( (Local) null );
    }
    public final void caseInvokeStmt(InvokeStmt s) {
	statement = s;
	caseInvokeStmt( null, (InvokeExpr) s.getInvokeExpr() );
    }
    public final void caseIdentityStmt(IdentityStmt s) {
	statement = s;
	Value lhs = s.getLeftOp();
	Value rhs = s.getRightOp();
	if( !( lhs.getType() instanceof RefType ) 
	&& !(lhs.getType() instanceof ArrayType ) ) {
	     caseUninterestingStmt( s );
	     return;
	}
	Local llhs = (Local) lhs;
	IdentityRef rrhs = (IdentityRef) rhs;
	caseIdentityStmt( llhs, rrhs );
    }
}

