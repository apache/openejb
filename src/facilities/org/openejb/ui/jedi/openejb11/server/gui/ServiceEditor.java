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
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.openejb.ui.jedi.openejb11.ejb.gui.MapEditor;

/**
 * The GUI for editing services such as security and transactions.
 * @author Aaron Mulder (ammulder@alumni.princeton.edu)
 * @version $Revision$
 */
public class ServiceEditor extends JComponent {
    GridBagLayout gridBagLayout1 = new GridBagLayout();
    JLabel jLabel1 = new JLabel();
    JLabel jLabel2 = new JLabel();
    JComboBox cbClassName = new JComboBox();
    MapEditor props = new MapEditor();
    JLabel jLabel3 = new JLabel();
    JTextField tfName = new JTextField();
    JLabel jLabel4 = new JLabel();
    JLabel jLabel5 = new JLabel();
    JTextField tfDisplayName = new JTextField();
    JTextField tfDescription = new JTextField();
    protected boolean modified = false;

    public ServiceEditor() {
        try {
            jbInit();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        initializeEvents();
    }

    private void jbInit() throws Exception {
        jLabel1.setText("Class Name:");
        this.setLayout(gridBagLayout1);
        jLabel2.setText("Properties:");
        cbClassName.setEditable(true);
        jLabel3.setText("Service Name:");
        jLabel4.setText("Description:");
        jLabel5.setText("Display Name:");
        this.add(jLabel1, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        this.add(jLabel2, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0
            ,GridBagConstraints.NORTHEAST, GridBagConstraints.NONE, new Insets(2, 0, 0, 0), 0, 0));
        this.add(cbClassName, new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
        this.add(props, new GridBagConstraints(1, 4, 1, 1, 1.0, 0.2
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
        this.add(jLabel3, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        this.add(tfName, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
        this.add(jLabel4, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0
            ,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        this.add(jLabel5, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
            ,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        this.add(tfDisplayName, new GridBagConstraints(1, 2, 1, 1, 1.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
        this.add(tfDescription, new GridBagConstraints(1, 3, 1, 1, 1.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
    }

    private void initializeEvents() {
        ActionListener al = new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                modified = true;
            }
        };
        cbClassName.addActionListener(al);
        props.addActionListener(al);
        DocumentListener dl = new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {modified = true;}
            public void removeUpdate(DocumentEvent e) {modified = true;}
            public void changedUpdate(DocumentEvent e) {modified = true;}
        };
        tfName.getDocument().addDocumentListener(dl);
        tfDisplayName.getDocument().addDocumentListener(dl);
        tfDescription.getDocument().addDocumentListener(dl);
    }

    public void stopEditing() {
        Object item  = cbClassName.getEditor().getItem();
        if(item != null && !item.equals(cbClassName.getSelectedItem()) &&
                  !(item.equals("") && cbClassName.getSelectedItem() == null)) {
            cbClassName.setSelectedItem(item);
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
        return false;
    }

    public void setName(String name) {tfName.setText(name);}
    public String getName() {return tfName.getText();}

    public void setDescription(String desc) {tfDescription.setText(desc);}
    public String getDescription() {return tfDescription.getText();}

    public void setDisplayName(String name) {tfDisplayName.setText(name);}
    public String getDisplayName() {return tfDisplayName.getText();}

    public void setClassName(String className) {cbClassName.setSelectedItem(className);}
    public String getClassName() {return (String)cbClassName.getSelectedItem();}
    public void setClassNameOptions(String[] options) {
        cbClassName.setModel(new DefaultComboBoxModel(options));
    }

    public void setMap(Map m) {props.setMap(m);}
    public Map getMap() {return props.getMap();}
}
