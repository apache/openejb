/**
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright
 *    statements and notices.  Redistributions must also contain a
 *    copy of this document.
 *
 * 2. Redistributions in binary form must reproduce the
 *    above copyright notice, this list of conditions and the
 *    following disclaimer in the documentation and/or other
 *    materials provided with the distribution.
 *
 * 3. The name "Exolab" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of Exoffice Technologies.  For written permission,
 *    please contact info@exolab.org.
 *
 * 4. Products derived from this Software may not be called "Exolab"
 *    nor may "Exolab" appear in their names without prior written
 *    permission of Exoffice Technologies. Exolab is a registered
 *    trademark of Exoffice Technologies.
 *
 * 5. Due credit should be given to the Exolab Project
 *    (http://www.exolab.org/).
 *
 * THIS SOFTWARE IS PROVIDED BY EXOFFICE TECHNOLOGIES AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * EXOFFICE TECHNOLOGIES OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 1999 (C) Exoffice Technologies Inc. All Rights Reserved.
 *
 * $Id$
 */
package org.openejb.ui.jedi.openejb11.server.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;

import org.openejb.ui.jedi.openejb11.ejb.gui.MapEditor;

/**
 * The GUI for editing Intra-VM servre configuration.
 * @author Aaron Mulder (ammulder@alumni.princeton.edu)
 * @version $Revision$
 */
public class ServerEditor extends JComponent {
    GridBagLayout gridBagLayout1 = new GridBagLayout();
    JLabel jLabel1 = new JLabel();
    JLabel jLabel9 = new JLabel();
    JComboBox cbCodebase = new JComboBox();
    JLabel jLabel4 = new JLabel();
    MapEditor props = new MapEditor();

    private boolean modified = false;
    JComboBox cbClassName = new JComboBox();

    public ServerEditor() {
        try {
            jbInit();
        } catch(Exception e) {
            e.printStackTrace();
        }
        initializeEvents();
        setDefaults();
    }

    private void jbInit() throws Exception {
        this.setLayout(gridBagLayout1);
        jLabel1.setText("Proxy Factory Class:");
        jLabel9.setText("Proxy Codebase:");
        jLabel4.setText("Proxy Properties:");
        cbCodebase.setEditable(true);
        cbClassName.setEditable(true);
        this.add(jLabel1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        this.add(jLabel9, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        this.add(cbCodebase, new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
        this.add(jLabel4, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
            ,GridBagConstraints.NORTHEAST, GridBagConstraints.NONE, new Insets(2, 0, 0, 0), 0, 0));
        this.add(props, new GridBagConstraints(1, 2, 1, 1, 1.0, 0.2
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
        this.add(cbClassName, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
    }

    private void initializeEvents() {
        ActionListener al = new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                modified = true;
            }
        };
        cbCodebase.addActionListener(al);
        cbClassName.addActionListener(al);
        props.addActionListener(al);
    }

    private void setDefaults() {
        cbCodebase.setModel(new DefaultComboBoxModel(new String[]{
            "CLASSPATH"
        }));
        cbClassName.setModel(new DefaultComboBoxModel(new String[]{
            "org.openejb.util.proxy.jdk12.Jdk12ProxyFactory",
            "org.openejb.util.proxy.jdk13.Jdk13ProxyFactory",
            "org.openejb.util.proxy.DynamicProxyFactory"
        }));
    }

    public void stopEditing() {
        Object item  = cbClassName.getEditor().getItem();
        if(item != null && !item.equals(cbClassName.getSelectedItem()) &&
                  !(item.equals("") && cbClassName.getSelectedItem() == null)) {
            cbClassName.setSelectedItem(item);
        }
        item  = cbCodebase.getEditor().getItem();
        if(item != null && !item.equals(cbCodebase.getSelectedItem()) &&
                  !(item.equals("") && cbCodebase.getSelectedItem() == null)) {
            cbCodebase.setSelectedItem(item);
        }
    }

    public void clearModified() {
        modified = false;
    }

    public boolean isModified() {
        if(modified) {
            return true;
        }
        Object item  = cbClassName.getEditor().getItem();
        if(item != null && !item.equals(cbClassName.getSelectedItem()) &&
                  !(item.equals("") && cbClassName.getSelectedItem() == null)) {
            return true;
        }
        item  = cbCodebase.getEditor().getItem();
        if(item != null && !item.equals(cbCodebase.getSelectedItem()) &&
                  !(item.equals("") && cbCodebase.getSelectedItem() == null)) {
            return true;
        }
        return false;
    }

    public void setProxyFactoryClass(String className) {cbClassName.setSelectedItem(className);}
    public String getProxyFactoryClass() {return (String)cbClassName.getSelectedItem();}

    public void setProxyCodebase(String desc) {cbCodebase.setSelectedItem(desc);}
    public String getProxyCodebase() {return (String)cbCodebase.getSelectedItem();}

    public void setProxyMap(Map m) {props.setMap(m);}
    public Map getProxyMap() {return props.getMap();}
}
