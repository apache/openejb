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
package org.openejb.ui.jedi.openejb11.ejb.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;

/**
 * GUI for editing EJB properties.
 *
 * @author Aaron Mulder (ammulder@alumni.princeton.edu)
 * @version $Revision$
 */
public class BeanEditor extends JComponent {
    GridBagLayout gridBagLayout1 = new GridBagLayout();
    JLabel jLabel1 = new JLabel();
    JTextField tfEjbName = new JTextField();

    private boolean modified = false;
    JLabel jLabel3 = new JLabel();
    JComboBox cbConfig = new JComboBox();

    public BeanEditor() {
        try {
            jbInit();
        } catch(Exception e) {
            e.printStackTrace();
        }
        initializeEvents();
    }

    private void jbInit() throws Exception {
        jLabel1.setText("EJB Name:");
        this.setLayout(gridBagLayout1);
        tfEjbName.setOpaque(false);
        tfEjbName.setEditable(false);
        jLabel3.setText("Container:");
        cbConfig.setEditable(true);
        this.add(jLabel1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        this.add(tfEjbName, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
        this.add(jLabel3, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        this.add(cbConfig, new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
    }

    private void initializeEvents() {
        cbConfig.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                modified = true;
            }
        });
    }

    public void stopEditing() {
        Object item  = cbConfig.getEditor().getItem();
        if(item != null && !item.equals(cbConfig.getSelectedItem()) &&
                  !(item.equals("") && cbConfig.getSelectedItem() == null)) {
            cbConfig.setSelectedItem(item);
        }
    }

    public void clearModified() {
        modified = false;
    }

    public boolean isModified() {
        if(modified) {
            return true;
        }
        Object item  = cbConfig.getEditor().getItem();
        if(item != null && !item.equals(cbConfig.getSelectedItem()) &&
                  !(item.equals("") && cbConfig.getSelectedItem() == null)) {
            return true;
        }
        return false;
    }

    public void setEjbName(String name) {
        if(name == null)
            name = "";
        String old = getEJBName();
        tfEjbName.setText(name);
    }
    public String getEJBName() {
        String s = tfEjbName.getText();
        if(s == null)
            return "";
        else
            return s;
    }

    public void setContainerOptions(String[] options) {
        cbConfig.setModel(new DefaultComboBoxModel(options));
    }
    public void setContainer(String config) {
        if(config == null)
            cbConfig.setSelectedItem("");
        else
            cbConfig.setSelectedItem(config);
    }
    public String getContainer() {
        String result = (String)cbConfig.getSelectedItem();
        if(result == null)
            return "";
        return result;
    }
}
