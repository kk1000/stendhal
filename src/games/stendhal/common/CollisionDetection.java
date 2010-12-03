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
package games.stendhal.common;

import games.stendhal.tools.tiled.LayerDefinition;

import java.awt.geom.Rectangle2D;

/**
 * This class loads the map and allow you to determine if a player collides or
 * not with any of the non trespasable areas of the world.
 */
public class CollisionDetection {
	CollisionMap map;

	private int width;

	private int height;

	/**
	 * Clear the collision map.
	 */
	public void clear() {
		if (map == null) {
			map = new CollisionMap(width, height);
		}
	}

	/**
	 * Initialize the collision map to desired size.
	 * 
	 * @param width width of the map
	 * @param height height of the map
	 */
	public void init(final int width, final int height) {
		if (this.width != width || this.height != height) {
			map = null;
		}
		
		this.width = width;
		this.height = height;
		
		clear();
	}

	/**
	 * Set a position in the collision map to static collision.
	 * 
	 * @param x x coordinate
	 * @param y y coordinate
	 */
	public void setCollide(final int x, final int y) {
		if ((x < 0) || (x >= width) || (y < 0) || (y >= height)) {
			return;
		}
		map.set(x, y);
	}
	
	/**
	 * Fill the collision map from layer data.
	 * 
	 * @param collisionLayer
	 */
	public void setCollisionData(final LayerDefinition collisionLayer) {
		/* First we build the int array. */
		collisionLayer.build();
		init(collisionLayer.getWidth(), collisionLayer.getHeight());
		
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				/*
				 * NOTE: Right now our collision detection system is binary, so
				 * something or is blocked or is not.
				 */
				boolean b = collisionLayer.getTileAt(x, y) != 0;
				if (b) {
					map.set(x, y);
				}		
			}
		}
	}

	/**
	 * Print the area around the (x,y) useful for debugging.
	 *  
	 * @param x 
	 * @param y 
	 * @param size
	 */
	public void printaround(final int x, final int y, final int size) {
		for (int j = y - size; j < y + size; j++) {
			for (int i = x - size; i < x + size; i++) {
				if ((j >= 0) && (j < height) && (i >= 0) && (i < width)) {
					if ((j == y) && (i == x)) {
						System.out.print("O");
					} else if (map.get(i, j)) {
						System.out.print("X");
					} else {
						System.out.print(".");
					}
				}
			}
			System.out.println();
		}
	}

	/**
	 * Check if a rectangle is at least partially outside the map.
	 * 
	 * @param shape area to be checked
	 * @return <code>true</code> if shape is at least partially outside the map,
	 * 	<code>false</code> otherwise
	 */
	public boolean leavesZone(final Rectangle2D shape) {
		final double x = shape.getX();
		final double y = shape.getY();
		final double w = shape.getWidth();
		final double h = shape.getHeight();

		if ((x < 0) || (x + w > width)) {
			return true;
		}

		if ((y < 0) || (y + h > height)) {
			return true;
		}

		return false;
	}

	/**
	 * Check if a rectangle overlaps colliding areas.
	 * 
	 * @param shape checked area
	 * @return <code>true</code> if the shape enters in any of the non
	 *	trespassable areas of the map, <code>false</code> otherwise
	 */
	public boolean collides(final Rectangle2D shape) {
		final double x = shape.getX();
		final double y = shape.getY();
		final double w = shape.getWidth();
		final double h = shape.getHeight();
		if ((x < 0) || (x + w > width)) {
			return true;
		}

		if ((y < 0) || (y + h > height)) {
			return true;
		}

		final double startx;
		if (x >= 0) {
			startx = x;
		} else {
			startx = 0;
		}
		final double endx;
		if (x + w < width) {
			endx = x + w;
		} else {
			endx = width;
		}
		final double starty;
		if (y >= 0) {
			starty = y;
		} else {
			starty = 0;
		}
		final double endy;
		if (y + h < height) {
			endy = y + h;
		} else {
			endy = height;
		}

		/*
		 * TODO: the advantages from using bitset in collissionmap are lost here
		 * the weird behaviour with client side player when using map.collides (x,y,width, height)
		 * is caused by the delta calculation for 2dviewtiles; 
		 */
		
		for (int k = (int) starty; k < endy; k++) {
			
			for (int i = (int) startx; i < endx; i++) {
				if (map.get(i, k)) {
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * Check if a location is marked with collision.
	 * 
	 * @param x x coordinate
	 * @param y y coordinate
	 * @return <code>true</code> if the map position is a collision tile,
	 * 	otherwise <code>false</code> 
	 */
	public boolean collides(final int x, final int y) {
		if ((x < 0) || (x >= width)) {
			return true;
		}

		if ((y < 0) || (y >= height)) {
			return true;
		}
		return map.get(x, y);
	}

	/**
	 * Get the width of the collision map.
	 * 
	 * @return width
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * Get the height of the collision map.
	 * 
	 * @return height
	 */
	public int getHeight() {
		return height;
	}
}
