/**
 * EndReason.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis WSDL2Java emitter.
 */

package com.tenfen.www.webservice.chinatelecom.schema.ctcc.common;

public class EndReason implements java.io.Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = -7818700797334685162L;
	private java.lang.String _value_;
    private static java.util.HashMap _table_ = new java.util.HashMap();

    // Constructor
    protected EndReason(java.lang.String value) {
        _value_ = value;
        _table_.put(_value_,this);
    }

    public static final java.lang.String _value1 = "0";
    public static final java.lang.String _value2 = "-1";
    public static final java.lang.String _value3 = "1";
    public static final java.lang.String _value4 = "2";
    public static final EndReason value1 = new EndReason(_value1);
    public static final EndReason value2 = new EndReason(_value2);
    public static final EndReason value3 = new EndReason(_value3);
    public static final EndReason value4 = new EndReason(_value4);
    public java.lang.String getValue() { return _value_;}
    public static EndReason fromValue(java.lang.String value)
          throws java.lang.IllegalStateException {
        EndReason enum1 = (EndReason)
            _table_.get(value);
        if (enum1==null) throw new java.lang.IllegalStateException();
        return enum1;
    }
    public static EndReason fromString(java.lang.String value)
          throws java.lang.IllegalStateException {
        return fromValue(value);
    }
    @Override
	public boolean equals(java.lang.Object obj) {return (obj == this);}
    @Override
	public int hashCode() { return toString().hashCode();}
    @Override
	public java.lang.String toString() { return _value_;}
    public java.lang.Object readResolve() throws java.io.ObjectStreamException { return fromValue(_value_);}
}
