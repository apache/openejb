
package com.titan.cabin;

import javax.ejb.EntityContext;


public class CabinBean implements javax.ejb.EntityBean {

    public Integer id;
    public String name;
    public int deckLevel;
    public int shipId;
    public int bedCount;

    public Integer ejbCreate( Integer id ) {
	setId( id );
	return null;
    }

    public void ejbPostCreate( Integer id ) {
	// do nothing
    }

    public String getName() {
	return name;
    }

    public void setName( String str ) {
	name = str;
    }

    public int getDeckLevel() {
	return deckLevel;
    }

    public void setDeckLevel( int level ) {
	deckLevel = level;
    }

    public int getShipId() {
	return shipId;
    }

    public void setShipId( int sp ) {
	shipId = sp;
    }

    public int getBedCount() {
	return bedCount;
    }

    public void setBedCount( int bc ) {
	bedCount = bc;
    }

    public Integer getId() {
	return id;
    }

    public void setId( Integer id ) {
	this.id = id;
    }

    public void setEntityContext( EntityContext ctx ) {
	// not implemented
    }

    public void unsetEntityContext() {
	// not implemented
    }

    public void ejbActivate() {
	// not implemented
    }

    public void ejbPassivate() {
	// not implemented
    }

    public void ejbLoad() {
	// not implemented
    }

    public void ejbStore() {
	// not implemented
    }

    public void ejbRemove() {
	// not implemented
    }
}
