package io.metersphere.log.utils.diff.json;

/*
 Copyright (c) 2009, incava.org
 All rights reserved.

 Redistribution and use in source and binary forms, with or without modification,
 are permitted provided that the following conditions are met:

 * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.

 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.

 * Neither the name of incava.org nor the names of its contributors may be
 * used to endorse or promote products derived from this software without
 * specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

/**
 * Represents a difference, as used in <code>Diff</code>. A difference consists of two pairs of starting and ending
 * points, each pair representing either the "from" or the "to" collection passed to <code>Diff</code>. If an ending
 * point is -1, then the difference was either a deletion or an addition. For example, if <code>getDeletedEnd()</code>
 * returns -1, then the difference represents an addition.
 */
public class IncavaEntry
{

    public static final int NONE = -1;

    /**
     * The point at which the deletion starts.
     */
    private int delStart = NONE;

    /**
     * The point at which the deletion ends.
     */
    private int delEnd = NONE;

    /**
     * The point at which the addition starts.
     */
    private int addStart = NONE;

    /**
     * The point at which the addition ends.
     */
    private int addEnd = NONE;


    /**
     * Creates the difference for the given start and end points for the deletion and addition.
     */
    public IncavaEntry(int delStart, int delEnd, int addStart, int addEnd)
    {
        this.delStart = delStart;
        this.delEnd = delEnd;
        this.addStart = addStart;
        this.addEnd = addEnd;
    }


    /**
     * The point at which the deletion starts, if any. A value equal to <code>NONE</code> means this is an addition.
     */
    public int getDeletedStart()
    {
        return delStart;
    }


    /**
     * The point at which the deletion ends, if any. A value equal to <code>NONE</code> means this is an addition.
     */
    public int getDeletedEnd()
    {
        return delEnd;
    }


    /**
     * The point at which the addition starts, if any. A value equal to <code>NONE</code> means this must be an
     * addition.
     */
    public int getAddedStart()
    {
        return addStart;
    }


    /**
     * The point at which the addition ends, if any. A value equal to <code>NONE</code> means this must be an addition.
     */
    public int getAddedEnd()
    {
        return addEnd;
    }


    /**
     * Sets the point as deleted. The start and end points will be modified to include the given line.
     */
    public void setDeleted(int line)
    {
        delStart = Math.min(line, delStart);
        delEnd = Math.max(line, delEnd);
    }


    /**
     * Sets the point as added. The start and end points will be modified to include the given line.
     */
    public void setAdded(int line)
    {
        addStart = Math.min(line, addStart);
        addEnd = Math.max(line, addEnd);
    }


    /**
     * Compares this object to the other for equality. Both objects must be of type Difference, with the same starting
     * and ending points.
     */
    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof IncavaEntry) {
            IncavaEntry other = (IncavaEntry) obj;

            return (delStart == other.delStart &&
                    delEnd == other.delEnd &&
                    addStart == other.addStart && addEnd == other.addEnd);
        }
        else {
            return false;
        }
    }


    /**
     * Returns a string representation of this difference.
     */
    @Override
    public String toString()
    {
        StringBuffer buf = new StringBuffer();
        buf.append("del: [" + delStart + ", " + delEnd + "]");
        buf.append(" ");
        buf.append("add: [" + addStart + ", " + addEnd + "]");
        return buf.toString();
    }

}
