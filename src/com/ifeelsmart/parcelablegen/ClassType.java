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
 * File: ClassType.java
 *
 */
 
 package com.ifeelsmart.parcelablegen;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ClassType extends ObjectType {
	private static class BuildInType {
		String fileName;
		String targetName;
		String retName;
		String writeFormat;
		String readFormat;
		public BuildInType(String fn, String tn, String rn, String wf, String rf) {
			fileName = fn;
			targetName = tn;
			retName = rn;
			writeFormat = wf;
			readFormat = rf;
		}
	}
	
	private static class ClassField {
		String originClass;
		String targetClass;
		String name;
	}
	
	private static HashMap<String, BuildInType> BUILT_IN_TYPES;
	static {
		BUILT_IN_TYPES = new HashMap<String, BuildInType>();
		BUILT_IN_TYPES.put("bool", new BuildInType("bool", "bool", "bool", "writeInt32(%s ? 1 : 0)", "%s.readInt32() == 0 ? false : true"));
		BUILT_IN_TYPES.put("byte", new BuildInType("byte", "uint8_t", "uint8_t", "writeInt32(%s)", "(uint8_t) %s.readInt32()"));
		BUILT_IN_TYPES.put("int", new BuildInType("int", "int32_t", "int32_t", "writeInt32(%s)", "%s.readInt32()"));
		BUILT_IN_TYPES.put("long", new BuildInType("long", "int64_t", "int64_t", "writeInt64(%s)", "%s.readInt64()"));
		BUILT_IN_TYPES.put("double", new BuildInType("double", "double", "double", "writeDouble(%s)", "%s.readDouble()"));
		BUILT_IN_TYPES.put("String", new BuildInType("String", "String16", "const String16&", "writeString16(%s)", "%s.readString16()"));
	}
	
	private List<ClassField> fields = new ArrayList<ClassField>();
	
	public ClassType(String[] desc) {
		super(desc[0]);
		int nbValues = (desc.length - 3) / 3;
		for(int n = 0; n < nbValues; n++) {
			ClassField field = new ClassField();
			field.originClass = desc[3 + n * 3];
			field.targetClass = desc[4 + n * 3];
			field.name = desc[5 + n * 3];
			fields.add(field);
		}
	}
	
	public void generate(HashMap<String, ObjectType> hashTypes) {
		try {
			File hFile = new File(ParcelableGen.OUTPUT_PATH, this.getName() + ".h");
			File cppFile = new File(ParcelableGen.OUTPUT_PATH, this.getName() + ".cpp");
			BufferedWriter hWS = new BufferedWriter(new FileWriter(hFile));
			BufferedWriter cppWS = new BufferedWriter(new FileWriter(cppFile));
			
			hWS.write("#ifndef " + this.getName().toUpperCase() + "_H\n");
			hWS.write("#define " + this.getName().toUpperCase() + "_H\n");
			hWS.write("\n");
			hWS.write("\n");
			hWS.write("#include <binder/Parcel.h>\n");
			hWS.write("\n");
			for(ClassField field : fields) {
				if (!BUILT_IN_TYPES.containsKey(field.targetClass)) {
					if (hashTypes.containsKey(field.targetClass)) {
						hWS.write("#include \"" + field.targetClass + ".h\"\n");
					}
				}
			}
			hWS.write("\n");
			cppWS.write("#include \"" + this.getName() + ".h\"\n");
			cppWS.write("\n");
			for(String path:this.getPackagePath()) {
				hWS.write("namespace " + path + " {\n");
				cppWS.write("namespace " + path + " {\n");
			}
			hWS.write("\n");
			cppWS.write("\n");
			
			// Fields
			hWS.write("class " + this.getName() + "{\n");
			hWS.write("private:\n");
			for(int f = 0; f < fields.size(); f++) {
				ClassField field = fields.get(f);
				BuildInType declaredType = BUILT_IN_TYPES.get(field.targetClass);
				if (declaredType != null) {
					hWS.write("\t" + declaredType.targetName + " " + field.name + ";\n");
				} else if (hashTypes.containsKey(field.targetClass)) {
					hWS.write("\t" + hashTypes.get(field.targetClass).getCPPQualifiedName() + "* " + field.name + ";\n");
				} else {
					System.err.println("Cannot find type " + field.targetClass + ".");
				}
			}
			
			// Method
			hWS.write("public:\n");
			hWS.write("\tstatus_t writeToParcel(Parcel* parcel);\n");
			cppWS.write("status_t " + this.getName() + "::writeToParcel(Parcel* parcel) {\n");
			for(int f = 0; f < fields.size(); f++) {
				ClassField field = fields.get(f);
				BuildInType declaredType = BUILT_IN_TYPES.get(field.targetClass);
				if (declaredType != null) {
					cppWS.write("\tparcel->" + String.format(declaredType.writeFormat, "this->" + field.name) + ";\n");
				} else if (hashTypes.containsKey(field.targetClass)) {
					cppWS.write("\tthis->" + field.name + "->writeToParcel(parcel);\n");
				} else {
					System.err.println("Cannot find type " + field.targetClass + ".");
				}
			}
			cppWS.write("}\n");
			cppWS.write("\n");
			hWS.write("\tstatus_t readFromParcel(const Parcel& parcel);\n");
			cppWS.write("status_t " + this.getName() + "::readFromParcel(const Parcel& parcel) {\n");
			for(int f = 0; f < fields.size(); f++) {
				ClassField field = fields.get(f);
				BuildInType declaredType = BUILT_IN_TYPES.get(field.targetClass);
				if (declaredType != null) {
					cppWS.write("\tthis->" + field.name + " = " + String.format(declaredType.readFormat, "parcel") + ";\n");
				} else if (hashTypes.containsKey(field.targetClass)) {
					cppWS.write("\tthis->" + field.name + " = " + hashTypes.get(field.targetClass).getCPPQualifiedName() + "::createFromParcel(parcel);\n");
				} else {
					System.err.println("Cannot find type " + field.targetClass + ".");
				}
			}
			cppWS.write("}\n");
			cppWS.write("\n");
			hWS.write("\tstatic " + this.getName() + "* createFromParcel(const Parcel& parcel);\n");
			cppWS.write(this.getName() + "* " + this.getName() + "::createFromParcel(const Parcel& parcel) {\n");
			cppWS.write("\t" + this.getName() + "* ret = new " + this.getName() + "();\n");
			cppWS.write("\tret->readFromParcel(parcel);\n");
			cppWS.write("\treturn ret;\n");
			cppWS.write("}\n");
			cppWS.write("\n");
			for(int f = 0; f < fields.size(); f++) {
				ClassField field = fields.get(f);
				BuildInType declaredType = BUILT_IN_TYPES.get(field.targetClass);
				if (declaredType != null) {
					hWS.write("\t" + declaredType.retName + " get" + upCaseFirstChar(field.name) + "();\n");
					cppWS.write(declaredType.retName + " " + this.getName() + "::get" + upCaseFirstChar(field.name) + "() {\n");
					cppWS.write("\treturn this->" + field.name + ";\n");
					cppWS.write("}\n");
					cppWS.write("\n");
					hWS.write("\tvoid set" + upCaseFirstChar(field.name) + "(" + declaredType.retName + " value);\n");
					cppWS.write("void " + this.getName() + "::set" + upCaseFirstChar(field.name) + "(" + declaredType.retName + " value) {\n");
					cppWS.write("\tthis->" + field.name + " = value;\n");
					cppWS.write("}\n");
					cppWS.write("\n");
				} else if (hashTypes.containsKey(field.targetClass)) {
					hWS.write("\t" + hashTypes.get(field.targetClass).getCPPQualifiedName() + "* get" + upCaseFirstChar(field.name) + "();\n");
					cppWS.write(hashTypes.get(field.targetClass).getCPPQualifiedName() + "* " + this.getName() + "::get" + upCaseFirstChar(field.name) + "() {\n");
					cppWS.write("\treturn this->" + field.name + ";\n");
					cppWS.write("}\n");
					cppWS.write("\n");
					hWS.write("\tvoid set" + upCaseFirstChar(field.name) + "(" + hashTypes.get(field.targetClass).getCPPQualifiedName() + "* value);\n");
					cppWS.write("void " + this.getName() + "::set" + upCaseFirstChar(field.name) + "(" + hashTypes.get(field.targetClass).getCPPQualifiedName() + "* value) {\n");
					cppWS.write("\tthis->" + field.name + " = value;\n");
					cppWS.write("}\n");
					cppWS.write("\n");
				} else {
					System.err.println("Cannot find type " + field.targetClass + ".");
				}
			}
			hWS.write("};\n");
			
			hWS.write("\n");
			cppWS.write("\n");
			for(@SuppressWarnings("unused") String path:this.getPackagePath()) {
				hWS.write("}\n");
				cppWS.write("}\n");
			}
			hWS.write("\n");
			hWS.write("#endif\n");
			hWS.flush();
			hWS.close();
			cppWS.flush();
			cppWS.close();
		} catch (IOException e) {
			System.err.println("Cannot generate output file (" + e.getMessage() + ").");
		}
		
	}
	
	static String upCaseFirstChar(String str) {
		if (str == null) return null;
		if (str.length() == 0) return str;
		return str.substring(0, 1).toUpperCase() + str.substring(1);
	}
}
