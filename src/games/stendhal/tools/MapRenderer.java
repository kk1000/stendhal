/* $Id$ */

/***************************************************************************
 *                      (C) Copyright 2003 - Marauroa                      *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
/*
 * MapRenderer.java
 *
 * Created on 13. Oktober 2005, 18:24
 *
 */
package games.stendhal.tools;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;

import tiled.core.Map;
import tiled.core.MapLayer;
import tiled.plugins.tiled.XMLMapTransformer;
import tiled.view.MapView;
import tiled.view.OrthoMapView;

/**
 * Renders Stendhal maps from *.tmx into PNG files of the same base name. This class can be started
 * from the command line or through an ant task.
 * 
 * @author mtotz
 */
public class MapRenderer extends Task {
	private String imagePath;

	/** list of *.tmx files to convert. */
	private List<FileSet> filesets = new ArrayList<FileSet>();

	/** converts the map files. 
	 * @param tmxFile 
	 * @throws Exception */
	public void convert(String tmxFile) throws Exception {
		File file = new File(tmxFile);

		String filename = file.getAbsolutePath();
		Map map = new XMLMapTransformer().readMap(filename);
		saveImageMap(map, tmxFile);
	}

	@SuppressWarnings("unchecked")
	private void saveImageMap(Map map, String tmxFile) {
		File file = new File(tmxFile);
		String filename = file.getAbsolutePath();
		for (MapLayer layer : map.getLayerList()) {
			if (layer.getName().equals("navigation")
					|| layer.getName().equals("collision")
					|| layer.getName().equals("objects")
					|| layer.getName().equals("protection")) {
				layer.setVisible(false);
			} else {
				layer.setVisible(true);
			}
		}

		MapView myView = new OrthoMapView();
		myView.setMap(map);
		myView.setScale(0.0625);
		Dimension d = myView.getSize();
		BufferedImage i = new BufferedImage(d.width, d.height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = i.createGraphics();
		g.setClip(0, 0, d.width, d.height);
		myView.draw(g);
		g.dispose();

		String area = file.getParentFile().getName();
		String level;
		String fileContainer = file.getParentFile().getParent();

		if (fileContainer.contains("Level ")) {
			level = fileContainer.split("Level ")[1];
		} else {
			level = "int";
		}

		if (level.equals("int") && area.equals("abstract")) {
			filename = imagePath + File.separator + level.replace("-", "sub_")
					+ "_" + file.getName().replaceAll("\\.tmx", ".png");
		} else {
			filename = imagePath + File.separator + level.replace("-", "sub_")
					+ "_" + area + "_"
					+ file.getName().replaceAll("\\.tmx", ".png");
		}

		try {
			ImageIO.write(i, "png", new File(filename));
		} catch (java.io.IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Adds a set of files to copy.
	 * 
	 * @param set
	 *            a set of files to copy
	 */
	public void addFileset(FileSet set) {
		filesets.add(set);
	}

	/**
	 * The setter for the "stendPath" attribute.
	 * @param imagePath 
	 */
	public void setImagePath(String imagePath) {
		this.imagePath = imagePath;
	}

	/**
	 * ants execute method.
	 */
	@Override
	public void execute() {
		try {
			for (FileSet fileset : filesets) {
				DirectoryScanner ds = fileset.getDirectoryScanner(getProject());
				String[] includedFiles = ds.getIncludedFiles();
				for (String filename : includedFiles) {
					System.out.println(ds.getBasedir().getAbsolutePath()
							+ File.separator + filename);
					convert(ds.getBasedir().getAbsolutePath() + File.separator
							+ filename);
				}
			}
		} catch (Exception e) {
			throw new BuildException(e);
		}
	}

	public static void main(String[] args) throws Exception {
		if (args.length < 1) {
			System.out.println("usage: java games.stendhal.tools.MapRenderer <tmx file>");
			return;
		}

		// do the job
		MapRenderer renderet = new MapRenderer();
		renderet.imagePath = args[1];
		renderet.convert(args[0]);
	}

}
