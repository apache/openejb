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
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.DefaultCellEditor;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;

/**
 * Edits a list of name/value pairs.  All ultimate values are expected to be
 * Strings, though in the Maps assiged to and returned from this class, both
 * names and values may be grouped into java.util.Collections.  These
 * Collections will be divided into individual values for editing, and then
 * rewrapped into Collections for return.
 * @see java.util.Map
 * @see java.util.Collection
 * @author Aaron Mulder (ammulder@alumni.princeton.edu)
 * @version $Revision$
 */
public class MapEditor extends JComponent {
    GridBagLayout gridBagLayout1 = new GridBagLayout();
    JScrollPane jScrollPane1 = new JScrollPane();
    JTable tbProperties = new JTable();
    private PropertiesTableModel model;
    private Vector listeners = new Vector();

    public MapEditor() {
        model = new PropertiesTableModel();
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

        ((DefaultCellEditor)tbProperties.getDefaultEditor(String.class)).setClickCountToStart(1);
    }

    private void initializeEvents() {
        tbProperties.getModel().addTableModelListener(new TableModelListener() {
            public void tableChanged(TableModelEvent evt) {
                if(evt.getType() == TableModelEvent.UPDATE) {
                    int row = evt.getFirstRow();
                    if(row >= model.getRowCount() || row < 0)
                        return;
                    Property prop = model.getProperty(row);
                    if(prop.name.equals("") && prop.value.equals("")) {
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

    public void setNameEditor(TableCellEditor editor) {
        tbProperties.getColumnModel().getColumn(0).setCellEditor(editor);
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

    public void setColumnNames(String nameName, String valueName) {
        model.setColumnNames(new String[]{nameName, valueName});
    }

    public Map getMap() {
        Map map = new HashMap();
        for(int i=model.getRowCount()-1; i >= 0; i--) {
            Property prop = model.getProperty(i);
            if(!prop.name.equals("")) {
                Object oldValue = map.get(prop.name);
                if(oldValue != null) {
                    if(oldValue instanceof Collection) {
                        ((Collection)oldValue).add(prop.value);
                    } else {
                        List list = new LinkedList();
                        list.add(oldValue);
                        list.add(prop.value);
                        map.put(prop.name, list);
                    }
                } else {
                    map.put(prop.name, prop.value);
                }
            }
        }
        return map;
    }

    public void setMap(Map map) {
        model.clear();
        if(map != null) {
            for(Iterator it = map.keySet().iterator(); it.hasNext();) {
                Object key = it.next();
                Object value = map.get(key);
                addMapping(key, value);
            }
        }
    }

    private void addMapping(Object key, Object value) {
        if(key instanceof Collection) {
            for(Iterator it = ((Collection)key).iterator(); it.hasNext();) {
                addMapping(it.next(), value);
            }
        } else {
            if(value instanceof Collection) {
                for(Iterator it = ((Collection)value).iterator(); it.hasNext();) {
                    addMapping(key, it.next());
                }
            } else {
                model.addRow(new Property(key, value));
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

    private static class Property {
        Object name, value;

        public Property(Object name, Object value) {
            this.name = name;
            this.value = value;
        }
    }

    private static class PropertiesTableModel extends AbstractTableModel {
        private java.util.List data = new LinkedList();
        private String[] names = {"Name","Value"};

        public PropertiesTableModel() {
            addRow();
        }

        public void setColumnNames(String[] names) {
            if(names.length != 2)
                throw new IllegalArgumentException();
            this.names = names;
            fireTableStructureChanged();
        }

        public Object getValueAt(int row, int col) {
            Property prop = (Property)data.get(row);
            if(col == 0)
                return prop.name;
            if(col == 1)
                return prop.value;
            return null;
        }

        public int getColumnCount() {
            return 2;
        }

        public int getRowCount() {
            return data.size();
        }

        public String getColumnName(int col) {
            return names[col];
        }

        public Class getColumnClass(int col) {
            return String.class;
        }

        public boolean isCellEditable(int row, int col) {
            return true;
        }

        public void setValueAt(Object o, int row, int col) {
            Property prop = (Property)data.get(row);
            if(col == 0)
                prop.name = (String)o;
            else if(col == 1)
                prop.value = (String)o;
            fireTableCellUpdated(row, col);
        }

        public void clear() {
            data.clear();
            fireTableDataChanged();
            addRow();
        }

        public void addRow() {
            addRow(new Property("", ""));
        }

        public void addRow(Property prop) {
            if(data.size() > 0 && (!prop.name.equals("") || !prop.value.equals("")))
                data.add(data.size()-1, prop);
            else
                data.add(prop);
            fireTableRowsInserted(data.size()-1, data.size()-1);
        }

        public void deleteRow(int row) {
            data.remove(row);
            fireTableRowsDeleted(row, row);
        }

        public Property getProperty(int row) {
            return (Property)data.get(row);
        }
    }
}
