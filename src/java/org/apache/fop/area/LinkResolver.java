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

// Java
import java.util.List;
import java.io.Serializable;

// FOP
import org.apache.fop.area.Trait;
import org.apache.fop.area.Resolveable;
import org.apache.fop.area.PageViewport;
import org.apache.fop.area.Area;

/**
 * Link resolving for resolving internal links.
 */
public class LinkResolver implements Resolveable, Serializable {
    private boolean resolved = false;
    private String idRef;
    private Area area;

    /**
     * Create a new link resolver.
     *
     * @param id the id to resolve
     * @param a the area that will have the link attribute
     */
    public LinkResolver(String id, Area a) {
        idRef = id;
        area = a;
    }

    /**
     * @return true if this link is resolved
     */
    public boolean isResolved() {
        return resolved;
    }

    public String[] getIDs() {
        return new String[] {idRef};
    }

    /**
     * Resolve by adding an internal link.
     */
    public void resolve(String id, List pages) {
        resolved = true;
        if (idRef.equals(id) && pages != null) {
            PageViewport page = (PageViewport)pages.get(0);
            area.addTrait(Trait.INTERNAL_LINK, page.getKey());
        }
    }
}
