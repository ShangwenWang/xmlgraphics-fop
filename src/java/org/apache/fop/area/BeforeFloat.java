/*
 * Copyright 1999-2004 The Apache Software Foundation.
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
 */

/* $Id$ */
 
package org.apache.fop.area;

/**
 * The before float area.
 * This is used to place the before float areas.
 * It has an optional separator and before float block children.
 */
public class BeforeFloat extends BlockParent {
    // this is an optional block area that will be rendered
    // as the separator only if there are float areas
    private Block separator = null;

    /**
     * Set the separator area for this before float.
     *
     * @param sep the before float separator area
     */
    public void setSeparator(Block sep) {
        separator = sep;
    }

    /**
     * Get the separator area for this before float.
     *
     * @return the before float separator area
     */
    public Block getSeparator() {
        return separator;
    }

    /**
     * Get the height of this before float.
     * It gets the height of the children and if there is a
     * separator its height is also added.
     *
     * @return the height of the before float including separator
     */
    public int getHeight() {
        int h = super.getHeight();
        if (separator != null) {
            h += separator.getHeight();
        }
        return h;
    }

}

