package games.stendhal.client.entity;

import java.awt.geom.Rectangle2D;

import marauroa.common.game.RPObject;
import marauroa.common.game.RPSlot;

public interface IEntity {

	/**
	 * Animated property.
	 */
	Property PROP_ANIMATED = new Property();
	/**
	 * Entity class/subclass property.
	 */
	Property PROP_CLASS = new Property();
	/**
	 * Name property.
	 */
	Property PROP_NAME = new Property();
	/**
	 * Position property.
	 */
	Property PROP_POSITION = new Property();
	/**
	 * Size property.
	 */
	Property PROP_SIZE = new Property();
	/**
	 * Title property.
	 */
	Property PROP_TITLE = new Property();
	/**
	 * Type property.
	 */
	Property PROP_TYPE = new Property();
	/**
	 * Visibility property.
	 */
	Property PROP_VISIBILITY = new Property();

	/**
	 * Add a change listener.
	 * 
	 * @param listener
	 *            The listener.
	 */
	void addChangeListener(final EntityChangeListener listener);

	/**
	 * Get the area the entity occupies.
	 * 
	 * @return A rectange (in world coordinate units).
	 */
	Rectangle2D getArea();

	/**
	 * Get the entity visibility.
	 * 
	 * @return The entity visibility (0 - 100).
	 */
	int getVisibility();

	/**
	 * Get the entity height.
	 * 
	 * @return The height.
	 */
	double getHeight();

	/** @return the represented arianne object id. */
	RPObject.ID getID();

	/**
	 * Get the entity class.
	 * 
	 * @return The entity class.
	 */
	String getEntityClass();

	/**
	 * Get the name.
	 * 
	 * @return The name.
	 */
	String getName();

	/**
	 * Get the entity sub-class.
	 * 
	 * @return The entity sub-class.
	 */
	String getEntitySubClass();

	/**
	 * Get the nicely formatted entity title.
	 * 
	 * This searches the follow attribute order: title, name (w/o underscore),
	 * type (w/o underscore).
	 * 
	 * @return The title, or <code>null</code> if unknown.
	 */
	String getTitle();

	/**
	 * Get the entity type.
	 * 
	 * @return The type.
	 */
	String getType();

	/**
	 * Get the X coordinate.
	 * 
	 * @return The X coordinate.
	 */
	double getX();

	/**
	 * Get the Y coordinate.
	 * 
	 * @return The Y coordinate.
	 */
	double getY();

	/**
	 * Get the RPObject this represents.
	 * 
	 * @return The RPObject.
	 */
	RPObject getRPObject();

	/**
	 * Get the entity width.
	 * 
	 * @return The width.
	 */
	double getWidth();

	/**
	 * Determine if this entity is on the ground.
	 * 
	 * @return <code>true</code> if the entity is on the ground.
	 */
	boolean isOnGround();

	/**
	 * @return the absolute world area (coordinates) to which audibility of
	 * entity sounds is confined. Returns <b>null</b> if confines do not exist
	 * (audible everywhere).
	 */
	Rectangle2D getAudibleArea();

	/**
	 * Sets the audible range as radius distance from this entity's position,
	 * expressed in coordinate units. This reflects an abstract capacity of this
	 * unit to emit sounds and influences the result of
	 * <code>getAudibleArea()</code>.
	 * 
	 * @param range
	 *            double audibility area radius in coordinate units
	 */
	void setAudibleRange(final double range);

	/**
	 * Get the resistance this has on other entities (0-100).
	 * 
	 * @return The resistance.
	 */
	int getResistance();

	/**
	 * Get the amount of resistance between this and another entity (0-100).
	 * 
	 * @param entity
	 *            The entity to check against.
	 * 
	 * @return The effective resistance.
	 */
	int getResistance(final IEntity entity);

	/**
	 * Gets the slot specified by name.
	 * @param name of the slot
	 * 
	 * @return    the specified slot or <code>null</code> if the entity does not
	 * have this slot
	 */
	RPSlot getSlot(final String name);

	/**
	 * Initialize this entity for an object.
	 * 
	 * @param object
	 *            The object.
	 * 
	 * @see #release()
	 */
	void initialize(final RPObject object);

	/**
	 * Determine if this is an obstacle for another entity.
	 * 
	 * @param entity
	 *            The entity to check against.
	 * 
	 * @return <code>true</code> the entity can not enter this entity's area.
	 */
	boolean isObstacle(final IEntity entity);

	/**
	 * Release this entity. This should clean anything that isn't automatically
	 * released (such as unregister callbacks, cancel external operations, etc).
	 * 
	 * @see #initialize(RPObject)
	 */
	void release();

	/**
	 * Remove a change listener.
	 * 
	 * @param listener
	 *            The listener.
	 */
	void removeChangeListener(final EntityChangeListener listener);

	/**
	 * Update cycle.
	 * 
	 * @param delta
	 *            The time (in ms) since last call.
	 */
	void update(final int delta);

}
