package todolist;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.swing.table.DefaultTableModel;
import javax.swing.JOptionPane;

public class DataTasks extends javax.swing.JFrame {
    
    Connection conn;
    int selectedTaskId = -1;
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(DataTasks.class.getName());

    public void loadData() {

        try {
            String sql = "SELECT * FROM tasks";
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            DefaultTableModel model
                    = (DefaultTableModel) tableTasks.getModel();

            model.setRowCount(0);

            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getString("status"),
                    rs.getDate("created_at")
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public DataTasks() {
        initComponents();
        
        DefaultTableModel model = new DefaultTableModel(
                new String[]{"ID", "Task", "Status", "Date"}, 0
        );
        
        tableTasks.setModel(model);
        
        conn = DBConnection.connect();
        loadData();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblJudul = new javax.swing.JLabel();
        lblInputTask = new javax.swing.JLabel();
        txtTask = new javax.swing.JTextField();
        btnTambah = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tableTasks = new javax.swing.JTable();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        lblJudul.setText("Todo List");

        lblInputTask.setText("Input Task");

        txtTask.addActionListener(this::txtTaskActionPerformed);

        btnTambah.setText("Tambah");
        btnTambah.addActionListener(this::btnTambahActionPerformed);

        tableTasks.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "ID", "Task", "Status", "Deadline"
            }
        ));
        tableTasks.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tableTasksMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tableTasks);

        jButton1.setText("Selesai");
        jButton1.addActionListener(this::jButton1ActionPerformed);

        jButton2.setText("Hapus");
        jButton2.addActionListener(this::jButton2ActionPerformed);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(177, 177, 177)
                        .addComponent(lblJudul))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(lblInputTask)
                        .addGap(18, 18, 18)
                        .addComponent(txtTask, javax.swing.GroupLayout.PREFERRED_SIZE, 171, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btnTambah))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 375, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(37, 37, 37)
                        .addComponent(jButton1)
                        .addGap(41, 41, 41)
                        .addComponent(jButton2)))
                .addContainerGap(19, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblJudul)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblInputTask)
                    .addComponent(txtTask, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnTambah))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1)
                    .addComponent(jButton2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 243, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void txtTaskActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtTaskActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtTaskActionPerformed

    private void btnTambahActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTambahActionPerformed

        String task = txtTask.getText().trim();

        // ❗ VALIDASI INPUT
        if (task.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Task tidak boleh kosong!");
            return;
        }

        try {
            String sql = "INSERT INTO tasks(title, status, created_at) VALUES (?, 'pending', CURDATE())";

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, task);

            int result = ps.executeUpdate();

            if (result > 0) {
                JOptionPane.showMessageDialog(this, "Task berhasil ditambahkan ✨");

                txtTask.setText(""); // clear input
                loadData(); // refresh tabel
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }//GEN-LAST:event_btnTambahActionPerformed

    private void tableTasksMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tableTasksMouseClicked
        int row = tableTasks.getSelectedRow();

        if (row != -1) {
            selectedTaskId = Integer.parseInt(
                    tableTasks.getValueAt(row, 0).toString()
            );
        }
        // TODO add your handling code here:
    }//GEN-LAST:event_tableTasksMouseClicked

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed

        if (selectedTaskId == -1) {
            JOptionPane.showMessageDialog(this, "Pilih task dulu!");
            return;
        }

        try {
            String sql = "UPDATE tasks SET status='done' WHERE id=?";

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, selectedTaskId);

            ps.executeUpdate();
            loadData();

            JOptionPane.showMessageDialog(this, "Task selesai 🎉");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed

        if (selectedTaskId == -1) {
            JOptionPane.showMessageDialog(this, "Pilih task dulu!");
            return;
        }

        try {
            String sql = "DELETE FROM tasks WHERE id=?";

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, selectedTaskId);

            ps.executeUpdate();
            loadData();

            JOptionPane.showMessageDialog(this, "Task dihapus 🗑️");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }//GEN-LAST:event_jButton2ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new DataTasks().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnTambah;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblInputTask;
    private javax.swing.JLabel lblJudul;
    private javax.swing.JTable tableTasks;
    private javax.swing.JTextField txtTask;
    // End of variables declaration//GEN-END:variables
}
