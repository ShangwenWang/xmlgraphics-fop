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

package org.apache.fop.fo;

import org.apache.fop.fo.properties.CommonBorderAndPadding;
import org.apache.fop.util.CharUtilities;
import java.util.NoSuchElementException;


public class InlineCharIterator extends RecursiveCharIterator {
    private boolean bStartBoundary = false;
    private boolean bEndBoundary = false;

    /**
     * @param fobj the object for whose character contents and for whose
     * descendant's character contents should be iterated
     * @param bap the CommonBorderAndPadding properties to be applied
     */
    public InlineCharIterator(FObj fobj, CommonBorderAndPadding bap) {
        super(fobj);
        checkBoundaries(bap);
    }


    private void checkBoundaries(CommonBorderAndPadding bap) {
        bStartBoundary = (bap.getBorderStartWidth(false) > 0
                       || bap.getPaddingStart(false) > 0);
        bEndBoundary = (bap.getBorderEndWidth(false) > 0
                     || bap.getPaddingEnd(false) > 0);
    }

    /**
     * @return true if there are more characters
     */
    public boolean hasNext() {
        if (bStartBoundary) {
            return true;
        }
        return (super.hasNext() || bEndBoundary);
        /* If super.hasNext() returns false,
         * we return true if we are going to return a "boundary" signal
         * else false.
         */
    }

    /**
     * @return the next character
     * @throws NoSuchElementException if there are no more characters
     */
    public char nextChar() throws NoSuchElementException {
        if (bStartBoundary) {
            bStartBoundary = false;
            return CharUtilities.CODE_EOT;
        }
        try {
            return super.nextChar();
        } catch (NoSuchElementException e) {
            // Underlying has nothing more to return
            // Check end boundary char
            if (bEndBoundary) {
                bEndBoundary = false;
                return CharUtilities.CODE_EOT;
            } else {
                throw e;
            }
        }
    }
}

