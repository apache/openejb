package transactiontests;

import java.io.Serializable;

public class BMPEntityPK implements java.io.Serializable {
    public  java.lang.Integer _id;

    public BMPEntityPK(java.lang.Integer id) {
        this._id = id;
    }

    public BMPEntityPK() {
    }

    public int hashCode() {
        return _id.hashCode();
    }

    public boolean equals(Object obj) {
        return _id.equals(obj);
    }
    
    public String toString() {
        return _id.toString();
    }
}