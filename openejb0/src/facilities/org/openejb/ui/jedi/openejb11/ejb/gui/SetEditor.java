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
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;

/**
 * GUI for Editing a list of values.  All values are expected to be Strings.
 *
 * @see java.util.Set
 *
 * @author Aaron Mulder (ammulder@alumni.princeton.edu)
 * @version $Revision$
 */
public class SetEditor extends JComponent {
    GridBagLayout gridBagLayout1 = new GridBagLayout();
    JScrollPane jScrollPane1 = new JScrollPane();
    JTable tbProperties = new JTable();
    private SetTableModel model;
    private Vector listeners = new Vector();

    public SetEditor() {
        model = new SetTableModel();
        tbProperties.setModel(model);
        try {
            jbInit();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        initializeEvents();
    }
    private void jbInit() throws Exception {
        this.setLayout(gridBagLayout1);
        this.add(jScrollPane1, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
        jScrollPane1.getViewport().add(tbProperties, null);
    }

    private void initializeEvents() {
        tbProperties.getModel().addTableModelListener(new TableModelListener() {
            public void tableChanged(TableModelEvent evt) {
                if(evt.getType() == TableModelEvent.UPDATE) {
                    int row = evt.getFirstRow();
                    if(row >= model.getRowCount() || row < 0)
                        return;
                    String value = model.getValue(row);
                    if(value.equals("")) {
                        if(row < model.getRowCount()-1) {
                            model.deleteRow(row);
                        }
                    } else if(row == model.getRowCount()-1) {
                        model.addRow();
                    }
                    fireActionEvent();
                }
            }
        });
    }

    public boolean isEditing() {
        return tbProperties.isEditing();
    }

    public void stopEditing() {
        if(tbProperties.isEditing()) {
            tbProperties.getCellEditor().stopCellEditing();
        }
    }

    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        tbProperties.setEnabled(enabled);
    }

    public void setOpaque(boolean opaque) {
        super.setOpaque(opaque);
        tbProperties.setOpaque(opaque);
    }

    public void setColumnName(String name) {
        model.setColumnName(name);
    }

    public Set getSet() {
        Set set = new HashSet();
        for(int i=model.getRowCount()-1; i >= 0; i--) {
            String value = model.getValue(i);
            if(!value.equals("")) {
                set.add(value);
            }
        }
        return set;
    }

    public void setSet(Set set) {
        model.clear();
        if(set != null) {
            for(Iterator it = set.iterator(); it.hasNext();) {
                model.addRow((String)it.next());
            }
        }
    }

    public void addActionListener(ActionListener al) {
        listeners.add(al);
    }

    public void removeActionListener(ActionListener al) {
        listeners.remove(al);
    }

    private void fireActionEvent() {
        ActionEvent evt = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "");
        Vector local = (Vector)listeners.clone();
        for(int i=local.size()-1; i>= 0; i--)
            ((ActionListener)local.get(i)).actionPerformed(evt);
    }

    private static class SetTableModel extends AbstractTableModel {
        private java.util.List data = new LinkedList();
        private String name = "Values";

        public SetTableModel() {
            addRow();
        }

        public void setColumnName(String name) {
            this.name = name;
            fireTableStructureChanged();
        }

        public Object getValueAt(int row, int col) {
            if(col == 0)
                return data.get(row);
            return null;
        }

        public int getColumnCount() {
            return 1;
        }

        public int getRowCount() {
            return data.size();
        }

        public String getColumnName(int col) {
            if(col == 0)
                return name;
            return null;
        }

        public Class getColumnClass(int col) {
            return String.class;
        }

        public boolean isCellEditable(int row, int col) {
            return true;
        }

        public void setValueAt(Object o, int row, int col) {
            if(col == 0)
                data.set(row, (String)o);
            fireTableCellUpdated(row, col);
        }

        public void clear() {
            data.clear();
            fireTableDataChanged();
            addRow();
        }

        public void addRow() {
            addRow("");
        }

        public void addRow(String value) {
            int max = data.size();
            if(data.size() > 0 && !value.equals("")) {
                data.add(max-1, value);
                fireTableRowsInserted(max-1, max-1);
            } else {
                data.add(value);
                fireTableRowsInserted(max, max);
            }
        }

        public void deleteRow(int row) {
            data.remove(row);
            fireTableRowsDeleted(row, row);
        }

        public String getValue(int row) {
            return (String)data.get(row);
        }
    }
}
