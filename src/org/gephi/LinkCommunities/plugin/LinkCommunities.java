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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.graph.api.Node;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.data.attributes.api.AttributeOrigin;
import org.gephi.data.attributes.api.AttributeTable;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.HierarchicalUndirectedGraph;
import org.gephi.statistics.spi.Statistics;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;


public class LinkCommunities implements Statistics, LongTask {

    private String report = "";
    private boolean cancel = false;
    private ProgressTicket progressTicket;
    private double threshold;
    private double density;
    private double elapsedTime;

    @Override
    public void execute(GraphModel gm, AttributeModel am) {
        
        HierarchicalUndirectedGraph hdg = gm.getHierarchicalUndirectedGraph();
        List<EdgePair> heapList = new ArrayList<EdgePair>();
        AttributeTable edgeTable = am.getEdgeTable();
        AttributeColumn attrCol;
        hdg.readLock();
        if (edgeTable.getColumn("community") == null) {
            attrCol = edgeTable.addColumn("community", "Community", AttributeType.INT, AttributeOrigin.COMPUTED, 0);
        }

        Edge[] edges = hdg.getEdges().toArray();
        Node[] nodes = hdg.getNodes().toArray();

        ArrayList<HashSet<Edge>> edgesComm = new ArrayList<HashSet<Edge>>();
        ArrayList<HashSet<Node>> nodesComm = new ArrayList<HashSet<Node>>();
        SetTheory<Node> st = new SetTheory<Node>();
        SetTheory<Edge> ste = new SetTheory<Edge>();


        int cid = 0;

        //one edge for each community
        for (int i = 0; i < edges.length; i++) {
            edges[i].getEdgeData().getAttributes().setValue("community", cid);
            edgesComm.add(new HashSet<Edge>());
            edgesComm.get(cid).add(edges[i]);
            nodesComm.add(new HashSet<Node>());
            nodesComm.get(cid).add(edges[i].getSource());
            nodesComm.get(cid).add(edges[i].getTarget());
            cid++;
        }

        for (int i = 0; i < nodes.length; i++) {
            Edge[] adj = hdg.getEdgesAndMetaEdges(nodes[i]).toArray();
            int len = adj.length;

            if (len > 1) {
                //create the edges combinations
                //"[18,0]"-> nodes[i],adj[j]
                for (int j = 0; j < len - 1; j++) {
                    Node value1;
                    if (adj[j].getSource().equals(nodes[i])) {
                        value1 = adj[j].getTarget();
                    } else {
                        value1 = adj[j].getSource();
                    }
                    for (int k = j + 1; k < len; k++) {
                        Node value2;
                        if (adj[k].getSource().equals(nodes[i])) {
                            value2 = adj[k].getTarget();
                        } else {
                            value2 = adj[k].getSource();
                        }

                        Node[] ngbs1 = hdg.getNeighbors(value1).toArray();
                        Node[] ngbs2 = hdg.getNeighbors(value2).toArray();
                        Set<Node> a = new HashSet<Node>(Arrays.asList(ngbs1));
                        a.add(value1);
                        Set<Node> b = new HashSet<Node>(Arrays.asList(ngbs2));
                        b.add(value2);
                        Set<Node> u = new HashSet<Node>();
                        Set<Node> inter = new HashSet<Node>();
                        st.union(a, b, u);
                        st.intersection(a, b, inter);
                        double similarity = 1.0 * inter.size() / u.size();
                        EdgePair ep = new EdgePair(1 - similarity, adj[j], adj[k]);
                        heapList.add(ep);
                    }
                }
            }
        }
        //push in structure every EdgePair combinations  
        MinHeap<EdgePair> heap = new MinHeap<EdgePair>(heapList);

        double sim_prev = -1.0;
        double best_sim = 1.0;
        double best_dens = 0.0;
        Progress.start(progressTicket, hdg.getEdgeCount());
        int heap_count = 0;
        int count = 0;


        while (!heap.isEmpty()) {
            EdgePair temp = heap.remove();
            double sim = 1 - temp.getDistance();

            if (sim < threshold) {
                break;
            }
            if (sim != sim_prev) {
                if (density >= best_dens) {
                    best_dens = density;
                    best_sim = sim;
                }
                sim_prev = sim;
            }
            int cid1 = (Integer) (temp.getEdge1().getAttributes().getValue("community"));
            int cid2 = (Integer) (temp.getEdge2().getAttributes().getValue("community"));


            if (cid1 != cid2) {
                count++;
                int m1 = edgesComm.get(cid1).size();
                int m2 = edgesComm.get(cid2).size();
                int n1 = nodesComm.get(cid1).size();
                int n2 = nodesComm.get(cid2).size();


                //partition density for communities
                double d_cid1 = partitionDensityCommunity(m1, n1);
                double d_cid2 = partitionDensityCommunity(m2, n2);
                double d_cid12 = 0.0;

                if (m1 >= m2) {
                    HashSet<Edge> edgesTemp = new HashSet<Edge>();
                    ste.union(edgesComm.get(cid2), edgesComm.get(cid1), edgesTemp);
                    HashSet<Node> nodesTemp = new HashSet<Node>();
                    st.union(nodesComm.get(cid2), nodesComm.get(cid1), nodesTemp);
                    nodesComm.set(cid1, nodesTemp);
                    Iterator<Edge> it = edgesComm.get(cid2).iterator();
                    while (it.hasNext()) {
                        it.next().getEdgeData().getAttributes().setValue("community", cid1);
                    }
                    edgesComm.get(cid2).clear();
                    edgesComm.get(cid1).clear();
                    nodesComm.get(cid2).clear();

                    edgesComm.set(cid1, edgesTemp);


                    int m = edgesComm.get(cid1).size();
                    int n = nodesComm.get(cid1).size();
                    d_cid12 = partitionDensityCommunity(m, n);
                } else {
                    HashSet<Edge> edgesTemp = new HashSet<Edge>();
                    ste.union(edgesComm.get(cid2), edgesComm.get(cid1), edgesTemp);
                    HashSet<Node> nodesTemp = new HashSet<Node>();
                    st.union(nodesComm.get(cid2), nodesComm.get(cid1), nodesTemp);
                    nodesComm.set(cid2, nodesTemp);
                    Iterator<Edge> it = edgesComm.get(cid1).iterator();
                    while (it.hasNext()) {
                        it.next().getEdgeData().getAttributes().setValue("community", cid2);
                    }
                    edgesComm.get(cid2).clear();
                    edgesComm.get(cid1).clear();
                    nodesComm.get(cid1).clear();
                    edgesComm.set(cid2, edgesTemp);


                    int m = edgesComm.get(cid2).size();
                    int n = nodesComm.get(cid2).size();
                    d_cid12 = partitionDensityCommunity(m, n);
                }
                density = density + (d_cid12 - d_cid1 - d_cid2) * (2.0 / edges.length);
            }
            if (cancel) {
                break;
            }
            heap_count++;
            Progress.progress(progressTicket, heap_count);
        }
        
    }

    @Override
    public String getReport() {
        report = "<HTML> <BODY> <h1> Link Communities Report </h1> "
                + "<hr>"
                + "<br />" + "<h2> Parameters: </h2>"
                + "Network Interpretation: undirected <br />"
                + "Threshold: " + threshold + " <br />"
                + "<br>" + "<h2> Results: </h2>"
                + "Partition Density " + density + "<br />"
                + "<br/> <h2>Reference: </h2> <br/> "
                + "\"Yong-Yeol Ahn, James P. Bagrow & Sune Lehmann\" \"Link communities reveal multiscale complexity in networks\" 2010"
                + "</BODY> </HTML>";
        return report;
    }

    @Override
    public boolean cancel() {
        cancel = true;
        return cancel;
    }

    @Override
    public void setProgressTicket(ProgressTicket pt) {
        this.progressTicket = pt;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }

    public double partitionDensityCommunity(int m, int n) {
        if (n <= 2) {
            return 0.0;
        }
        return m * (m - n + 1.0) / (n - 2.0) / (n - 1.0);
    }
}
