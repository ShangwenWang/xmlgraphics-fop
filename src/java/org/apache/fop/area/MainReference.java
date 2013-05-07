/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

import java.util.ArrayList;
import java.util.List;

import org.apache.fop.traits.WritingModeTraitsGetter;

/**
 * The main-reference-area generated by an fo:region-body
 * This object holds one or more span-reference-areas (block-areas
 * stacked in the block progression direction)
 * See fo:region-body definition in the XSL Rec for more information.
 */
public class MainReference extends Area {

    private static final long serialVersionUID = 7635126485620012448L;

    private BodyRegion parent;
    private List<Span> spanAreas = new java.util.ArrayList<Span>();
    private boolean isEmpty = true;

    /**
     * Constructor
     *
     * @param parent the body region this reference area is placed in.
     */
    public MainReference(BodyRegion parent) {
        this.parent = parent;
        addTrait(Trait.IS_REFERENCE_AREA, Boolean.TRUE);
    }

    /**
     * Add a span area to this area.
     *
     * @param spanAll whether to make a single-column span
     * @return the created span area.
     */
    public Span createSpan(boolean spanAll) {
        if (spanAreas.size() > 0 && getCurrentSpan().isEmpty()) {
            //Remove the current one if it is empty
            spanAreas.remove(spanAreas.size() - 1);
        }
        RegionViewport rv = parent.getRegionViewport();
        int ipdWidth = parent.getIPD()
            - rv.getBorderAndPaddingWidthStart() - rv.getBorderAndPaddingWidthEnd();

        Span newSpan = new Span(((spanAll) ? 1 : getColumnCount()),
                getColumnGap(), ipdWidth);
        spanAreas.add(newSpan);
        return getCurrentSpan();
    }

    /**
     * Get the span areas from this area.
     *
     * @return the list of span areas
     */
    public List<Span> getSpans() {
        return spanAreas;
    }

    /**
     * Do not use. Used to handle special page-master for last page: transfer the content
     * that had already been added to a normal page to this main reference for the last
     * page. TODO this is hacky.
     *
     * @param spans content already laid out
     */
    public void setSpans(List<Span> spans) {
        spanAreas = new ArrayList<Span>(spans);
    }

    /**
     * Get the span area currently being filled (i.e., the last span created).
     * @return the active span.
     */
    public Span getCurrentSpan() {
        return spanAreas.get(spanAreas.size() - 1);
    }

    /**
     * Indicates whether any child areas have been added to this reference area.
     *
     * This is achieved by looping through each span.
     * @return true if no child areas have been added yet.
     */
    public boolean isEmpty() {
        if (isEmpty && spanAreas != null) {
            for (Span spanArea : spanAreas) {
                if (!spanArea.isEmpty()) {
                    isEmpty = false;
                    break;
                }
            }
        }
        return isEmpty;
    }

    /** @return the number of columns */
    public int getColumnCount() {
        return parent.getColumnCount();
    }

    /** @return the column gap in millipoints */
    public int getColumnGap() {
        return parent.getColumnGap();
    }

    /**
     * Sets the writing mode traits for the spans of this main
     * reference area.
     * @param wmtg a WM traits getter
     */
    public void setWritingModeTraits(WritingModeTraitsGetter wmtg) {
        for (Span s : getSpans()) {
            s.setWritingModeTraits(wmtg);
        }
    }

}

