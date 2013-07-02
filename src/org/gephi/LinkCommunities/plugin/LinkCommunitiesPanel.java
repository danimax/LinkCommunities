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

import java.awt.Dimension;
import java.awt.Font;
import javax.swing.SwingConstants;
import java.awt.GridLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.EventListenerList;
import javax.swing.plaf.ComponentUI;
import org.jdesktop.swingx.JXHeader;


class LinkCommunitiesPanel extends JPanel {

    private JXHeader jXHeader1;
    private JXHeader jXHeader2;
    private JXHeader jXHeader3;
    private JTextField threshold;
    private JLabel labelTh;
    private JPanel panel2;

    public LinkCommunitiesPanel() {
        initComponents();
        //Disable directed if the graph is undirecteds
    }

    public EventListenerList getListenerList() {
        return listenerList;
    }

    public void setListenerList(EventListenerList listenerList) {
        this.listenerList = listenerList;
    }

    public ComponentUI getUi() {
        return ui;
    }

    public void setUi(ComponentUI ui) {
        this.ui = ui;
    }

    public double isSet() {
        double value;
        try {
            value = Double.parseDouble(threshold.getText());
        } catch (NumberFormatException e) {
            return 0.5;
        }
        if (value >= 0.0 && value <= 1.0) {
            return value;
        }
        return 0.5;
    }

    /* public void setThreshold(double soglia){
    threshold = new JTextField(""+soglia,2);
    }
     */
    private void initComponents() {

        jXHeader1 = new JXHeader();
        jXHeader2 = new JXHeader();
        panel2 = new JPanel();

        jXHeader1.setDescription("Reference: Link communities reveal multiscale complexity in networks\n"
                + "Yong-Yeol Ahn, James P. Bagrow & Sune Lehmann\n"
                + "implemented by Massimiliano Vella & Danilo Domenino");
        jXHeader1.setTitle("Link Communities");
        jXHeader2.setDescription("This algorithm is suited for undirected network. the threshold must be a value between 0.0 and 1.0.\nThe default value is 0.5.");
        jXHeader2.setTitle("Description");

        threshold = new JTextField();
        threshold.setText("0.50");
        threshold.setAlignmentX(SwingConstants.CENTER);
        threshold.setHorizontalAlignment(JTextField.CENTER);
        threshold.setPreferredSize(new Dimension(50, 20));

        labelTh = new JLabel("Threshold");
        labelTh.setHorizontalAlignment(SwingConstants.LEFT);
        labelTh.setFont(new Font("Sans Serif", Font.BOLD, 11));

        panel2.add(labelTh);
        panel2.add(threshold);

        this.add(jXHeader1);
        this.add(jXHeader2);
        this.add(panel2, SwingConstants.LEFT);

        GridLayout experimentLayout = new GridLayout(3, 1);
        this.setLayout(experimentLayout);

    }
}
