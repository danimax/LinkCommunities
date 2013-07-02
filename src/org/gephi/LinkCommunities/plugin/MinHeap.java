/*
Copyright 2008-2013 Gephi
Authors : Danilo Domenino <danilodomenino@yahoo.it>, Massimiliano Vella <vella.massi@gmail.com>
Website : http://www.gephi.org

This file is part of Gephi.

DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

Copyright 2012 Gephi Consortium. All rights reserved.

The contents of this file are subject to the terms of either the GNU
General Public License Version 3 only ("GPL") or the Common
Development and Distribution License("CDDL") (collectively, the
"License"). You may not use this file except in compliance with the
License. You can obtain a copy of the License at
http://gephi.org/about/legal/license-notice/
or /cddl-1.0.txt and /gpl-3.0.txt. See the License for the
specific language governing permissions and limitations under the
License.  When distributing the software, include this License Header
Notice in each file and include the License files at
/cddl-1.0.txt and /gpl-3.0.txt. If applicable, add the following below the
License Header, with the fields enclosed by brackets [] replaced by
your own identifying information:
"Portions Copyrighted [year] [name of copyright owner]"

If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 3, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 3] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 3 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 3 code and therefore, elected the GPL
Version 3 license, then the option applies only if the new code is
made subject to such option by the copyright holder.

Contributor(s):

 Portions Copyrighted 2011 Gephi Consortium.
 */

package org.gephi.LinkCommunities.plugin;

import java.util.ArrayList;
import java.util.List;


public final class MinHeap<E extends Comparable<E>> {

    List<E> h = new ArrayList<E>();

    public MinHeap() {
    }

    public MinHeap(List<E> h) {
        this.h = h;
        for (int k = h.size() / 2 - 1; k >= 0; k--) {
            percolateDown(k, h.get(k));
        }
    }

    public void add(E node) {
        h.add(null);
        int k = h.size() - 1;
        while (k > 0) {
            int parent = (k - 1) / 2;
            E p = h.get(parent);
            if (node.compareTo(p) >= 0) {
                break;
            }
            h.set(k, p);
            k = parent;
        }
        h.set(k, node);
    }

    public E remove() {
        E removedNode = h.get(0);
        E lastNode = h.remove(h.size() - 1);
        percolateDown(0, lastNode);
        return removedNode;
    }

    public E min() {
        return h.get(0);
    }

    public boolean isEmpty() {
        return h.isEmpty();
    }

    void percolateDown(int k, E node) {
        if (h.isEmpty()) {
            return;
        }
        while (k < h.size() / 2) {
            int child = 2 * k + 1;
            if (child < h.size() - 1 && h.get(child).compareTo(h.get(child + 1)) > 0) {
                child++;
            }
            if (node.compareTo(h.get(child)) <= 0) {
                break;
            }
            h.set(k, h.get(child));
            k = child;
        }
        h.set(k, node);
    }
    /*
    // Usage example
    public static void main(String[] args) {
    MinHeap<Integer> heap = new MinHeap<Integer>(new Integer[] { 2, 5, 1, 3 });
    // print keys in sorted order
    while (!heap.isEmpty()) {
    System.out.println(heap.remove());
    }
    }
    
     */
}
