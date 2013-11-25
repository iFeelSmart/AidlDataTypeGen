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
 * File: ParcelableGen.java
 *
 */
 
 package com.ifeelsmart.parcelablegen;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ParcelableGen {
	public static String OUTPUT_PATH = "output";

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 1) {
			List<ObjectType> types = parseFile(args[0]);
			File outputPath = new File(OUTPUT_PATH);
			if (!outputPath.exists()) outputPath.mkdir();
			HashMap<String, ObjectType> hashTypes = new HashMap<String, ObjectType>();
			for(ObjectType type : types) hashTypes.put(type.getName(), type);
			for(ObjectType type : types) {
				type.generate(hashTypes);
			}
		} else {	
			System.err.println("Please provide a filename as argument.");
		}
	}

	private static List<ObjectType> parseFile(String filename) {
		List<ObjectType> ret = new ArrayList<ObjectType>();
		File f = new File(filename);
		if (f.exists()) {
			try {
				BufferedReader sr = new BufferedReader(new FileReader(f));
				String line;
				try {
					while ((line = sr.readLine()) != null) {
						if (line.startsWith("//STOP")) break;
						if (line.trim().length() == 0 || line.startsWith("//")) continue;
						
						String[] cells = line.split(";");
						if (cells.length >= 3) {
							ObjectType type = null; // new ObjectType(cells[0].trim());
							if (cells[1].trim().equals("enum")) {
								type = new EnumType(cells);
							} else if (cells[1].trim().equals("class")) {
								type = new ClassType(cells);
							}
							ret.add(type);
							if (type != null) System.out.println(type.getClass().getName() + " " + type.getCPPQualifiedName());
							else System.err.println("Problem with " + cells[0]);
						}
					}
				} catch (IOException e) {
					System.err.println("Error while reading the file " + f.getAbsolutePath() + " (" + e.getMessage() + ").");
				}
			} catch (FileNotFoundException e) {
				System.err.println("The file " + f.getAbsolutePath() + " cannot be opened (" + e.getMessage() + ").");
				return null;
			}
		} else {
			System.err.println("The file " + f.getAbsolutePath() + " does not exist.");
			return null;
		}
		return ret;
	}
}
