// This is a generated file. Not intended for manual editing.
package com.goide.psi.impl;

import com.goide.psi.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.goide.GoTypes.RANGE;

public class GoRangeClauseImpl extends GoCompositeElementImpl implements GoRangeClause {

  public GoRangeClauseImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof GoVisitor) ((GoVisitor)visitor).visitRangeClause(this);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public GoExpression getExpression() {
    return findNotNullChildByClass(GoExpression.class);
  }

  @Override
  @Nullable
  public GoExpressionList getExpressionList() {
    return findChildByClass(GoExpressionList.class);
  }

  @Override
  @Nullable
  public GoIdentifierList getIdentifierList() {
    return findChildByClass(GoIdentifierList.class);
  }

  @Override
  @NotNull
  public PsiElement getRange() {
    return findNotNullChildByType(RANGE);
  }

}