/*
 * Copyright 2013 Grzegorz Ligas <ligasgr@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.intellij.xquery.formatter;

import com.intellij.formatting.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.TokenType;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.intellij.psi.formatter.FormatterUtil;
import com.intellij.psi.formatter.common.AbstractBlock;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.containers.ContainerUtil;
import org.intellij.xquery.psi.XQueryExpr;
import org.intellij.xquery.psi.XQueryExprSingle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.intellij.xquery.psi.XQueryTypes.*;

/**
 * User: ligasgr
 * Date: 21/08/13
 * Time: 19:37
 */
public class XQueryFormattingBlock extends AbstractBlock {

    private static final Set<IElementType> BIN_OPERATORS = ContainerUtil.set(
            K_OR, K_AND, PIPE_PIPE, K_TO, OP_PLUS, OP_MINUS, STAR_SIGN, K_DIV, K_IDIV, K_MOD,
            K_UNION, PIPE, K_INTERSECT, K_EXCEPT, K_INSTANCE, K_OF, K_TREAT, K_AS, K_CASTABLE,
            K_CAST, EQ, NE, LT, LE, GT, GE, EQUAL, NOT_EQUAL, LT_CHAR, LE_CHARS, GT_CHAR, GE_CHARS,
            K_IS, NODECOMP_LT, NODECOMP_GT, EXCLAMATION_MARK, SLASH, SLASH_SLASH
    );

    private final SpacingBuilder spacingBuilder;
    private final CommonCodeStyleSettings settings;

    public XQueryFormattingBlock(@NotNull ASTNode node, @Nullable Wrap wrap, @Nullable Alignment alignment,
                                 @NotNull CommonCodeStyleSettings settings, @NotNull SpacingBuilder spacingBuilder) {
        super(node, wrap, alignment);
        this.spacingBuilder = spacingBuilder;
        this.settings = settings;
    }

    @Override
    protected List<Block> buildChildren() {
        List<Block> blocks = new ArrayList<Block>();
        ASTNode child = myNode.getFirstChildNode();
        while (child != null) {
            if (child.getElementType() != TokenType.WHITE_SPACE && child.getTextRange().getLength() != 0) {
                Block block = new XQueryFormattingBlock(child, Wrap.createWrap(WrapType.NONE, false), null, settings,
                        spacingBuilder);
                blocks.add(block);
            }
            child = child.getTreeNext();
        }
        return blocks;
    }

    @Nullable
    @Override
    public Spacing getSpacing(@Nullable Block child1, @NotNull Block child2) {
        return spacingBuilder.getSpacing(this, child1, child2);
    }

    @Override
    public boolean isLeaf() {
        return myNode.getFirstChildNode() == null;
    }

    @Override
    public Indent getIndent() {
        IElementType type = myNode.getElementType();
        ASTNode parent = myNode.getTreeParent();
        IElementType parentType = parent != null ? parent.getElementType() : null;

        if (parent == null)
            return Indent.getNoneIndent();
        if (isFunctionBody(type, parentType) || isXmlChild(type) || isForOrLetBinding(parentType))
            return Indent.getNormalIndent();
        if (isASingleExpression()) {
            if (parentType == IF_EXPR) {
                return Indent.getNormalIndent();
            }
            if (parentType == WHERE_CLAUSE ||
                    parentType == RETURN_CLAUSE ||
                    parentType == VAR_VALUE ||
                    parentType == ORDER_SPEC) {
                return Indent.getNormalIndent();
            }
        }
        if (isParenthesizedList(parentType) && (type != L_PAR && type != R_PAR)) {
            return Indent.getContinuationIndent();
        }
        if (isChildOfSingleExpression()) {
            IElementType prevType = getTypeOfPreviousElement(myNode);
            if (BIN_OPERATORS.contains(type) || BIN_OPERATORS.contains(prevType)) {
                return Indent.getContinuationIndent();
            }
        }
        if (isChildOfExpressionList()) {
            IElementType prevType = getTypeOfPreviousElement(myNode);
            if (type == COMMA || prevType == COMMA) {
                return Indent.getContinuationIndent();
            }
        }

        return Indent.getNoneIndent();
    }

    private boolean isASingleExpression() {
        return myNode.getPsi() instanceof XQueryExprSingle;
    }

    private IElementType getTypeOfPreviousElement(ASTNode myNode) {
        ASTNode prevSibling = FormatterUtil.getPreviousNonWhitespaceSibling(myNode);
        IElementType prevType = prevSibling != null ? prevSibling.getElementType() : null;
        return prevType;
    }

    private boolean isChildOfExpressionList() {
        return myNode.getPsi().getParent() instanceof XQueryExpr;
    }

    private boolean isChildOfSingleExpression() {
        return myNode.getPsi().getParent() instanceof XQueryExprSingle;
    }

    private boolean isParenthesizedList(IElementType parentType) {
        return parentType == PARAM_LIST || parentType == ARGUMENT_LIST;
    }

    private boolean isForOrLetBinding(IElementType parentType) {
        return parentType == LET_BINDING || parentType == FOR_BINDING;
    }

    private boolean isXmlChild(IElementType type) {
        return type == DIR_ELEM_CONTENT;
    }

    private boolean isFunctionBody(IElementType type, IElementType parentType) {
        return type == EXPR && parentType == ENCLOSED_EXPR;
    }
}