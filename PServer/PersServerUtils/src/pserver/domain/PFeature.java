/* 
 * Copyright 2011 NCSR "Demokritos"
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");   
 * you may not use this file except in compliance with the License.   
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *    
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
*/
package pserver.domain;

/**
 *
 * @author alexm
 */
public class PFeature {

    private String name;
    private String strValue;
    private String strDefValue;
    private float value;
    private float defValue;

    public PFeature() {
        this.name = null;
        this.value = this.defValue = 0.0f;
        this.strValue = "" + value;
        this.strDefValue = "" + this.defValue;
    }

    public PFeature( String name, String value, String defValue ) {
        this.name = name;
        this.setStrValue( value );
        this.setStrDefValue( defValue );
    }
    
    public String getName() {
        return name;
    }

    public void setName( String name ) {
        this.name = name;
    }

    public float getValue() {
        return value;
    }

    public void setValue( float value ) {
        this.value = value;
        this.strValue = "" + value;
    }

    public float getDefValue() {
        return defValue;
    }

    public void setDefValue( float defValue ) {
        this.defValue = defValue;
    }

    public String getStrValue() {
        return strValue;
    }

    public void setStrValue( String strValue ) {
        this.strValue = strValue;
        try {
            this.value = Float.parseFloat( this.strValue );
        } catch ( NumberFormatException e ) {
            this.value = 0.0f;
        }
    }

    public String getStrDefValue() {
        return strDefValue;
    }

    public void setStrDefValue( String strDefValue ) {
        this.strDefValue = strDefValue;
        try {
            this.defValue = Float.parseFloat( this.strDefValue );
        } catch ( NumberFormatException e ) {
            this.defValue = 0.0f;
        }
    }
}
