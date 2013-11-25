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
 * File: EnumType.java
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

public class EnumType extends ObjectType {
	private static class EnumValue {
		int value;
		String name;
	}
	
	private List<EnumValue> values = new ArrayList<EnumValue>();
	
	public EnumType(String[] desc) {
		super(desc[0]);
		int nbValues = (desc.length - 2) / 2;
		for(int n = 0; n < nbValues; n++) {
			int intValue = Integer.parseInt(desc[2 + n * 2]);
			String name = desc[3 + n * 2];
			EnumValue value = new EnumValue();
			value.name = name;
			value.value = intValue;
			values.add(value);
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
			cppWS.write("#include \"" + this.getName() + ".h\"\n");
			cppWS.write("\n");
			for(String path:this.getPackagePath()) {
				hWS.write("namespace " + path + " {\n");
				cppWS.write("namespace " + path + " {\n");
			}
			hWS.write("\n");
			cppWS.write("\n");
			hWS.write("class " + this.getName() + "{\n");
			hWS.write("private:\n");
			hWS.write("\tint value;\n");
			hWS.write("\n");
			hWS.write("public:\n");
			hWS.write("\tenum {\n");
			for(int v = 0; v < values.size(); v++) {
				EnumValue value = values.get(v);
				hWS.write("\t\t" + value.name + " = " + value.value + (v == values.size() - 1 ? "\n" : ",\n"));
			}
			hWS.write("\t};\n");
			hWS.write("\tstatus_t writeToParcel(Parcel* parcel);\n");
			cppWS.write("status_t " + this.getName() + "::writeToParcel(Parcel* parcel) {\n");
			cppWS.write("\treturn parcel->writeInt32(value);\n");
			cppWS.write("}\n");
			cppWS.write("\n");
			hWS.write("\tstatus_t readFromParcel(Parcel& parcel);\n");
			cppWS.write("status_t " + this.getName() + "::readFromParcel(Parcel& parcel) {\n");
			cppWS.write("\treturn parcel.readInt32(&value);\n");
			cppWS.write("}\n");
			cppWS.write("\n");
			hWS.write("\tstatic " + this.getName() + "* createFromParcel(const Parcel& parcel);\n");
			cppWS.write(this.getName() + "* " + this.getName() + "::createFromParcel(const Parcel& parcel) {\n");
			cppWS.write("\t" + this.getName() + "* ret = new " + this.getName() + "();\n");
			cppWS.write("\tret->setValue(parcel.readInt32());\n");
			cppWS.write("\treturn ret;\n");
			cppWS.write("}\n");
			cppWS.write("\n");
			hWS.write("\tvoid setValue(int newValue);\n");
			cppWS.write("void " + this.getName() + "::setValue(int newValue) {\n");
			cppWS.write("\tthis->value = newValue;\n");
			cppWS.write("}\n");
			cppWS.write("\n");
			hWS.write("\tint getValue();\n");
			cppWS.write("int " + this.getName() + "::getValue() {\n");
			cppWS.write("\treturn this->value;\n");
			cppWS.write("}\n");
			cppWS.write("\n");
			hWS.write("\tinline " + this.getName() + "& operator=(int value) { setValue(value); }\n");
			hWS.write("\tinline operator int() { return getValue(); }\n");
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
}
