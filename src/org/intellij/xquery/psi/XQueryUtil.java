/*
 * Copyright 2013 Grzegorz Ligas
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

package org.intellij.xquery.psi;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.indexing.FileBasedIndex;
import org.intellij.xquery.XQueryFileType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * User: ligasgr
 * Date: 08/06/13
 * Time: 21:40
 */
public class XQueryUtil {

    public static List<XQueryVarName> findVarNames(PsiFile file) {
        List<XQueryVarName> result = null;
        XQueryFile xQueryFile = (XQueryFile) file;
        if (xQueryFile != null) {
            Collection<XQueryVarName> variableNames = PsiTreeUtil.findChildrenOfType(xQueryFile, XQueryVarName.class);
            if (variableNames != null) {
                if (result == null) {
                    result = new ArrayList<XQueryVarName>();
                }
                result.addAll(variableNames);
            }
        }
        return result != null ? result : Collections.<XQueryVarName>emptyList();
    }

    public static List<XQueryVarName> findVarNames(PsiFile file, String key) {
        List<XQueryVarName> result = null;
        XQueryFile xQueryFile = (XQueryFile) file;
        if (xQueryFile != null) {
            Collection<XQueryVarName> variableNames = PsiTreeUtil.findChildrenOfType(xQueryFile, XQueryVarName.class);
            if (variableNames != null) {
                for (XQueryVarName variableName : variableNames) {
                    if (key.equals(variableName.getText())) {
                        if (result == null) {
                            result = new ArrayList<XQueryVarName>();
                        }
                        result.add(variableName);
                    }
                }
            }
        }
        return result != null ? result : Collections.<XQueryVarName>emptyList();
    }

    private static Collection<VirtualFile> findXQueryFiles(Project project) {
        return FileBasedIndex.getInstance().getContainingFiles(FileTypeIndex.NAME, XQueryFileType.INSTANCE,
                GlobalSearchScope.allScope(project));
    }
}