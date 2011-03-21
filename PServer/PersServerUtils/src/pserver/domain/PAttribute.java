/*
 * 
 */
package pserver.domain;

/**
 *
 * @author alexm
 */
public class PAttribute {

    private String name;
    private String value;
    private String defValue;

    public PAttribute() {
        name = null;
        value = defValue = null;        
    }

    public PAttribute( String name, String value ) {
        this.name = name;
        this.value = defValue = value;
    }

    public String getName() {
        return name;
    }

    public void setName( String name ) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue( String value ) {
        this.value = value;
    }

    public String getDefValue() {
        return defValue;
    }

    public void setDefValue( String defValue ) {
        this.defValue = defValue;
    }
}
