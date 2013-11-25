/* iFeelSmart
 *
 * Copyright © 2012-2013, iFeelSmart.
 *
 *
 * GNU Lesser General Public License Usage
 * This file may be used under the terms of the GNU Lesser General Public
 * License version 2.1 as published by the Free Software Foundation and
 * appearing in the file LICENSE.LGPL included in the packaging of this
 * file. Please review the following information to ensure the GNU Lesser
 * General Public License version 2.1 requirements will be met:
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html.
 *
 * File: ObjectType.java
 *
 */
 
 package com.ifeelsmart.parcelablegen;

import java.util.ArrayList;
import java.util.HashMap;

public abstract class ObjectType {
	private String name;
	public String getName() { return name; }
	public void setName(String newName) { name = newName; }
	
	private String javaQualifiedName;
	public String getJavaQualifiedName() { return javaQualifiedName; }
	public void setJavaQualifiedName(String newName) { javaQualifiedName = newName; }
	
	private String cppQualifiedName;
	public String getCPPQualifiedName() { return cppQualifiedName; }
	public void setCPPQualifiedName(String newName) { cppQualifiedName = newName; }
	
	private ArrayList<String> packagePath;
	public String[] getPackagePath() { String[] tmp = new String[0]; return packagePath.toArray(tmp); }
	
	public abstract void generate(HashMap<String, ObjectType> hashTypes);
	
	public ObjectType(String rawName) {
		String[] nameSubParts = rawName.split("\\.");
		if (nameSubParts.length > 0) {
			packagePath = new ArrayList<String>();
			StringBuilder cppSb = new StringBuilder();
			for(int i = 0; i < nameSubParts.length - 1; i++) {
				cppSb.append(nameSubParts[i]).append("::");
				packagePath.add(nameSubParts[i]);
			}
			name = nameSubParts[nameSubParts.length - 1];
			cppSb.append(name);
			javaQualifiedName = rawName;
			cppQualifiedName = cppSb.toString();
		}
	}
}
