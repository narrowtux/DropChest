/*
 * Copyright (C) 2011 Moritz Schmale <narrow.m@gmail.com>
 *
 * DropChest is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/gpl.html>.
 */

package com.narrowtux.dropchest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

import org.bukkit.Material;

public class FlatFileReader {
	private File file;
	private boolean caseSensitive;
	private HashMap<String, String> keySet = new HashMap<String,String>();

	public FlatFileReader(File file, boolean caseSensitive){
		this.file = file;
		this.caseSensitive = caseSensitive;
		reload();
	}

	public void reload(){
		keySet.clear();
		load();
	}

	public int getInteger(String key, int fallback){
		if(keySet.containsKey(key)){
			int ret;
			try{
				ret = Integer.valueOf(keySet.get(key));
			} catch(Exception e){
				ret = fallback;
			}
			return ret;
		} else {
			return fallback;
		}
	}

	public String getString(String key, String fallback){
		if(keySet.containsKey(key)){
			return keySet.get(key);
		} else {
			return fallback;
		}
	}

	public boolean getBoolean(String key, boolean fallback){
		if(keySet.containsKey(key)){
			boolean ret;
			try{
				ret = Boolean.valueOf(keySet.get(key));
			} catch(Exception e){
				ret = fallback;
			}
			return ret;
		} else {
			return fallback;
		}
	}

	public double getDouble(String key, double fallback){
		if(keySet.containsKey(key)){
			double ret;
			try{
				ret = Double.valueOf(keySet.get(key));
			} catch(Exception e){
				ret = fallback;
			}
			return ret;
		} else {
			return fallback;
		}
	}

	public float getFloat(String key, float fallback){
		if(keySet.containsKey(key)){
			float ret;
			try{
				ret = Float.valueOf(keySet.get(key));
			} catch(Exception e){
				ret = fallback;
			}
			return ret;
		} else {
			return fallback;
		}
	}

	public Material getMaterial(String key, Material fallback){
		if(keySet.containsKey(key)){
			Material ret;
			try{
				ret = Material.getMaterial(keySet.get(key));
			} catch(Exception e){
				try{
					ret = Material.getMaterial(Integer.valueOf(keySet.get(key)));
				} catch(Exception ex){
					return fallback;
				}
			}
			return ret;
		}
		return fallback;
	}

	private boolean load(){
		if(file.exists()){
			FileInputStream input;
			try{
				input = new FileInputStream(file.getAbsoluteFile());
				InputStreamReader ir = new InputStreamReader(input);
				BufferedReader r = new BufferedReader(ir);
				while(true){
					String line = r.readLine();
					if(line==null)
						break;
					if(!line.startsWith("#")){
						String splt[] = line.split("=");
						if(splt.length==2){
							String key = splt[0];
							String value = splt[1];
							if(!caseSensitive){
								key = key.toLowerCase();
							}
							keySet.put(key, value);
						}
					}
				}
				r.close();
			} catch(IOException e){
				e.printStackTrace();
			}
		} else {
			System.out.println("File "+file.getAbsoluteFile()+" not found.");
			return false;
		}
		return true;
	}
}
